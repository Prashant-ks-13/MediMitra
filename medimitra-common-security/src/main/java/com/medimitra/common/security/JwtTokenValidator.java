package com.medimitra.common.security;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.Region;
import com.medimitra.common.core.UserId;
import com.medimitra.common.core.exception.AuthenticationFailedException;
import com.medimitra.common.core.exception.TokenTamperedException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Section 8.3, rules 1-4 and 7: alg allowlist, kid-selected key, iss/aud/typ, exp/nbf/iat skew, jti/sid denylist. */
public class JwtTokenValidator {

    private static final Set<JWSAlgorithm> ALLOWED_ALGORITHMS = Set.of(JWSAlgorithm.ES256, JWSAlgorithm.RS256);
    private static final String EXPECTED_TYPE = "at+jwt";

    private final JwkKeySource jwkKeySource;
    private final TokenDenylist tokenDenylist;
    private final ClockProvider clockProvider;
    private final String expectedIssuer;
    private final String expectedAudience;
    private final Duration clockSkew;

    public JwtTokenValidator(
            JwkKeySource jwkKeySource,
            TokenDenylist tokenDenylist,
            ClockProvider clockProvider,
            String expectedIssuer,
            String expectedAudience,
            Duration clockSkew) {
        this.jwkKeySource = jwkKeySource;
        this.tokenDenylist = tokenDenylist;
        this.clockProvider = clockProvider;
        this.expectedIssuer = expectedIssuer;
        this.expectedAudience = expectedAudience;
        this.clockSkew = clockSkew;
    }

    public AuthenticatedPrincipal validate(String token) {
        SignedJWT signedJWT = parse(token);
        checkAlgorithmAllowed(signedJWT);
        checkType(signedJWT);
        verifySignature(signedJWT);
        JWTClaimsSet claims = claims(signedJWT);
        checkIssuer(claims);
        checkAudience(claims);
        checkTiming(claims, clockProvider.now());
        checkDenylist(claims);
        return toPrincipal(claims);
    }

    private SignedJWT parse(String token) {
        try {
            return SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new AuthenticationFailedException(SecurityErrorCodes.SIGNATURE_INVALID, "malformed access token");
        }
    }

    private void checkAlgorithmAllowed(SignedJWT signedJWT) {
        if (!ALLOWED_ALGORITHMS.contains(signedJWT.getHeader().getAlgorithm())) {
            throw new TokenTamperedException(
                    SecurityErrorCodes.INVALID_ALGORITHM, "algorithm not allowed: " + signedJWT.getHeader().getAlgorithm());
        }
    }

    private void checkType(SignedJWT signedJWT) {
        var type = signedJWT.getHeader().getType();
        if (type == null || !EXPECTED_TYPE.equalsIgnoreCase(type.getType())) {
            throw new TokenTamperedException(SecurityErrorCodes.TOKEN_TYPE_INVALID, "token typ must be " + EXPECTED_TYPE);
        }
    }

    private void verifySignature(SignedJWT signedJWT) {
        String keyId = signedJWT.getHeader().getKeyID();
        if (keyId == null) {
            throw new TokenTamperedException(SecurityErrorCodes.UNKNOWN_KEY_ID, "token has no kid header");
        }
        JWK jwk = jwkKeySource
                .findByKeyId(keyId, clockProvider.now())
                .orElseThrow(() -> new TokenTamperedException(SecurityErrorCodes.UNKNOWN_KEY_ID, "no key found for kid " + keyId));
        try {
            JWSVerifier verifier = toVerifier(jwk);
            if (!signedJWT.verify(verifier)) {
                throw new TokenTamperedException(SecurityErrorCodes.SIGNATURE_INVALID, "signature verification failed");
            }
        } catch (JOSEException e) {
            throw new TokenTamperedException(SecurityErrorCodes.SIGNATURE_INVALID, "signature verification failed");
        }
    }

    private JWSVerifier toVerifier(JWK jwk) throws JOSEException {
        if (jwk instanceof RSAKey rsaKey) {
            return new RSASSAVerifier(rsaKey);
        }
        if (jwk instanceof ECKey ecKey) {
            return new ECDSAVerifier(ecKey);
        }
        throw new TokenTamperedException(SecurityErrorCodes.SIGNATURE_INVALID, "unsupported key type " + jwk.getKeyType());
    }

    private JWTClaimsSet claims(SignedJWT signedJWT) {
        try {
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new AuthenticationFailedException(SecurityErrorCodes.SIGNATURE_INVALID, "malformed claims");
        }
    }

    private void checkIssuer(JWTClaimsSet claims) {
        if (!expectedIssuer.equals(claims.getIssuer())) {
            throw new TokenTamperedException(SecurityErrorCodes.ISSUER_MISMATCH, "unexpected issuer: " + claims.getIssuer());
        }
    }

    private void checkAudience(JWTClaimsSet claims) {
        List<String> audience = claims.getAudience();
        if (audience == null || !audience.contains(expectedAudience)) {
            throw new TokenTamperedException(SecurityErrorCodes.AUDIENCE_MISMATCH, "token audience does not include " + expectedAudience);
        }
    }

    private void checkTiming(JWTClaimsSet claims, Instant now) {
        Date exp = claims.getExpirationTime();
        Date nbf = claims.getNotBeforeTime();
        if (exp == null || now.isAfter(exp.toInstant().plus(clockSkew))) {
            throw new AuthenticationFailedException(SecurityErrorCodes.TOKEN_EXPIRED, "access token expired");
        }
        if (nbf != null && now.isBefore(nbf.toInstant().minus(clockSkew))) {
            throw new AuthenticationFailedException(SecurityErrorCodes.TOKEN_NOT_YET_VALID, "access token not yet valid");
        }
    }

    private void checkDenylist(JWTClaimsSet claims) {
        if (tokenDenylist.isDenied(claims.getJWTID(), stringClaim(claims, "sid"))) {
            throw new AuthenticationFailedException(SecurityErrorCodes.TOKEN_REVOKED, "token or session has been revoked");
        }
    }

    private AuthenticatedPrincipal toPrincipal(JWTClaimsSet claims) {
        UserId subject = UserId.of(claims.getSubject());
        Set<String> roles = stringListClaim(claims, "roles");
        Set<String> scopes = splitScope(stringClaim(claims, "scope"));
        Region region = Region.valueOf(stringClaim(claims, "region"));
        String acr = stringClaim(claims, "acr");
        String sid = stringClaim(claims, "sid");
        String jti = claims.getJWTID();
        String dpopThumbprint = nestedClaim(claims, "cnf", "jkt");
        String actingOnBehalfOf = nestedClaim(claims, "act", "sub");
        return new AuthenticatedPrincipal(subject, roles, scopes, region, acr, sid, jti, dpopThumbprint, actingOnBehalfOf);
    }

    private static String stringClaim(JWTClaimsSet claims, String name) {
        try {
            return claims.getStringClaim(name);
        } catch (ParseException e) {
            return null;
        }
    }

    private static Set<String> stringListClaim(JWTClaimsSet claims, String name) {
        try {
            List<String> values = claims.getStringListClaim(name);
            return values == null ? Set.of() : new HashSet<>(values);
        } catch (ParseException e) {
            return Set.of();
        }
    }

    private static Set<String> splitScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return Set.of();
        }
        return new HashSet<>(Arrays.asList(scope.split(" ")));
    }

    private static String nestedClaim(JWTClaimsSet claims, String parent, String child) {
        Object value = claims.getClaim(parent);
        if (value instanceof Map<?, ?> map) {
            Object nested = map.get(child);
            return nested == null ? null : nested.toString();
        }
        return null;
    }
}

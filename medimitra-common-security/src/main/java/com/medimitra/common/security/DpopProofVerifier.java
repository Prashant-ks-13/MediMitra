package com.medimitra.common.security;

import com.medimitra.common.core.ClockProvider;
import com.medimitra.common.core.exception.TokenTamperedException;
import com.nimbusds.jose.JOSEException;
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
import java.util.Date;

/** Verifies a DPoP proof (RFC 9449) against the access token's {@code cnf.jkt} binding (section 8.3, rule 5). */
public class DpopProofVerifier {

    private static final String EXPECTED_TYPE = "dpop+jwt";

    private final DpopReplayCache replayCache;
    private final ClockProvider clockProvider;
    private final Duration freshnessWindow;

    public DpopProofVerifier(DpopReplayCache replayCache, ClockProvider clockProvider, Duration freshnessWindow) {
        this.replayCache = replayCache;
        this.clockProvider = clockProvider;
        this.freshnessWindow = freshnessWindow;
    }

    public void verify(String dpopProof, String httpMethod, String httpUri, String expectedKeyThumbprint) {
        SignedJWT proof = parse(dpopProof);
        checkType(proof);
        JWK jwk = extractEmbeddedKey(proof);
        checkThumbprintBinding(jwk, expectedKeyThumbprint);
        verifySignature(proof, jwk);
        JWTClaimsSet claims = claims(proof);
        checkMethodAndUri(claims, httpMethod, httpUri);
        checkFreshnessAndReplay(claims);
    }

    private SignedJWT parse(String dpopProof) {
        try {
            return SignedJWT.parse(dpopProof);
        } catch (ParseException e) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "malformed DPoP proof");
        }
    }

    private void checkType(SignedJWT proof) {
        var type = proof.getHeader().getType();
        if (type == null || !EXPECTED_TYPE.equalsIgnoreCase(type.getType())) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "DPoP proof typ must be " + EXPECTED_TYPE);
        }
    }

    private JWK extractEmbeddedKey(SignedJWT proof) {
        JWK jwk = proof.getHeader().getJWK();
        if (jwk == null) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "DPoP proof is missing its embedded jwk header");
        }
        return jwk;
    }

    private void checkThumbprintBinding(JWK jwk, String expectedKeyThumbprint) {
        try {
            String actual = jwk.computeThumbprint().toString();
            if (!actual.equals(expectedKeyThumbprint)) {
                throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "DPoP proof key does not match token's cnf.jkt");
            }
        } catch (JOSEException e) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "unable to compute DPoP key thumbprint");
        }
    }

    private void verifySignature(SignedJWT proof, JWK jwk) {
        try {
            JWSVerifier verifier = toVerifier(jwk);
            if (!proof.verify(verifier)) {
                throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "DPoP proof signature invalid");
            }
        } catch (JOSEException e) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "DPoP proof signature invalid");
        }
    }

    private JWSVerifier toVerifier(JWK jwk) throws JOSEException {
        if (jwk instanceof RSAKey rsaKey) {
            return new RSASSAVerifier(rsaKey);
        }
        if (jwk instanceof ECKey ecKey) {
            return new ECDSAVerifier(ecKey);
        }
        throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "unsupported DPoP key type " + jwk.getKeyType());
    }

    private JWTClaimsSet claims(SignedJWT proof) {
        try {
            return proof.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "malformed DPoP proof claims");
        }
    }

    private void checkMethodAndUri(JWTClaimsSet claims, String httpMethod, String httpUri) {
        String htm = stringClaim(claims, "htm");
        String htu = stringClaim(claims, "htu");
        if (htm == null || !htm.equalsIgnoreCase(httpMethod)) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "DPoP htm does not match request method");
        }
        if (htu == null || !htu.equals(httpUri)) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "DPoP htu does not match request URI");
        }
    }

    private void checkFreshnessAndReplay(JWTClaimsSet claims) {
        Date iat = claims.getIssueTime();
        String jti = stringClaim(claims, "jti");
        if (iat == null || jti == null) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "DPoP proof missing iat or jti");
        }
        Instant issuedAt = iat.toInstant();
        Instant now = clockProvider.now();
        if (issuedAt.isBefore(now.minus(freshnessWindow)) || issuedAt.isAfter(now.plus(freshnessWindow))) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_INVALID, "DPoP proof is not fresh");
        }
        if (replayCache.isReplayed(jti)) {
            throw new TokenTamperedException(SecurityErrorCodes.DPOP_PROOF_REPLAYED, "DPoP proof jti already used");
        }
        replayCache.remember(jti, now.plus(freshnessWindow));
    }

    private static String stringClaim(JWTClaimsSet claims, String name) {
        try {
            return claims.getStringClaim(name);
        } catch (ParseException e) {
            return null;
        }
    }
}

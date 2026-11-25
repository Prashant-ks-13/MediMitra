package com.medimitra.common.security;

import com.medimitra.common.core.UserId;
import java.util.UUID;
import org.springframework.web.client.RestClient;

public class RestConsentClient implements ConsentClient {

    private record AccessCheckResponse(boolean allowed) {
    }

    private final RestClient restClient;

    public RestConsentClient(RestClient.Builder restClientBuilder, String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public boolean isAllowed(UserId actor, UUID patientId, String action) {
        AccessCheckResponse response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/access/check")
                        .queryParam("actorId", actor.value())
                        .queryParam("patientId", patientId)
                        .queryParam("action", action)
                        .build())
                .retrieve()
                .body(AccessCheckResponse.class);
        return response != null && response.allowed();
    }
}

package com.studenthub.business.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AuthServiceClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${auth-service.base-url}") String baseUrl
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUrl = baseUrl;
    }

    public AuthValidateResponse validateToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<AuthValidateResponse> response = restTemplate.exchange(
                    baseUrl + "/auth/validate",
                    HttpMethod.GET,
                    entity,
                    AuthValidateResponse.class
            );

            AuthValidateResponse body = response.getBody();
            if (body == null || body.userId() == null || body.role() == null) {
                throw new IllegalStateException("Invalid auth-service response");
            }

            return body;
        } catch (RestClientException ex) {
            throw new IllegalStateException("Auth-service validation failed", ex);
        }
    }
}

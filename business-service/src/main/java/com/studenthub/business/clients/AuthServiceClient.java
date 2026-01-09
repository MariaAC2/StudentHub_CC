package com.studenthub.business.clients;

import com.studenthub.business.dtos.AuthRegisterRequest;
import com.studenthub.business.dtos.AuthRegisterResponse;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    void log() {
        System.out.println("AUTH BASE URL = " + baseUrl);
    }

    public AuthRegisterResponse register(String email, String password, String role) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        AuthRegisterRequest payload = new AuthRegisterRequest(email, password, role);
        HttpEntity<AuthRegisterRequest> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<AuthRegisterResponse> response = restTemplate.exchange(
                    baseUrl + "/auth/register",
                    HttpMethod.POST,
                    entity,
                    AuthRegisterResponse.class
            );

            AuthRegisterResponse body = response.getBody();
            if (body == null || body.id() == null || body.token() == null) {
                throw new IllegalStateException("Invalid auth-service register response");
            }

            return body;
        } catch (RestClientException ex) {
            throw new IllegalStateException("Auth-service register failed", ex);
        }
    }

    public void deleteUser(Long id) {
        try {
            restTemplate.exchange(
                    baseUrl + "/auth/users/" + id,
                    HttpMethod.DELETE,
                    HttpEntity.EMPTY,
                    Void.class
            );
        } catch (RestClientException ex) {
            // log at least; don't hide original error if used for compensation
            throw new IllegalStateException("Auth-service delete user failed", ex);
        }
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

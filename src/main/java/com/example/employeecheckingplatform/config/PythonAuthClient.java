// service/PythonAuthClient.java
package com.example.employeecheckingplatform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PythonAuthClient {
    private final RestTemplate rest;

    @Value("${app.face.token.url}")
    private String tokenUrl;
    @Value("${app.face.token.basic-username}")
    private String basicUser;
    @Value("${app.face.token.basic-password}")
    private String basicPass;
    @Value("${app.face.token.username}")
    private String roUser;
    @Value("${app.face.token.password}")
    private String roPass;

    private volatile String cachedToken;
    private volatile Instant tokenExp;

    public synchronized String getAccessToken() {
        if (cachedToken != null && tokenExp != null && tokenExp.isAfter(Instant.now().plusSeconds(30))) {
            return cachedToken;
        }
        return requestNewToken();
    }

    private String requestNewToken() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(basicUser, basicPass);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("username", roUser);
        form.add("password", roPass);

        try {
            var resp = rest.postForEntity(tokenUrl, new HttpEntity<>(form, headers), TokenResponse.class);
            var body = resp.getBody();
            if (body.access_token() == null || body.access_token().isBlank())
                throw new IllegalStateException("Token javobi yaroqsiz");
            cachedToken = body.access_token();
            long expires = body.expires_in() != null ? body.expires_in() : 3600;
            tokenExp = Instant.now().plusSeconds(expires);
            return cachedToken;
        } catch (RestClientResponseException e) {
            throw new RuntimeException("Python token xatosi: " + e.getStatusText() + " " + e.getResponseBodyAsString(), e);
        }
    }

    public record TokenResponse(String access_token, String token_type, Long expires_in, String refresh_token,
                                String scope) {
    }
}

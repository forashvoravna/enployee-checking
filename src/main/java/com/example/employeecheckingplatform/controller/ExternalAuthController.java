// package com.example.employeecheckingplatform.controller;
package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.TokenRequest;
import com.example.employeecheckingplatform.dto.TokenResponse;
import com.example.employeecheckingplatform.service.ExternalAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/external")
@RequiredArgsConstructor
@Validated
public class ExternalAuthController {

    private final ExternalAuthService authService;

    /**
     * One-step endpoint:
     * - Requires Basic auth header (server-to-server trust)
     * - Requires JSON body { "username": "...", "password": "..." } for JWT credentials
     * Returns JWT token JSON on success.
     */
    @PostMapping("/token")
    public ResponseEntity<?> token(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestBody TokenRequest body
    ) {
        // 1) basic auth tekshiriwi
        if (!authService.validateBasicAuthHeader(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Basic authentication failed"));
        }

        // 2) body credentials -> issue JWT
        try {
            String jwt = authService.issueJwtIfCredentialsValid(body.username(), body.password());
            TokenResponse resp = new TokenResponse(jwt, "Bearer", authService.getExpirationMs() / 1000L);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not create token"));
        }
    }
}

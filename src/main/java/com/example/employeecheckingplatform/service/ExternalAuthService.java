// package com.example.employeecheckingplatform.service;
package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.config.jwt.JwtService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExternalAuthService {

    private final JwtService jwtService;

    // Basic auth credential (server-to-server)
    @Value("${external.basic.username}")
    private String basicUsername;

    @Value("${external.basic.password}")
    private String basicPassword;

    // JWT-issuing credential (who is allowed to get jwt)
    @Value("${external.jwt.username}")
    private String jwtUsername;

    @Value("${external.jwt.password}")
    private String jwtPassword;

    @Getter
    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    /**
     * Validate Basic Auth header (Authorization: Basic base64(user:pass))
     */
    public boolean validateBasicAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) return false;
        try {
            String base64 = authHeader.substring("Basic ".length());
            String decoded = new String(Base64.getDecoder().decode(base64));
            String[] parts = decoded.split(":", 2);
            if (parts.length != 2) return false;
            String user = parts[0];
            String pass = parts[1];
            return basicUsername.equals(user) && basicPassword.equals(pass);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * After Basic Auth validated, validate JWT-credentials (body username/password)
     * and produce a token.
     */
    public String issueJwtIfCredentialsValid(String username, String password) {
        if (!jwtUsername.equals(username) || !jwtPassword.equals(password)) {
            throw new IllegalArgumentException("Invalid JWT credentials");
        }

        // subject can be username or other identifier
        return jwtService.generateForSubject(username, Map.of("client", "external"));
    }

}

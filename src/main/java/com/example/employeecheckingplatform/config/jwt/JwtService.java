package com.example.employeecheckingplatform.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-exp-min}") long expMin) {
        this.key = buildKey(secret);
        this.accessMillis = expMin * 60_000;
    }

    private SecretKey buildKey(String s) {
        byte[] raw;
        try {
            raw = Decoders.BASE64.decode(s.replace('-', '+').replace('_', '/'));
        } catch (Exception e) {
            raw = s.getBytes(StandardCharsets.UTF_8);
        }
        // kamida 256-bit bo'lsin
        if (raw.length < 32) {
            try { raw = MessageDigest.getInstance("SHA-256").digest(raw); }
            catch (Exception ignore) {}
        }
        return Keys.hmacShaKeyFor(raw);
    }

    public String generate(UserDetails user, Map<String, Object> extra) {
        return generateForSubject(user.getUsername(), extra);
    }

    public String generateForSubject(String subject, Map<String, Object> extra) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessMillis);
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(extra)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256) // JJWT 0.11.5 sintaksisi
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getBody().getSubject();
    }


    public boolean isValid(String token, UserDetails user) {
        try {
            Jws<Claims> jws = parse(token);
            String sub = jws.getBody().getSubject();
            Date exp = jws.getBody().getExpiration();
            return user.getUsername().equals(sub) && exp.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    /** 0.11.5: parserBuilder + setSigningKey */
    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}

package org.resume.s3filemanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import lombok.Getter;
import org.resume.s3filemanager.constant.SecurityErrorMessages;
import org.resume.s3filemanager.constant.SecurityConstants;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.properties.JwtTokenProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@EnableConfigurationProperties(JwtTokenProperties.class)
public class JwtTokenService {

    private final Key key;

    @Getter
    private final long expirationSeconds;

    public JwtTokenService(JwtTokenProperties jwtTokenConstants) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtTokenConstants.getSecretKey());
            if (keyBytes.length < 32) {
                throw new WeakKeyException(SecurityErrorMessages.WEAK_KEY_MESSAGE);
            }
            this.key = Keys.hmacShaKeyFor(keyBytes);
            this.expirationSeconds = jwtTokenConstants.getExpiration();
        } catch (Exception e) {
            throw new SecurityException(SecurityErrorMessages.INVALID_SECRET_KEY, e);
        }
    }

    public String generateToken(String subject, UserRole userRole) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationSeconds, ChronoUnit.SECONDS);

        return Jwts.builder()
                .header()
                .type(SecurityConstants.TOKEN_TYPE)
                .and()
                .claim(SecurityConstants.CLAIM_ROLE, userRole.getAuthority())
                .subject(subject)
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get(SecurityConstants.CLAIM_ROLE, String.class);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

package com.substring.auth.auth_app_backend.security;

import com.substring.auth.auth_app_backend.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
@Setter
public class JwtService {
   private final SecretKey key;
   private final long accessTtlseconds;
   private final long refreshTtlseconds;
   private final String issuer;

    public JwtService(
                      @Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.access-ttl-seconds}") long accessTtlseconds,
                      @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlseconds,
                      @Value("${security.jwt.issuer}")String issuer) {
        if(secret==null || secret.length()<64)
        {
            throw new IllegalArgumentException("Invalid secret");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlseconds = accessTtlseconds;
        this.refreshTtlseconds = refreshTtlseconds;
        this.issuer = issuer;
    }

    /* generate token : */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roles =user.getRoles().stream().map(role->role.getName()).toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())     // jti
                .subject(user.getId().toString())                      // sub
                .issuer(issuer)                      // iss
                .issuedAt(Date.from(now))            // iat
                .expiration(Date.from(now.plusSeconds(accessTtlseconds))) // exp
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "typ", "access"
                ))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }


    public String generateRefreshToken(User user, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(jti)                             // same jti as access token
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlseconds)))
                .claim("typ", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }


    public Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)        // verify signature
                .requireIssuer(issuer)    // verify issuer
                .build()
                .parseSignedClaims(token);
    }

    public boolean isAccessToken(String token){
        Claims c=parseToken(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public boolean isRefreshToken(String token){
        Claims c=parseToken(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserId(String token){
        Claims c=parseToken(token).getPayload();
        return UUID.fromString(c.getSubject());
    }

    public String getJti(String token){
        Claims c=parseToken(token).getPayload();
        return c.getId();
    }
}

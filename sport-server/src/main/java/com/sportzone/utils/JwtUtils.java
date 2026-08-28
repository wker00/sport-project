package com.sportzone.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

public String genToken(Map<String, Object> claims) {
    return JWT.create()
            .withClaim("claims", claims)
            .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
            .sign(Algorithm.HMAC256(secret));
}

public Map<String, Object> parseToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }
}
package com.dukani.apigateway.helper;

import com.dukani.apigateway.config.JwtPropertiesConfig;
import com.dukani.apigateway.exceptions.CustomJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;


@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtPropertiesConfig jwtPropertiesConfig;

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtPropertiesConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException ex) {
            throw new CustomJwtException("Token expired", HttpStatus.UNAUTHORIZED);
        } catch (JwtException ex) {
            throw new CustomJwtException("Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }

    public boolean isTokenValid(String jwt, String userLoginId) {
        return extractAllClaims(jwt).getSubject().equals(userLoginId);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date(System.currentTimeMillis()));
    }
}

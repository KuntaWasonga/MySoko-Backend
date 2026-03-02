package com.dukani.apigateway.config;

import com.dukani.apigateway.exceptions.CustomJwtException;
import com.dukani.apigateway.helper.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    @Value("${spring.cloud.gateway.routes[0].uri}")
    private String userAccessUrl;

    private final JwtUtils jwtUtils;
    private final WebClient webClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Skip authentication for public endpoints
        if (
            path.contains("ua/api/v1/auth") ||  // TO AUTHENTICATE EASILY
            path.contains("/ua/api/v1/otp")     // TO RECEIVE AND VALIDATE OTP
        ) {
            log.info("Skipping JWT validation for {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("Invalid authorisation header: {}", authHeader);
            exchange.getResponse().getHeaders().add("X-Error", "Expired JWT");
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            String token = authHeader.substring(7);
            String username = jwtUtils.extractUsername(token);

            if (username == null || !jwtUtils.isTokenValid(token, username)) {
                log.error("Invalid token for request to {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().add("X-Error", "Expired JWT");
                return exchange.getResponse().setComplete();
            }

            if (jwtUtils.isTokenExpired(token)) {
                log.error("Expired token for request to {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                exchange.getResponse().getHeaders().add("X-Error", "JWT expired");
                return exchange.getResponse().setComplete();
            }

            // Enabled token check on all routes
            return webClient.get()
                    .uri(userAccessUrl + "/api/v1/auth/session/validate")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header("X-Gateway-Key", "my-secure-gateway-token")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            Mono.error(new AccessDeniedException("Session validation failed"))
                    )
                    .bodyToMono(Void.class)
                    .then(chain.filter(exchange.mutate()
                            .request(request.mutate().header("X-Authenticated-User", username).build())
                            .build())
                    )
                    .onErrorResume(AccessDeniedException.class, ex -> {
                        log.error("Access denied: {}", ex.getMessage());
                        return writeErrorResponse(exchange,
                                "Access Denied", "Invalid or expired token.");
                    });

        } catch (AccessDeniedException ex) {
            log.error("Token expired and access denied: {}", ex.getMessage());

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().add("X-Error", "Access Denied");
            // ✅ write JSON body to ensure headers are preserved
            String errorJson = "{\"error\": \"Access Denied\", \"message\": \"Invalid or expired token.\"}";
            DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
            DataBuffer dataBuffer = bufferFactory.wrap(errorJson.getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));

        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT: {}", ex.getMessage());
            return writeErrorResponse(exchange, "Expired JWT", "Expired JWT" );
        } catch (JwtException ex) {
            log.error("Invalid JWT: {}", ex.getMessage());
            return writeErrorResponse(exchange, "Invalid JWT", "Invalid JWT" );
        } catch (CustomJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
            return writeErrorResponse(exchange, "Expired JWT", "Expired JWT" );
        } catch (Exception ex) {
            log.error("Unexpected error during JWT validation", ex);
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }


    @Override
    public int getOrder() {
        return -2;
    }


    private Mono<Void> writeErrorResponse(ServerWebExchange exchange,
                                          String error, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Error", error);
        exchange.getResponse().getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        String errorJson = String.format("{\"error\": \"%s\", \"message\": \"%s\"}", error, message);
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        DataBuffer dataBuffer = bufferFactory.wrap(errorJson.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}

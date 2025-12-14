package com.allocat.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthHeaderForwardFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthHeaderForwardFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        log.info("Gateway Filter - Path: {}, Authorization header present: {}", 
                path, authHeader != null ? "YES (Bearer ***)" : "NO");
        
        // Skip auth header warning for external services that don't require auth
        boolean isExternalService = path.startsWith("/api/v1/extract") || 
                                     path.startsWith("/api/health") ||
                                     path.startsWith("/actuator");
        
        if (authHeader != null && !authHeader.isEmpty()) {
            // Explicitly forward the Authorization header to the downstream service
            exchange = exchange.mutate()
                    .request(r -> r.header(HttpHeaders.AUTHORIZATION, authHeader))
                    .build();
            log.debug("Gateway Filter - Forwarding Authorization header to downstream service for path: {}", path);
        } else if (!isExternalService) {
            // Only warn for paths that typically require authentication
            log.warn("Gateway Filter - No Authorization header found for path: {}", path);
        } else {
            log.debug("Gateway Filter - No Authorization header for external service path: {} (expected)", path);
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Execute this filter early in the chain
        return Ordered.HIGHEST_PRECEDENCE;
    }
}


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
        
        if (authHeader != null && !authHeader.isEmpty()) {
            // Explicitly forward the Authorization header to the downstream service
            exchange = exchange.mutate()
                    .request(r -> r.header(HttpHeaders.AUTHORIZATION, authHeader))
                    .build();
            log.info("Gateway Filter - Forwarding Authorization header to downstream service");
        } else {
            log.warn("Gateway Filter - No Authorization header found for path: {}", path);
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Execute this filter early in the chain
        return Ordered.HIGHEST_PRECEDENCE;
    }
}


package com.allocat.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.authorizeHttpRequests(reg -> reg
				.requestMatchers(
					"/actuator/**",
					"/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
					"/api/chat/health", "/health",
					"/auth/**", "/api/auth/**"
				).permitAll()
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
		return http.build();
	}

	@Bean
	public ReactiveJwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
		SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
		return NimbusReactiveJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
	}
}



package com.allocat.api.controller;

import com.allocat.common.dto.ApiResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ApiResponse<Object>> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                log.warn("404 Not Found: {}", requestUri);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("API endpoint does not exist: " + requestUri));
            }
            
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                log.warn("403 Forbidden: {}", requestUri);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You don't have permission to access this resource"));
            }
            
            if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                log.warn("401 Unauthorized: {}", requestUri);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required. Please provide a valid token."));
            }
            
            if (statusCode == HttpStatus.METHOD_NOT_ALLOWED.value()) {
                log.warn("405 Method Not Allowed: {}", requestUri);
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                        .body(ApiResponse.error("HTTP method not allowed for this endpoint"));
            }
            
            if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                log.error("500 Internal Server Error: {}", requestUri);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Internal server error occurred"));
            }
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}


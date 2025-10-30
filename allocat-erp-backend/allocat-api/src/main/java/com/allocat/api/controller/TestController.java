package com.allocat.api.controller;

import com.allocat.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "Test endpoints for API validation")
public class TestController {

    @GetMapping
    @Operation(summary = "Test endpoint", description = "Returns a test message to verify API is working")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Test successful")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> test() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Allocat ERP API is running!");
        data.put("timestamp", java.time.LocalDateTime.now());
        data.put("version", "1.0.0");
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}

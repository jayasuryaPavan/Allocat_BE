package com.allocat.api.controller;

import com.allocat.common.dto.ApiResponse;
import com.allocat.inventory.service.AisleTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aisles/tags")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Aisle Tags", description = "APIs for generating aisle tags")
public class AisleTagController {

    private final AisleTagService aisleTagService;

    @GetMapping("/{productId}")
    @Operation(summary = "Generate aisle tag data")
    public ResponseEntity<ApiResponse<AisleTagService.AisleTagData>> generateTagData(
            @PathVariable Long productId,
            @RequestParam(required = false) Long aisleId) {
        return ResponseEntity
                .ok(ApiResponse.success(aisleTagService.generateTagData(productId, aisleId), "Tag data generated"));
    }
}

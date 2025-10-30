package com.allocat.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @Schema(example = "suryajaya", description = "Username or email")
    @NotBlank(message = "Username is required")
    private String username;
    
    @Schema(example = "goodboy", description = "User password")
    @NotBlank(message = "Password is required")
    private String password;
}


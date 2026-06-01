package dev.lucas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "the field 'username' is required")
        String username,

        @NotBlank(message = "the field 'password' is required")
        @Size(min = 8, message = "Password must have 8 characters minimum")
        String password
) {
}

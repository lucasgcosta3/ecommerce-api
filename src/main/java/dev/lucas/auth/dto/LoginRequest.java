package dev.lucas.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "the field 'username' is required")
        String username,

        @NotBlank(message = "the field 'password' is required")
        String password
) {
}

package com.charge.backend.dto;

import com.charge.backend.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank String fullName,
            @Email @NotBlank String email,
            @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères") String password
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record ForgotPasswordRequest(
            @Email @NotBlank String email
    ) {}

    public record ResetPasswordRequest(
            @NotBlank String token,
            @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères") String newPassword
    ) {}

    public record MessageResponse(String message) {}

    public record AuthResponse(
            String token,
            Long userId,
            String fullName,
            Role role
    ) {}
}

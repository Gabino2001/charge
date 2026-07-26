package com.charge.backend.controller;

import com.charge.backend.dto.AuthDtos.AuthResponse;
import com.charge.backend.dto.AuthDtos.ForgotPasswordRequest;
import com.charge.backend.dto.AuthDtos.LoginRequest;
import com.charge.backend.dto.AuthDtos.MessageResponse;
import com.charge.backend.dto.AuthDtos.RegisterRequest;
import com.charge.backend.dto.AuthDtos.ResetPasswordRequest;
import com.charge.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Inscription d'un préparateur physique. Les comptes joueurs sont créés par leur préparateur. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerCoach(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** Demande de réinitialisation : envoie un email si le compte existe. Réponse identique dans tous les cas. */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok(new MessageResponse(
                "Si un compte existe avec cet email, un lien de réinitialisation vient d'être envoyé."
        ));
    }

    /** Finalise la réinitialisation à partir du jeton reçu par email. */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Mot de passe mis à jour avec succès."));
    }
}

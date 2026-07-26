package com.charge.backend.service;

import com.charge.backend.domain.Role;
import com.charge.backend.domain.User;
import com.charge.backend.dto.AuthDtos.AuthResponse;
import com.charge.backend.dto.AuthDtos.LoginRequest;
import com.charge.backend.dto.AuthDtos.RegisterRequest;
import com.charge.backend.repository.UserRepository;
import com.charge.backend.security.JwtService;
import com.charge.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /** Inscription d'un préparateur physique. Les joueurs sont créés par leur préparateur (voir PlayerService). */
    @Transactional
    public AuthResponse registerCoach(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");
        }
        User coach = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.COACH)
                .build();
        userRepository.save(coach);
        return buildAuthResponse(coach);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable après authentification."));
        return buildAuthResponse(user);
    }

    /**
     * Génère un jeton de réinitialisation et envoie l'email correspondant.
     * Ne révèle jamais si l'email existe ou non (sécurité) : renvoie toujours un succès côté appelant.
     */
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
            userRepository.save(user);

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
        // Si l'email n'existe pas, on ne fait rien mais on ne le dit pas à l'appelant.
    }

    /** Valide le jeton et met à jour le mot de passe. */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Lien de réinitialisation invalide."));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Ce lien de réinitialisation a expiré. Refais une demande.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getRole());
    }
}

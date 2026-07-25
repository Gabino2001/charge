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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getRole());
    }
}

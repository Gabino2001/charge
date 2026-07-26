package com.charge.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /** Poste sur le terrain, uniquement pertinent pour un joueur (ex. "Milieu offensif"). */
    private String poste;

    /** Initiales affichées dans l'interface (ex. "LB"). Calculées à la création si absentes. */
    private String initials;

    /** Le préparateur physique auquel ce joueur est rattaché. Null si l'utilisateur est lui-même préparateur. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id")
    private User coach;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** Jeton temporaire pour la réinitialisation de mot de passe (null si aucune demande en cours). */
    @Column(name = "reset_token")
    private String resetToken;

    /** Date d'expiration du jeton de réinitialisation. */
    @Column(name = "reset_token_expiry")
    private Instant resetTokenExpiry;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.initials == null && this.fullName != null) {
            this.initials = deriveInitials(this.fullName);
        }
    }

    private static String deriveInitials(String name) {
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) sb.append(Character.toUpperCase(part.charAt(0)));
            if (sb.length() >= 2) break;
        }
        return sb.toString();
    }
}

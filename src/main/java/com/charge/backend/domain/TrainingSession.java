package com.charge.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Une séance d'entraînement distincte. Un joueur peut avoir plusieurs séances le même jour
 * (ex. le préparateur envoie un second lot d'exercices après la première séance déjà validée) :
 * chacune a son propre numéro de séance et son propre RPE global, au lieu d'un RPE unique par jour.
 */
@Entity
@Table(name = "training_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long playerId;

    @Column(nullable = false)
    private LocalDate sessionDate;

    /** 1 pour la première séance du jour, 2 pour la suivante, etc. */
    @Column(nullable = false)
    @Builder.Default
    private Integer sessionNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TrainingSessionStatus status = TrainingSessionStatus.IN_PROGRESS;

    /** Ressenti à l'effort global de la séance (0 à 10), envoyé une fois tous les exercices terminés. */
    private Integer rpe;

    private Integer durationMinutes;

    @Column(length = 500)
    private String comment;

    private Instant completedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (sessionDate == null) sessionDate = LocalDate.now();
    }

    /** Charge d'entraînement de la séance (RPE × durée), utilisée pour les courbes de charge. */
    public Integer getTrainingLoad() {
        if (rpe == null || durationMinutes == null) return null;
        return rpe * durationMinutes;
    }
}

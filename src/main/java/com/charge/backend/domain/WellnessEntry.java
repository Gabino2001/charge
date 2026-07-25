package com.charge.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Réponses du joueur au questionnaire de bien-être, rempli une fois par jour
 * avant d'accéder à ses exercices (humeur, sommeil, fatigue, douleurs, stress).
 */
@Entity
@Table(
        name = "wellness_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "entry_date"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WellnessEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    /** Échelles 1 (mauvais) à 5 (excellent), voir WELLNESS_QUESTIONS côté frontend pour le libellé exact. */
    @Column(nullable = false)
    private Integer mood;

    @Column(nullable = false)
    private Integer sleep;

    @Column(nullable = false)
    private Integer fatigue;

    /** 1 = beaucoup de douleur, 5 = aucune douleur. */
    @Column(nullable = false)
    private Integer soreness;

    @Column(nullable = false)
    private Integer stress;

    /** Renseigné uniquement si une douleur est signalée (soreness <= 2). */
    private String painLocation;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.entryDate == null) {
            this.entryDate = LocalDate.now();
        }
    }
}

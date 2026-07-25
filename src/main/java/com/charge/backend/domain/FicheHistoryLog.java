package com.charge.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Journal historique des 1RM calculés, un enregistrement à chaque test ajouté ou corrigé
 * sur la fiche de musculation. Sert uniquement à tracer la progression dans le temps
 * (graphiques de tendance) sans changer le comportement de la fiche "courante".
 */
@Entity
@Table(name = "fiche_history_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FicheHistoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @Column(nullable = false)
    private String exerciseName;

    @Column(nullable = false)
    private Double oneRepMax;

    @Column(nullable = false, updatable = false)
    private Instant recordedAt;

    @PrePersist
    protected void onCreate() {
        this.recordedAt = Instant.now();
    }
}

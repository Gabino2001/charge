package com.charge.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Ressenti du joueur après l'effort (RPE, échelle de Borg CR10 : 0 = repos, 10 = effort maximal),
 * rempli une fois par jour après la séance. Combiné à la durée, permet de calculer la charge
 * de séance (méthode "session-RPE" de Foster) : charge = RPE x durée (minutes).
 */
@Entity
@Table(
        name = "rpe_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "entry_date"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    /** Échelle de Borg CR10 : 0 (repos) à 10 (effort maximal). */
    @Column(nullable = false)
    private Integer rpe;

    /** Durée de la séance en minutes, utilisée pour calculer la charge (RPE x durée). */
    private Integer durationMinutes;

    private String comment;

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

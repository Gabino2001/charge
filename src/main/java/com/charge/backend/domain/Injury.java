package com.charge.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/** Registre de blessures d'un joueur, suivi manuellement par le préparateur. */
@Entity
@Table(name = "injuries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Injury {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InjuryStatus status = InjuryStatus.EN_COURS;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.startDate == null) {
            this.startDate = LocalDate.now();
        }
    }
}

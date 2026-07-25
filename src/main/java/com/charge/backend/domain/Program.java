package com.charge.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Modèle d'entraînement réutilisable, créé par un préparateur, assignable à plusieurs joueurs. */
@Entity
@Table(name = "programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    /** Type de séance choisi par le préparateur : ATELIER (repos entre chaque exercice) ou SUPERSET (repos uniquement en fin de bloc). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SessionType sessionType = SessionType.ATELIER;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coach_id", nullable = false)
    private User coach;

    /** Les blocs (ateliers) de la séance, dans l'ordre. Chaque bloc regroupe les exercices enchaînés ensemble. */
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramBlock> blocks = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

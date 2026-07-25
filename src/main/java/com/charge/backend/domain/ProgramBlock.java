package com.charge.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Un bloc ("atelier") au sein d'un programme : regroupe les exercices que le joueur enchaîne
 * ensemble avant de récupérer.
 * - En mode ATELIER, le joueur récupère entre chaque exercice du bloc (voir
 *   ProgramExercise.recoveryTimeSeconds), puis recoveryTimeSeconds une fois le bloc terminé
 *   avant de repartir pour un tour.
 * - En mode SUPERSET, les exercices du bloc s'enchaînent sans repos ; recoveryTimeSeconds
 *   n'est pris qu'une fois le bloc terminé, avant de repartir pour un tour.
 * Le préparateur peut créer autant de blocs qu'il veut dans une même séance.
 */
@Entity
@Table(name = "program_blocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    /** Temps de récupération (en secondes) une fois le bloc terminé, avant de reprendre un tour. */
    private Integer recoveryTimeSeconds;

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProgramExercise> exercises = new ArrayList<>();
}

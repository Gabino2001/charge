package com.charge.backend.domain;

import jakarta.persistence.*;
import lombok.*;

/** Un exercice modèle au sein d'un programme (indépendant de tout joueur). */
@Entity
@Table(name = "program_exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    private ProgramBlock block;

    @Column(nullable = false)
    private String title;

    private Integer sets;
    private String reps;

    /** Tempo d'exécution au format 4 temps (ex. "3-1-1-0", "4-0-X-0"). */
    private String tempo;

    /** Charge de travail en kilogrammes (ex. 60.0 pour 60 kg). */
    private Double loadKg;

    private String videoUrl;

    /** Temps de récupération (en secondes) après cet exercice précis, noté par le préparateur. */
    private Integer recoveryTimeSeconds;

    /** Pourcentage du 1RM auquel le joueur doit travailler cet exercice (ex. 75 pour 75%). */
    private Integer percentRm;

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;
}
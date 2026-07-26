package com.charge.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private Integer sets;

    /** Chaîne libre pour rester flexible : "8", "45 s", "1"... */
    private String reps;

    /** Tempo d'exécution au format 4 temps : excentrique-pause basse-concentrique-pause haute (ex. "3-1-1-0", "4-0-X-0"). */
    private String tempo;

    /** Charge de travail en kilogrammes, donnée par le préparateur (ex. 60.0 pour 60 kg). */
    private Double loadKg;

    private String videoUrl;

    /** Temps de récupération (en secondes) après cet exercice précis, noté par le préparateur. */
    private Integer recoveryTimeSeconds;

    /** Type de séance du jour : ATELIER (repos entre chaque exercice) ou SUPERSET (repos en fin de bloc). */
    @Enumerated(EnumType.STRING)
    private SessionType sessionType;

    /** Numéro de l'atelier (bloc) auquel appartient cet exercice au sein de la séance (1, 2, 3...). */
    private Integer blockIndex;

    /** Temps de récupération (en secondes) une fois tout l'atelier (bloc) terminé, avant de reprendre un tour. */
    private Integer blockRecoveryTimeSeconds;

    /** Date à laquelle l'exercice doit être réalisé. Aujourd'hui par défaut si non précisée. */
    @Column(nullable = false)
    private LocalDate scheduledFor;

    @Column(nullable = false)
    @Builder.Default
    private boolean done = false;

    /** Ressenti à l'effort (échelle de Borg CR10, 0 à 10) noté par le joueur juste après cet exercice précis. */
    private Integer exerciseRpe;

    /** Suppression logique : l'exercice reste en base (historique de charge, RPE) mais n'apparaît plus nulle part. */
    @Column(nullable = false)
    @Builder.Default
    private boolean archived = false;

    /** Programme (modèle réutilisable) à l'origine de cet exercice, si assigné via un programme. Null si assigné directement. */
    private Long sourceProgramId;

    /** Séance d'entraînement à laquelle appartient cet exercice (un joueur peut avoir plusieurs séances/jour). */
    private Long sessionId;

    /** Numéro de la séance dans la journée (1, 2...), recopié depuis la séance pour un affichage simple côté coach. */
    private Integer sessionNumber;

    /** Pourcentage du 1RM auquel le joueur doit travailler cet exercice (ex. 75 pour 75% du 1RM), donné par le préparateur. */
    private Integer percentRm;

    /** Ressenti du joueur sur la charge donnée par le coach : TOO_HEAVY, PERFECT ou TOO_LIGHT. */
    private String loadFeedback;

    /** Commentaire libre du joueur sur la charge (ex. "je pouvais encore faire 2 à 3 répétitions de plus"). */
    @Column(length = 300)
    private String loadComment;

    /** Ordre d'affichage de l'exercice au sein de son atelier (modifiable par le coach). Null = trié par date de création. */
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.scheduledFor == null) {
            this.scheduledFor = LocalDate.now();
        }
    }
}
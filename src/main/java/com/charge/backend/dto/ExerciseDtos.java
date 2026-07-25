package com.charge.backend.dto;

import com.charge.backend.domain.SessionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;

public class ExerciseDtos {

    public record CreateExerciseRequest(
            @NotBlank String title,
            Integer sets,
            String reps,
            String videoUrl,
            LocalDate scheduledFor,
            /** Temps de récupération (en secondes) après cet exercice, donné par le préparateur. */
            Integer recoveryTimeSeconds,
            /** Type de séance du jour : ATELIER ou SUPERSET, choisi par le préparateur (optionnel pour un exercice isolé). */
            SessionType sessionType,
            /** Numéro de l'atelier (bloc) auquel appartient cet exercice (optionnel pour un exercice isolé). */
            Integer blockIndex,
            /** Temps de récupération (en secondes) une fois l'atelier terminé. */
            Integer blockRecoveryTimeSeconds,
            /** Pourcentage du 1RM auquel le joueur doit travailler (ex. 75 pour 75%). */
            @Min(0) @Max(100) Integer percentRm
    ) {}

    public record UpdateExerciseRequest(
            @NotBlank String title,
            Integer sets,
            String reps,
            String videoUrl,
            LocalDate scheduledFor,
            Integer recoveryTimeSeconds,
            SessionType sessionType,
            Integer blockIndex,
            Integer blockRecoveryTimeSeconds,
            @Min(0) @Max(100) Integer percentRm
    ) {}

    public record ExerciseResponse(
            Long id,
            String title,
            Integer sets,
            String reps,
            String videoUrl,
            LocalDate scheduledFor,
            boolean done,
            Long playerId,
            Instant createdAt,
            Instant completedAt,
            Integer recoveryTimeSeconds,
            SessionType sessionType,
            Integer blockIndex,
            Integer blockRecoveryTimeSeconds,
            Integer exerciseRpe,
            Long sessionId,
            Integer sessionNumber,
            Integer percentRm,
            String loadFeedback,
            String loadComment,
            Integer orderIndex
    ) {}

    /** Le joueur note son ressenti (0 à 10) et, en option, son ressenti sur la charge donnée juste après avoir terminé cet exercice précis. */
    public record SubmitExerciseRpeRequest(
            @NotNull @Min(0) @Max(10) Integer rpe,
            /** TOO_HEAVY, PERFECT ou TOO_LIGHT — optionnel. */
            String loadFeedback,
            @jakarta.validation.constraints.Size(max = 300) String loadComment
    ) {}

    /** Le coach réordonne les exercices d'un atelier : liste des ID dans le nouvel ordre souhaité. */
    public record ReorderExercisesRequest(
            @NotNull java.util.List<Long> orderedExerciseIds
    ) {}
}

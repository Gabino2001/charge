package com.charge.backend.dto;

import com.charge.backend.domain.SessionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ProgramDtos {

    public record ProgramExerciseRequest(
            @NotBlank String title,
            Integer sets,
            String reps,
            String videoUrl,
            /** Temps de récupération (en secondes) après cet exercice, donné par le préparateur (mode ATELIER surtout). */
            Integer recoveryTimeSeconds,
            /** Pourcentage du 1RM auquel le joueur doit travailler (ex. 75 pour 75%). */
            Integer percentRm
    ) {}

    /** Un bloc ("atelier") : 2, 3 exercices ou plus, enchaînés ensemble avant de récupérer. */
    public record ProgramBlockRequest(
            /** Temps de récupération (en secondes) une fois le bloc terminé, avant de reprendre un tour. */
            Integer recoveryTimeSeconds,
            @NotEmpty List<@Valid ProgramExerciseRequest> exercises
    ) {}

    public record CreateProgramRequest(
            @NotBlank String name,
            String description,
            /** Type de séance du jour : ATELIER ou SUPERSET, choisi par le préparateur. */
            @NotNull SessionType sessionType,
            /** Les ateliers (blocs) de la séance ; le préparateur peut en créer autant qu'il veut. */
            @NotEmpty List<@Valid ProgramBlockRequest> blocks
    ) {}

    public record ProgramExerciseResponse(
            Long id, String title, Integer sets, String reps, String videoUrl, Integer recoveryTimeSeconds, Integer percentRm
    ) {}

    public record ProgramBlockResponse(
            Long id, Integer recoveryTimeSeconds, List<ProgramExerciseResponse> exercises
    ) {}

    public record ProgramResponse(
            Long id, String name, String description, SessionType sessionType, List<ProgramBlockResponse> blocks
    ) {}

    public record AssignProgramRequest(@NotEmpty List<Long> playerIds) {}

    /** Résumé du résultat d'assignation : combien d'exercices ont été créés, pour quels joueurs. */
    public record AssignProgramResponse(int playersCount, int exercisesCreatedPerPlayer) {}
}

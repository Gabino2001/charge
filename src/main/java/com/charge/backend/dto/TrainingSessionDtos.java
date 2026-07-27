package com.charge.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;

public class TrainingSessionDtos {

    /** Le joueur note son ressenti global (0 à 10) une fois toute la séance terminée. */
    public record SubmitSessionRpeRequest(
            @NotNull @Min(0) @Max(10) Integer rpe,
            Integer durationMinutes,
            String comment
    ) {}

    public record SessionResponse(
            Long id,
            LocalDate sessionDate,
            Integer sessionNumber,
            String status,
            Integer rpe,
            Integer durationMinutes,
            Integer trainingLoad,
            String comment,
            Instant completedAt
    ) {}

    /** Ratio de charge aiguë (7 j) / chronique (28 j). Zone : LOW, OPTIMAL, ELEVATED, HIGH_RISK, INSUFFICIENT_DATA. */
    public record AcwrResponse(
            Double acuteLoad,
            Double chronicLoad,
            Double ratio,
            String zone
    ) {}

    /** Un point de la courbe d'évolution de l'ACWR (un ratio calculé pour un jour donné). */
    public record AcwrHistoryPoint(
            LocalDate date,
            Double ratio,
            String zone
    ) {}
}
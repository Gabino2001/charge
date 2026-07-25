package com.charge.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class FicheDtos {

    public record CreateFicheEntryRequest(
            @NotBlank String exerciseName,
            @NotNull @Positive Double weight,
            @NotNull @Positive Integer reps
    ) {}

    public record UpdateFicheEntryRequest(
            @NotNull @Positive Double weight,
            @NotNull @Positive Integer reps
    ) {}

    /** Une ligne du tableau de charges : palier de %RM -> poids correspondant en kg. */
    public record RMEntry(int percentage, double weight) {}

    public record FicheEntryResponse(
            Long id,
            String exerciseName,
            Double weight,
            Integer reps,
            double oneRepMax,
            List<RMEntry> rmTable,
            java.time.Instant testedAt
    ) {}
}

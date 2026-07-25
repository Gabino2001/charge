package com.charge.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class RpeDtos {

    public record SubmitRpeRequest(
            @NotNull @Min(0) @Max(10) Integer rpe,
            @Positive Integer durationMinutes,
            String comment
    ) {}

    public record RpeResponse(
            Long id,
            LocalDate entryDate,
            Integer rpe,
            Integer durationMinutes,
            Integer trainingLoad,
            String comment
    ) {}

    /** Renvoyé au joueur pour savoir si le RPE du jour a déjà été rempli. */
    public record RpeStatus(boolean submittedToday, RpeResponse today) {}
}

package com.charge.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class WellnessDtos {

    public record SubmitWellnessRequest(
            @NotNull @Min(1) @Max(5) Integer mood,
            @NotNull @Min(1) @Max(5) Integer sleep,
            @NotNull @Min(1) @Max(5) Integer fatigue,
            @NotNull @Min(1) @Max(5) Integer soreness,
            @NotNull @Min(1) @Max(5) Integer stress,
            String painLocation
    ) {}

    public record WellnessResponse(
            Long id,
            LocalDate entryDate,
            Integer mood,
            Integer sleep,
            Integer fatigue,
            Integer soreness,
            Integer stress,
            String painLocation
    ) {}

    /** Renvoyé au joueur pour savoir si le questionnaire du jour doit encore être rempli. */
    public record WellnessStatus(boolean submittedToday, WellnessResponse today) {}
}

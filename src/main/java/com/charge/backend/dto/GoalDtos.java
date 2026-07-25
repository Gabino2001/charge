package com.charge.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class GoalDtos {

    public record CreateGoalRequest(
            @NotBlank String exerciseName,
            @NotNull @Positive Double targetOneRepMax,
            LocalDate targetDate
    ) {}

    public record GoalResponse(
            Long id,
            String exerciseName,
            Double targetOneRepMax,
            LocalDate targetDate,
            Double currentOneRepMax,
            Integer progressPercent,
            boolean achieved
    ) {}
}

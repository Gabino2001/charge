package com.charge.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PlayerDtos {

    public record CreatePlayerRequest(
            @NotBlank String fullName,
            @Email @NotBlank String email,
            @NotBlank String password,
            String poste
    ) {}

    public record PlayerResponse(
            Long id,
            String fullName,
            String poste,
            String initials,
            long exercisesDone,
            long exercisesTotal,
            boolean wellnessSubmittedToday,
            boolean rpeSubmittedToday,
            boolean hasActiveAlerts,
            Double acwrRatio,
            String acwrZone
    ) {}
}

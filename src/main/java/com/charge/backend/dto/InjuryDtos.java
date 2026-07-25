package com.charge.backend.dto;

import com.charge.backend.domain.InjuryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class InjuryDtos {

    public record CreateInjuryRequest(
            @NotBlank String title,
            String description,
            LocalDate startDate
    ) {}

    public record UpdateInjuryRequest(
            @NotNull InjuryStatus status,
            LocalDate endDate
    ) {}

    public record InjuryResponse(
            Long id,
            String title,
            String description,
            InjuryStatus status,
            LocalDate startDate,
            LocalDate endDate
    ) {}
}

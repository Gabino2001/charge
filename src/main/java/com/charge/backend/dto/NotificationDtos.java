package com.charge.backend.dto;

import java.time.Instant;

public class NotificationDtos {

    public record NotificationResponse(
            Long id,
            String message,
            boolean read,
            Long relatedExerciseId,
            Instant createdAt
    ) {}
}

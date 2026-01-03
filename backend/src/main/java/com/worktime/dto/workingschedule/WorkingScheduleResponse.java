package com.worktime.dto.workingschedule;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.UUID;

@Builder
public record WorkingScheduleResponse(
        UUID id,
        String userId,
        DayOfWeek dayOfWeek,
        Instant startTime,
        Instant endTime,
        String timezone,
        Boolean isActive,
        Instant effectiveFrom,
        Instant effectiveTo,

        // Audit fields
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Boolean isDeleted
) {
}

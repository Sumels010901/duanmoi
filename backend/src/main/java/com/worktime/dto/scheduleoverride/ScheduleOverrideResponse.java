package com.worktime.dto.scheduleoverride;

import com.worktime.model.enums.OverrideType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ScheduleOverrideResponse(
        UUID id,
        Instant date,
        OverrideType overrideType,
        Instant customStartTime,
        Instant customEndTime,
        String reason,

        // Audit fields
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Boolean isDeleted
) {
}

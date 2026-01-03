package com.worktime.dto.scheduleoverride;

import com.worktime.model.enums.OverrideType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ScheduleOverrideRequest(
        @NotNull(message = "Date is required")
        Instant date,

        @NotNull(message = "Override type is required")
        OverrideType overrideType,

        Instant customStartTime,

        Instant customEndTime,

        String reason
) {
}
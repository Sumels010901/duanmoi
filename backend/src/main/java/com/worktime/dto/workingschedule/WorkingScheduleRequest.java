package com.worktime.dto.workingschedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.Instant;

@Builder
public record WorkingScheduleRequest(
        @NotBlank(message = "User ID is required")
        String userId,

        @NotNull(message = "Day of week is required")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Start time is required")
        Instant startTime,

        @NotNull(message = "End time is required")
        Instant endTime,

        @NotBlank(message = "Timezone is required")
        String timezone,

        Boolean isActive,

        Instant effectiveFrom,

        Instant effectiveTo
) {
}
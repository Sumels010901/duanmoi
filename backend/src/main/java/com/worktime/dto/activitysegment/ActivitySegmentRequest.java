package com.worktime.dto.activitysegment;

import com.worktime.model.enums.TimeSegmentType;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ActivitySegmentRequest(
        @NotNull(message = "Session ID is required")
        UUID sessionId,

        @NotNull(message = "Segment type is required")
        TimeSegmentType segmentType,

        @NotNull(message = "Activity date is required")
        Instant activityDate,

        @NotNull(message = "Start time is required")
        Instant startTime,

        @NotNull(message = "End time is required")
        Instant endTime,

        @NotNull(message = "Duration seconds is required")
        @Positive(message = "Duration must be positive")
        Long durationSeconds,

        // Allocated metrics
        @PositiveOrZero(message = "Step count must be zero or positive")
        Long stepCount,

        @PositiveOrZero(message = "Calories burned must be zero or positive")
        Double caloriesBurned,

        @PositiveOrZero(message = "Average heart rate must be zero or positive")
        Integer averageHeartRate,

        @PositiveOrZero(message = "Min heart rate must be zero or positive")
        Integer minHeartRate,

        @PositiveOrZero(message = "Max heart rate must be zero or positive")
        Integer maxHeartRate,

        // Calculation metadata
        @NotNull(message = "Allocation ratio is required")
        @DecimalMin(value = "0.0", message = "Allocation ratio must be between 0 and 1")
        @DecimalMax(value = "1.0", message = "Allocation ratio must be between 0 and 1")
        Double allocationRatio,

        @NotNull(message = "Is split flag is required")
        Boolean isSplit
) {
}
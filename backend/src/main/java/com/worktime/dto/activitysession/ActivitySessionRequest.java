package com.worktime.dto.activitysession;

import com.worktime.model.enums.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ActivitySessionRequest(
        @NotBlank(message = "User ID is required")
        String userId,

        @NotNull(message = "Activity type is required")
        ActivityType activityType,

        @NotNull(message = "Start time is required")
        Instant startTime,

        @NotNull(message = "End time is required")
        Instant endTime,

        @NotBlank(message = "Timezone is required")
        String timezone,

        // Metrics - nullable based on activity type
        @Positive(message = "Step count must be positive")
        Long stepCount,

        @Positive(message = "Calories burned must be positive")
        Double caloriesBurned,

        @Positive(message = "Average heart rate must be positive")
        Integer averageHeartRate,

        @Positive(message = "Min heart rate must be positive")
        Integer minHeartRate,

        @Positive(message = "Max heart rate must be positive")
        Integer maxHeartRate,

        // Exercise-specific fields
        String exerciseType,

        String exerciseTitle,

        // Metadata
        @NotBlank(message = "Data source is required")
        String dataSource,

        String healthConnectRecordId,

        @NotNull(message = "Ingested at timestamp is required")
        Instant ingestedAt,

        Boolean processed
) {
}
package com.worktime.dto.activitysession;

import com.worktime.model.enums.ActivityType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ActivitySessionResponse(
        UUID id,
        String userId,
        ActivityType activityType,
        Instant startTime,
        Instant endTime,
        String timezone,

        // Metrics
        Long stepCount,
        Double caloriesBurned,
        Integer averageHeartRate,
        Integer minHeartRate,
        Integer maxHeartRate,

        // Exercise-specific fields
        String exerciseType,
        String exerciseTitle,

        // Metadata
        String dataSource,
        String healthConnectRecordId,
        Instant ingestedAt,
        Boolean processed,
        Long version,

        // Audit fields
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Boolean isDeleted
) {
}
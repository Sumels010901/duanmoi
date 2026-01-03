package com.worktime.dto.activitysegment;

import com.worktime.model.enums.TimeSegmentType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ActivitySegmentResponse(
        UUID id,
        UUID sessionId,
        TimeSegmentType segmentType,
        Instant activityDate,
        Instant startTime,
        Instant endTime,
        Long durationSeconds,

        // Allocated metrics
        Long stepCount,
        Double caloriesBurned,
        Integer averageHeartRate,
        Integer minHeartRate,
        Integer maxHeartRate,

        // Calculation metadata
        Double allocationRatio,
        Boolean isSplit,

        // Audit fields
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Boolean isDeleted
) {
}
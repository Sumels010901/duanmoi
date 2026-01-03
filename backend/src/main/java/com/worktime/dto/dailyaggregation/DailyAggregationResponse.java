package com.worktime.dto.dailyaggregation;

import com.worktime.model.enums.DayType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record DailyAggregationResponse(
        UUID id,
        String userId,
        Instant date,
        DayType dayType,

        // Work hours metrics
        Long workHoursSteps,
        Double workHoursCalories,
        Integer workHoursActiveMinutes,
        Integer workHoursAvgHeartRate,

        // Off hours metrics
        Long offHoursSteps,
        Double offHoursCalories,
        Integer offHoursActiveMinutes,
        Integer offHoursAvgHeartRate,

        // Total metrics
        Long totalSteps,
        Double totalCalories,
        Integer totalActiveMinutes,

        // Sleep metrics
        Long sleepDurationSeconds,
        Double sleepQualityScore,

        Instant computedAt,

        // Audit fields
        Instant createdAt,
        UUID createdBy,
        Instant updatedAt,
        UUID updatedBy,
        Boolean isDeleted
) {
}

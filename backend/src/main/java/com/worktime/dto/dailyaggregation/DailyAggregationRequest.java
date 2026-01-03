package com.worktime.dto.dailyaggregation;

import com.worktime.model.enums.DayType;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.Instant;

@Builder
public record DailyAggregationRequest(
        @NotBlank(message = "User ID is required")
        String userId,

        @NotNull(message = "Date is required")
        Instant date,

        @NotNull(message = "Day type is required")
        DayType dayType,

        // Work hours metrics
        @PositiveOrZero(message = "Work hours steps must be zero or positive")
        Long workHoursSteps,

        @PositiveOrZero(message = "Work hours calories must be zero or positive")
        Double workHoursCalories,

        @PositiveOrZero(message = "Work hours active minutes must be zero or positive")
        Integer workHoursActiveMinutes,

        @PositiveOrZero(message = "Work hours average heart rate must be zero or positive")
        Integer workHoursAvgHeartRate,

        // Off hours metrics
        @PositiveOrZero(message = "Off hours steps must be zero or positive")
        Long offHoursSteps,

        @PositiveOrZero(message = "Off hours calories must be zero or positive")
        Double offHoursCalories,

        @PositiveOrZero(message = "Off hours active minutes must be zero or positive")
        Integer offHoursActiveMinutes,

        @PositiveOrZero(message = "Off hours average heart rate must be zero or positive")
        Integer offHoursAvgHeartRate,

        // Total metrics
        @PositiveOrZero(message = "Total steps must be zero or positive")
        Long totalSteps,

        @PositiveOrZero(message = "Total calories must be zero or positive")
        Double totalCalories,

        @PositiveOrZero(message = "Total active minutes must be zero or positive")
        Integer totalActiveMinutes,

        // Sleep metrics
        @PositiveOrZero(message = "Sleep duration must be zero or positive")
        Long sleepDurationSeconds,

        @DecimalMin(value = "0.0", message = "Sleep quality score must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "Sleep quality score must be between 0 and 100")
        Double sleepQualityScore,

        @NotNull(message = "Computed at timestamp is required")
        Instant computedAt
) {
}
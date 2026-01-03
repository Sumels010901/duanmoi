package com.worktime.mapper;

import com.worktime.dto.activitysegment.ActivitySegmentRequest;
import com.worktime.dto.activitysegment.ActivitySegmentResponse;
import com.worktime.dto.activitysession.ActivitySessionRequest;
import com.worktime.dto.activitysession.ActivitySessionResponse;
import com.worktime.dto.dailyaggregation.DailyAggregationRequest;
import com.worktime.dto.dailyaggregation.DailyAggregationResponse;
import com.worktime.dto.scheduleoverride.ScheduleOverrideRequest;
import com.worktime.dto.scheduleoverride.ScheduleOverrideResponse;
import com.worktime.dto.workingschedule.WorkingScheduleRequest;
import com.worktime.dto.workingschedule.WorkingScheduleResponse;
import com.worktime.model.*;

/**
 * Utility class for mapping between entities and DTOs.
 * Provides static methods for bidirectional conversion.
 *
 * @author Thang
 * @since 2026-01-03
 */
public final class DtoMapper {

    private DtoMapper() {
        // Private constructor to prevent instantiation
    }

    // ==================== ActivitySession Mapping ====================

    /**
     * Convert ActivitySessionRequest to ActivitySession entity.
     *
     * @param request the request DTO
     * @return the entity
     */
    public static ActivitySession toEntity(ActivitySessionRequest request) {
        if (request == null) {
            return null;
        }

        return ActivitySession.builder()
                .userId(request.userId())
                .activityType(request.activityType())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .timezone(request.timezone())
                .stepCount(request.stepCount())
                .caloriesBurned(request.caloriesBurned())
                .averageHeartRate(request.averageHeartRate())
                .minHeartRate(request.minHeartRate())
                .maxHeartRate(request.maxHeartRate())
                .exerciseType(request.exerciseType())
                .exerciseTitle(request.exerciseTitle())
                .dataSource(request.dataSource())
                .healthConnectRecordId(request.healthConnectRecordId())
                .ingestedAt(request.ingestedAt())
                .processed(request.processed() != null ? request.processed() : false)
                .build();
    }

    /**
     * Convert ActivitySession entity to ActivitySessionResponse.
     *
     * @param entity the entity
     * @return the response DTO
     */
    public static ActivitySessionResponse toDto(ActivitySession entity) {
        if (entity == null) {
            return null;
        }

        return ActivitySessionResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .activityType(entity.getActivityType())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .timezone(entity.getTimezone())
                .stepCount(entity.getStepCount())
                .caloriesBurned(entity.getCaloriesBurned())
                .averageHeartRate(entity.getAverageHeartRate())
                .minHeartRate(entity.getMinHeartRate())
                .maxHeartRate(entity.getMaxHeartRate())
                .exerciseType(entity.getExerciseType())
                .exerciseTitle(entity.getExerciseTitle())
                .dataSource(entity.getDataSource())
                .healthConnectRecordId(entity.getHealthConnectRecordId())
                .ingestedAt(entity.getIngestedAt())
                .processed(entity.getProcessed())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    // ==================== ActivitySegment Mapping ====================

    /**
     * Convert ActivitySegmentRequest to ActivitySegment entity.
     * Note: The session must be set separately as it requires a repository lookup.
     *
     * @param request the request DTO
     * @return the entity (session field will be null)
     */
    public static ActivitySegment toEntity(ActivitySegmentRequest request) {
        if (request == null) {
            return null;
        }

        return ActivitySegment.builder()
                .segmentType(request.segmentType())
                .activityDate(java.time.LocalDate.ofInstant(request.activityDate(), java.time.ZoneOffset.UTC))
                .startTime(request.startTime())
                .endTime(request.endTime())
                .durationSeconds(request.durationSeconds())
                .stepCount(request.stepCount())
                .caloriesBurned(request.caloriesBurned())
                .averageHeartRate(request.averageHeartRate())
                .minHeartRate(request.minHeartRate())
                .maxHeartRate(request.maxHeartRate())
                .allocationRatio(request.allocationRatio())
                .isSplit(request.isSplit())
                .build();
    }

    /**
     * Convert ActivitySegment entity to ActivitySegmentResponse.
     *
     * @param entity the entity
     * @return the response DTO
     */
    public static ActivitySegmentResponse toDto(ActivitySegment entity) {
        if (entity == null) {
            return null;
        }

        return ActivitySegmentResponse.builder()
                .id(entity.getId())
                .sessionId(entity.getSession() != null ? entity.getSession().getId() : null)
                .segmentType(entity.getSegmentType())
                .activityDate(entity.getActivityDate().atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .durationSeconds(entity.getDurationSeconds())
                .stepCount(entity.getStepCount())
                .caloriesBurned(entity.getCaloriesBurned())
                .averageHeartRate(entity.getAverageHeartRate())
                .minHeartRate(entity.getMinHeartRate())
                .maxHeartRate(entity.getMaxHeartRate())
                .allocationRatio(entity.getAllocationRatio())
                .isSplit(entity.getIsSplit())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    // ==================== WorkingSchedule Mapping ====================

    /**
     * Convert WorkingScheduleRequest to WorkingSchedule entity.
     *
     * @param request the request DTO
     * @return the entity
     */
    public static WorkingSchedule toEntity(WorkingScheduleRequest request) {
        if (request == null) {
            return null;
        }

        return WorkingSchedule.builder()
                .userId(request.userId())
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .timezone(request.timezone())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .effectiveFrom(request.effectiveFrom())
                .effectiveTo(request.effectiveTo())
                .build();
    }

    /**
     * Convert WorkingSchedule entity to WorkingScheduleResponse.
     *
     * @param entity the entity
     * @return the response DTO
     */
    public static WorkingScheduleResponse toDto(WorkingSchedule entity) {
        if (entity == null) {
            return null;
        }

        return WorkingScheduleResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .dayOfWeek(entity.getDayOfWeek())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .timezone(entity.getTimezone())
                .isActive(entity.getIsActive())
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    // ==================== ScheduleOverride Mapping ====================

    /**
     * Convert ScheduleOverrideRequest to ScheduleOverride entity.
     *
     * @param request the request DTO
     * @return the entity
     */
    public static ScheduleOverride toEntity(ScheduleOverrideRequest request) {
        if (request == null) {
            return null;
        }

        return ScheduleOverride.builder()
                .date(java.time.LocalDate.ofInstant(request.date(), java.time.ZoneOffset.UTC))
                .overrideType(request.overrideType())
                .customStartTime(request.customStartTime())
                .customEndTime(request.customEndTime())
                .reason(request.reason())
                .build();
    }

    /**
     * Convert ScheduleOverride entity to ScheduleOverrideResponse.
     *
     * @param entity the entity
     * @return the response DTO
     */
    public static ScheduleOverrideResponse toDto(ScheduleOverride entity) {
        if (entity == null) {
            return null;
        }

        return ScheduleOverrideResponse.builder()
                .id(entity.getId())
                .date(entity.getDate().atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
                .overrideType(entity.getOverrideType())
                .customStartTime(entity.getCustomStartTime())
                .customEndTime(entity.getCustomEndTime())
                .reason(entity.getReason())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    // ==================== DailyAggregation Mapping ====================

    /**
     * Convert DailyAggregationRequest to DailyAggregation entity.
     *
     * @param request the request DTO
     * @return the entity
     */
    public static DailyAggregation toEntity(DailyAggregationRequest request) {
        if (request == null) {
            return null;
        }

        return DailyAggregation.builder()
                .userId(request.userId())
                .date(java.time.LocalDate.ofInstant(request.date(), java.time.ZoneOffset.UTC))
                .dayType(request.dayType())
                .workHoursSteps(request.workHoursSteps())
                .workHoursCalories(request.workHoursCalories())
                .workHoursActiveMinutes(request.workHoursActiveMinutes())
                .workHoursAvgHeartRate(request.workHoursAvgHeartRate())
                .offHoursSteps(request.offHoursSteps())
                .offHoursCalories(request.offHoursCalories())
                .offHoursActiveMinutes(request.offHoursActiveMinutes())
                .offHoursAvgHeartRate(request.offHoursAvgHeartRate())
                .totalSteps(request.totalSteps())
                .totalCalories(request.totalCalories())
                .totalActiveMinutes(request.totalActiveMinutes())
                .sleepDurationSeconds(request.sleepDurationSeconds())
                .sleepQualityScore(request.sleepQualityScore())
                .computedAt(request.computedAt())
                .build();
    }

    /**
     * Convert DailyAggregation entity to DailyAggregationResponse.
     *
     * @param entity the entity
     * @return the response DTO
     */
    public static DailyAggregationResponse toDto(DailyAggregation entity) {
        if (entity == null) {
            return null;
        }

        return DailyAggregationResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .date(entity.getDate().atStartOfDay(java.time.ZoneOffset.UTC).toInstant())
                .dayType(entity.getDayType())
                .workHoursSteps(entity.getWorkHoursSteps())
                .workHoursCalories(entity.getWorkHoursCalories())
                .workHoursActiveMinutes(entity.getWorkHoursActiveMinutes())
                .workHoursAvgHeartRate(entity.getWorkHoursAvgHeartRate())
                .offHoursSteps(entity.getOffHoursSteps())
                .offHoursCalories(entity.getOffHoursCalories())
                .offHoursActiveMinutes(entity.getOffHoursActiveMinutes())
                .offHoursAvgHeartRate(entity.getOffHoursAvgHeartRate())
                .totalSteps(entity.getTotalSteps())
                .totalCalories(entity.getTotalCalories())
                .totalActiveMinutes(entity.getTotalActiveMinutes())
                .sleepDurationSeconds(entity.getSleepDurationSeconds())
                .sleepQualityScore(entity.getSleepQualityScore())
                .computedAt(entity.getComputedAt())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .isDeleted(entity.getIsDeleted())
                .build();
    }
}

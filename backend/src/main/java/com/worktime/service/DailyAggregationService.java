package com.worktime.service;

import com.worktime.dto.dailyaggregation.DailyAggregationResponse;
import com.worktime.mapper.DtoMapper;
import com.worktime.model.*;
import com.worktime.model.enums.ActivityType;
import com.worktime.model.enums.DayType;
import com.worktime.model.enums.OverrideType;
import com.worktime.model.enums.TimeSegmentType;
import com.worktime.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service for computing and managing daily activity aggregations.
 *
 * <p>This service handles:
 * <ul>
 *   <li>Computing daily summaries from activity segments</li>
 *   <li>Separating work hours and off hours metrics</li>
 *   <li>Calculating total daily metrics</li>
 *   <li>Processing sleep session data</li>
 *   <li>Determining day type (WORKDAY, NON_WORKDAY, HOLIDAY, etc.)</li>
 *   <li>Storing pre-computed aggregations for fast analytics</li>
 * </ul>
 *
 * @author Thang
 * @since 2026-01-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyAggregationService {

    private final DailyAggregationRepository dailyAggregationRepository;
    private final ActivitySegmentRepository activitySegmentRepository;
    private final ActivitySessionRepository activitySessionRepository;
    private final WorkingScheduleRepository workingScheduleRepository;
    private final ScheduleOverrideRepository scheduleOverrideRepository;

    /**
     * Compute daily aggregation for a specific user and date.
     * Creates a new aggregation or updates an existing one.
     *
     * @param userId the user ID
     * @param date the date to compute aggregation for
     * @return the computed daily aggregation response
     */
    @Transactional
    public DailyAggregationResponse computeDailyAggregation(String userId, LocalDate date) {
        log.info("Computing daily aggregation for user {} on {}", userId, date);

        // Get all segments for the date
        List<ActivitySegment> segments = activitySegmentRepository.findByActivityDate(date);

        // Filter segments by user ID
        List<ActivitySegment> userSegments = segments.stream()
                .filter(segment -> segment.getSession().getUserId().equals(userId))
                .filter(segment -> !segment.getIsDeleted())
                .toList();

        log.debug("Found {} segments for user {} on {}", userSegments.size(), userId, date);

        // Separate work hours and off hours segments
        List<ActivitySegment> workHoursSegments = userSegments.stream()
                .filter(segment -> segment.getSegmentType() == TimeSegmentType.WORK_HOURS)
                .toList();

        List<ActivitySegment> offHoursSegments = userSegments.stream()
                .filter(segment -> segment.getSegmentType() == TimeSegmentType.OFF_HOURS)
                .toList();

        // Calculate metrics
        AggregatedMetrics workMetrics = aggregateSegments(workHoursSegments);
        AggregatedMetrics offMetrics = aggregateSegments(offHoursSegments);

        // Calculate totals
        Long totalSteps = safeAdd(workMetrics.steps(), offMetrics.steps());
        Double totalCalories = safeAdd(workMetrics.calories(), offMetrics.calories());
        Integer totalActiveMinutes = safeAdd(workMetrics.activeMinutes(), offMetrics.activeMinutes());

        // Get sleep metrics
        SleepMetrics sleepMetrics = calculateSleepMetrics(userId, date);

        // Determine day type
        DayType dayType = determineDayType(userId, date);

        // Find or create daily aggregation
        Optional<DailyAggregation> existingAgg = dailyAggregationRepository
                .findByUserIdAndDate(userId, date);

        DailyAggregation aggregation;
        if (existingAgg.isPresent()) {
            log.debug("Updating existing aggregation for {} on {}", userId, date);
            aggregation = existingAgg.get();
        } else {
            log.debug("Creating new aggregation for {} on {}", userId, date);
            aggregation = new DailyAggregation();
            aggregation.setUserId(userId);
            aggregation.setDate(date);
        }

        // Update aggregation fields
        aggregation.setDayType(dayType);
        aggregation.setWorkHoursSteps(workMetrics.steps());
        aggregation.setWorkHoursCalories(workMetrics.calories());
        aggregation.setWorkHoursActiveMinutes(workMetrics.activeMinutes());
        aggregation.setWorkHoursAvgHeartRate(workMetrics.avgHeartRate());
        aggregation.setOffHoursSteps(offMetrics.steps());
        aggregation.setOffHoursCalories(offMetrics.calories());
        aggregation.setOffHoursActiveMinutes(offMetrics.activeMinutes());
        aggregation.setOffHoursAvgHeartRate(offMetrics.avgHeartRate());
        aggregation.setTotalSteps(totalSteps);
        aggregation.setTotalCalories(totalCalories);
        aggregation.setTotalActiveMinutes(totalActiveMinutes);
        aggregation.setSleepDurationSeconds(sleepMetrics.durationSeconds());
        aggregation.setSleepQualityScore(sleepMetrics.qualityScore());
        aggregation.setComputedAt(Instant.now());

        // Save aggregation
        DailyAggregation savedAggregation = dailyAggregationRepository.save(aggregation);

        log.info("Daily aggregation computed for {} on {}: {} steps, {} calories, day type: {}",
                userId, date, totalSteps, totalCalories, dayType);

        return DtoMapper.toDto(savedAggregation);
    }

    /**
     * Aggregate metrics from a list of activity segments.
     *
     * @param segments the activity segments
     * @return aggregated metrics
     */
    private AggregatedMetrics aggregateSegments(List<ActivitySegment> segments) {
        long totalSteps = 0;
        double totalCalories = 0.0;
        long totalDurationSeconds = 0;
        int heartRateSum = 0;
        int heartRateCount = 0;

        for (ActivitySegment segment : segments) {
            if (segment.getStepCount() != null) {
                totalSteps += segment.getStepCount();
            }
            if (segment.getCaloriesBurned() != null) {
                totalCalories += segment.getCaloriesBurned();
            }
            if (segment.getDurationSeconds() != null) {
                totalDurationSeconds += segment.getDurationSeconds();
            }
            if (segment.getAverageHeartRate() != null) {
                heartRateSum += segment.getAverageHeartRate();
                heartRateCount++;
            }
        }

        // Convert duration to minutes
        Integer activeMinutes = totalDurationSeconds > 0
                ? (int) (totalDurationSeconds / 60)
                : null;

        // Calculate average heart rate
        Integer avgHeartRate = heartRateCount > 0
                ? heartRateSum / heartRateCount
                : null;

        return new AggregatedMetrics(
                totalSteps > 0 ? totalSteps : null,
                totalCalories > 0 ? totalCalories : null,
                activeMinutes,
                avgHeartRate
        );
    }

    /**
     * Calculate sleep metrics for a specific date.
     * Sleep sessions are typically recorded the night before.
     *
     * @param userId the user ID
     * @param date the date
     * @return sleep metrics
     */
    private SleepMetrics calculateSleepMetrics(String userId, LocalDate date) {
        // Get sleep sessions for the user
        List<ActivitySession> sleepSessions = activitySessionRepository
                .findByUserId(userId).stream()
                .filter(session -> session.getActivityType() == ActivityType.SLEEP_SESSION)
                .filter(session -> !session.getIsDeleted())
                .filter(session -> {
                    // Sleep session typically ends on the target date
                    LocalDate endDate = session.getEndTime()
                            .atZone(session.getZoneId())
                            .toLocalDate();
                    return endDate.equals(date);
                })
                .toList();

        if (sleepSessions.isEmpty()) {
            return new SleepMetrics(null, null);
        }

        // Sum up sleep duration (in case of multiple sleep sessions)
        long totalSleepSeconds = sleepSessions.stream()
                .mapToLong(session -> session.getDuration().getSeconds())
                .sum();

        // Calculate sleep quality score (simplified - can be enhanced)
        // For now, using duration-based score: 7-9 hours = 100, less or more = lower score
        Double qualityScore = calculateSleepQualityScore(totalSleepSeconds);

        return new SleepMetrics(totalSleepSeconds, qualityScore);
    }

    /**
     * Calculate sleep quality score based on duration.
     * Optimal sleep: 7-9 hours (25200-32400 seconds) = 100 score
     * Less or more sleep reduces the score.
     *
     * @param sleepSeconds total sleep duration in seconds
     * @return quality score (0-100)
     */
    private Double calculateSleepQualityScore(long sleepSeconds) {
        if (sleepSeconds <= 0) {
            return 0.0;
        }

        double sleepHours = sleepSeconds / 3600.0;

        // Optimal range: 7-9 hours
        if (sleepHours >= 7.0 && sleepHours <= 9.0) {
            return 100.0;
        }

        // Too little sleep
        if (sleepHours < 7.0) {
            return Math.max(0, (sleepHours / 7.0) * 100.0);
        }

        // Too much sleep (over 9 hours)
        double excessHours = sleepHours - 9.0;
        return Math.max(0, 100.0 - (excessHours * 10.0)); // -10 points per excess hour
    }

    /**
     * Determine the day type for a specific date.
     *
     * @param userId the user ID
     * @param date the date
     * @return the day type
     */
    private DayType determineDayType(String userId, LocalDate date) {
        // Check for schedule override first
        Optional<ScheduleOverride> override = scheduleOverrideRepository.findByDate(date);
        if (override.isPresent()) {
            OverrideType overrideType = override.get().getOverrideType();
            return switch (overrideType) {
                case HOLIDAY -> DayType.HOLIDAY;
                case PTO -> DayType.PTO;
                case IRREGULAR_WORK, CUSTOM -> DayType.WORKDAY;
            };
        }

        // Check regular working schedule
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<WorkingSchedule> schedule = workingScheduleRepository
                .findByUserIdAndDayOfWeek(userId, dayOfWeek);

        if (schedule.isPresent() && schedule.get().getIsActive()) {
            return DayType.WORKDAY;
        }

        return DayType.NON_WORKDAY;
    }

    /**
     * Get daily aggregation for a specific user and date.
     *
     * @param userId the user ID
     * @param date the date
     * @return the daily aggregation response
     * @throws IllegalArgumentException if aggregation not found
     */
    @Transactional(readOnly = true)
    public DailyAggregationResponse getDailyAggregation(String userId, LocalDate date) {
        log.debug("Fetching daily aggregation for user {} on {}", userId, date);

        DailyAggregation aggregation = dailyAggregationRepository
                .findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Daily aggregation not found for user " + userId + " on " + date));

        return DtoMapper.toDto(aggregation);
    }

    /**
     * Get daily aggregations for a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of daily aggregation responses
     */
    @Transactional(readOnly = true)
    public List<DailyAggregationResponse> getDailyAggregationsInRange(
            LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching daily aggregations from {} to {}", startDate, endDate);

        List<DailyAggregation> aggregations = dailyAggregationRepository
                .findByDateBetweenOrderByDateDesc(startDate, endDate);

        return aggregations.stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    /**
     * Recompute aggregations for a date range.
     * Useful for batch processing or fixing incorrect data.
     *
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return number of aggregations recomputed
     */
    @Transactional
    public int recomputeAggregationsInRange(String userId, LocalDate startDate, LocalDate endDate) {
        log.info("Recomputing aggregations for user {} from {} to {}", userId, startDate, endDate);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int recomputedCount = 0;

        for (int i = 0; i < daysBetween; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            try {
                computeDailyAggregation(userId, currentDate);
                recomputedCount++;
            } catch (Exception e) {
                log.error("Failed to recompute aggregation for {} on {}: {}",
                        userId, currentDate, e.getMessage(), e);
                // Continue processing other dates
            }
        }

        log.info("Recomputed {}/{} aggregations for user {}", recomputedCount, daysBetween, userId);
        return recomputedCount;
    }

    // Helper methods for safe arithmetic operations

    private Long safeAdd(Long a, Long b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        return a + b;
    }

    private Double safeAdd(Double a, Double b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        return a + b;
    }

    private Integer safeAdd(Integer a, Integer b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        return a + b;
    }

    // Helper record classes for internal data structures

    private record AggregatedMetrics(
            Long steps,
            Double calories,
            Integer activeMinutes,
            Integer avgHeartRate
    ) {}

    private record SleepMetrics(
            Long durationSeconds,
            Double qualityScore
    ) {}
}

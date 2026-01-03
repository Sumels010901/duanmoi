package com.worktime.service;

import com.worktime.model.*;
import com.worktime.model.enums.OverrideType;
import com.worktime.model.enums.TimeSegmentType;
import com.worktime.repository.ActivitySegmentRepository;
import com.worktime.repository.ScheduleOverrideRepository;
import com.worktime.repository.WorkingScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for splitting activity sessions into work-time and off-hours segments.
 *
 * <p>This service implements the core session splitting algorithm that:
 * <ul>
 *   <li>Analyzes activity sessions against user's working schedule</li>
 *   <li>Handles schedule overrides (holidays, PTO, irregular work days)</li>
 *   <li>Splits sessions that cross work hour boundaries</li>
 *   <li>Handles sessions spanning multiple days</li>
 *   <li>Proportionally allocates metrics to each segment</li>
 * </ul>
 *
 * @author Thang
 * @since 2026-01-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionSplitterService {

    private final WorkingScheduleRepository workingScheduleRepository;
    private final ScheduleOverrideRepository scheduleOverrideRepository;
    private final ActivitySegmentRepository activitySegmentRepository;

    /**
     * Split an activity session into work-time and off-hours segments.
     *
     * @param session the activity session to split
     * @return list of activity segments
     */
    @Transactional
    public List<ActivitySegment> splitSession(ActivitySession session) {
        log.info("Splitting session {} for user {} (type: {}, duration: {} - {})",
                session.getId(), session.getUserId(), session.getActivityType(),
                session.getStartTime(), session.getEndTime());

        List<ActivitySegment> segments = new ArrayList<>();

        // Convert session times to user's timezone
        ZoneId userZone = ZoneId.of(session.getTimezone());
        ZonedDateTime sessionStart = session.getStartTime().atZone(userZone);
        ZonedDateTime sessionEnd = session.getEndTime().atZone(userZone);

        // Handle sessions spanning multiple days
        LocalDate currentDate = sessionStart.toLocalDate();
        LocalDate endDate = sessionEnd.toLocalDate();

        ZonedDateTime currentSegmentStart = sessionStart;

        while (!currentDate.isAfter(endDate)) {
            // Get work hours for current day
            Optional<WorkHoursBoundary> workHours = getWorkHoursForDate(
                    session.getUserId(), currentDate, userZone);

            // Determine end of current day segment
            ZonedDateTime dayEnd = currentDate.equals(endDate)
                    ? sessionEnd
                    : currentDate.plusDays(1).atStartOfDay(userZone);

            // Split current day segment
            List<ActivitySegment> daySegments = splitDaySegment(
                    session, currentSegmentStart, dayEnd, workHours, currentDate);

            segments.addAll(daySegments);

            // Move to next day
            currentDate = currentDate.plusDays(1);
            currentSegmentStart = currentDate.atStartOfDay(userZone);
        }

        // Save all segments
        List<ActivitySegment> savedSegments = activitySegmentRepository.saveAll(segments);

        log.info("Session {} split into {} segments", session.getId(), savedSegments.size());
        return savedSegments;
    }

    /**
     * Split a single day's portion of a session into segments.
     *
     * @param session the original session
     * @param segmentStart start time of this day's segment
     * @param segmentEnd end time of this day's segment
     * @param workHours work hours for this day (empty if non-work day)
     * @param date the date being processed
     * @return list of segments for this day
     */
    private List<ActivitySegment> splitDaySegment(
            ActivitySession session,
            ZonedDateTime segmentStart,
            ZonedDateTime segmentEnd,
            Optional<WorkHoursBoundary> workHours,
            LocalDate date) {

        List<ActivitySegment> segments = new ArrayList<>();

        if (workHours.isEmpty()) {
            // No work hours for this day - entire segment is off-hours
            log.debug("No work hours for date {}, creating off-hours segment", date);
            segments.add(createSegment(session, segmentStart, segmentEnd,
                    TimeSegmentType.OFF_HOURS, date, false));
            return segments;
        }

        WorkHoursBoundary boundary = workHours.get();
        ZonedDateTime workStart = boundary.startTime();
        ZonedDateTime workEnd = boundary.endTime();

        log.debug("Work hours for {}: {} - {}", date, workStart, workEnd);

        // Case 1: Segment entirely before work hours
        if (segmentEnd.isBefore(workStart) || segmentEnd.equals(workStart)) {
            segments.add(createSegment(session, segmentStart, segmentEnd,
                    TimeSegmentType.OFF_HOURS, date, false));
        }
        // Case 2: Segment entirely after work hours
        else if (segmentStart.isAfter(workEnd) || segmentStart.equals(workEnd)) {
            segments.add(createSegment(session, segmentStart, segmentEnd,
                    TimeSegmentType.OFF_HOURS, date, false));
        }
        // Case 3: Segment entirely within work hours
        else if ((segmentStart.isAfter(workStart) || segmentStart.equals(workStart)) &&
                 (segmentEnd.isBefore(workEnd) || segmentEnd.equals(workEnd))) {
            segments.add(createSegment(session, segmentStart, segmentEnd,
                    TimeSegmentType.WORK_HOURS, date, false));
        }
        // Case 4: Segment spans work hours (needs splitting)
        else {
            // Before work hours
            if (segmentStart.isBefore(workStart)) {
                segments.add(createSegment(session, segmentStart, workStart,
                        TimeSegmentType.OFF_HOURS, date, true));
            }

            // During work hours
            ZonedDateTime workSegmentStart = segmentStart.isBefore(workStart) ? workStart : segmentStart;
            ZonedDateTime workSegmentEnd = segmentEnd.isAfter(workEnd) ? workEnd : segmentEnd;
            segments.add(createSegment(session, workSegmentStart, workSegmentEnd,
                    TimeSegmentType.WORK_HOURS, date, true));

            // After work hours
            if (segmentEnd.isAfter(workEnd)) {
                segments.add(createSegment(session, workEnd, segmentEnd,
                        TimeSegmentType.OFF_HOURS, date, true));
            }
        }

        return segments;
    }

    /**
     * Create an activity segment from a time range.
     *
     * @param session the original session
     * @param start segment start time
     * @param end segment end time
     * @param segmentType work hours or off hours
     * @param date the activity date
     * @param isSplit whether this segment was split from a larger session
     * @return the created segment
     */
    private ActivitySegment createSegment(
            ActivitySession session,
            ZonedDateTime start,
            ZonedDateTime end,
            TimeSegmentType segmentType,
            LocalDate date,
            boolean isSplit) {

        // Calculate duration and allocation ratio
        Duration sessionDuration = session.getDuration();
        Duration segmentDuration = Duration.between(start, end);
        double allocationRatio = (double) segmentDuration.getSeconds() / sessionDuration.getSeconds();

        log.debug("Creating {} segment: {} - {} (ratio: {}, split: {})",
                segmentType, start, end, String.format("%.3f", allocationRatio), isSplit);

        // Allocate metrics proportionally
        return ActivitySegment.builder()
                .session(session)
                .segmentType(segmentType)
                .activityDate(date)
                .startTime(start.toInstant())
                .endTime(end.toInstant())
                .durationSeconds(segmentDuration.getSeconds())
                .stepCount(allocateMetric(session.getStepCount(), allocationRatio))
                .caloriesBurned(allocateMetric(session.getCaloriesBurned(), allocationRatio))
                .averageHeartRate(session.getAverageHeartRate()) // Average doesn't scale
                .minHeartRate(session.getMinHeartRate())
                .maxHeartRate(session.getMaxHeartRate())
                .allocationRatio(allocationRatio)
                .isSplit(isSplit)
                .build();
    }

    /**
     * Get work hours boundaries for a specific date.
     *
     * @param userId the user ID
     * @param date the date to check
     * @param zone the timezone
     * @return optional work hours boundary
     */
    private Optional<WorkHoursBoundary> getWorkHoursForDate(
            String userId, LocalDate date, ZoneId zone) {

        // Check for schedule override first
        Optional<ScheduleOverride> override = scheduleOverrideRepository.findByDate(date);
        if (override.isPresent()) {
            return handleScheduleOverride(override.get(), date, zone);
        }

        // Get regular working schedule for day of week
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<WorkingSchedule> schedule = workingScheduleRepository
                .findByUserIdAndDayOfWeek(userId, dayOfWeek);

        if (schedule.isEmpty() || !schedule.get().getIsActive()) {
            log.debug("No active working schedule for user {} on {}", userId, dayOfWeek);
            return Optional.empty();
        }

        WorkingSchedule workSchedule = schedule.get();

        // Convert schedule times to ZonedDateTime for the specific date
        ZonedDateTime startTime = workSchedule.getStartTime().atZone(zone);
        ZonedDateTime endTime = workSchedule.getEndTime().atZone(zone);

        return Optional.of(new WorkHoursBoundary(startTime, endTime));
    }

    /**
     * Handle schedule override to determine work hours.
     *
     * @param override the schedule override
     * @param date the date
     * @param zone the timezone
     * @return optional work hours boundary
     */
    private Optional<WorkHoursBoundary> handleScheduleOverride(
            ScheduleOverride override, LocalDate date, ZoneId zone) {

        log.debug("Found schedule override for {}: type={}", date, override.getOverrideType());

        // HOLIDAY or PTO means no work hours
        if (override.getOverrideType() == OverrideType.HOLIDAY ||
            override.getOverrideType() == OverrideType.PTO) {
            return Optional.empty();
        }

        // CUSTOM or IRREGULAR_WORK with custom times
        if (override.getCustomStartTime() != null && override.getCustomEndTime() != null) {
            ZonedDateTime startTime = override.getCustomStartTime().atZone(zone);
            ZonedDateTime endTime = override.getCustomEndTime().atZone(zone);
            return Optional.of(new WorkHoursBoundary(startTime, endTime));
        }

        return Optional.empty();
    }

    /**
     * Allocate a Long metric proportionally.
     *
     * @param originalValue the original value
     * @param ratio the allocation ratio
     * @return the allocated value
     */
    private Long allocateMetric(Long originalValue, double ratio) {
        if (originalValue == null) {
            return null;
        }
        return Math.round(originalValue * ratio);
    }

    /**
     * Allocate a Double metric proportionally.
     *
     * @param originalValue the original value
     * @param ratio the allocation ratio
     * @return the allocated value
     */
    private Double allocateMetric(Double originalValue, double ratio) {
        if (originalValue == null) {
            return null;
        }
        return originalValue * ratio;
    }

    /**
     * Record class to hold work hours boundary times.
     */
    private record WorkHoursBoundary(ZonedDateTime startTime, ZonedDateTime endTime) {}
}

package com.worktime.repository;

import com.worktime.model.ActivitySegment;
import com.worktime.model.enums.TimeSegmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ActivitySegment entity.
 * Provides data access operations for activity segments classified as work-time or off-hours.
 *
 * <p>This repository supports queries for:
 * <ul>
 *   <li>Finding segments by activity date</li>
 *   <li>Finding segments by segment type (work hours vs off hours)</li>
 *   <li>Finding segments by date and type combination</li>
 *   <li>Finding segments within a date range</li>
 * </ul>
 *
 * <p>These queries are essential for computing daily aggregations and analytics.
 *
 * @see ActivitySegment
 * @author Thang
 * @since 2026-01-02
 */
@Repository
public interface ActivitySegmentRepository extends JpaRepository<ActivitySegment, UUID> {

    /**
     * Find all activity segments for a specific date.
     * Used for daily aggregation computation.
     *
     * @param activityDate the date to search for
     * @return list of activity segments for the date
     */
    List<ActivitySegment> findByActivityDate(LocalDate activityDate);

    /**
     * Find all activity segments of a specific type.
     *
     * @param segmentType the segment type (WORK_HOURS or OFF_HOURS)
     * @return list of activity segments matching the type
     */
    List<ActivitySegment> findBySegmentType(TimeSegmentType segmentType);

    /**
     * Find activity segments for a specific date and segment type.
     * Useful for computing work hours vs off hours metrics for a given day.
     *
     * @param activityDate the date to search for
     * @param segmentType the segment type (WORK_HOURS or OFF_HOURS)
     * @return list of activity segments matching both criteria
     */
    List<ActivitySegment> findByActivityDateAndSegmentType(LocalDate activityDate, TimeSegmentType segmentType);

    /**
     * Find activity segments within a date range.
     * Used for weekly and monthly trend analysis.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of activity segments within the date range
     */
    List<ActivitySegment> findByActivityDateBetween(LocalDate startDate, LocalDate endDate);
}

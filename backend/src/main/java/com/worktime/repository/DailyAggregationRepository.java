package com.worktime.repository;

import com.worktime.model.DailyAggregation;
import com.worktime.model.enums.DayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for DailyAggregation entity.
 * Provides data access operations for pre-computed daily activity summaries.
 *
 * <p>This repository supports queries for:
 * <ul>
 *   <li>Finding aggregation for a specific date</li>
 *   <li>Finding aggregations by user ID</li>
 *   <li>Finding aggregation for a specific user and date</li>
 *   <li>Finding aggregations by day type within a date range</li>
 *   <li>Finding aggregations within a date range, ordered by date descending</li>
 * </ul>
 *
 * <p>These queries enable fast analytics without scanning individual activity segments.
 * The repository is central to the daily comparison and trend analysis features.
 *
 * @see DailyAggregation
 * @author Thang
 * @since 2026-01-02
 */
@Repository
public interface DailyAggregationRepository extends JpaRepository<DailyAggregation, UUID> {

    /**
     * Find daily aggregation for a specific date.
     * Used to retrieve pre-computed metrics for a single day.
     *
     * @param date the date to search for
     * @return optional containing the daily aggregation if found
     */
    Optional<DailyAggregation> findByDate(LocalDate date);

    /**
     * Find all daily aggregations for a specific user.
     *
     * @param userId the user ID to search for
     * @return list of daily aggregations for the user
     */
    List<DailyAggregation> findByUserId(String userId);

    /**
     * Find daily aggregation for a specific user and date.
     *
     * @param userId the user ID to search for
     * @param date the date to search for
     * @return optional containing the daily aggregation if found
     */
    Optional<DailyAggregation> findByUserIdAndDate(String userId, LocalDate date);

    /**
     * Find daily aggregations by day type within a date range.
     * Used for finding comparable days (e.g., most recent workday for comparison).
     *
     * @param dayType the type of day (WORKDAY, NON_WORKDAY, HOLIDAY, etc.)
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of daily aggregations matching the day type within the range
     */
    List<DailyAggregation> findByDayTypeAndDateBetween(DayType dayType, LocalDate startDate, LocalDate endDate);

    /**
     * Find daily aggregations within a date range, ordered by date descending.
     * Used for weekly and monthly summaries with most recent data first.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of daily aggregations within the range, ordered by date descending
     */
    List<DailyAggregation> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
}

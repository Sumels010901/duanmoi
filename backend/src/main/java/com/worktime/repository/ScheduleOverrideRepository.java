package com.worktime.repository;

import com.worktime.model.ScheduleOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ScheduleOverride entity.
 * Provides data access operations for schedule exceptions (holidays, PTO, irregular work days).
 *
 * <p>This repository supports queries for:
 * <ul>
 *   <li>Finding override for a specific date</li>
 *   <li>Finding overrides within a date range</li>
 * </ul>
 *
 * <p>These queries are used to handle exceptions to regular working schedules
 * when determining work hours boundaries for activity classification.
 *
 * @see ScheduleOverride
 * @author Thang
 * @since 2026-01-02
 */
@Repository
public interface ScheduleOverrideRepository extends JpaRepository<ScheduleOverride, UUID> {

    /**
     * Find schedule override for a specific date.
     * Used to check if a date has special handling (holiday, PTO, etc.).
     *
     * @param date the date to search for
     * @return optional containing the schedule override if found
     */
    Optional<ScheduleOverride> findByDate(LocalDate date);

    /**
     * Find all schedule overrides within a date range.
     * Useful for planning and calendar views.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of schedule overrides within the date range
     */
    List<ScheduleOverride> findByDateBetween(LocalDate startDate, LocalDate endDate);
}

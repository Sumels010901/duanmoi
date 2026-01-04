package com.worktime.repository;

import com.worktime.model.WorkingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for WorkingSchedule entity.
 * Provides data access operations for user working schedule configuration.
 *
 * <p>This repository supports queries for:
 * <ul>
 *   <li>Finding active schedules for a user</li>
 *   <li>Finding schedules by day of week</li>
 *   <li>Finding specific user schedule for a day of week</li>
 * </ul>
 *
 * <p>These queries are used by the session splitting algorithm to determine
 * work hours boundaries for activity classification.
 *
 * @see WorkingSchedule
 * @author Thang
 * @since 2026-01-02
 */
@Repository
public interface WorkingScheduleRepository extends JpaRepository<WorkingSchedule, UUID> {

    /**
     * Find all active working schedules for a specific user.
     * Returns schedules where isActive = true.
     *
     * @param userId the user ID to search for
     * @return list of active working schedules for the user
     */
    List<WorkingSchedule> findByUserIdAndIsActiveTrue(String userId);

    /**
     * Find all working schedules for a specific day of the week.
     *
     * @param dayOfWeek the day of week (MONDAY, TUESDAY, etc.)
     * @return list of working schedules for the specified day
     */
    List<WorkingSchedule> findByDayOfWeek(DayOfWeek dayOfWeek);

    /**
     * Find a specific user's working schedule for a day of the week.
     * Used to determine work hours boundaries for session splitting.
     *
     * @param userId the user ID to search for
     * @param dayOfWeek the day of week (MONDAY, TUESDAY, etc.)
     * @return optional containing the working schedule if found
     */
    Optional<WorkingSchedule> findByUserIdAndDayOfWeek(String userId, DayOfWeek dayOfWeek);

    /**
     * Find all active and non-deleted working schedules for a specific user.
     * Used by ScheduleManagementService to retrieve user's current schedule configuration.
     *
     * @param userId the user ID to search for
     * @return list of active, non-deleted working schedules for the user
     */
    List<WorkingSchedule> findByUserIdAndIsActiveTrueAndIsDeletedFalse(String userId);
}

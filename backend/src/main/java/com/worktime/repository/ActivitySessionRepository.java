package com.worktime.repository;

import com.worktime.model.ActivitySession;
import com.worktime.model.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ActivitySession entity.
 * Provides data access operations for raw activity sessions ingested from Health Connect.
 *
 * <p>This repository supports queries for:
 * <ul>
 *   <li>Finding sessions by user ID</li>
 *   <li>Finding sessions by activity type</li>
 *   <li>Finding unprocessed sessions (not yet segmented)</li>
 *   <li>Finding sessions within a time range</li>
 *   <li>Finding unprocessed sessions for a specific user</li>
 * </ul>
 *
 * @see ActivitySession
 * @author Thang
 * @since 2026-01-02
 */
@Repository
public interface ActivitySessionRepository extends JpaRepository<ActivitySession, UUID> {

    /**
     * Find all activity sessions for a specific user.
     *
     * @param userId the user ID to search for
     * @return list of activity sessions for the user
     */
    List<ActivitySession> findByUserId(String userId);

    /**
     * Find all activity sessions of a specific activity type.
     *
     * @param activityType the type of activity (STEPS, HEART_RATE, etc.)
     * @return list of activity sessions matching the type
     */
    List<ActivitySession> findByActivityType(ActivityType activityType);

    /**
     * Find all unprocessed activity sessions.
     * Useful for batch processing sessions that haven't been segmented yet.
     *
     * @return list of unprocessed activity sessions
     */
    List<ActivitySession> findByProcessedFalse();

    /**
     * Find activity sessions within a specific time range.
     *
     * @param start the start of the time range (inclusive)
     * @param end the end of the time range (inclusive)
     * @return list of activity sessions starting within the range
     */
    List<ActivitySession> findByStartTimeBetween(Instant start, Instant end);

    /**
     * Find unprocessed activity sessions for a specific user.
     * Combines user filtering with processed status for targeted batch processing.
     *
     * @param userId the user ID to search for
     * @return list of unprocessed activity sessions for the user
     */
    List<ActivitySession> findByUserIdAndProcessedFalse(String userId);

    /**
     * Find activity session by Health Connect record ID.
     * Used for duplicate detection during ingestion.
     *
     * @param healthConnectRecordId the Health Connect record ID
     * @return optional containing the activity session if found
     */
    Optional<ActivitySession> findByHealthConnectRecordId(String healthConnectRecordId);

    /**
     * Check if an activity session exists with the given Health Connect record ID.
     * Used for efficient duplicate detection during batch ingestion.
     *
     * @param healthConnectRecordId the Health Connect record ID
     * @return true if a session with the record ID exists, false otherwise
     */
    boolean existsByHealthConnectRecordId(String healthConnectRecordId);
}

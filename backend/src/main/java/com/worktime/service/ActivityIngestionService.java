package com.worktime.service;

import com.worktime.dto.activitysession.ActivitySessionRequest;
import com.worktime.dto.activitysession.ActivitySessionResponse;
import com.worktime.mapper.DtoMapper;
import com.worktime.model.ActivitySegment;
import com.worktime.model.ActivitySession;
import com.worktime.repository.ActivitySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for ingesting activity data from the Android application.
 *
 * <p>This service handles:
 * <ul>
 *   <li>Receiving activity sessions from Health Connect via Android app</li>
 *   <li>Deduplication using healthConnectRecordId</li>
 *   <li>Persisting raw activity sessions</li>
 *   <li>Triggering session splitting into work-time and off-hours segments</li>
 *   <li>Managing processed status of sessions</li>
 * </ul>
 *
 * @author Thang
 * @since 2026-01-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityIngestionService {

    private final ActivitySessionRepository activitySessionRepository;
    private final SessionSplitterService sessionSplitterService;

    /**
     * Ingest a single activity session from the Android app.
     *
     * @param request the activity session request
     * @return the saved activity session response
     */
    @Transactional
    public ActivitySessionResponse ingestSession(ActivitySessionRequest request) {
        log.info("Ingesting activity session for user {} (type: {}, healthConnectId: {})",
                request.userId(), request.activityType(), request.healthConnectRecordId());

        // Check for duplicate
        if (request.healthConnectRecordId() != null) {
            Optional<ActivitySession> existing = activitySessionRepository
                    .findByHealthConnectRecordId(request.healthConnectRecordId());

            if (existing.isPresent()) {
                log.warn("Duplicate session detected with healthConnectRecordId: {}. Skipping ingestion.",
                        request.healthConnectRecordId());
                return DtoMapper.toDto(existing.get());
            }
        }

        // Convert DTO to entity
        ActivitySession session = DtoMapper.toEntity(request);

        // Save session
        ActivitySession savedSession = activitySessionRepository.save(session);
        log.info("Activity session saved with ID: {}", savedSession.getId());

        // Process session asynchronously (split into segments)
        try {
            processSession(savedSession);
        } catch (Exception e) {
            log.error("Error processing session {}: {}", savedSession.getId(), e.getMessage(), e);
            // Don't rollback transaction - session is saved, processing can be retried
        }

        return DtoMapper.toDto(savedSession);
    }

    /**
     * Ingest multiple activity sessions in batch.
     *
     * @param requests list of activity session requests
     * @return list of saved activity session responses
     */
    @Transactional
    public List<ActivitySessionResponse> ingestSessionsBatch(List<ActivitySessionRequest> requests) {
        log.info("Ingesting batch of {} activity sessions", requests.size());

        return requests.stream()
                .map(this::ingestSession)
                .toList();
    }

    /**
     * Process an activity session by splitting it into segments.
     * Marks the session as processed after successful splitting.
     *
     * @param session the activity session to process
     */
    @Transactional
    public void processSession(ActivitySession session) {
        log.info("Processing session {} for user {}", session.getId(), session.getUserId());

        if (session.getProcessed()) {
            log.debug("Session {} already processed. Skipping.", session.getId());
            return;
        }

        try {
            // Split session into segments
            List<ActivitySegment> segments = sessionSplitterService.splitSession(session);

            // Mark session as processed
            session.setProcessed(true);
            activitySessionRepository.save(session);

            log.info("Session {} processed successfully. Created {} segments.",
                    session.getId(), segments.size());

        } catch (Exception e) {
            log.error("Failed to process session {}: {}", session.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process session", e);
        }
    }

    /**
     * Reprocess unprocessed sessions for a specific user.
     * Useful for batch processing or fixing failed processing attempts.
     *
     * @param userId the user ID
     * @return number of sessions processed
     */
    @Transactional
    public int reprocessUnprocessedSessions(String userId) {
        log.info("Reprocessing unprocessed sessions for user {}", userId);

        List<ActivitySession> unprocessedSessions = activitySessionRepository
                .findByUserIdAndProcessedFalse(userId);

        log.info("Found {} unprocessed sessions for user {}", unprocessedSessions.size(), userId);

        int processedCount = 0;
        for (ActivitySession session : unprocessedSessions) {
            try {
                processSession(session);
                processedCount++;
            } catch (Exception e) {
                log.error("Failed to reprocess session {}: {}", session.getId(), e.getMessage(), e);
                // Continue processing other sessions
            }
        }

        log.info("Reprocessed {}/{} sessions for user {}",
                processedCount, unprocessedSessions.size(), userId);

        return processedCount;
    }

    /**
     * Reprocess all unprocessed sessions across all users.
     * Useful for batch processing jobs.
     *
     * @return number of sessions processed
     */
    @Transactional
    public int reprocessAllUnprocessedSessions() {
        log.info("Reprocessing all unprocessed sessions");

        List<ActivitySession> unprocessedSessions = activitySessionRepository.findByProcessedFalse();

        log.info("Found {} unprocessed sessions", unprocessedSessions.size());

        int processedCount = 0;
        for (ActivitySession session : unprocessedSessions) {
            try {
                processSession(session);
                processedCount++;
            } catch (Exception e) {
                log.error("Failed to reprocess session {}: {}", session.getId(), e.getMessage(), e);
                // Continue processing other sessions
            }
        }

        log.info("Reprocessed {}/{} sessions", processedCount, unprocessedSessions.size());

        return processedCount;
    }

    /**
     * Get an activity session by ID.
     *
     * @param sessionId the session ID
     * @return the activity session response
     * @throws IllegalArgumentException if session not found
     */
    @Transactional(readOnly = true)
    public ActivitySessionResponse getSessionById(UUID sessionId) {
        log.debug("Fetching session with ID: {}", sessionId);

        ActivitySession session = activitySessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        return DtoMapper.toDto(session);
    }

    /**
     * Get all activity sessions for a user.
     *
     * @param userId the user ID
     * @return list of activity session responses
     */
    @Transactional(readOnly = true)
    public List<ActivitySessionResponse> getSessionsByUser(String userId) {
        log.debug("Fetching sessions for user: {}", userId);

        List<ActivitySession> sessions = activitySessionRepository.findByUserId(userId);

        return sessions.stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    /**
     * Get all activity sessions for a user within a time range.
     *
     * @param userId the user ID
     * @param startTime the start time
     * @param endTime the end time
     * @return list of activity session responses
     */
    @Transactional(readOnly = true)
    public List<ActivitySessionResponse> getSessionsByUserAndTimeRange(
            String userId, Instant startTime, Instant endTime) {
        log.debug("Fetching sessions for user: {} from {} to {}", userId, startTime, endTime);

        return activitySessionRepository.findByStartTimeBetween(startTime, endTime).stream()
                .filter(session -> session.getUserId().equals(userId))
                .map(DtoMapper::toDto)
                .toList();
    }

    /**
     * Delete an activity session by ID (soft delete).
     *
     * @param sessionId the session ID
     */
    @Transactional
    public void deleteSession(UUID sessionId) {
        log.info("Soft deleting session: {}", sessionId);

        ActivitySession session = activitySessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        session.softDelete();
        activitySessionRepository.save(session);

        log.info("Session {} soft deleted", sessionId);
    }
}

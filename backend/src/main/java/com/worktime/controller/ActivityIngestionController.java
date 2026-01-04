package com.worktime.controller;

import com.worktime.dto.activitysession.ActivitySessionRequest;
import com.worktime.dto.activitysession.ActivitySessionResponse;
import com.worktime.service.ActivityIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for activity data ingestion.
 * Handles incoming activity sessions from Health Connect.
 *
 * Base path: /api/v1/activity
 *
 * @author Thang
 * @since 2026-01-02
 */
@RestController
@RequestMapping("/api/v1/activity")
@RequiredArgsConstructor
@Slf4j
public class ActivityIngestionController {

    private final ActivityIngestionService ingestionService;

    /**
     * Ingest a single activity session.
     *
     * POST /api/v1/activity/sessions
     */
    @PostMapping("/sessions")
    public ResponseEntity<ActivitySessionResponse> ingestSession(
        @Valid @RequestBody ActivitySessionRequest request
    ) {
        log.info("Received activity session ingestion request: {}", request.activityType());

        ActivitySessionResponse response = ingestionService.ingestSession(request);

        log.info("Successfully ingested session: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Batch ingest multiple activity sessions.
     *
     * POST /api/v1/activity/sessions/batch
     */
    @PostMapping("/sessions/batch")
    public ResponseEntity<BatchIngestionResponse> ingestBatch(
        @Valid @RequestBody List<ActivitySessionRequest> requests
    ) {
        log.info("Received batch ingestion request: {} sessions", requests.size());

        List<ActivitySessionResponse> responses = ingestionService.ingestSessionsBatch(requests);

        BatchIngestionResponse batchResponse = new BatchIngestionResponse(
            responses.size(),
            responses
        );

        log.info("Successfully ingested {} sessions", responses.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(batchResponse);
    }

    /**
     * Get a single activity session by ID.
     *
     * GET /api/v1/activity/sessions/{id}
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<ActivitySessionResponse> getSession(
        @PathVariable UUID id
    ) {
        log.info("Fetching session: {}", id);

        ActivitySessionResponse response = ingestionService.getSessionById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all activity sessions for a user within a time range.
     *
     * GET /api/v1/activity/sessions?userId={userId}&startTime={startTime}&endTime={endTime}
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ActivitySessionResponse>> getSessions(
        @RequestParam String userId,
        @RequestParam Instant startTime,
        @RequestParam Instant endTime
    ) {
        log.info("Fetching sessions for user: {} ({} to {})", userId, startTime, endTime);

        List<ActivitySessionResponse> responses = ingestionService.getSessionsByUserAndTimeRange(
            userId, startTime, endTime
        );

        log.info("Found {} sessions", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Reprocess all unprocessed sessions for a user.
     * Useful for retry after failures.
     *
     * POST /api/v1/activity/sessions/reprocess?userId={userId}
     */
    @PostMapping("/sessions/reprocess")
    public ResponseEntity<ReprocessResponse> reprocessUnprocessedSessions(
        @RequestParam String userId
    ) {
        log.info("Reprocessing unprocessed sessions for user: {}", userId);

        int count = ingestionService.reprocessUnprocessedSessions(userId);

        ReprocessResponse response = new ReprocessResponse(count);

        log.info("Reprocessed {} sessions", count);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete (soft delete) an activity session.
     *
     * DELETE /api/v1/activity/sessions/{id}
     */
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(
        @PathVariable UUID id
    ) {
        log.info("Deleting session: {}", id);

        ingestionService.deleteSession(id);

        log.info("Successfully deleted session: {}", id);
        return ResponseEntity.noContent().build();
    }

    // Response DTOs
    public record BatchIngestionResponse(
        int totalIngested,
        List<ActivitySessionResponse> sessions
    ) {}

    public record ReprocessResponse(
        int sessionsReprocessed
    ) {}
}

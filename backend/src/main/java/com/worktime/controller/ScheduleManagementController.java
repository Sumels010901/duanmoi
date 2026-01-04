package com.worktime.controller;

import com.worktime.dto.scheduleoverride.ScheduleOverrideRequest;
import com.worktime.dto.scheduleoverride.ScheduleOverrideResponse;
import com.worktime.dto.workingschedule.WorkingScheduleRequest;
import com.worktime.dto.workingschedule.WorkingScheduleResponse;
import com.worktime.service.ScheduleManagementService;
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
 * REST controller for managing working schedules and overrides.
 *
 * Base path: /api/v1/schedules
 *
 * @author Thang
 * @since 2026-01-02
 */
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleManagementController {

    private final ScheduleManagementService scheduleService;

    // ==================== Working Schedules ====================

    /**
     * Create a new working schedule.
     *
     * POST /api/v1/schedules/working
     */
    @PostMapping("/working")
    public ResponseEntity<WorkingScheduleResponse> createWorkingSchedule(
        @Valid @RequestBody WorkingScheduleRequest request
    ) {
        log.info("Creating working schedule for user: {}, day: {}",
            request.userId(), request.dayOfWeek());

        WorkingScheduleResponse response = scheduleService.createWorkingSchedule(request);

        log.info("Created working schedule: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all active working schedules for a user.
     *
     * GET /api/v1/schedules/working?userId={userId}
     */
    @GetMapping("/working")
    public ResponseEntity<List<WorkingScheduleResponse>> getWorkingSchedules(
        @RequestParam String userId
    ) {
        log.info("Fetching working schedules for user: {}", userId);

        List<WorkingScheduleResponse> responses = scheduleService.getActiveWorkingSchedules(userId);

        log.info("Found {} working schedules", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Update a working schedule.
     *
     * PUT /api/v1/schedules/working/{id}
     */
    @PutMapping("/working/{id}")
    public ResponseEntity<WorkingScheduleResponse> updateWorkingSchedule(
        @PathVariable UUID id,
        @Valid @RequestBody WorkingScheduleRequest request
    ) {
        log.info("Updating working schedule: {}", id);

        WorkingScheduleResponse response = scheduleService.updateWorkingSchedule(id, request);

        log.info("Updated working schedule: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete (soft delete) a working schedule.
     *
     * DELETE /api/v1/schedules/working/{id}
     */
    @DeleteMapping("/working/{id}")
    public ResponseEntity<Void> deleteWorkingSchedule(
        @PathVariable UUID id
    ) {
        log.info("Deleting working schedule: {}", id);

        scheduleService.deleteWorkingSchedule(id);

        log.info("Deleted working schedule: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Schedule Overrides ====================

    /**
     * Create a schedule override (holiday, PTO, custom hours).
     *
     * POST /api/v1/schedules/overrides
     */
    @PostMapping("/overrides")
    public ResponseEntity<ScheduleOverrideResponse> createOverride(
        @Valid @RequestBody ScheduleOverrideRequest request
    ) {
        log.info("Creating schedule override for date: {}, type: {}",
            request.date(), request.overrideType());

        ScheduleOverrideResponse response = scheduleService.createScheduleOverride(request);

        log.info("Created schedule override: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get schedule overrides within a date range.
     *
     * GET /api/v1/schedules/overrides?startDate={startDate}&endDate={endDate}
     */
    @GetMapping("/overrides")
    public ResponseEntity<List<ScheduleOverrideResponse>> getOverrides(
        @RequestParam Instant startDate,
        @RequestParam Instant endDate
    ) {
        log.info("Fetching schedule overrides from {} to {}", startDate, endDate);

        List<ScheduleOverrideResponse> responses = scheduleService.getScheduleOverrides(
            startDate, endDate
        );

        log.info("Found {} schedule overrides", responses.size());
        return ResponseEntity.ok(responses);
    }

    /**
     * Delete a schedule override.
     *
     * DELETE /api/v1/schedules/overrides/{id}
     */
    @DeleteMapping("/overrides/{id}")
    public ResponseEntity<Void> deleteOverride(
        @PathVariable UUID id
    ) {
        log.info("Deleting schedule override: {}", id);

        scheduleService.deleteScheduleOverride(id);

        log.info("Deleted schedule override: {}", id);
        return ResponseEntity.noContent().build();
    }
}

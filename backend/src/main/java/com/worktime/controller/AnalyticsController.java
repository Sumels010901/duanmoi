package com.worktime.controller;

import com.worktime.dto.dailyaggregation.DailyAggregationResponse;
import com.worktime.service.DailyAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * REST controller for analytics and aggregations.
 *
 * Base path: /api/v1/analytics
 *
 * @author Thang
 * @since 2026-01-02
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final DailyAggregationService aggregationService;

    /**
     * Get daily aggregation for a specific date.
     * If not computed, computes it on-the-fly.
     *
     * GET /api/v1/analytics/daily/{date}?userId={userId}
     */
    @GetMapping("/daily/{date}")
    public ResponseEntity<DailyAggregationResponse> getDailyAggregation(
        @PathVariable LocalDate date,
        @RequestParam String userId
    ) {
        log.info("Fetching daily aggregation for user: {}, date: {}", userId, date);

        DailyAggregationResponse response = aggregationService.getDailyAggregation(userId, date);

        if (response == null) {
            // Compute if not exists
            log.info("Daily aggregation not found, computing...");
            response = aggregationService.computeDailyAggregation(userId, date);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Force recomputation of daily aggregation.
     *
     * POST /api/v1/analytics/daily/{date}/recompute?userId={userId}
     */
    @PostMapping("/daily/{date}/recompute")
    public ResponseEntity<DailyAggregationResponse> recomputeDailyAggregation(
        @PathVariable LocalDate date,
        @RequestParam String userId
    ) {
        log.info("Recomputing daily aggregation for user: {}, date: {}", userId, date);

        DailyAggregationResponse response = aggregationService.computeDailyAggregation(userId, date);

        log.info("Recomputed daily aggregation: {}", response.id());
        return ResponseEntity.ok(response);
    }

    /**
     * Batch recompute daily aggregations for a date range.
     *
     * POST /api/v1/analytics/daily/recompute-range
     */
    @PostMapping("/daily/recompute-range")
    public ResponseEntity<RecomputeRangeResponse> recomputeRange(
        @RequestParam String userId,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate
    ) {
        log.info("Recomputing aggregations for user: {} from {} to {}",
            userId, startDate, endDate);

        int count = aggregationService.recomputeAggregationsInRange(
            userId, startDate, endDate
        );

        RecomputeRangeResponse response = new RecomputeRangeResponse(count);

        log.info("Recomputed {} aggregations", count);
        return ResponseEntity.ok(response);
    }

    // Response DTOs
    public record RecomputeRangeResponse(
        int totalRecomputed
    ) {}
}

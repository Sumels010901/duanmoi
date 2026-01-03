package com.worktime.model;

import com.worktime.model.enums.TimeSegmentType;
import com.worktime.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity representing activity segments classified as work-time or off-hours.
 * Derived from ActivitySession through the session splitting algorithm.
 */
@Entity
@Table(name = "activity_segments", indexes = {
    @Index(name = "idx_activity_segment_date", columnList = "activityDate"),
    @Index(name = "idx_activity_segment_type", columnList = "segmentType"),
    @Index(name = "idx_activity_segment_session", columnList = "session_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySegment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ActivitySession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeSegmentType segmentType;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Column(nullable = false)
    private Long durationSeconds;  // Store as seconds for easy querying

    // Allocated metrics (proportional to time)
    private Long stepCount;

    private Double caloriesBurned;

    private Integer averageHeartRate;

    private Integer minHeartRate;

    private Integer maxHeartRate;

    // Calculation metadata
    @Column(nullable = false)
    private Double allocationRatio;  // What % of original session is this segment?

    @Column(nullable = false)
    private Boolean isSplit;  // Was this split from a larger session?

    /**
     * Get duration as Duration object.
     */
    public Duration getDuration() {
        return Duration.ofSeconds(durationSeconds);
    }

    /**
     * Set duration from Duration object.
     */
    public void setDuration(Duration duration) {
        this.durationSeconds = duration.getSeconds();
    }
}

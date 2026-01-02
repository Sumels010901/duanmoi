package com.worktime.model;

import com.worktime.model.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Entity representing a raw activity session from Health Connect.
 * This is the source of truth for all ingested activity data before segmentation.
 */
@Entity
@Table(name = "activity_sessions", indexes = {
    @Index(name = "idx_activity_session_start_time", columnList = "startTime"),
    @Index(name = "idx_activity_session_end_time", columnList = "endTime"),
    @Index(name = "idx_activity_session_type", columnList = "activityType"),
    @Index(name = "idx_activity_session_user", columnList = "userId"),
    @Index(name = "idx_activity_session_processed", columnList = "processed")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySession {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Column(nullable = false)
    private String timezone;  // Store as String, convert to ZoneId when needed

    // Metrics - nullable based on activity type
    private Long stepCount;

    private Double caloriesBurned;

    private Integer averageHeartRate;

    private Integer minHeartRate;

    private Integer maxHeartRate;

    // Exercise-specific fields
    private String exerciseType;

    private String exerciseTitle;

    // Metadata
    @Column(nullable = false)
    private String dataSource;  // "Samsung Health", "Google Fit", etc.

    @Column(unique = true)
    private String healthConnectRecordId;  // Original Health Connect record ID

    @Column(nullable = false)
    private Instant ingestedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean processed = false;  // Has this been segmented?

    @Version
    private Long version;  // Optimistic locking

    /**
     * Calculate the duration of this session.
     */
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    /**
     * Get the timezone as ZoneId.
     */
    public ZoneId getZoneId() {
        return ZoneId.of(timezone);
    }
}

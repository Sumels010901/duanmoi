package com.worktime.model;

import com.worktime.model.enums.DayType;
import com.worktime.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity representing pre-computed daily aggregations.
 * Used for fast analytics queries without scanning all segments.
 */
@Entity
@Table(name = "daily_aggregations", indexes = {
    @Index(name = "idx_daily_agg_date", columnList = "date"),
    @Index(name = "idx_daily_agg_user", columnList = "userId"),
    @Index(name = "idx_daily_agg_day_type", columnList = "dayType")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAggregation extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayType dayType;

    // Work hours metrics
    private Long workHoursSteps;
    private Double workHoursCalories;
    private Integer workHoursActiveMinutes;
    private Integer workHoursAvgHeartRate;

    // Off hours metrics
    private Long offHoursSteps;
    private Double offHoursCalories;
    private Integer offHoursActiveMinutes;
    private Integer offHoursAvgHeartRate;

    // Total metrics
    private Long totalSteps;
    private Double totalCalories;
    private Integer totalActiveMinutes;

    // Sleep metrics
    private Long sleepDurationSeconds;  // Store as seconds
    private Double sleepQualityScore;

    @Column(nullable = false)
    private Instant computedAt;
}

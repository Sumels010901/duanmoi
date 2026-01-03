package com.worktime.model;

import com.worktime.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entity representing the user's working schedule.
 * Defines working hours for each day of the week.
 */
@Entity
@Table(name = "working_schedules", indexes = {
    @Index(name = "idx_working_schedule_user", columnList = "userId"),
    @Index(name = "idx_working_schedule_day", columnList = "dayOfWeek"),
    @Index(name = "idx_working_schedule_active", columnList = "isActive")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingSchedule extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Column(nullable = false)
    private String timezone;  // Store as String (e.g., "Asia/Ho_Chi_Minh")

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    private Instant effectiveFrom;

    private Instant effectiveTo;
}

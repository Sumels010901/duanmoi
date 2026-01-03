package com.worktime.model;

import com.worktime.model.enums.OverrideType;
import com.worktime.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entity representing exceptions to the regular working schedule.
 * Used for holidays, PTO, irregular work days, etc.
 */
@Entity
@Table(name = "schedule_overrides", indexes = {
    @Index(name = "idx_schedule_override_date", columnList = "date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleOverride extends BaseEntity {

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OverrideType overrideType;

    private Instant customStartTime;

    private Instant customEndTime;

    private String reason;
}

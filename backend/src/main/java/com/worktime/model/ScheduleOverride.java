package com.worktime.model;

import com.worktime.model.enums.OverrideType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

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
public class ScheduleOverride {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OverrideType overrideType;

    private LocalTime customStartTime;

    private LocalTime customEndTime;

    private String reason;
}

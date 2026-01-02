# Task 02: Create JPA Entities

## Objective
Create all JPA entity classes and enums for the core data models.

## Working Directory
```
/Users/dangthang/duancanhan/duanmoi/backend/src/main/java/com/worktime/model
```

## Prerequisites
- Task 01 completed (Spring Boot project setup)
- Lombok dependency available in pom.xml

## Entities to Create

### 1. ActivitySession.java
**Location:** `backend/src/main/java/com/worktime/model/ActivitySession.java`

**Purpose:** Stores raw activity sessions as ingested from Health Connect.

**Specification from PRD Section 6.1:**
```java
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
```

### 2. ActivitySegment.java
**Location:** `backend/src/main/java/com/worktime/model/ActivitySegment.java`

```java
package com.worktime.model;

import com.worktime.model.enums.TimeSegmentType;
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
public class ActivitySegment {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;
    
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
    
    @Column(nullable = false)
    private Instant createdAt;
    
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
```

### 3. WorkingSchedule.java
**Location:** `backend/src/main/java/com/worktime/model/WorkingSchedule.java`

```java
package com.worktime.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.DayOfWeek;
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
public class WorkingSchedule {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;
    
    @Column(nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column(nullable = false)
    private String timezone;  // Store as String (e.g., "Asia/Ho_Chi_Minh")
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    private LocalDate effectiveFrom;
    
    private LocalDate effectiveTo;
}
```

### 4. ScheduleOverride.java
**Location:** `backend/src/main/java/com/worktime/model/ScheduleOverride.java`

```java
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
```

### 5. DailyAggregation.java
**Location:** `backend/src/main/java/com/worktime/model/DailyAggregation.java`

```java
package com.worktime.model;

import com.worktime.model.enums.DayType;
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
public class DailyAggregation {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;
    
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
```

---

## Enums to Create

### ActivityType.java
**Location:** `backend/src/main/java/com/worktime/model/enums/ActivityType.java`

```java
package com.worktime.model.enums;

/**
 * Enumeration of activity types supported by the system.
 */
public enum ActivityType {
    STEPS,
    HEART_RATE,
    EXERCISE_SESSION,
    SLEEP_SESSION,
    CALORIES_BURNED
}
```

### TimeSegmentType.java
**Location:** `backend/src/main/java/com/worktime/model/enums/TimeSegmentType.java`

```java
package com.worktime.model.enums;

/**
 * Enumeration of time segment classifications.
 */
public enum TimeSegmentType {
    WORK_HOURS,
    OFF_HOURS
}
```

### DayType.java
**Location:** `backend/src/main/java/com/worktime/model/enums/DayType.java`

```java
package com.worktime.model.enums;

/**
 * Enumeration of day types for classification.
 */
public enum DayType {
    WORKDAY,
    NON_WORKDAY,
    HOLIDAY,
    PTO,
    SICK_DAY
}
```

### OverrideType.java
**Location:** `backend/src/main/java/com/worktime/model/enums/OverrideType.java`

```java
package com.worktime.model.enums;

/**
 * Enumeration of schedule override types.
 */
public enum OverrideType {
    HOLIDAY,
    PTO,
    IRREGULAR_WORK,
    CUSTOM
}
```

---

## Success Criteria
- [ ] All 5 entity classes created
- [ ] All 4 enum classes created
- [ ] Proper JPA annotations applied (@Entity, @Table, @Id, @Column, @Index)
- [ ] Lombok annotations used (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- [ ] UUID used for all primary keys
- [ ] Proper relationships defined (ManyToOne in ActivitySegment)
- [ ] Indexes added for common query patterns
- [ ] JavaDoc comments added for all classes
- [ ] Project compiles without errors

## Validation Commands
```bash
cd /Users/dangthang/duancanhan/duanmoi/backend

# Compile the project
mvn clean compile

# Should show no errors
```

## File Structure Check
After completion, verify this structure exists:
```
backend/src/main/java/com/worktime/
├── WorkTimeAnalyticsApplication.java
└── model/
    ├── ActivitySession.java
    ├── ActivitySegment.java
    ├── WorkingSchedule.java
    ├── ScheduleOverride.java
    ├── DailyAggregation.java
    └── enums/
        ├── ActivityType.java
        ├── TimeSegmentType.java
        ├── DayType.java
        └── OverrideType.java
```

## Notes
- All temporal fields use appropriate Java time types (Instant, LocalDate, LocalTime)
- Duration stored as Long (seconds) for easier database querying
- Timezone stored as String to avoid serialization issues
- UUID used for all IDs for better distributed system support
- Indexes added based on expected query patterns from PRD Section 8

## Next Task
After completing this task, proceed to:
- Task 03: Create Flyway Database Migration Scripts

## Reference Documents
- PRD Section 6: Data Models
- PRD Section 6.2: Enumerations
- docs/DEVELOPMENT_CONTEXT.md

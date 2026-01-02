# Product Requirements Document (PRD)

**Product Name:** Personal Work-Time Activity Analytics System  
**Version:** 1.0  
**Status:** Final Draft  
**Last Updated:** January 2, 2026  
**Owner:** Thang  

---

## 1. Purpose & Vision

Provide personalized insights into physical activity by distinguishing activity during working hours and outside working hours, enabling fair day-to-day comparison beyond Samsung Health limitations. The system will own the data, provide deterministic analytics, and support long-term trend analysis.

**Core Philosophy:**
- Privacy-first: Single-user, all data on-premise or user-controlled infrastructure
- Accuracy over simplicity: Precise time-based segmentation with proper overlap handling
- Maintainability: Clean architecture, well-documented, reproducible analytics

---

## 2. Goals

### Primary Goals
1. **Data Ownership**: Ingest and store all Galaxy Watch activity data via Health Connect
2. **Time-based Classification**: Accurately classify activity into Working Hours vs Off-Hours
3. **Fair Comparison**: Compare today's activity with the most recent comparable day (same day type, similar context)
4. **Trend Analysis**: Track activity patterns over weeks and months

### Success Metrics
- 100% of daily activity data captured and classified
- < 1-minute accuracy in work/off-hours boundary calculations
- Daily sync completion within 5 minutes
- Zero data loss during ingestion

---

## 3. Target User

**Profile:**
- Single user (Thang)
- Samsung Galaxy Watch 7 + Samsung S21 FE user
- Interested in self-optimization and data-driven insights
- Values privacy and data ownership
- Technical background, comfortable with system setup

**User Needs:**
- Understand activity distribution across work/personal time
- Identify sedentary work days vs active personal days
- Track compensation patterns (e.g., "Did I move more after a sedentary work day?")
- Long-term health trend monitoring

---

## 4. Core Features

### 4.1 Data Ingestion
**Description:** Automated synchronization of health data from Health Connect to backend database

**Functional Requirements:**
- FR-1.1: Android companion app reads data from Health Connect API
- FR-1.2: Support the following data types:
  - Steps (StepsRecord)
  - Heart Rate (HeartRateRecord)
  - Exercise Sessions (ExerciseSessionRecord)
  - Calories Burned (TotalCaloriesBurnedRecord)
  - Sleep Sessions (SleepSessionRecord)
- FR-1.3: Sync frequency: Hourly when app is running, daily via WorkManager background job
- FR-1.4: Handle offline scenarios with queue-based retry mechanism
- FR-1.5: Preserve all timestamps with timezone information

**Non-Functional Requirements:**
- NFR-1.1: Sync must complete within 5 minutes for a typical day's data
- NFR-1.2: Battery impact < 2% per day
- NFR-1.3: Handle network failures gracefully with exponential backoff

### 4.2 Working Schedule Configuration
**Description:** Define and manage working hours to enable accurate time-based classification

**Functional Requirements:**
- FR-2.1: Configure default working schedule (days of week, start time, end time)
- FR-2.2: Support schedule overrides for specific dates (holidays, PTO, irregular days)
- FR-2.3: Configure timezone handling for work schedule
- FR-2.4: Support multiple schedule types:
  - Regular: Fixed hours (e.g., Mon-Fri 9:00-18:00)
  - Flexible: Core hours with flex time
  - Shift: Rotating schedules
- FR-2.5: REST API to create, update, retrieve working schedules

**Data Model:**
```
WorkingSchedule {
  id: UUID
  userId: String (always single user for now)
  dayOfWeek: Enum (MON, TUE, WED, THU, FRI, SAT, SUN)
  startTime: LocalTime
  endTime: LocalTime
  timezone: String (ZoneId)
  isActive: Boolean
  effectiveFrom: LocalDate
  effectiveTo: LocalDate (nullable)
}

ScheduleOverride {
  id: UUID
  date: LocalDate
  overrideType: Enum (HOLIDAY, PTO, IRREGULAR_WORK, CUSTOM)
  customStartTime: LocalTime (nullable)
  customEndTime: LocalTime (nullable)
  reason: String
}
```

### 4.3 Activity Session Splitting
**Description:** Split activity sessions that cross work/off-hours boundaries into separate segments

**Functional Requirements:**
- FR-3.1: Detect sessions that overlap with working hour boundaries
- FR-3.2: Split overlapping sessions into work-time and off-hours segments
- FR-3.3: Proportionally allocate metrics (steps, calories, heart rate) based on time overlap
- FR-3.4: Preserve original session metadata and add derived segment metadata
- FR-3.5: Handle edge cases:
  - Sessions crossing midnight
  - Sessions crossing multiple work boundaries (e.g., lunch break)
  - Multi-day sessions (e.g., sleep)

**Business Logic:**
```
For a session S with startTime and endTime:
1. Get applicable WorkingSchedule for the date range
2. Calculate overlap: O = intersection(S.timeRange, WorkSchedule.timeRange)
3. If overlap exists:
   - Create WorkSegment: {start: max(S.start, W.start), end: min(S.end, W.end)}
   - Create OffHoursSegment: S.timeRange - WorkSegment.timeRange
4. Allocate metrics proportionally:
   - Work metrics = Total metrics × (WorkSegment.duration / S.duration)
   - OffHours metrics = Total metrics × (OffHoursSegment.duration / S.duration)
```

### 4.4 Daily Comparison & Analytics
**Description:** Compare current day with most recent comparable day and provide insights

**Functional Requirements:**
- FR-4.1: Define "comparable day" logic:
  - Same day type (workday vs non-workday)
  - Exclude anomalies (sick days, travel days)
  - Look back maximum 14 days
- FR-4.2: Calculate delta metrics:
  - Steps: work hours vs off hours
  - Active minutes
  - Calories burned
  - Average heart rate
- FR-4.3: Provide percentage change and absolute difference
- FR-4.4: REST API endpoint: GET /api/analytics/daily-comparison?date={date}

**Response Format:**
```json
{
  "targetDate": "2026-01-02",
  "comparisonDate": "2025-12-26",
  "dayType": "WORKDAY",
  "metrics": {
    "workHours": {
      "steps": { "current": 3500, "comparison": 4200, "delta": -700, "percentChange": -16.7 },
      "activeMinutes": { "current": 45, "comparison": 52, "delta": -7, "percentChange": -13.5 },
      "caloriesBurned": { "current": 280, "comparison": 320, "delta": -40, "percentChange": -12.5 }
    },
    "offHours": {
      "steps": { "current": 8200, "comparison": 6500, "delta": 1700, "percentChange": 26.2 },
      "activeMinutes": { "current": 95, "comparison": 78, "delta": 17, "percentChange": 21.8 },
      "caloriesBurned": { "current": 520, "comparison": 450, "delta": 70, "percentChange": 15.6 }
    }
  },
  "insights": [
    "You were 17% less active during work hours today",
    "You compensated with 26% more steps during off hours"
  ]
}
```

### 4.5 Trend Reporting
**Description:** Visualize activity trends over time (weekly, monthly)

**Functional Requirements:**
- FR-5.1: Weekly summary: Last 7 days aggregated by work/off hours
- FR-5.2: Monthly summary: Last 30 days with weekly breakdowns
- FR-5.3: REST API endpoints:
  - GET /api/analytics/weekly-summary
  - GET /api/analytics/monthly-summary
- FR-5.4: Support filtering by activity type and time segment

---

## 5. Technical Stack

### Backend
- **Framework:** Java 17+ with Spring Boot 3.2+
- **Build Tool:** Maven or Gradle
- **Database:** PostgreSQL 15+
- **ORM:** Spring Data JPA with Hibernate
- **API:** RESTful JSON APIs
- **Testing:** JUnit 5, Mockito, TestContainers

### Android Application
- **Language:** Kotlin
- **SDK:** Health Connect Client SDK (androidx.health.connect:connect-client)
- **Architecture:** MVVM with Jetpack Compose
- **Background Work:** WorkManager for scheduled sync
- **Network:** Retrofit + OkHttp
- **Storage:** Room (for local queue/cache)

### Web Frontend
- **Framework:** React 18+ with TypeScript
- **UI Library:** Material-UI (MUI) or Ant Design
- **Charts:** Recharts or Chart.js
- **State Management:** React Context API or Redux Toolkit
- **Build Tool:** Vite
- **API Client:** Axios

### Infrastructure
- **Deployment:** Docker + Docker Compose (for local/personal server)
- **Database Migrations:** Flyway or Liquibase
- **Logging:** SLF4J + Logback
- **Monitoring:** Spring Boot Actuator + optional Prometheus/Grafana

---

## 6. Data Models

### 6.1 Core Entities

#### ActivitySession (Raw Data)
Stores raw activity sessions as ingested from Health Connect.

```java
@Entity
@Table(name = "activity_sessions")
public class ActivitySession {
    @Id
    private UUID id;
    
    private String userId; // Always single user for now
    
    @Enumerated(EnumType.STRING)
    private ActivityType activityType; // STEPS, HEART_RATE, EXERCISE, SLEEP, CALORIES
    
    private Instant startTime;
    private Instant endTime;
    private ZoneId timezone;
    
    // Metrics (nullable based on activity type)
    private Long stepCount;
    private Double caloriesBurned;
    private Integer averageHeartRate;
    private Integer minHeartRate;
    private Integer maxHeartRate;
    
    // Exercise-specific
    private String exerciseType; // RUNNING, WALKING, CYCLING, etc.
    private String exerciseTitle;
    
    // Metadata
    private String dataSource; // "Samsung Health", "Google Fit", etc.
    private String healthConnectRecordId; // Original HC record ID
    private Instant ingestedAt;
    private Boolean processed; // Has this been segmented?
    
    @Version
    private Long version; // Optimistic locking
}
```

#### ActivitySegment (Derived Data)
Stores activity segments classified as work-time or off-hours.

```java
@Entity
@Table(name = "activity_segments")
public class ActivitySegment {
    @Id
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "session_id")
    private ActivitySession sourceSession;
    
    @Enumerated(EnumType.STRING)
    private TimeSegmentType segmentType; // WORK_HOURS, OFF_HOURS
    
    private LocalDate activityDate;
    private Instant startTime;
    private Instant endTime;
    private Duration duration;
    
    // Allocated metrics (proportional to time)
    private Long stepCount;
    private Double caloriesBurned;
    private Integer averageHeartRate;
    private Integer minHeartRate;
    private Integer maxHeartRate;
    
    // Calculation metadata
    private Double allocationRatio; // What % of original session is this segment?
    private Boolean isSplit; // Was this split from a larger session?
    
    private Instant createdAt;
    
    @Index
    private LocalDate activityDate;
    
    @Index
    private TimeSegmentType segmentType;
}
```

#### WorkingSchedule
Already defined in section 4.2.

#### DailyAggregation
Pre-computed daily rollups for fast querying.

```java
@Entity
@Table(name = "daily_aggregations")
public class DailyAggregation {
    @Id
    private UUID id;
    
    private String userId;
    
    @Column(unique = true)
    private LocalDate date;
    
    @Enumerated(EnumType.STRING)
    private DayType dayType; // WORKDAY, NON_WORKDAY, HOLIDAY
    
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
    private Duration sleepDuration;
    private Double sleepQualityScore; // If available from HC
    
    private Instant computedAt;
    
    @Index
    private LocalDate date;
}
```

### 6.2 Enumerations

```java
public enum ActivityType {
    STEPS,
    HEART_RATE,
    EXERCISE_SESSION,
    SLEEP_SESSION,
    CALORIES_BURNED
}

public enum TimeSegmentType {
    WORK_HOURS,
    OFF_HOURS
}

public enum DayType {
    WORKDAY,
    NON_WORKDAY,
    HOLIDAY,
    PTO,
    SICK_DAY
}

public enum ExerciseType {
    RUNNING,
    WALKING,
    CYCLING,
    SWIMMING,
    WEIGHT_TRAINING,
    YOGA,
    OTHER
}
```

---

## 7. Key Business Logic

### 7.1 Session Splitting Algorithm

```java
public class SessionSplitter {
    
    /**
     * Split an ActivitySession into work and off-hours segments
     */
    public List<ActivitySegment> splitSession(
        ActivitySession session, 
        WorkingSchedule schedule
    ) {
        List<ActivitySegment> segments = new ArrayList<>();
        
        // Get work time boundaries for the session date(s)
        List<TimeRange> workRanges = getWorkRangesForSession(session, schedule);
        
        // For each work range, calculate overlap
        for (TimeRange workRange : workRanges) {
            TimeRange overlap = calculateOverlap(session.getTimeRange(), workRange);
            
            if (overlap != null) {
                // Create work segment
                ActivitySegment workSegment = createSegment(
                    session, 
                    overlap, 
                    TimeSegmentType.WORK_HOURS
                );
                segments.add(workSegment);
            }
        }
        
        // Calculate remaining time as off-hours
        List<TimeRange> offHoursRanges = subtractRanges(
            session.getTimeRange(), 
            workRanges
        );
        
        for (TimeRange offRange : offHoursRanges) {
            ActivitySegment offSegment = createSegment(
                session, 
                offRange, 
                TimeSegmentType.OFF_HOURS
            );
            segments.add(offSegment);
        }
        
        return segments;
    }
    
    private ActivitySegment createSegment(
        ActivitySession session,
        TimeRange range,
        TimeSegmentType segmentType
    ) {
        double ratio = range.getDuration() / session.getDuration();
        
        ActivitySegment segment = new ActivitySegment();
        segment.setSourceSession(session);
        segment.setSegmentType(segmentType);
        segment.setStartTime(range.getStart());
        segment.setEndTime(range.getEnd());
        segment.setDuration(range.getDuration());
        
        // Proportionally allocate metrics
        if (session.getStepCount() != null) {
            segment.setStepCount((long)(session.getStepCount() * ratio));
        }
        if (session.getCaloriesBurned() != null) {
            segment.setCaloriesBurned(session.getCaloriesBurned() * ratio);
        }
        // Heart rate: use weighted average, not simple proportion
        segment.setAverageHeartRate(session.getAverageHeartRate());
        
        segment.setAllocationRatio(ratio);
        segment.setIsSplit(ratio < 1.0);
        
        return segment;
    }
}
```

### 7.2 Comparable Day Selection

```java
public class ComparableDayFinder {
    
    /**
     * Find the most recent comparable day for analysis
     * 
     * Rules:
     * 1. Same day type (workday vs non-workday)
     * 2. Not marked as anomaly (sick, travel)
     * 3. Within last 14 days
     * 4. Has complete data
     */
    public Optional<LocalDate> findComparableDay(
        LocalDate targetDate,
        DayType targetDayType
    ) {
        return dailyAggregationRepository
            .findByDayTypeAndDateBetween(
                targetDayType,
                targetDate.minusDays(14),
                targetDate.minusDays(1)
            )
            .stream()
            .filter(agg -> !isAnomalous(agg))
            .filter(agg -> hasCompleteData(agg))
            .max(Comparator.comparing(DailyAggregation::getDate))
            .map(DailyAggregation::getDate);
    }
    
    private boolean isAnomalous(DailyAggregation agg) {
        // Check for extreme outliers
        if (agg.getTotalSteps() < 500) return true; // Likely sick/inactive
        if (agg.getSleepDuration() < Duration.ofHours(3)) return true;
        
        // Check for manual flags
        return agg.getDayType() == DayType.SICK_DAY;
    }
    
    private boolean hasCompleteData(DailyAggregation agg) {
        return agg.getTotalSteps() != null 
            && agg.getTotalCalories() != null
            && agg.getTotalActiveMinutes() != null;
    }
}
```

### 7.3 Daily Aggregation Computation

```java
@Service
public class DailyAggregationService {
    
    /**
     * Compute daily aggregation from activity segments
     */
    @Transactional
    public DailyAggregation computeDailyAggregation(LocalDate date) {
        List<ActivitySegment> segments = activitySegmentRepository
            .findByActivityDate(date);
        
        DailyAggregation aggregation = new DailyAggregation();
        aggregation.setUserId("default_user");
        aggregation.setDate(date);
        aggregation.setDayType(determineDayType(date));
        
        // Aggregate work hours
        aggregation.setWorkHoursSteps(
            segments.stream()
                .filter(s -> s.getSegmentType() == TimeSegmentType.WORK_HOURS)
                .mapToLong(s -> s.getStepCount() != null ? s.getStepCount() : 0)
                .sum()
        );
        
        aggregation.setWorkHoursCalories(
            segments.stream()
                .filter(s -> s.getSegmentType() == TimeSegmentType.WORK_HOURS)
                .mapToDouble(s -> s.getCaloriesBurned() != null ? s.getCaloriesBurned() : 0)
                .sum()
        );
        
        // Similar for off-hours...
        
        // Compute totals
        aggregation.setTotalSteps(
            aggregation.getWorkHoursSteps() + aggregation.getOffHoursSteps()
        );
        
        aggregation.setComputedAt(Instant.now());
        
        return dailyAggregationRepository.save(aggregation);
    }
    
    private DayType determineDayType(LocalDate date) {
        Optional<ScheduleOverride> override = scheduleOverrideRepository
            .findByDate(date);
        
        if (override.isPresent()) {
            return override.get().getOverrideType() == OverrideType.HOLIDAY 
                ? DayType.HOLIDAY 
                : DayType.NON_WORKDAY;
        }
        
        WorkingSchedule schedule = workingScheduleRepository
            .findByDayOfWeek(date.getDayOfWeek())
            .orElse(null);
        
        return schedule != null && schedule.isActive() 
            ? DayType.WORKDAY 
            : DayType.NON_WORKDAY;
    }
}
```

---

## 8. API Specifications

### 8.1 Data Ingestion API

**POST /api/v1/activity/ingest**
```json
Request:
{
  "sessions": [
    {
      "healthConnectRecordId": "hc_12345",
      "activityType": "STEPS",
      "startTime": "2026-01-02T08:00:00Z",
      "endTime": "2026-01-02T09:00:00Z",
      "timezone": "Asia/Ho_Chi_Minh",
      "stepCount": 1200,
      "dataSource": "Samsung Health"
    }
  ]
}

Response: 201 Created
{
  "ingestedCount": 1,
  "processedSessionIds": ["uuid-1"],
  "errors": []
}
```

### 8.2 Schedule Management API

**GET /api/v1/schedule/working**
```json
Response: 200 OK
{
  "schedules": [
    {
      "id": "uuid-1",
      "dayOfWeek": "MONDAY",
      "startTime": "09:00",
      "endTime": "18:00",
      "timezone": "Asia/Ho_Chi_Minh",
      "isActive": true
    }
  ]
}
```

**PUT /api/v1/schedule/working**
```json
Request:
{
  "dayOfWeek": "MONDAY",
  "startTime": "09:00",
  "endTime": "18:00",
  "timezone": "Asia/Ho_Chi_Minh"
}

Response: 200 OK
{
  "id": "uuid-1",
  "message": "Schedule updated successfully"
}
```

**POST /api/v1/schedule/override**
```json
Request:
{
  "date": "2026-01-15",
  "overrideType": "HOLIDAY",
  "reason": "Lunar New Year"
}

Response: 201 Created
```

### 8.3 Analytics API

**GET /api/v1/analytics/daily-comparison?date=2026-01-02**
Response format already specified in section 4.4.

**GET /api/v1/analytics/weekly-summary?startDate=2025-12-26**
```json
Response: 200 OK
{
  "startDate": "2025-12-26",
  "endDate": "2026-01-01",
  "dailySummaries": [
    {
      "date": "2025-12-26",
      "dayType": "WORKDAY",
      "workHours": {
        "steps": 4200,
        "activeMinutes": 52,
        "caloriesBurned": 320
      },
      "offHours": {
        "steps": 6500,
        "activeMinutes": 78,
        "caloriesBurned": 450
      }
    }
    // ... 6 more days
  ],
  "weeklyTotals": {
    "totalSteps": 75000,
    "workHoursSteps": 28000,
    "offHoursSteps": 47000
  }
}
```

---

## 9. Non-Functional Requirements

### 9.1 Performance
- API response time: < 200ms for p95
- Daily aggregation computation: < 30 seconds
- Data ingestion: < 5 minutes for a full day's data
- Database queries: < 100ms for indexed lookups

### 9.2 Scalability
- Support 10+ years of historical data (3,650+ days)
- Handle 500+ activity sessions per day
- Database size projection: ~10GB for 10 years

### 9.3 Reliability
- System uptime: 99% (acceptable for personal use)
- Data backup: Daily automated backups
- Zero data loss tolerance for ingested data

### 9.4 Security
- Android app to backend: HTTPS with certificate pinning
- Web frontend to backend: HTTPS
- API authentication: JWT tokens
- Database: Encrypted at rest (optional for personal use)

### 9.5 Maintainability
- Code coverage: > 70%
- Documentation: Inline comments + README + API docs
- Database migrations: Version-controlled with Flyway

---

## 10. Out of Scope

The following are explicitly OUT OF SCOPE for v1.0:

1. **Raw Sensor Data**: No direct access to accelerometer, gyroscope, or raw sensor streams
2. **Medical Diagnosis**: No health advice, diagnosis, or medical claims
3. **Multi-User Support**: System is designed for single-user operation only
4. **Social Features**: No sharing, leaderboards, or social comparisons
5. **Third-Party Integrations**: No Strava, Fitbit, or other platform integrations
6. **Mobile Notifications**: No push notifications or alerts
7. **Wearable App**: No companion app on Galaxy Watch itself
8. **Real-time Analytics**: Analytics are batch-computed, not real-time
9. **AI/ML Predictions**: No predictive models or anomaly detection (for v1.0)

---

## 11. Future Enhancements (Post-v1.0)

### Phase 2 (3-6 months)
- Weekly email reports
- PDF export of monthly summaries
- Advanced filtering (by exercise type, heart rate zones)
- Custom time segments (e.g., "morning commute", "lunch break")

### Phase 3 (6-12 months)
- ML-based activity quality scoring
- Anomaly detection (unusual activity patterns)
- Correlation analysis (sleep quality vs next-day activity)
- Data export to CSV/JSON for external analysis

### Phase 4 (12+ months)
- Mobile app with read-only dashboard
- Voice assistant integration (Google Assistant queries)
- Integration with nutrition tracking
- Advanced visualizations (heatmaps, trends, correlations)

---

## 12. Development Phases

### Phase 1: Backend Foundation (Weeks 1-3)
**Goal:** Establish data models, database schema, and core business logic

- Week 1: Project setup, database schema, entity models
- Week 2: Repository layer, core services, session splitting logic
- Week 3: REST API endpoints, unit tests, integration tests

**Deliverables:**
- Working REST API with all endpoints
- Database with test data
- 70%+ code coverage
- API documentation

### Phase 2: Android Sync Application (Weeks 4-6)
**Goal:** Build Android app to ingest Health Connect data

- Week 4: Health Connect integration, permission handling
- Week 5: Background sync with WorkManager, local queue
- Week 6: Network layer (Retrofit), error handling, testing

**Deliverables:**
- Android app successfully syncing data to backend
- Background sync working reliably
- Local queue for offline scenarios

### Phase 3: Web Dashboard (Weeks 7-9)
**Goal:** Build web interface for data visualization and management

- Week 7: React project setup, routing, API client
- Week 8: Daily comparison view, weekly trends view
- Week 9: Schedule management, settings, responsive design

**Deliverables:**
- Functional web dashboard
- All core features accessible via UI
- Mobile-responsive design

### Phase 4: Testing & Refinement (Weeks 10-11)
**Goal:** End-to-end testing, bug fixes, documentation

- Week 10: Integration testing, bug fixes, performance tuning
- Week 11: User acceptance testing, documentation, deployment

**Deliverables:**
- Production-ready system
- Complete documentation
- Deployment scripts

### Phase 5: Deployment (Week 12)
**Goal:** Deploy to personal server/infrastructure

- Docker Compose setup
- Database migrations
- Monitoring setup
- Backup automation

**Deliverables:**
- Running production system
- Monitoring dashboards
- Backup verification

---

## 13. Success Criteria

### MVP Success (End of Phase 1)
✅ Backend REST APIs fully functional  
✅ Data models support all core features  
✅ Session splitting logic tested and accurate  
✅ Database schema supports 10+ years of data  

### v1.0 Success (End of Phase 5)
✅ Android app syncing daily without manual intervention  
✅ Web dashboard showing accurate daily comparisons  
✅ 30+ consecutive days of data collected and analyzed  
✅ Zero critical bugs in production  
✅ User (Thang) using system daily for insights  

### Long-term Success (6 months post-launch)
✅ 180+ days of continuous data collection  
✅ Clear activity pattern trends identified  
✅ System running with < 1 hour/month maintenance  
✅ Data-driven behavior changes observed  

---

## 14. Risks & Mitigations

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Health Connect API changes | High | Medium | Monitor Android release notes, version pin dependencies |
| Data loss during sync | High | Low | Implement idempotent operations, local queue with retry |
| Inaccurate session splitting | Medium | Medium | Extensive unit testing, manual validation with sample data |
| Backend server downtime | Medium | Low | Automated backups, queue-based architecture for resilience |
| Battery drain from Android app | Medium | Medium | Optimize sync frequency, use WorkManager constraints |
| Timezone handling bugs | Medium | High | Store all times as UTC+offset, comprehensive timezone tests |

---

## 15. Appendices

### Appendix A: Database Schema Diagram
(To be created during implementation)

### Appendix B: API Collection
(Postman/Insomnia collection to be created)

### Appendix C: Sample Data
(Test dataset for development and validation)

### Appendix D: Deployment Guide
(Docker Compose configuration, setup instructions)

---

**Document History:**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1 | 2026-01-02 | Thang | Initial draft |
| 1.0 | 2026-01-02 | Thang + Claude | Complete PRD with all sections |

---

**Next Steps:**
1. Review and approve PRD
2. Set up development environment
3. Begin Phase 1: Backend Foundation

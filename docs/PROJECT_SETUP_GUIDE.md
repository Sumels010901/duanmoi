# Project Setup Guide

## Working Directory
```bash
/Users/dangthang/duancanhan/duanmoi
```

## Initial Setup Commands

### Step 1: Create Project Structure
```bash
cd /Users/dangthang/duancanhan/duanmoi

# Create main directories
mkdir -p docs
mkdir -p backend
mkdir -p android-app
mkdir -p web-frontend
mkdir -p tasks
mkdir -p scripts

# Create documentation structure
mkdir -p docs/architecture
mkdir -p docs/api
mkdir -p docs/database
```

### Step 2: Copy PRD to Project
```bash
# Download the PRD from this conversation and save it
cp ~/Downloads/Personal_Work_Time_Activity_Analytics_PRD_Complete.md ./docs/PRD.md
```

### Step 3: Initialize Git Repository
```bash
cd /Users/dangthang/duancanhan/duanmoi
git init
echo "# Personal Work-Time Activity Analytics System" > README.md

# Create .gitignore
cat > .gitignore << 'EOF'
# IDE
.idea/
*.iml
.vscode/

# Backend
backend/target/
backend/.mvn/
backend/mvnw
backend/mvnw.cmd

# Android
android-app/.gradle/
android-app/build/
android-app/local.properties
android-app/.idea/
*.apk

# Frontend
web-frontend/node_modules/
web-frontend/build/
web-frontend/dist/

# OS
.DS_Store
Thumbs.db

# Database
*.db
*.sqlite

# Logs
*.log

# Environment
.env
.env.local
EOF

git add .
git commit -m "Initial project structure"
```

### Step 4: Create Development Context File
This file will be used by Claude Code to understand the project.

```bash
cat > docs/DEVELOPMENT_CONTEXT.md << 'EOF'
# Development Context

## Project Information
**Name:** Personal Work-Time Activity Analytics System  
**Developer:** Thang  
**Start Date:** January 2, 2026  
**IDE:** IntelliJ IDEA  
**Working Directory:** /Users/dangthang/duancanhan/duanmoi  

## Current Phase
**Phase 1: Backend Foundation (Weeks 1-3)**  
Status: Starting  
Focus: Database schema, JPA entities, core business logic, REST APIs  

## Technology Stack

### Backend
- Language: Java 17
- Framework: Spring Boot 3.2+
- Build Tool: Maven
- Database: PostgreSQL 15+
- ORM: Spring Data JPA + Hibernate
- Migrations: Flyway
- Testing: JUnit 5, Mockito, TestContainers

### Android Application
- Language: Kotlin
- Min SDK: 28 (Android 9.0)
- Target SDK: 34 (Android 14)
- Health SDK: androidx.health.connect:connect-client
- Architecture: MVVM + Jetpack Compose
- Background Work: WorkManager
- Network: Retrofit + OkHttp
- Local Storage: Room

### Web Frontend
- Framework: React 18+
- Language: TypeScript
- UI Library: Material-UI (MUI)
- Charts: Recharts
- State Management: React Context API
- Build Tool: Vite
- API Client: Axios

### Infrastructure
- Containerization: Docker + Docker Compose
- Database Migrations: Flyway
- Logging: SLF4J + Logback

## Project Structure
```
/Users/dangthang/duancanhan/duanmoi/
├── docs/                          # Documentation
│   ├── PRD.md                     # Product Requirements Document
│   ├── DEVELOPMENT_CONTEXT.md     # This file
│   ├── architecture/              # Architecture diagrams
│   ├── api/                       # API documentation
│   └── database/                  # Database schema docs
├── backend/                       # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/worktime/
│   │   │   │       ├── model/          # JPA Entities
│   │   │   │       ├── repository/     # Spring Data Repositories
│   │   │   │       ├── service/        # Business Logic
│   │   │   │       ├── controller/     # REST Controllers
│   │   │   │       ├── dto/            # Data Transfer Objects
│   │   │   │       ├── config/         # Spring Configuration
│   │   │   │       └── util/           # Utilities
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/       # Flyway migrations
│   │   └── test/
│   └── pom.xml
├── android-app/                   # Kotlin Android app
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/
│   │   │   │   │   └── com/worktime/sync/
│   │   │   │   │       ├── ui/
│   │   │   │   │       ├── viewmodel/
│   │   │   │   │       ├── data/
│   │   │   │   │       ├── worker/     # WorkManager jobs
│   │   │   │   │       └── network/    # Retrofit API
│   │   │   │   └── AndroidManifest.xml
│   │   │   └── test/
│   │   └── build.gradle.kts
│   └── settings.gradle.kts
├── web-frontend/                  # React web dashboard
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/              # API clients
│   │   ├── hooks/
│   │   ├── types/
│   │   └── App.tsx
│   ├── package.json
│   └── vite.config.ts
├── scripts/                       # Utility scripts
└── docker-compose.yml             # Local development setup
```

## Key Data Models (from PRD Section 6)

### 1. ActivitySession (Raw Data)
Stores raw activity sessions from Health Connect.
- Primary table for ingested data
- Contains all metrics from wearable
- Source of truth before segmentation

### 2. ActivitySegment (Derived Data)
Activity classified as WORK_HOURS or OFF_HOURS.
- Derived from ActivitySession
- Split based on WorkingSchedule
- Proportional metric allocation

### 3. WorkingSchedule
User's working hours configuration.
- Day of week, start/end times
- Timezone information
- Supports overrides for holidays

### 4. DailyAggregation
Pre-computed daily summaries.
- Work hours vs off hours totals
- Used for fast analytics queries
- Updated daily via batch job

## Core Business Logic

### Session Splitting Algorithm
When an activity session overlaps with work hours:
1. Calculate time overlap with work schedule
2. Create work-hours segment for overlap period
3. Create off-hours segment(s) for remaining time
4. Allocate metrics proportionally by duration

### Comparable Day Selection
Find most recent comparable day for analysis:
1. Same day type (workday vs weekend)
2. Not marked as anomaly (sick, travel)
3. Within last 14 days
4. Has complete data

## User Configuration

### Working Schedule (Default)
- Monday to Friday: 09:00 - 18:00
- Timezone: Asia/Ho_Chi_Minh (UTC+7)
- Weekends: Non-working days

### Health Data Types to Track
- Steps (StepsRecord)
- Heart Rate (HeartRateRecord)
- Exercise Sessions (ExerciseSessionRecord)
- Calories Burned (TotalCaloriesBurnedRecord)
- Sleep Sessions (SleepSessionRecord)

## Development Priorities

### Week 1: Backend Setup
- [ ] Create Spring Boot project structure
- [ ] Configure PostgreSQL connection
- [ ] Create JPA entities
- [ ] Set up Flyway migrations
- [ ] Basic project runs successfully

### Week 2: Core Services
- [ ] Implement repositories
- [ ] Implement session splitting service
- [ ] Implement daily aggregation service
- [ ] Unit tests for business logic

### Week 3: REST APIs
- [ ] Data ingestion endpoints
- [ ] Schedule management endpoints
- [ ] Analytics endpoints
- [ ] Integration tests
- [ ] API documentation

## Database Configuration

### PostgreSQL Setup
```yaml
Database Name: worktime_analytics
User: worktime_user
Port: 5432
Schema: public
```

### Connection String (Development)
```
jdbc:postgresql://localhost:5432/worktime_analytics
```

## API Endpoints (Phase 1)

### Data Ingestion
- POST /api/v1/activity/ingest - Batch insert activity sessions
- GET /api/v1/activity/sessions - List activity sessions

### Schedule Management
- GET /api/v1/schedule/working - Get working schedules
- POST /api/v1/schedule/working - Create/update working schedule
- POST /api/v1/schedule/override - Create schedule override

### Analytics
- GET /api/v1/analytics/daily-comparison - Daily comparison
- GET /api/v1/analytics/weekly-summary - Weekly summary
- GET /api/v1/analytics/monthly-summary - Monthly summary

## Testing Strategy

### Unit Tests
- All business logic services
- Session splitting algorithm
- Comparable day finder
- Metric allocation logic

### Integration Tests
- REST API endpoints
- Database operations
- Complete data flow tests

### Test Coverage Target
- Minimum: 70%
- Goal: 85%

## Reference Documents
- Complete PRD: `docs/PRD.md`
- Health Connect Codelab: https://developer.android.com/codelabs/health-connect
- Spring Boot Docs: https://spring.io/projects/spring-boot

## Notes for Claude Code
- Always reference this context file for project structure
- Follow Java naming conventions (camelCase for methods, PascalCase for classes)
- Use Spring Boot best practices
- Write tests for all business logic
- Add proper logging with SLF4J
- Include JavaDoc for public methods
EOF
```

---

## Opening IntelliJ and Starting Claude Code

### Step 5: Open Project in IntelliJ
```bash
# Open IntelliJ IDEA
open -a "IntelliJ IDEA" /Users/dangthang/duancanhan/duanmoi
```

### Step 6: Start Claude Code in IntelliJ Terminal
1. Open IntelliJ IDEA
2. Open Terminal (⌃` or View → Tool Windows → Terminal)
3. Verify you're in the right directory:
```bash
pwd
# Should show: /Users/dangthang/duancanhan/duanmoi
```

### Step 7: First Claude Code Session - Backend Setup
```bash
claude-code
```

Then paste this initial prompt:
```
I'm starting a Personal Work-Time Activity Analytics System. Read the complete context from docs/DEVELOPMENT_CONTEXT.md and docs/PRD.md.

Current task: Set up the Spring Boot backend project (Phase 1, Week 1).

Please:
1. Create a Spring Boot 3.2+ Maven project in the 'backend' directory
2. Configure dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, postgresql, flyway-core, lombok
3. Set up the basic project structure as defined in DEVELOPMENT_CONTEXT.md
4. Create application.yml with PostgreSQL configuration
5. Package structure: com.worktime

Show me the pom.xml and application.yml you'll create first.
```

---

## Alternative: Specific Task Files

If you prefer breaking down tasks into smaller files:

### Create Task 01: Backend Project Setup
```bash
cat > tasks/01-backend-project-setup.md << 'EOF'
# Task 01: Backend Project Setup

## Objective
Create the Spring Boot backend project structure with Maven configuration.

## Requirements
1. Spring Boot version: 3.2+
2. Java version: 17
3. Build tool: Maven
4. Package: com.worktime
5. Artifact ID: work-time-analytics-backend

## Dependencies Required
- spring-boot-starter-web (REST API)
- spring-boot-starter-data-jpa (Database ORM)
- postgresql (PostgreSQL driver)
- flyway-core (Database migrations)
- lombok (Reduce boilerplate)
- spring-boot-starter-test (Testing)

## Project Structure
```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── worktime/
│   │   │           └── WorkTimeAnalyticsApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/
│   │           └── migration/
│   └── test/
│       └── java/
└── pom.xml
```

## Configuration (application.yml)
```yaml
spring:
  application:
    name: work-time-analytics-backend
  
  datasource:
    url: jdbc:postgresql://localhost:5432/worktime_analytics
    username: worktime_user
    password: worktime_pass
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

server:
  port: 8080

logging:
  level:
    com.worktime: DEBUG
    org.springframework.web: INFO
```

## Success Criteria
- [ ] Project builds successfully with `mvn clean install`
- [ ] Application starts without errors
- [ ] Database connection configured (even if DB doesn't exist yet)
- [ ] Flyway ready for migrations

## Reference
- PRD Section 5: Technical Stack
- DEVELOPMENT_CONTEXT.md
EOF
```

### Create Task 02: JPA Entities
```bash
cat > tasks/02-create-jpa-entities.md << 'EOF'
# Task 02: Create JPA Entities

## Objective
Create JPA entity classes for the core data models.

## Entities to Create

### 1. ActivitySession
Location: `backend/src/main/java/com/worktime/model/ActivitySession.java`
- See PRD Section 6.1 for complete specification
- Table name: activity_sessions
- Fields: id, userId, activityType, startTime, endTime, timezone, stepCount, caloriesBurned, avgHeartRate, minHeartRate, maxHeartRate, exerciseType, exerciseTitle, dataSource, healthConnectRecordId, ingestedAt, processed
- Indexes: startTime, endTime, activityType, userId

### 2. ActivitySegment
Location: `backend/src/main/java/com/worktime/model/ActivitySegment.java`
- See PRD Section 6.1 for complete specification
- Table name: activity_segments
- Relationship: ManyToOne with ActivitySession
- Fields: id, sourceSession, segmentType, activityDate, startTime, endTime, duration, stepCount, caloriesBurned, avgHeartRate, minHeartRate, maxHeartRate, allocationRatio, isSplit, createdAt
- Indexes: activityDate, segmentType

### 3. WorkingSchedule
Location: `backend/src/main/java/com/worktime/model/WorkingSchedule.java`
- See PRD Section 4.2 for complete specification
- Table name: working_schedules
- Fields: id, userId, dayOfWeek, startTime, endTime, timezone, isActive, effectiveFrom, effectiveTo

### 4. ScheduleOverride
Location: `backend/src/main/java/com/worktime/model/ScheduleOverride.java`
- Table name: schedule_overrides
- Fields: id, date, overrideType, customStartTime, customEndTime, reason

### 5. DailyAggregation
Location: `backend/src/main/java/com/worktime/model/DailyAggregation.java`
- See PRD Section 6.1 for complete specification
- Table name: daily_aggregations
- Fields: id, userId, date, dayType, workHoursSteps, workHoursCalories, workHoursActiveMinutes, workHoursAvgHeartRate, offHoursSteps, offHoursCalories, offHoursActiveMinutes, offHoursAvgHeartRate, totalSteps, totalCalories, totalActiveMinutes, sleepDuration, sleepQualityScore, computedAt

## Enums to Create

### ActivityType
Location: `backend/src/main/java/com/worktime/model/enums/ActivityType.java`
Values: STEPS, HEART_RATE, EXERCISE_SESSION, SLEEP_SESSION, CALORIES_BURNED

### TimeSegmentType
Location: `backend/src/main/java/com/worktime/model/enums/TimeSegmentType.java`
Values: WORK_HOURS, OFF_HOURS

### DayType
Location: `backend/src/main/java/com/worktime/model/enums/DayType.java`
Values: WORKDAY, NON_WORKDAY, HOLIDAY, PTO, SICK_DAY

### OverrideType
Location: `backend/src/main/java/com/worktime/model/enums/OverrideType.java`
Values: HOLIDAY, PTO, IRREGULAR_WORK, CUSTOM

## Requirements
- Use Lombok annotations (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- Use proper JPA annotations (@Entity, @Table, @Id, @GeneratedValue, @Column, @Index)
- Use UUID for all IDs
- Use proper temporal types (Instant, LocalDate, LocalTime, Duration, ZoneId)
- Add JavaDoc comments for all classes and fields

## Success Criteria
- [ ] All entities compile without errors
- [ ] Proper JPA annotations applied
- [ ] Relationships correctly defined
- [ ] Indexes added for common query patterns

## Reference
- PRD Section 6: Data Models
- PRD Section 6.2: Enumerations
EOF
```

---

## Quick Reference: Common Claude Code Commands

Once you have the context files in place, use these commands:

### Initial Setup
```bash
# From IntelliJ Terminal at /Users/dangthang/duancanhan/duanmoi
claude-code --message "Execute task in tasks/01-backend-project-setup.md"
```

### Create Entities
```bash
claude-code --message "Execute task in tasks/02-create-jpa-entities.md"
```

### Create Repositories
```bash
claude-code --message "Create Spring Data JPA repositories for all entities in backend/src/main/java/com/worktime/repository/. Include custom query methods for common access patterns described in PRD Section 8."
```

### Create Flyway Migrations
```bash
claude-code --message "Create Flyway migration SQL scripts for all entities defined in backend/src/main/java/com/worktime/model/. Place them in backend/src/main/resources/db/migration/. Start with V1__create_initial_schema.sql"
```

### Create REST Controllers
```bash
claude-code --message "Create REST controllers for the API endpoints defined in PRD Section 8. Place them in backend/src/main/java/com/worktime/controller/"
```

---

## Summary

**Your working directory is now:**
```
/Users/dangthang/duancanhan/duanmoi
```

**Next steps:**
1. Run the setup commands above to create project structure
2. Copy the PRD to docs/PRD.md
3. Open IntelliJ at that directory
4. Start Claude Code from IntelliJ Terminal
5. Begin with backend setup using the task files

**All ready to start coding!** 🚀

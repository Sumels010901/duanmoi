# Development Context

## Project Information
**Name:** Personal Work-Time Activity Analytics System  
**Developer:** Thang  
**Start Date:** January 2, 2026  
**IDE:** IntelliJ IDEA  
**Working Directory:** /Users/dangthang/duancanhan/duanmoi  

## Current Phase
**Phase 1: Backend Foundation (Weeks 1-3)**  
Status: Starting - Task 01  
Focus: Spring Boot project setup  

## Technology Stack

### Backend
- Language: Java 17
- Framework: Spring Boot 3.2+
- Build Tool: Maven
- Database: PostgreSQL 15+
- ORM: Spring Data JPA + Hibernate
- Migrations: Flyway
- Testing: JUnit 5, Mockito

### Android Application
- Language: Kotlin
- Min SDK: 28 (Android 9.0)
- Health SDK: androidx.health.connect:connect-client

### Web Frontend
- Framework: React 18+
- Language: TypeScript
- UI Library: Material-UI

## Project Structure
```
/Users/dangthang/duancanhan/duanmoi/
├── docs/                          # Documentation
│   ├── PRD.md                     # Product Requirements Document
│   ├── DEVELOPMENT_CONTEXT.md     # This file
│   ├── SETUP.md                   # Setup guide
│   └── CLAUDE_CODE_REFERENCE.md   # Claude Code commands
├── tasks/                         # Task definitions
│   ├── 01-backend-project-setup.md
│   └── 02-create-jpa-entities.md
├── backend/                       # Spring Boot backend (to be created)
├── android-app/                   # Android app (future)
└── web-frontend/                  # React app (future)
```

## Working Schedule (for testing)
- Monday to Friday: 09:00 - 18:00
- Timezone: Asia/Ho_Chi_Minh (UTC+7)

## Current Task
Task 01: Backend Project Setup
- Create Spring Boot project structure
- Configure Maven dependencies
- Set up application.yml
- Verify project builds

## Reference Documents
- Complete PRD: `docs/PRD.md`
- Setup Guide: `docs/SETUP.md`
- Claude Code Commands: `docs/CLAUDE_CODE_REFERENCE.md`

## Notes for Claude Code
- Always reference this context file for project structure
- Follow Java naming conventions
- Use Spring Boot best practices
- Working directory: /Users/dangthang/duancanhan/duanmoi

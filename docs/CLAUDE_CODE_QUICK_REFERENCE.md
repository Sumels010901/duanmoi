# Claude Code Quick Reference

## Your Project Directory
```bash
/Users/dangthang/duancanhan/duanmoi
```

---

## Initial Setup (One-Time)

### 1. Create Project Structure
```bash
cd /Users/dangthang/duancanhan/duanmoi

# Create directories
mkdir -p docs backend android-app web-frontend tasks scripts
mkdir -p docs/architecture docs/api docs/database

# Copy files from this conversation:
# - PROJECT_SETUP_GUIDE.md → docs/
# - Personal_Work_Time_Activity_Analytics_PRD_Complete.md → docs/PRD.md
# - TASK_01_Backend_Project_Setup.md → tasks/01-backend-project-setup.md
# - TASK_02_Create_JPA_Entities.md → tasks/02-create-jpa-entities.md

# Initialize git
git init
```

### 2. Create Context Files
Copy the DEVELOPMENT_CONTEXT.md content from PROJECT_SETUP_GUIDE.md:
```bash
# See PROJECT_SETUP_GUIDE.md Step 4 for the full content
```

---

## Starting Claude Code in IntelliJ

### Open IntelliJ Terminal
1. Open IntelliJ IDEA
2. Open your project: `/Users/dangthang/duancanhan/duanmoi`
3. Open Terminal (⌃` or View → Tool Windows → Terminal)
4. Verify location:
```bash
pwd
# Should show: /Users/dangthang/duancanhan/duanmoi
```

### Launch Claude Code
```bash
claude-code
```

---

## Phase 1: Backend Development Commands

### Task 01: Backend Project Setup
```bash
claude-code --message "Execute the task in tasks/01-backend-project-setup.md. Create the Spring Boot project structure in the backend directory as specified."
```

**Or interactive:**
```bash
claude-code
```
Then paste:
```
Read tasks/01-backend-project-setup.md and create the Spring Boot backend project with all specified dependencies, configuration files, and directory structure. The working directory is /Users/dangthang/duancanhan/duanmoi/backend.
```

### Task 02: Create JPA Entities
```bash
claude-code --message "Execute the task in tasks/02-create-jpa-entities.md. Create all JPA entity classes and enums as specified in the PRD Section 6."
```

### Task 03: Create Flyway Migrations
```bash
claude-code --message "Create Flyway migration scripts for all entities in backend/src/main/java/com/worktime/model/. Generate SQL DDL statements for PostgreSQL and save them in backend/src/main/resources/db/migration/ starting with V1__create_initial_schema.sql"
```

### Task 04: Create Spring Data Repositories
```bash
claude-code --message "Create Spring Data JPA repositories for all entities. Include custom query methods for common access patterns defined in PRD Section 8. Place them in backend/src/main/java/com/worktime/repository/"
```

### Task 05: Create Service Layer
```bash
claude-code --message "Create service classes for core business logic: ActivityIngestionService, SessionSplitterService, DailyAggregationService, and ComparableDayFinderService. Implement the algorithms defined in PRD Section 7. Place them in backend/src/main/java/com/worktime/service/"
```

### Task 06: Create REST Controllers
```bash
claude-code --message "Create REST controllers for the API endpoints defined in PRD Section 8: DataIngestionController, ScheduleManagementController, and AnalyticsController. Place them in backend/src/main/java/com/worktime/controller/"
```

### Task 07: Create DTOs
```bash
claude-code --message "Create Data Transfer Objects (DTOs) for API requests and responses based on the JSON examples in PRD Section 8. Place them in backend/src/main/java/com/worktime/dto/"
```

### Task 08: Write Unit Tests
```bash
claude-code --message "Create unit tests for the session splitting algorithm in SessionSplitterService and the comparable day finder logic. Use JUnit 5 and Mockito. Place tests in backend/src/test/java/com/worktime/service/"
```

---

## Database Setup Commands

### Setup PostgreSQL (macOS with Homebrew)
```bash
# Install PostgreSQL
brew install postgresql@15

# Start PostgreSQL
brew services start postgresql@15

# Create database and user
psql postgres
```

Then in psql:
```sql
CREATE DATABASE worktime_analytics;
CREATE USER worktime_user WITH PASSWORD 'worktime_pass';
GRANT ALL PRIVILEGES ON DATABASE worktime_analytics TO worktime_user;
\q
```

### Run Flyway Migrations
```bash
cd /Users/dangthang/duancanhan/duanmoi/backend
mvn flyway:migrate
```

---

## Common Development Tasks

### Build and Run Backend
```bash
cd /Users/dangthang/duancanhan/duanmoi/backend

# Build
mvn clean install

# Run
mvn spring-boot:run

# Run tests
mvn test
```

### Check Code Quality
```bash
# Compile and check for errors
mvn clean compile

# Run all tests
mvn clean test

# Generate test coverage report
mvn clean test jacoco:report
```

### Add a New Feature
```bash
claude-code --message "I need to add a new feature: [describe feature]. Based on the existing code structure and PRD, help me implement this feature with proper service layer, controller, and tests."
```

### Fix a Bug
```bash
claude-code --message "I'm seeing this error: [paste error]. Help me debug and fix it based on the code in [file path]."
```

### Refactor Code
```bash
claude-code --message "Review the code in [file path] and suggest refactoring improvements following Spring Boot best practices and the architecture defined in PRD."
```

---

## Useful Patterns for Claude Code

### Pattern 1: Task-Based Execution
**Best for:** Well-defined tasks with clear requirements
```bash
claude-code --message "Execute task in tasks/[task-file].md"
```

### Pattern 2: Reference-Based Implementation
**Best for:** Implementing specific sections from PRD
```bash
claude-code --message "Implement the [feature name] as defined in PRD Section [X]. Place code in [directory]."
```

### Pattern 3: File-Specific Operations
**Best for:** Working with existing files
```bash
claude-code --message "In the file [path], add [functionality]. Follow the existing code style and patterns."
```

### Pattern 4: Multi-Step Operations
**Best for:** Complex changes
```bash
claude-code
```
Then have a conversation:
```
Me: I need to implement session splitting logic
Claude: [asks clarifying questions]
Me: Use the algorithm from PRD Section 7.1
Claude: [implements]
Me: Now add unit tests for edge cases
Claude: [adds tests]
```

---

## Debugging Claude Code Issues

### If Claude Code is not finding files:
```bash
# Always use absolute paths or verify current directory
pwd
ls -la

# Then provide full path in message
claude-code --message "Read /Users/dangthang/duancanhan/duanmoi/docs/PRD.md and..."
```

### If generated code has compilation errors:
```bash
# Ask Claude Code to fix
claude-code --message "The code you generated has compilation errors. Please review [file path] and fix the following errors: [paste errors]"
```

### If you need to see what Claude Code did:
```bash
# Review with git
git status
git diff

# Then discuss with Claude Code if needed
```

---

## Integration with This Conversation

### When to Use Claude.ai (This Conversation)
- ✅ High-level architecture discussions
- ✅ Design decision trade-offs
- ✅ API design reviews
- ✅ Complex algorithm design
- ✅ PRD updates and clarifications

### When to Use Claude Code
- ✅ Actual code generation
- ✅ File creation and modification
- ✅ Running tests and builds
- ✅ Debugging and fixing errors
- ✅ Refactoring existing code

### Workflow Example:
1. **Claude.ai**: "How should I handle timezone edge cases in session splitting?"
2. **Claude.ai**: [discusses approaches, provides pseudocode]
3. **Claude Code**: "Implement the timezone handling approach discussed, using the pseudocode from docs/algorithms/session-splitting.md"
4. **Claude Code**: [generates code]
5. **Claude Code**: "Run tests"
6. **Claude.ai** (if issues): "The tests failed with [error], what's wrong with the approach?"

---

## Quick Tips

1. **Always verify your working directory:**
   ```bash
   pwd
   ```

2. **Keep context files updated:**
   - Update `docs/DEVELOPMENT_CONTEXT.md` as you progress
   - Add new learnings to `docs/NOTES.md`

3. **Use git commits frequently:**
   ```bash
   git add .
   git commit -m "Completed task 01: Backend project setup"
   ```

4. **Reference existing code:**
   ```bash
   claude-code --message "Create a new controller similar to DataIngestionController but for [new feature]"
   ```

5. **Ask for explanations:**
   ```bash
   claude-code --message "Explain how the session splitting algorithm works in SessionSplitterService.java"
   ```

---

## Your First Commands (Right Now!)

```bash
# 1. Navigate to project
cd /Users/dangthang/duancanhan/duanmoi

# 2. Create structure
mkdir -p docs backend android-app web-frontend tasks scripts

# 3. Copy downloaded files to appropriate locations
# (Do this manually from Finder or terminal)

# 4. Open in IntelliJ
open -a "IntelliJ IDEA" /Users/dangthang/duancanhan/duanmoi

# 5. In IntelliJ Terminal:
claude-code --message "Read docs/PRD.md and tasks/01-backend-project-setup.md, then create the Spring Boot backend project structure in the backend directory."
```

**That's it! You're ready to start coding! 🚀**

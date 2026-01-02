-- =====================================================
-- Flyway Migration V1: Create Initial Schema
-- =====================================================
-- Description: Creates all core tables for the Work-Time Activity Analytics System
-- Author: Thang
-- Date: 2026-01-02
-- =====================================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- Table: activity_sessions
-- Description: Stores raw activity sessions from Health Connect
-- =====================================================
CREATE TABLE activity_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    timezone VARCHAR(50) NOT NULL,

    -- Metrics (nullable based on activity type)
    step_count BIGINT,
    calories_burned DOUBLE PRECISION,
    average_heart_rate INTEGER,
    min_heart_rate INTEGER,
    max_heart_rate INTEGER,

    -- Exercise-specific fields
    exercise_type VARCHAR(100),
    exercise_title VARCHAR(255),

    -- Metadata
    data_source VARCHAR(100) NOT NULL,
    health_connect_record_id VARCHAR(255) UNIQUE,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,

    -- Optimistic locking
    version BIGINT,

    -- Constraints
    CONSTRAINT chk_activity_type CHECK (activity_type IN ('STEPS', 'HEART_RATE', 'EXERCISE_SESSION', 'SLEEP_SESSION', 'CALORIES_BURNED')),
    CONSTRAINT chk_time_order CHECK (end_time > start_time)
);

-- Indexes for activity_sessions
CREATE INDEX idx_activity_session_start_time ON activity_sessions(start_time);
CREATE INDEX idx_activity_session_end_time ON activity_sessions(end_time);
CREATE INDEX idx_activity_session_type ON activity_sessions(activity_type);
CREATE INDEX idx_activity_session_user ON activity_sessions(user_id);
CREATE INDEX idx_activity_session_processed ON activity_sessions(processed);

-- =====================================================
-- Table: activity_segments
-- Description: Stores activity segments classified as work-time or off-hours
-- =====================================================
CREATE TABLE activity_segments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL,
    segment_type VARCHAR(20) NOT NULL,
    activity_date DATE NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_seconds BIGINT NOT NULL,

    -- Allocated metrics (proportional to time)
    step_count BIGINT,
    calories_burned DOUBLE PRECISION,
    average_heart_rate INTEGER,
    min_heart_rate INTEGER,
    max_heart_rate INTEGER,

    -- Calculation metadata
    allocation_ratio DOUBLE PRECISION NOT NULL,
    is_split BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    -- Foreign key constraint
    CONSTRAINT fk_activity_segment_session FOREIGN KEY (session_id)
        REFERENCES activity_sessions(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_segment_type CHECK (segment_type IN ('WORK_HOURS', 'OFF_HOURS')),
    CONSTRAINT chk_segment_time_order CHECK (end_time > start_time),
    CONSTRAINT chk_allocation_ratio CHECK (allocation_ratio >= 0 AND allocation_ratio <= 1),
    CONSTRAINT chk_duration_positive CHECK (duration_seconds > 0)
);

-- Indexes for activity_segments
CREATE INDEX idx_activity_segment_date ON activity_segments(activity_date);
CREATE INDEX idx_activity_segment_type ON activity_segments(segment_type);
CREATE INDEX idx_activity_segment_session ON activity_segments(session_id);

-- =====================================================
-- Table: working_schedules
-- Description: Stores user's working schedule configuration
-- =====================================================
CREATE TABLE working_schedules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    timezone VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE,
    effective_to DATE,

    -- Constraints
    CONSTRAINT chk_day_of_week CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    CONSTRAINT chk_schedule_time_order CHECK (end_time > start_time),
    CONSTRAINT chk_effective_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

-- Indexes for working_schedules
CREATE INDEX idx_working_schedule_user ON working_schedules(user_id);
CREATE INDEX idx_working_schedule_day ON working_schedules(day_of_week);
CREATE INDEX idx_working_schedule_active ON working_schedules(is_active);

-- =====================================================
-- Table: schedule_overrides
-- Description: Stores exceptions to regular working schedule (holidays, PTO, etc.)
-- =====================================================
CREATE TABLE schedule_overrides (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    date DATE NOT NULL UNIQUE,
    override_type VARCHAR(20) NOT NULL,
    custom_start_time TIME,
    custom_end_time TIME,
    reason VARCHAR(500),

    -- Constraints
    CONSTRAINT chk_override_type CHECK (override_type IN ('HOLIDAY', 'PTO', 'IRREGULAR_WORK', 'CUSTOM')),
    CONSTRAINT chk_override_time_order CHECK (
        (custom_start_time IS NULL AND custom_end_time IS NULL) OR
        (custom_start_time IS NOT NULL AND custom_end_time IS NOT NULL AND custom_end_time > custom_start_time)
    )
);

-- Index for schedule_overrides
CREATE INDEX idx_schedule_override_date ON schedule_overrides(date);

-- =====================================================
-- Table: daily_aggregations
-- Description: Stores pre-computed daily aggregations for fast analytics
-- =====================================================
CREATE TABLE daily_aggregations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255) NOT NULL,
    date DATE NOT NULL UNIQUE,
    day_type VARCHAR(20) NOT NULL,

    -- Work hours metrics
    work_hours_steps BIGINT,
    work_hours_calories DOUBLE PRECISION,
    work_hours_active_minutes INTEGER,
    work_hours_avg_heart_rate INTEGER,

    -- Off hours metrics
    off_hours_steps BIGINT,
    off_hours_calories DOUBLE PRECISION,
    off_hours_active_minutes INTEGER,
    off_hours_avg_heart_rate INTEGER,

    -- Total metrics
    total_steps BIGINT,
    total_calories DOUBLE PRECISION,
    total_active_minutes INTEGER,

    -- Sleep metrics
    sleep_duration_seconds BIGINT,
    sleep_quality_score DOUBLE PRECISION,

    computed_at TIMESTAMP WITH TIME ZONE NOT NULL,

    -- Constraints
    CONSTRAINT chk_day_type CHECK (day_type IN ('WORKDAY', 'NON_WORKDAY', 'HOLIDAY', 'PTO', 'SICK_DAY')),
    CONSTRAINT chk_sleep_quality CHECK (sleep_quality_score IS NULL OR (sleep_quality_score >= 0 AND sleep_quality_score <= 100))
);

-- Indexes for daily_aggregations
CREATE INDEX idx_daily_agg_date ON daily_aggregations(date);
CREATE INDEX idx_daily_agg_user ON daily_aggregations(user_id);
CREATE INDEX idx_daily_agg_day_type ON daily_aggregations(day_type);

-- =====================================================
-- Comments on Tables
-- =====================================================
COMMENT ON TABLE activity_sessions IS 'Raw activity sessions ingested from Health Connect';
COMMENT ON TABLE activity_segments IS 'Activity segments classified into work hours and off hours';
COMMENT ON TABLE working_schedules IS 'User working schedule configuration by day of week';
COMMENT ON TABLE schedule_overrides IS 'Exceptions to regular working schedule';
COMMENT ON TABLE daily_aggregations IS 'Pre-computed daily activity summaries for fast queries';

-- =====================================================
-- End of Migration V1
-- =====================================================

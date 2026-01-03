-- =====================================================
-- Flyway Migration V2: Add Audit Fields
-- =====================================================
-- Description: Adds audit fields (created_at, created_by, updated_at, updated_by, is_deleted) to all tables
-- Author: Thang
-- Date: 2026-01-03
-- =====================================================

-- =====================================================
-- Table: activity_sessions
-- =====================================================
ALTER TABLE activity_sessions
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN created_by UUID,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN updated_by UUID,
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Set created_at for existing rows
UPDATE activity_sessions SET created_at = NOW() WHERE created_at IS NULL;

-- Make created_at NOT NULL after setting values
ALTER TABLE activity_sessions ALTER COLUMN created_at SET NOT NULL;

-- Create index on is_deleted
CREATE INDEX idx_activity_session_is_deleted ON activity_sessions(is_deleted);

-- =====================================================
-- Table: activity_segments
-- Note: This table already has created_at column
-- =====================================================
ALTER TABLE activity_segments
    ADD COLUMN created_by UUID,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN updated_by UUID,
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index on is_deleted
CREATE INDEX idx_activity_segment_is_deleted ON activity_segments(is_deleted);

-- =====================================================
-- Table: working_schedules
-- =====================================================
ALTER TABLE working_schedules
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN created_by UUID,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN updated_by UUID,
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Set created_at for existing rows
UPDATE working_schedules SET created_at = NOW() WHERE created_at IS NULL;

-- Make created_at NOT NULL after setting values
ALTER TABLE working_schedules ALTER COLUMN created_at SET NOT NULL;

-- Create index on is_deleted
CREATE INDEX idx_working_schedule_is_deleted ON working_schedules(is_deleted);

-- =====================================================
-- Table: schedule_overrides
-- =====================================================
ALTER TABLE schedule_overrides
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN created_by UUID,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN updated_by UUID,
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Set created_at for existing rows
UPDATE schedule_overrides SET created_at = NOW() WHERE created_at IS NULL;

-- Make created_at NOT NULL after setting values
ALTER TABLE schedule_overrides ALTER COLUMN created_at SET NOT NULL;

-- Create index on is_deleted
CREATE INDEX idx_schedule_override_is_deleted ON schedule_overrides(is_deleted);

-- =====================================================
-- Table: daily_aggregations
-- =====================================================
ALTER TABLE daily_aggregations
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN created_by UUID,
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN updated_by UUID,
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Set created_at for existing rows
UPDATE daily_aggregations SET created_at = NOW() WHERE created_at IS NULL;

-- Make created_at NOT NULL after setting values
ALTER TABLE daily_aggregations ALTER COLUMN created_at SET NOT NULL;

-- Create index on is_deleted
CREATE INDEX idx_daily_agg_is_deleted ON daily_aggregations(is_deleted);

-- =====================================================
-- Comments on Audit Fields
-- =====================================================
COMMENT ON COLUMN activity_sessions.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN activity_sessions.created_by IS 'UUID of user who created the record';
COMMENT ON COLUMN activity_sessions.updated_at IS 'Timestamp when the record was last updated';
COMMENT ON COLUMN activity_sessions.updated_by IS 'UUID of user who last updated the record';
COMMENT ON COLUMN activity_sessions.is_deleted IS 'Soft delete flag';

COMMENT ON COLUMN activity_segments.created_by IS 'UUID of user who created the record';
COMMENT ON COLUMN activity_segments.updated_at IS 'Timestamp when the record was last updated';
COMMENT ON COLUMN activity_segments.updated_by IS 'UUID of user who last updated the record';
COMMENT ON COLUMN activity_segments.is_deleted IS 'Soft delete flag';

COMMENT ON COLUMN working_schedules.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN working_schedules.created_by IS 'UUID of user who created the record';
COMMENT ON COLUMN working_schedules.updated_at IS 'Timestamp when the record was last updated';
COMMENT ON COLUMN working_schedules.updated_by IS 'UUID of user who last updated the record';
COMMENT ON COLUMN working_schedules.is_deleted IS 'Soft delete flag';

COMMENT ON COLUMN schedule_overrides.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN schedule_overrides.created_by IS 'UUID of user who created the record';
COMMENT ON COLUMN schedule_overrides.updated_at IS 'Timestamp when the record was last updated';
COMMENT ON COLUMN schedule_overrides.updated_by IS 'UUID of user who last updated the record';
COMMENT ON COLUMN schedule_overrides.is_deleted IS 'Soft delete flag';

COMMENT ON COLUMN daily_aggregations.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN daily_aggregations.created_by IS 'UUID of user who created the record';
COMMENT ON COLUMN daily_aggregations.updated_at IS 'Timestamp when the record was last updated';
COMMENT ON COLUMN daily_aggregations.updated_by IS 'UUID of user who last updated the record';
COMMENT ON COLUMN daily_aggregations.is_deleted IS 'Soft delete flag';

-- =====================================================
-- End of Migration V2
-- =====================================================
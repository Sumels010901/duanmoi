ALTER TABLE activity_sessions
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE;

-- =====================================================
-- Table: activity_segments
-- Note: This table already has created_at column
-- =====================================================
ALTER TABLE activity_segments
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE;

-- =====================================================
-- Table: working_schedules
-- =====================================================
ALTER TABLE working_schedules
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN start_time TYPE TIMESTAMPTZ
        USING (CURRENT_DATE + start_time) AT TIME ZONE 'UTC',
    ALTER COLUMN end_time TYPE TIMESTAMPTZ
        USING (CURRENT_DATE + end_time) AT TIME ZONE 'UTC',
    ALTER COLUMN effective_from TYPE TIMESTAMPTZ
        USING effective_from::timestamp AT TIME ZONE 'UTC',
    ALTER COLUMN effective_to TYPE TIMESTAMPTZ
        USING effective_to::timestamp AT TIME ZONE 'UTC';

-- =====================================================
-- Table: schedule_overrides
-- =====================================================
ALTER TABLE schedule_overrides
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN custom_start_time TYPE TIMESTAMPTZ
        USING (CURRENT_DATE + custom_start_time) AT TIME ZONE 'UTC',
    ALTER COLUMN custom_end_time TYPE TIMESTAMPTZ
        USING (CURRENT_DATE + custom_end_time) AT TIME ZONE 'UTC';
-- =====================================================
-- Table: daily_aggregations
-- =====================================================
ALTER TABLE daily_aggregations
    ALTER COLUMN created_at TYPE TIMESTAMP WITH TIME ZONE,
    ALTER COLUMN updated_at TYPE TIMESTAMP WITH TIME ZONE;
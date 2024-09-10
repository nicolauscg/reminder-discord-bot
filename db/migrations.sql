--liquibase formatted sql

--changeset nicolauscg:1
-- create reminder related tables
CREATE TABLE IF NOT EXISTS draft_reminder(
    first_interaction_id VARCHAR(20) PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    guild_id VARCHAR(20) NOT NULL,
    participant_user_ids TEXT,
    title VARCHAR(50),
    description VARCHAR(500),
    updated_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS reminder(
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    guild_id VARCHAR(20) NOT NULL,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    is_notified_after_complete BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS reminder_participant(
    reminder_id SERIAL NOT NULL REFERENCES reminder ON DELETE CASCADE,
    user_id VARCHAR(20) NOT NULL,
    is_complete BOOLEAN NOT NULL,
    reminded_count INTEGER NOT NULL,
    last_reminded_at TIMESTAMP,
    next_remind_at TIMESTAMP NOT NULL,
    PRIMARY KEY (reminder_id, user_id)
);
/* liquibase rollback
drop table reminder_participant, reminder, draft_reminder;
*/

--changeset nicolauscg:2
-- update timestamp columns to have timezone
ALTER TABLE draft_reminder
    ALTER COLUMN updated_at SET DATA TYPE TIMESTAMPTZ,
    ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE reminder
    ALTER COLUMN created_at SET DATA TYPE TIMESTAMPTZ,
    ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE reminder_participant
    ALTER COLUMN last_reminded_at SET DATA TYPE TIMESTAMPTZ;
ALTER TABLE reminder_participant
    ALTER COLUMN next_remind_at SET DATA TYPE TIMESTAMPTZ,
    ALTER COLUMN next_remind_at SET NOT NULL;
/* liquibase rollback
ALTER TABLE draft_reminder
    ALTER COLUMN updated_at SET DATA TYPE TIMESTAMP,
    ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE reminder
    ALTER COLUMN created_at SET DATA TYPE TIMESTAMP,
    ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE reminder_participant
    ALTER COLUMN last_reminded_at SET DATA TYPE TIMESTAMP;
ALTER TABLE reminder_participant
    ALTER COLUMN next_remind_at SET DATA TYPE TIMESTAMP,
    ALTER COLUMN next_remind_at SET NOT NULL;
*/
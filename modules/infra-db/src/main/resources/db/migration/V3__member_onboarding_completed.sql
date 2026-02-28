ALTER TABLE member
    ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;

-- Existing users should not be forced back into onboarding after deployment.
UPDATE member
SET onboarding_completed = TRUE;

-- Gate 0: decimal money, account state, token revocation, attendance uniqueness.
-- This file also upgrades a database that already contains the pre-Gate-0 schema.

ALTER TABLE users ADD COLUMN account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN token_version INT NOT NULL DEFAULT 1;

ALTER TABLE project
    MODIFY area_sq_ft DECIMAL(12, 2) NOT NULL,
    MODIFY rate_per_sq_ft DECIMAL(12, 2) NOT NULL,
    MODIFY total_estimated_cost DECIMAL(14, 2) NOT NULL,
    MODIFY contract_type VARCHAR(32),
    MODIFY status VARCHAR(32);

ALTER TABLE project_requirement
    MODIFY area_sq_ft DECIMAL(12, 2) NOT NULL,
    MODIFY contract_type VARCHAR(32),
    MODIFY status VARCHAR(32);

ALTER TABLE labour_assignment
    MODIFY daily_rate DECIMAL(12, 2) NOT NULL,
    MODIFY advance_taken DECIMAL(14, 2) NOT NULL,
    MODIFY status VARCHAR(32);

ALTER TABLE attendance
    MODIFY overtime_hours DECIMAL(6, 2) NOT NULL,
    MODIFY status VARCHAR(32);

ALTER TABLE payment
    MODIFY amount DECIMAL(14, 2) NOT NULL,
    MODIFY mode VARCHAR(32);

ALTER TABLE labour_payment
    MODIFY amount DECIMAL(14, 2) NOT NULL,
    MODIFY mode VARCHAR(32),
    MODIFY type VARCHAR(32);

ALTER TABLE expense
    MODIFY amount DECIMAL(14, 2) NOT NULL,
    MODIFY quantity DECIMAL(14, 3) NOT NULL,
    MODIFY category VARCHAR(32);

ALTER TABLE contractor_profile
    MODIFY rate_per_sq_ft DECIMAL(12, 2) NOT NULL,
    DROP COLUMN aadhar_number;

ALTER TABLE labour_profile
    MODIFY default_daily_rate DECIMAL(12, 2) NOT NULL,
    MODIFY skill_type VARCHAR(32),
    DROP COLUMN aadhar_number;

ALTER TABLE role MODIFY name VARCHAR(32);
ALTER TABLE otp_tokens MODIFY purpose VARCHAR(32) NOT NULL;

CREATE UNIQUE INDEX uk_attendance_assignment_date ON attendance (labour_assignment_id, date);
CREATE INDEX idx_requirement_city_status ON project_requirement (city, status);
CREATE INDEX idx_requirement_owner ON project_requirement (house_owner_id);
CREATE INDEX idx_assignment_labour ON labour_assignment (labour_id);
CREATE INDEX idx_assignment_project ON labour_assignment (project_id);
CREATE INDEX idx_attendance_assignment ON attendance (labour_assignment_id);
CREATE INDEX idx_payment_project ON payment (project_id);
CREATE INDEX idx_labour_payment_assignment ON labour_payment (labour_assignment_id);
CREATE INDEX idx_expense_project ON expense (project_id);

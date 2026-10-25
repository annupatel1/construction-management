-- H2 mirror of the MySQL Gate 0 migration used by tests.

ALTER TABLE users ADD COLUMN account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN token_version INT NOT NULL DEFAULT 1;

ALTER TABLE project ALTER COLUMN area_sq_ft SET DATA TYPE DECIMAL(12, 2);
ALTER TABLE project ALTER COLUMN rate_per_sq_ft SET DATA TYPE DECIMAL(12, 2);
ALTER TABLE project ALTER COLUMN total_estimated_cost SET DATA TYPE DECIMAL(14, 2);
ALTER TABLE project ALTER COLUMN contract_type SET DATA TYPE VARCHAR(32);
ALTER TABLE project ALTER COLUMN status SET DATA TYPE VARCHAR(32);

ALTER TABLE project_requirement ALTER COLUMN area_sq_ft SET DATA TYPE DECIMAL(12, 2);
ALTER TABLE project_requirement ALTER COLUMN contract_type SET DATA TYPE VARCHAR(32);
ALTER TABLE project_requirement ALTER COLUMN status SET DATA TYPE VARCHAR(32);

ALTER TABLE labour_assignment ALTER COLUMN daily_rate SET DATA TYPE DECIMAL(12, 2);
ALTER TABLE labour_assignment ALTER COLUMN advance_taken SET DATA TYPE DECIMAL(14, 2);
ALTER TABLE labour_assignment ALTER COLUMN status SET DATA TYPE VARCHAR(32);

ALTER TABLE attendance ALTER COLUMN overtime_hours SET DATA TYPE DECIMAL(6, 2);
ALTER TABLE attendance ALTER COLUMN status SET DATA TYPE VARCHAR(32);

ALTER TABLE payment ALTER COLUMN amount SET DATA TYPE DECIMAL(14, 2);
ALTER TABLE payment ALTER COLUMN mode SET DATA TYPE VARCHAR(32);

ALTER TABLE labour_payment ALTER COLUMN amount SET DATA TYPE DECIMAL(14, 2);
ALTER TABLE labour_payment ALTER COLUMN mode SET DATA TYPE VARCHAR(32);
ALTER TABLE labour_payment ALTER COLUMN type SET DATA TYPE VARCHAR(32);

ALTER TABLE expense ALTER COLUMN amount SET DATA TYPE DECIMAL(14, 2);
ALTER TABLE expense ALTER COLUMN quantity SET DATA TYPE DECIMAL(14, 3);
ALTER TABLE expense ALTER COLUMN category SET DATA TYPE VARCHAR(32);

ALTER TABLE contractor_profile ALTER COLUMN rate_per_sq_ft SET DATA TYPE DECIMAL(12, 2);
ALTER TABLE contractor_profile DROP COLUMN aadhar_number;

ALTER TABLE labour_profile ALTER COLUMN default_daily_rate SET DATA TYPE DECIMAL(12, 2);
ALTER TABLE labour_profile ALTER COLUMN skill_type SET DATA TYPE VARCHAR(32);
ALTER TABLE labour_profile DROP COLUMN aadhar_number;

CREATE UNIQUE INDEX uk_attendance_assignment_date ON attendance (labour_assignment_id, date);
CREATE INDEX idx_requirement_city_status ON project_requirement (city, status);
CREATE INDEX idx_requirement_owner ON project_requirement (house_owner_id);
CREATE INDEX idx_assignment_labour ON labour_assignment (labour_id);
CREATE INDEX idx_assignment_project ON labour_assignment (project_id);
CREATE INDEX idx_attendance_assignment ON attendance (labour_assignment_id);
CREATE INDEX idx_payment_project ON payment (project_id);
CREATE INDEX idx_labour_payment_assignment ON labour_payment (labour_assignment_id);
CREATE INDEX idx_expense_project ON expense (project_id);

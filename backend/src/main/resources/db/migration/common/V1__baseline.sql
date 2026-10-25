-- Baseline schema for BuildTrack.
-- Fresh databases run this file; existing databases are baselined at version 1 and then
-- receive V2 (see db/migration/mysql and db/migration/h2).

CREATE TABLE role (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(32),
    PRIMARY KEY (id),
    CONSTRAINT uk_role_name UNIQUE (name)
);

CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    phone VARCHAR(10) NOT NULL,
    address VARCHAR(255),
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    role_id INT,
    created_by_contractor_id INT,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone UNIQUE (phone),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES role (id),
    CONSTRAINT fk_users_created_by FOREIGN KEY (created_by_contractor_id) REFERENCES users (id)
);

CREATE TABLE contractor_profile (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT,
    experience_years INT NOT NULL DEFAULT 0,
    rate_per_sq_ft DECIMAL(12, 2) NOT NULL DEFAULT 0,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    aadhar_number VARCHAR(255),
    bio VARCHAR(255),
    city VARCHAR(255),
    area VARCHAR(255),
    pincode VARCHAR(255),
    is_available BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT uk_contractor_profile_user UNIQUE (user_id),
    CONSTRAINT fk_contractor_profile_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE labour_profile (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT,
    skill_type VARCHAR(32),
    default_daily_rate DECIMAL(12, 2) NOT NULL DEFAULT 0,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    aadhar_number VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_labour_profile_user UNIQUE (user_id),
    CONSTRAINT fk_labour_profile_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE project (
    id INT NOT NULL AUTO_INCREMENT,
    house_owner_id INT,
    contractor_id INT,
    title VARCHAR(255),
    location VARCHAR(255),
    area_sq_ft DECIMAL(12, 2) NOT NULL DEFAULT 0,
    rate_per_sq_ft DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_estimated_cost DECIMAL(14, 2) NOT NULL DEFAULT 0,
    contract_type VARCHAR(32),
    status VARCHAR(32),
    start_date DATE,
    end_date DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_owner FOREIGN KEY (house_owner_id) REFERENCES users (id),
    CONSTRAINT fk_project_contractor FOREIGN KEY (contractor_id) REFERENCES users (id)
);

CREATE TABLE project_requirement (
    id INT NOT NULL AUTO_INCREMENT,
    house_owner_id INT,
    city VARCHAR(255),
    area VARCHAR(255),
    pincode VARCHAR(255),
    area_sq_ft DECIMAL(12, 2) NOT NULL DEFAULT 0,
    contract_type VARCHAR(32),
    description VARCHAR(255),
    status VARCHAR(32),
    posted_date DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_requirement_owner FOREIGN KEY (house_owner_id) REFERENCES users (id)
);

CREATE TABLE labour_assignment (
    id INT NOT NULL AUTO_INCREMENT,
    labour_id INT,
    project_id INT,
    daily_rate DECIMAL(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(32),
    advance_taken DECIMAL(14, 2) NOT NULL DEFAULT 0,
    assigned_date DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_assignment_labour FOREIGN KEY (labour_id) REFERENCES users (id),
    CONSTRAINT fk_assignment_project FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE TABLE attendance (
    id INT NOT NULL AUTO_INCREMENT,
    labour_assignment_id INT,
    date DATE,
    status VARCHAR(32),
    overtime_hours DECIMAL(6, 2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_attendance_assignment FOREIGN KEY (labour_assignment_id) REFERENCES labour_assignment (id)
);

CREATE TABLE payment (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT,
    amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    payment_date DATE,
    mode VARCHAR(32),
    remarks VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_project FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE TABLE labour_payment (
    id INT NOT NULL AUTO_INCREMENT,
    labour_assignment_id INT,
    amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    payment_date DATE,
    mode VARCHAR(32),
    type VARCHAR(32),
    remarks VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_labour_payment_assignment FOREIGN KEY (labour_assignment_id) REFERENCES labour_assignment (id)
);

CREATE TABLE expense (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT,
    category VARCHAR(32),
    amount DECIMAL(14, 2) NOT NULL DEFAULT 0,
    quantity DECIMAL(14, 3) NOT NULL DEFAULT 0,
    unit VARCHAR(255),
    purchase_date DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_expense_project FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE TABLE otp_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    identifier VARCHAR(255) NOT NULL,
    purpose VARCHAR(32) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    consumed BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id)
);

CREATE INDEX idx_otp_identifier ON otp_tokens (identifier);

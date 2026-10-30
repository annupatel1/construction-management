-- Engineer marketplace: public profiles, invitation-based project membership, and invitations.

CREATE TABLE engineer_profile (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    skills VARCHAR(500),
    experience_years INT NOT NULL DEFAULT 0,
    city VARCHAR(100),
    service_area VARCHAR(255),
    languages VARCHAR(255),
    availability VARCHAR(32) NOT NULL DEFAULT 'AVAILABLE',
    bio VARCHAR(2000),
    portfolio_url VARCHAR(500),
    credential_status VARCHAR(32) NOT NULL DEFAULT 'UNVERIFIED',
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_engineer_profile_user UNIQUE (user_id),
    CONSTRAINT fk_engineer_profile_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_engineer_city ON engineer_profile (city);
CREATE INDEX idx_engineer_specialty ON engineer_profile (specialty);
CREATE INDEX idx_engineer_availability ON engineer_profile (availability);

CREATE TABLE project_membership (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    user_id INT NOT NULL,
    member_role VARCHAR(32) NOT NULL,
    permissions VARCHAR(255),
    invited_by_id INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    removed_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_membership_project_user UNIQUE (project_id, user_id),
    CONSTRAINT fk_membership_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_membership_inviter FOREIGN KEY (invited_by_id) REFERENCES users (id)
);

CREATE INDEX idx_membership_user ON project_membership (user_id, active);
CREATE INDEX idx_membership_project ON project_membership (project_id, active);

CREATE TABLE engineer_invitation (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    inviter_id INT NOT NULL,
    engineer_id INT NOT NULL,
    scope VARCHAR(2000) NOT NULL,
    start_date DATE,
    end_date DATE,
    fee_terms VARCHAR(1000),
    requested_permissions VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    response_deadline DATE,
    response_message VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    responded_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_invitation_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_invitation_inviter FOREIGN KEY (inviter_id) REFERENCES users (id),
    CONSTRAINT fk_invitation_engineer FOREIGN KEY (engineer_id) REFERENCES users (id)
);

CREATE INDEX idx_invitation_engineer_status ON engineer_invitation (engineer_id, status);
CREATE INDEX idx_invitation_project_status ON engineer_invitation (project_id, status);
CREATE INDEX idx_invitation_inviter ON engineer_invitation (inviter_id);

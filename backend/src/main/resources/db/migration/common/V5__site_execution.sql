-- Site execution: daily progress reports, photos, milestones, tasks, and quality issues.

CREATE TABLE progress_report (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    author_id INT NOT NULL,
    report_date DATE NOT NULL,
    planned_work VARCHAR(2000) NOT NULL,
    completed_work VARCHAR(2000) NOT NULL,
    blockers VARCHAR(2000),
    next_day_plan VARCHAR(2000),
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    published_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_progress_report_project_date UNIQUE (project_id, report_date),
    CONSTRAINT fk_progress_report_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_progress_report_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE INDEX idx_progress_report_project_status ON progress_report (project_id, status, report_date);
CREATE INDEX idx_progress_report_author ON progress_report (author_id);

CREATE TABLE progress_photo (
    id INT NOT NULL AUTO_INCREMENT,
    report_id INT NOT NULL,
    photo_url VARCHAR(1000) NOT NULL,
    caption VARCHAR(255),
    taken_date DATE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_progress_photo_report FOREIGN KEY (report_id) REFERENCES progress_report (id)
);

CREATE INDEX idx_progress_photo_report ON progress_photo (report_id);

CREATE TABLE milestone (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    target_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
    completed_date DATE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_milestone_project FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE INDEX idx_milestone_project ON milestone (project_id, target_date);

CREATE TABLE project_task (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    milestone_id INT,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    assignee_id INT,
    due_date DATE,
    status VARCHAR(32) NOT NULL DEFAULT 'TODO',
    delay_reason VARCHAR(1000),
    completed_at DATETIME(6),
    created_by_id INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_task_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_project_task_milestone FOREIGN KEY (milestone_id) REFERENCES milestone (id),
    CONSTRAINT fk_project_task_assignee FOREIGN KEY (assignee_id) REFERENCES users (id),
    CONSTRAINT fk_project_task_created_by FOREIGN KEY (created_by_id) REFERENCES users (id)
);

CREATE INDEX idx_project_task_project_status ON project_task (project_id, status, due_date);
CREATE INDEX idx_project_task_assignee ON project_task (assignee_id, status);
CREATE INDEX idx_project_task_milestone ON project_task (milestone_id);

CREATE TABLE project_issue (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    raised_by_id INT NOT NULL,
    assignee_id INT,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    category VARCHAR(32) NOT NULL DEFAULT 'QUALITY',
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    due_date DATE,
    resolution_note VARCHAR(2000),
    resolution_photo_url VARCHAR(1000),
    resolved_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_issue_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_project_issue_raised_by FOREIGN KEY (raised_by_id) REFERENCES users (id),
    CONSTRAINT fk_project_issue_assignee FOREIGN KEY (assignee_id) REFERENCES users (id)
);

CREATE INDEX idx_project_issue_project_status ON project_issue (project_id, status, due_date);
CREATE INDEX idx_project_issue_assignee ON project_issue (assignee_id, status);

CREATE TABLE issue_photo (
    id INT NOT NULL AUTO_INCREMENT,
    issue_id INT NOT NULL,
    photo_url VARCHAR(1000) NOT NULL,
    caption VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_issue_photo_issue FOREIGN KEY (issue_id) REFERENCES project_issue (id)
);

CREATE INDEX idx_issue_photo_issue ON issue_photo (issue_id);

CREATE TABLE issue_comment (
    id INT NOT NULL AUTO_INCREMENT,
    issue_id INT NOT NULL,
    author_id INT NOT NULL,
    message VARCHAR(2000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_issue_comment_issue FOREIGN KEY (issue_id) REFERENCES project_issue (id),
    CONSTRAINT fk_issue_comment_author FOREIGN KEY (author_id) REFERENCES users (id)
);

CREATE INDEX idx_issue_comment_issue ON issue_comment (issue_id, created_at);

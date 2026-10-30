CREATE TABLE audit_event (
    id INT NOT NULL AUTO_INCREMENT,
    actor_id INT,
    actor_role VARCHAR(32),
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64),
    entity_id INT,
    details VARCHAR(1000),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_audit_created ON audit_event (created_at);
CREATE INDEX idx_audit_actor ON audit_event (actor_id);
CREATE INDEX idx_audit_entity ON audit_event (entity_type, entity_id);

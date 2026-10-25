package com.construction.construction_management.repository;

import com.construction.construction_management.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Integer> {
}

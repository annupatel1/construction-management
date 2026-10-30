package com.construction.construction_management.service;

public interface AuditService {

    /**
     * Records who changed what. The write joins the caller's transaction, so a rolled-back
     * payment does not leave a misleading audit row behind.
     */
    void record(String action, String entityType, Integer entityId, String details);
}

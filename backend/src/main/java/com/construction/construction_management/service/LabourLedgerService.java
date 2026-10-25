package com.construction.construction_management.service;

import com.construction.construction_management.dto.LabourLedgerResponse;

import java.util.List;

public interface LabourLedgerService {
    /** Every worker on the logged-in contractor's sites, with daily rate, advance and pending money. */
    List<LabourLedgerResponse> getLedger();
}

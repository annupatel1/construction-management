package com.construction.construction_management.controller;

import com.construction.construction_management.dto.LabourLedgerResponse;
import com.construction.construction_management.service.LabourLedgerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/labour-ledger")
public class LabourLedgerController {

    private final LabourLedgerService labourLedgerService;

    public LabourLedgerController(LabourLedgerService labourLedgerService) {
        this.labourLedgerService = labourLedgerService;
    }

    /** All workers on the contractor's sites with rate, advance, paid and pending amounts. */
    @PreAuthorize("hasRole('CONTRACTOR')")
    @GetMapping
    public List<LabourLedgerResponse> getLedger() {
        return labourLedgerService.getLedger();
    }
}

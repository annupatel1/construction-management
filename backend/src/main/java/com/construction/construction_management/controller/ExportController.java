package com.construction.construction_management.controller;

import com.construction.construction_management.service.ReportExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects/{projectId}/exports")
public class ExportController {

    private final ReportExportService reportExportService;

    public ExportController(ReportExportService reportExportService) {
        this.reportExportService = reportExportService;
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','CONTRACTOR','ADMIN')")
    @GetMapping("/finance.csv")
    public ResponseEntity<String> finance(@PathVariable int projectId) {
        return csv(reportExportService.financeCsv(projectId), "finance", projectId);
    }

    @PreAuthorize("hasAnyRole('CONTRACTOR','ADMIN')")
    @GetMapping("/attendance.csv")
    public ResponseEntity<String> attendance(@PathVariable int projectId) {
        return csv(reportExportService.attendanceCsv(projectId), "attendance", projectId);
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','CONTRACTOR','ADMIN')")
    @GetMapping("/stock.csv")
    public ResponseEntity<String> stock(@PathVariable int projectId) {
        return csv(reportExportService.stockCsv(projectId), "stock", projectId);
    }

    @PreAuthorize("hasAnyRole('HOUSE_OWNER','CONTRACTOR','ENGINEER','ADMIN')")
    @GetMapping("/site.csv")
    public ResponseEntity<String> site(@PathVariable int projectId) {
        return csv(reportExportService.siteCsv(projectId), "site", projectId);
    }

    private static ResponseEntity<String> csv(String body, String name, int projectId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + name + "-project-" + projectId + ".csv\"");
        headers.setCacheControl("no-store");
        headers.set("X-Content-Type-Options", "nosniff");
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}

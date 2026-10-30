package com.construction.construction_management.service;

public interface ReportExportService {

    String financeCsv(int projectId);

    String attendanceCsv(int projectId);

    String stockCsv(int projectId);

    String siteCsv(int projectId);
}

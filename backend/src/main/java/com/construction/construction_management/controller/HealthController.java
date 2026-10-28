package com.construction.construction_management.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Public liveness/readiness probe for staging and uptime monitoring. */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;
    private final String serviceName;

    public HealthController(DataSource dataSource,
                            @Value("${spring.application.name:buildtrack-api}") String serviceName) {
        this.dataSource = dataSource;
        this.serviceName = serviceName;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        boolean databaseUp = databaseUp();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", serviceName);
        body.put("status", databaseUp ? "UP" : "DOWN");
        body.put("database", databaseUp ? "UP" : "DOWN");
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(databaseUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(body);
    }

    private boolean databaseUp() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception ex) {
            return false;
        }
    }
}

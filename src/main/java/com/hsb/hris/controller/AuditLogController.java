package com.hsb.hris.controller;

import com.hsb.hris.entity.AuditLog;
import com.hsb.hris.repository.AuditLogRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping({"/api/audit-logs", "/api/logs"})
public class AuditLogController {

    private final AuditLogRepository repo;

    public AuditLogController(AuditLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<AuditLog> getAllLogs() {
        try {
            return repo.findAllByOrderByTimestampDesc();
        } catch (DataAccessException ex) {
            return Collections.emptyList();
        }
    }

    @PostMapping
    public ResponseEntity<AuditLog> createLog(@RequestBody AuditLog log) {
        if (log == null) {
            log = new AuditLog();
        }
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        if (log.getPerformedBy() == null || log.getPerformedBy().isBlank()) {
            log.setPerformedBy("System Admin");
        }

        try {
            AuditLog saved = repo.save(log);
            return ResponseEntity.ok(saved);
        } catch (DataAccessException ex) {
            return ResponseEntity.accepted().body(log);
        }
    }
}

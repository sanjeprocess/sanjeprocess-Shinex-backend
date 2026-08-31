package com.hsb.hris.controller;

import com.hsb.hris.entity.AuditLog;
import com.hsb.hris.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
        return repo.findAllByOrderByTimestampDesc();
    }

    @PostMapping
    public ResponseEntity<AuditLog> createLog(@RequestBody AuditLog log) {
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        if (log.getPerformedBy() == null || log.getPerformedBy().isBlank()) {
            log.setPerformedBy("System Admin");
        }
        AuditLog saved = repo.save(log);
        return ResponseEntity.ok(saved);
    }
}

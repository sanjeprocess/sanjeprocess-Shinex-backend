package com.hsb.hris.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_Audit_Log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "Performed_By", length = 100)
    private String performedBy;

    @Column(name = "Action_Type", length = 20)
    private String action; // CREATE, UPDATE, DELETE

    @Column(name = "Module_Name", length = 50)
    private String module; // EMPLOYEE, ATTENDANCE, LOAN, SECTION, etc.

    @Column(name = "Entity_ID", length = 50)
    private String entityId;

    @Column(name = "Change_Details", length = 1000)
    private String details;

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String performedBy, String action, String module, String entityId, String details) {
        this.timestamp = LocalDateTime.now();
        this.performedBy = performedBy;
        this.action = action;
        this.module = module;
        this.entityId = entityId;
        this.details = details;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}

package com.hsb.hris.controller.master;

import com.hsb.hris.entity.BusinessCenter;
import com.hsb.hris.service.master.BusinessCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping({"/api/business-centers", "/api/master/business-centers"})
public class BusinessCenterController {
    private static final Logger log = LoggerFactory.getLogger(BusinessCenterController.class);

    private final BusinessCenterService service;

    public BusinessCenterController(BusinessCenterService service) { this.service = service; }

    @GetMapping
    public List<BusinessCenter> list() { return service.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessCenter> get(@PathVariable String id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BusinessCenter bc) {
        try {
            return ResponseEntity.ok(service.save(normalize(bc)));
        } catch (RuntimeException ex) {
            log.error("Failed to save business center", ex);
            return ResponseEntity.badRequest().body("Invalid business center data: " + rootMessage(ex));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody BusinessCenter bc) {
        return service.findById(id).map(existing -> {
            bc.setCompanyId(id);
            try {
                return ResponseEntity.ok(service.save(normalize(bc)));
            } catch (RuntimeException ex) {
                log.error("Failed to update business center {}", id, ex);
                return ResponseEntity.badRequest().body("Invalid business center data: " + rootMessage(ex));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    private BusinessCenter normalize(BusinessCenter bc) {
        bc.setCompanyId(trimToLength(bc.getCompanyId(), 10));
        bc.setCompanyName(emptyToNull(bc.getCompanyName()));
        bc.setCompanyAddress(emptyToNull(bc.getCompanyAddress()));
        bc.setTelNo(emptyToNull(bc.getTelNo()));
        bc.setEmailId(emptyToNull(bc.getEmailId()));
        bc.setWebAddress(emptyToNull(bc.getWebAddress()));
        bc.setEpfReg(emptyToNull(bc.getEpfReg()));
        bc.setVatReg(emptyToNull(bc.getVatReg()));
        bc.setBrNo(emptyToNull(bc.getBrNo()));
        bc.setFaxNo(emptyToNull(bc.getFaxNo()));
        return bc;
    }

    private String trimToLength(String value, int maxLength) {
        String normalized = emptyToNull(value);
        return normalized == null ? null : normalized.substring(0, Math.min(normalized.length(), maxLength));
    }

    private String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() == null ? "database rejected the value" : current.getMessage();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id.trim());
        return ResponseEntity.noContent().build();
    }
}

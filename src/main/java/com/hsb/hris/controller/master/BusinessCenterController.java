package com.hsb.hris.controller.master;

import com.hsb.hris.entity.BusinessCenter;
import com.hsb.hris.service.master.BusinessCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master/business-centers")
public class BusinessCenterController {

    private final BusinessCenterService service;

    public BusinessCenterController(BusinessCenterService service) { this.service = service; }

    @GetMapping
    public List<BusinessCenter> list() { return service.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessCenter> get(@PathVariable String id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public BusinessCenter create(@RequestBody BusinessCenter bc) { return service.save(bc); }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessCenter> update(@PathVariable String id, @RequestBody BusinessCenter bc) {
        return service.findById(id).map(existing -> {
            bc.setCompanyId(id);
            return ResponseEntity.ok(service.save(bc));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

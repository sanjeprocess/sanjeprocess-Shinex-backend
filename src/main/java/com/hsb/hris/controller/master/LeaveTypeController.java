package com.hsb.hris.controller.master;

import com.hsb.hris.entity.LeaveType;
import com.hsb.hris.repository.LeaveTypeRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping({"/api/leave-types", "/api/master/leave-types"})
public class LeaveTypeController extends GenericMasterController<LeaveType, String> {
    public LeaveTypeController(LeaveTypeRepository repo) { super(repo); }

    @Override
    @GetMapping
    public List<LeaveType> list(@RequestParam(value = "businessCenter", required = false) String businessCenter) {
        return repo.findAll().stream().map(this::normalize).toList();
    }

    @Override
    @PostMapping
    public LeaveType create(@RequestBody LeaveType entity) {
        return repo.save(normalize(entity));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<LeaveType> update(@PathVariable String id, @RequestBody LeaveType entity) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        entity.setLeaveType(id);
        return ResponseEntity.ok(repo.save(normalize(entity)));
    }

    private LeaveType normalize(LeaveType entity) {
        entity.setLeaveType(entity.getLeaveType());
        entity.setLeaveName(entity.getLeaveName());
        return entity;
    }
}

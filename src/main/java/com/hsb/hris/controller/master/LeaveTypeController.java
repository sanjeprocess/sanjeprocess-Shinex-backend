package com.hsb.hris.controller.master;

import com.hsb.hris.entity.LeaveType;
import com.hsb.hris.repository.LeaveTypeRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/leave-types", "/api/master/leave-types"})
public class LeaveTypeController extends GenericMasterController<LeaveType, String> {
    public LeaveTypeController(LeaveTypeRepository repo) { super(repo); }
}

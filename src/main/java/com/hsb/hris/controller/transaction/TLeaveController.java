package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.TLeave;
import com.hsb.hris.repository.TLeaveRepository;
import com.hsb.hris.controller.master.GenericMasterController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/leaves", "/api/transaction/leaves"})
public class TLeaveController extends GenericMasterController<TLeave, Integer> {
    public TLeaveController(TLeaveRepository repo) { super(repo); }
}

package com.hsb.hris.controller.master;

import com.hsb.hris.entity.LoanType;
import com.hsb.hris.repository.LoanTypeRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master/loan-types")
public class LoanTypeController extends GenericMasterController<LoanType, String> {
    public LoanTypeController(LoanTypeRepository repo) { super(repo); }
}

package com.hsb.hris.controller.master;

import com.hsb.hris.entity.DeductionMaster;
import com.hsb.hris.repository.DeductionMasterRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master/deductions")
public class DeductionMasterController extends GenericMasterController<DeductionMaster, String> {
    public DeductionMasterController(DeductionMasterRepository repo) { super(repo); }
}

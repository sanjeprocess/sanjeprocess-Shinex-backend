package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.TDeduction;
import com.hsb.hris.entity.id.TDeductionId;
import com.hsb.hris.repository.TDeductionRepository;
import com.hsb.hris.controller.master.GenericMasterController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction/deductions")
public class TDeductionController extends GenericMasterController<TDeduction, TDeductionId> {
    public TDeductionController(TDeductionRepository repo) { super(repo); }
}

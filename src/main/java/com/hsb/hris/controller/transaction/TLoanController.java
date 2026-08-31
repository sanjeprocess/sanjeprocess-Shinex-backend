package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.TLoan;
import com.hsb.hris.repository.TLoanRepository;
import com.hsb.hris.controller.master.GenericMasterController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/loans", "/api/transaction/loans"})
public class TLoanController extends GenericMasterController<TLoan, String> {
    public TLoanController(TLoanRepository repo) { super(repo); }
}

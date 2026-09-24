package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.TLoan;
import com.hsb.hris.repository.TLoanRepository;
import com.hsb.hris.controller.master.GenericMasterController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/loans", "/api/transaction/loans"})
public class TLoanController extends GenericMasterController<TLoan, String> {
    private final TLoanRepository loanRepo;

    public TLoanController(TLoanRepository repo) {
        super(repo);
        this.loanRepo = repo;
    }

    @Override
    @GetMapping
    public List<TLoan> list(@RequestParam(value = "businessCenter", required = false) String businessCenter) {
        if (businessCenter != null && !businessCenter.isBlank() && !"ALL".equalsIgnoreCase(businessCenter.trim())) {
            return loanRepo.findByBusinessCenterSmart(businessCenter.trim());
        }
        return loanRepo.findAll();
    }
}

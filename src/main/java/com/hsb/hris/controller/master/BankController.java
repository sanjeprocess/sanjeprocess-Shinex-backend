package com.hsb.hris.controller.master;

import com.hsb.hris.entity.Bank;
import com.hsb.hris.repository.BankRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master/banks")
public class BankController extends GenericMasterController<Bank, String> {
    public BankController(BankRepository repo) { super(repo); }
}

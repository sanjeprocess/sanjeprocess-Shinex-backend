package com.hsb.hris.controller.master;

import com.hsb.hris.entity.RateId;
import com.hsb.hris.repository.RateIdRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master/rates")
public class RateIdController extends GenericMasterController<RateId, String> {
    public RateIdController(RateIdRepository repo) { super(repo); }
}

package com.hsb.hris.controller.master;

import com.hsb.hris.entity.OtType;
import com.hsb.hris.repository.OtTypeRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master/ot-types")
public class OtTypeController extends GenericMasterController<OtType, String> {
    public OtTypeController(OtTypeRepository repo) { super(repo); }
}

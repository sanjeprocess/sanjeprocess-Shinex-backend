package com.hsb.hris.controller.master;

import com.hsb.hris.entity.AdditionMaster;
import com.hsb.hris.repository.AdditionMasterRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/addition-types", "/api/master/additions"})
public class AdditionMasterController extends GenericMasterController<AdditionMaster, String> {
    public AdditionMasterController(AdditionMasterRepository repo) { super(repo); }
}

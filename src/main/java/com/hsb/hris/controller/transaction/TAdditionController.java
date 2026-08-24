package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.TAddition;
import com.hsb.hris.entity.id.TAdditionId;
import com.hsb.hris.repository.TAdditionRepository;
import com.hsb.hris.controller.master.GenericMasterController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction/additions")
public class TAdditionController extends GenericMasterController<TAddition, TAdditionId> {
    public TAdditionController(TAdditionRepository repo) { super(repo); }
}

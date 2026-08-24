package com.hsb.hris.controller.master;

import com.hsb.hris.entity.BCard;
import com.hsb.hris.repository.BCardRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/master/bcards")
public class BCardController extends GenericMasterController<BCard, String> {
    public BCardController(BCardRepository repo) { super(repo); }
}

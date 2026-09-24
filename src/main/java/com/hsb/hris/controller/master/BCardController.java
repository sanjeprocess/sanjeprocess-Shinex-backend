package com.hsb.hris.controller.master;

import com.hsb.hris.entity.BCard;
import com.hsb.hris.repository.BCardRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/bcards", "/api/master/bcards"})
public class BCardController extends GenericMasterController<BCard, String> {
    private final BCardRepository bCardRepo;

    public BCardController(BCardRepository repo) {
        super(repo);
        this.bCardRepo = repo;
    }

    @Override
    @GetMapping
    public List<BCard> list(@RequestParam(value = "businessCenter", required = false) String businessCenter) {
        if (businessCenter != null && !businessCenter.isBlank() && !"ALL".equalsIgnoreCase(businessCenter.trim())) {
            return bCardRepo.findByBusinessCenterSmart(businessCenter.trim());
        }
        return bCardRepo.findAll();
    }
}

package com.hsb.hris.controller.master;

import com.hsb.hris.entity.Plant;
import com.hsb.hris.repository.PlantRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/customers", "/api/master/plants"})
public class PlantController extends GenericMasterController<Plant, String> {
    public PlantController(PlantRepository repo) { super(repo); }
}

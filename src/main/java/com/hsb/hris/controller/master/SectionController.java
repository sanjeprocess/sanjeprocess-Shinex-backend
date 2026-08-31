package com.hsb.hris.controller.master;

import com.hsb.hris.entity.Section;
import com.hsb.hris.repository.SectionRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/sections", "/api/master/sections"})
public class SectionController extends GenericMasterController<Section, String> {
    public SectionController(SectionRepository repo) { super(repo); }
}

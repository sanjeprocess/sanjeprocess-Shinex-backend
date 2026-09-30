package com.hsb.hris.controller.master;

import com.hsb.hris.entity.Section;
import com.hsb.hris.repository.SectionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/sections", "/api/master/sections"})
public class SectionController extends GenericMasterController<Section, String> {
    public SectionController(SectionRepository repo) { super(repo); }

    @Override
    @GetMapping
    public List<Section> list(@RequestParam(value = "businessCenter", required = false) String businessCenter) {
        List<Section> all = repo.findAll();
        if (businessCenter == null || businessCenter.trim().isEmpty() || businessCenter.equalsIgnoreCase("ALL")) {
            return all;
        }
        String cleanBc = businessCenter.trim().toUpperCase();
        return all.stream()
                .filter(s -> {
                    String bc = s.getBusinessCenter() == null ? "" : s.getBusinessCenter().trim().toUpperCase();
                    return bc.isEmpty() || bc.equals(cleanBc) || bc.startsWith(cleanBc) || cleanBc.startsWith(bc);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/next-code")
    public String getNextCode(@RequestParam(value = "businessCenter", required = false) String businessCenter) {
        List<Section> all = repo.findAll();
        long maxNum = 0;
        for (Section s : all) {
            if (s.getSectionCode() != null) {
                String digits = s.getSectionCode().replaceAll("\\D", "");
                if (!digits.isEmpty()) {
                    try {
                        long num = Long.parseLong(digits);
                        if (num > maxNum) maxNum = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return String.format("%03d", maxNum + 1);
    }
}


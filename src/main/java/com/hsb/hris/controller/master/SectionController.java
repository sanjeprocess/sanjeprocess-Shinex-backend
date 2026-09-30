package com.hsb.hris.controller.master;

import com.hsb.hris.entity.Section;
import com.hsb.hris.repository.SectionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/sections", "/api/master/sections"})
public class SectionController extends GenericMasterController<Section, String> {

    private final JdbcTemplate jdbcTemplate;

    public SectionController(SectionRepository repo, JdbcTemplate jdbcTemplate) {
        super(repo);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        String cleanId = id != null ? id.trim() : "";
        if (!cleanId.isEmpty()) {
            try {
                // Safely unassign this section code from all employees before deleting
                jdbcTemplate.update("UPDATE dbo.TBL_Emp_Master SET Emp_Section_Code = NULL WHERE RTRIM(LTRIM(Emp_Section_Code)) = ? OR Emp_Section_Code = ?", cleanId, cleanId);
            } catch (Exception ignored) {}

            try {
                // Delete the section from TBL_M_Section
                jdbcTemplate.update("DELETE FROM dbo.TBL_M_Section WHERE RTRIM(LTRIM(Section_Code)) = ? OR Section_Code = ?", cleanId, cleanId);
            } catch (Exception e) {
                repo.deleteById(cleanId);
            }
        }
        return ResponseEntity.noContent().build();
    }

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


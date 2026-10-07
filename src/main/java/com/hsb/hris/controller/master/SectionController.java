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
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM dbo.TBL_Emp_Master WHERE RTRIM(LTRIM(Emp_Section_Code)) = ? OR Emp_Section_Code = ?",
                    Integer.class, cleanId, cleanId);
                if (count != null && count > 0) {
                    throw new IllegalArgumentException("Cannot delete section: " + count + " employee(s) are assigned to this section.");
                }
            } catch (IllegalArgumentException e) {
                throw e;
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
    @PostMapping
    public Section create(@RequestBody Section entity) {
        if (entity.getSectionName() == null || entity.getSectionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Section name is required");
        }
        String cleanName = entity.getSectionName().trim().toLowerCase();
        String cleanBc = entity.getBusinessCenter() != null ? entity.getBusinessCenter().trim().toUpperCase() : "";

        List<Section> existing = repo.findAll();
        boolean isDuplicate = existing.stream().anyMatch(s -> {
            if (s.getSectionName() == null) return false;
            String sName = s.getSectionName().trim().toLowerCase();
            String sBc = s.getBusinessCenter() != null ? s.getBusinessCenter().trim().toUpperCase() : "";
            boolean sameBc = cleanBc.isEmpty() || cleanBc.equals("ALL") || sBc.isEmpty() || sBc.equals(cleanBc);
            return sameBc && sName.equals(cleanName);
        });

        if (isDuplicate) {
            throw new IllegalArgumentException("A section named '" + entity.getSectionName().trim() + "' already exists in this Business Center.");
        }

        return repo.save(entity);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Section> update(@PathVariable String id, @RequestBody Section entity) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();

        if (entity.getSectionName() == null || entity.getSectionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Section name is required");
        }
        String cleanName = entity.getSectionName().trim().toLowerCase();
        String cleanBc = entity.getBusinessCenter() != null ? entity.getBusinessCenter().trim().toUpperCase() : "";

        List<Section> existing = repo.findAll();
        boolean isDuplicate = existing.stream().anyMatch(s -> {
            if (s.getSectionCode() != null && s.getSectionCode().trim().equalsIgnoreCase(id.trim())) {
                return false; // Skip current record
            }
            if (s.getSectionName() == null) return false;
            String sName = s.getSectionName().trim().toLowerCase();
            String sBc = s.getBusinessCenter() != null ? s.getBusinessCenter().trim().toUpperCase() : "";
            boolean sameBc = cleanBc.isEmpty() || cleanBc.equals("ALL") || sBc.isEmpty() || sBc.equals(cleanBc);
            return sameBc && sName.equals(cleanName);
        });

        if (isDuplicate) {
            throw new IllegalArgumentException("A section named '" + entity.getSectionName().trim() + "' already exists in this Business Center.");
        }

        Section saved = repo.save(entity);
        if (entity.getBasicSalary() != null && entity.getBasicSalary() > 0 && id != null) {
            String cleanCode = id.trim();
            try {
                jdbcTemplate.update(
                    "UPDATE dbo.TBL_Emp_Master SET Emp_Basic_Salary = ? WHERE RTRIM(LTRIM(Emp_Section_Code)) = ? OR Emp_Section_Code = ?",
                    entity.getBasicSalary(), cleanCode, cleanCode
                );
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok(saved);
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


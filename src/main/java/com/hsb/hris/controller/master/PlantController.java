package com.hsb.hris.controller.master;

import com.hsb.hris.entity.Plant;
import com.hsb.hris.repository.PlantRepository;
import com.hsb.hris.security.BusinessCenterScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping({"/api/customers", "/api/master/plants"})
public class PlantController extends GenericMasterController<Plant, String> {

    private static final Logger log = LoggerFactory.getLogger(PlantController.class);

    // In-memory metadata store for extended properties (profitCenter, docPath)
    private static final Map<String, PlantMeta> metaStore = new ConcurrentHashMap<>();

    public static class PlantMeta {
        public String profitCenter;
        public String docPath;

        public PlantMeta(String profitCenter, String docPath) {
            this.profitCenter = profitCenter;
            this.docPath = docPath;
        }
    }

    public PlantController(PlantRepository repo) { super(repo); }

    @Override
    @GetMapping
    public List<Plant> list(@RequestParam(value = "businessCenter", required = false) String businessCenter) {
        List<Plant> plants = repo.findAll();
        
        // Enrich with stored metadata
        for (Plant p : plants) {
            if (p.getCustCode() != null) {
                PlantMeta meta = metaStore.get(p.getCustCode().trim());
                if (meta != null) {
                    if (meta.profitCenter != null) p.setProfitCenter(meta.profitCenter);
                    if (meta.docPath != null) p.setDocPath(meta.docPath);
                }
            }
        }

        if (businessCenter != null && !businessCenter.isBlank() && !"ALL".equalsIgnoreCase(businessCenter.trim())) {
            // A plant with no business center is shown only under "All", to the super admin
            return plants.stream().filter(p -> BusinessCenterScope.sameCenter(p.getBusinessCenter(), businessCenter)).toList();
        }
        return plants;
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Plant> get(@PathVariable String id) {
        String cleanId = id.trim();
        return repo.findById(cleanId).map(p -> {
            PlantMeta meta = metaStore.get(cleanId);
            if (meta != null) {
                p.setProfitCenter(meta.profitCenter);
                p.setDocPath(meta.docPath);
            }
            return ResponseEntity.ok(p);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    @PostMapping
    public Plant create(@RequestBody Plant plant) {
        Plant normalized = normalize(plant);
        String code = normalized.getCustCode();
        if (code != null) {
            repo.findById(code.trim()).ifPresent(BusinessCenterScope::assertVisible);
            metaStore.put(code.trim(), new PlantMeta(normalized.getProfitCenter(), normalized.getDocPath()));
        }
        try {
            Plant saved = repo.save(normalized);
            saved.setBusinessCenter(normalized.getBusinessCenter());
            saved.setProfitCenter(normalized.getProfitCenter());
            saved.setDocPath(normalized.getDocPath());
            return saved;
        } catch (Exception ex) {
            log.warn("Plant save error in DB, returning memory record: {}", ex.getMessage());
            return normalized;
        }
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Plant> update(@PathVariable String id, @RequestBody Plant plant) {
        String cleanId = id.trim();
        repo.findById(cleanId).ifPresent(BusinessCenterScope::assertVisible);
        Plant normalized = normalize(plant);
        normalized.setCustCode(cleanId);

        metaStore.put(cleanId, new PlantMeta(normalized.getProfitCenter(), normalized.getDocPath()));

        try {
            Plant saved = repo.save(normalized);
            saved.setBusinessCenter(normalized.getBusinessCenter());
            saved.setProfitCenter(normalized.getProfitCenter());
            saved.setDocPath(normalized.getDocPath());
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            log.warn("Plant update error in DB for {}, returning memory record: {}", cleanId, ex.getMessage());
            return ResponseEntity.ok(normalized);
        }
    }

    private Plant normalize(Plant plant) {
        plant.setCustCode(emptyToNull(plant.getCustCode()));
        plant.setCustName(emptyToNull(plant.getCustName()));
        plant.setAddress1(emptyToNull(plant.getAddress1()));
        plant.setAddress2(emptyToNull(plant.getAddress2()));
        plant.setAddress3(emptyToNull(plant.getAddress3()));
        plant.setEmail(emptyToNull(plant.getEmail()));
        plant.setContactNo(emptyToNull(plant.getContactNo()));
        plant.setContactName(emptyToNull(plant.getContactName()));
        plant.setOtCalculationAuto(normalizeFlag(plant.getOtCalculationAuto()));
        plant.setAttendanceAllowance(emptyToNull(plant.getAttendanceAllowance()));
        plant.setShiftStart(shortTime(plant.getShiftStart()));
        plant.setShiftEnd(shortTime(plant.getShiftEnd()));
        plant.setBusinessCenter(emptyToNull(BusinessCenterScope.codeOf(plant.getBusinessCenter())));
        plant.setProfitCenter(emptyToNull(plant.getProfitCenter()));
        plant.setDocPath(emptyToNull(plant.getDocPath()));
        return plant;
    }

    // "07:30:00" -> "07:30" (column holds HH:mm)
    private String shortTime(String value) {
        String time = emptyToNull(value);
        return time != null && time.length() > 5 ? time.substring(0, 5) : time;
    }

    private String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String normalizeFlag(String value) {
        String normalized = emptyToNull(value);
        if (normalized == null) return null;
        if ("true".equalsIgnoreCase(normalized) || "y".equalsIgnoreCase(normalized)) return "Y";
        if ("false".equalsIgnoreCase(normalized) || "n".equalsIgnoreCase(normalized)) return "N";
        return normalized;
    }
}

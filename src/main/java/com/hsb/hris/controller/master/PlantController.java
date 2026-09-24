package com.hsb.hris.controller.master;

import com.hsb.hris.entity.Plant;
import com.hsb.hris.repository.PlantRepository;
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

    // In-memory persistent metadata store for extended properties (businessCenter, profitCenter, docPath)
    private static final Map<String, PlantMeta> metaStore = new ConcurrentHashMap<>();

    public static class PlantMeta {
        public String businessCenter;
        public String profitCenter;
        public String docPath;

        public PlantMeta(String businessCenter, String profitCenter, String docPath) {
            this.businessCenter = businessCenter;
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
                    if (meta.businessCenter != null) p.setBusinessCenter(meta.businessCenter);
                    if (meta.profitCenter != null) p.setProfitCenter(meta.profitCenter);
                    if (meta.docPath != null) p.setDocPath(meta.docPath);
                }
            }
        }

        if (businessCenter != null && !businessCenter.isBlank() && !"ALL".equalsIgnoreCase(businessCenter.trim())) {
            String cleanBc = businessCenter.trim().toUpperCase();
            return plants.stream().filter(p -> {
                String pBc = p.getBusinessCenter();
                if (pBc == null || pBc.isBlank()) return true; // Unassigned plants visible to all
                String up = pBc.trim().toUpperCase();
                return up.equals(cleanBc) || up.startsWith(cleanBc) || cleanBc.startsWith(up);
            }).toList();
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
                p.setBusinessCenter(meta.businessCenter);
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
            metaStore.put(code.trim(), new PlantMeta(normalized.getBusinessCenter(), normalized.getProfitCenter(), normalized.getDocPath()));
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
        Plant normalized = normalize(plant);
        normalized.setCustCode(cleanId);

        metaStore.put(cleanId, new PlantMeta(normalized.getBusinessCenter(), normalized.getProfitCenter(), normalized.getDocPath()));

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
        plant.setBusinessCenter(emptyToNull(plant.getBusinessCenter()));
        plant.setProfitCenter(emptyToNull(plant.getProfitCenter()));
        plant.setDocPath(emptyToNull(plant.getDocPath()));
        return plant;
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

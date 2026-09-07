package com.hsb.hris.controller.master;

import com.hsb.hris.entity.Plant;
import com.hsb.hris.repository.PlantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/customers", "/api/master/plants"})
public class PlantController extends GenericMasterController<Plant, String> {
    public PlantController(PlantRepository repo) { super(repo); }

    @Override
    @PostMapping
    public Plant create(@RequestBody Plant plant) {
        return repo.save(normalize(plant));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<Plant> update(@PathVariable String id, @RequestBody Plant plant) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        plant.setCustCode(id);
        return ResponseEntity.ok(repo.save(normalize(plant)));
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

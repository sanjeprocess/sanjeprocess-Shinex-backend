package com.hsb.hris.controller.master;

import com.hsb.hris.entity.Employee;
import com.hsb.hris.service.master.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping({"/api/employees", "/api/master/employees"})
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) { this.service = service; }

    @GetMapping
    public List<Employee> list(@RequestParam(value = "businessCenter", required = false) String businessCenter) {
        if (businessCenter != null) return service.findByBusinessCenter(businessCenter);
        return service.findAll();
    }

    @GetMapping("/search")
    public List<Employee> search(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "section", required = false) String section,
            @RequestParam(value = "plant", required = false) String plant) {
        List<Employee> employees = service.findAll();
        String q = name == null ? "" : name.trim().toLowerCase();
        String sectionFilter = section == null ? "" : section.trim().toLowerCase();
        String plantFilter = plant == null ? "" : plant.trim().toLowerCase();

        return employees.stream().filter(e -> {
            if (q != null && !q.isEmpty()) {
                String fullName = ((e.getFirstName() == null ? "" : e.getFirstName()) + " " + (e.getLastName() == null ? "" : e.getLastName())).trim().toLowerCase();
                if (!fullName.contains(q) && !(e.getEpfNo() != null && e.getEpfNo().toLowerCase().contains(q))) {
                    return false;
                }
            }
            if (!sectionFilter.isEmpty() && (e.getSectionCode() == null || !e.getSectionCode().toLowerCase().contains(sectionFilter))) {
                return false;
            }
            if (!plantFilter.isEmpty() && (e.getPlantCode() == null || !e.getPlantCode().toLowerCase().contains(plantFilter))) {
                return false;
            }
            return true;
        }).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> get(@PathVariable String id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/next-epf")
    public ResponseEntity<java.util.Map<String, String>> getNextEpf(@RequestParam(value = "businessCenter", required = false) String businessCenter) {
        String nextEpf = service.getNextEpfNo(businessCenter);
        return ResponseEntity.ok(java.util.Map.of("nextEpf", nextEpf));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Employee e) {
        try {
            if (e.getEpfNo() == null || e.getEpfNo().trim().isEmpty()) {
                e.setEpfNo(service.getNextEpfNo(e.getBusinessCenter()));
            }
            normalize(e);
            if (e.getDateOfBirth() == null || e.getHiredDate() == null || e.getHiredMonth() == null || e.getHiredMonth().isBlank()) {
                return ResponseEntity.badRequest().body("Date of birth, hired date, and hired month are required");
            }
            return ResponseEntity.ok(service.save(e));
        } catch (DataIntegrityViolationException ex) {
            System.err.println("Employee database constraint failed");
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rootMessage(ex));
        } catch (Exception ex) {
            System.err.println("Failed to create employee");
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rootMessage(ex));
        }
    }

    private void normalize(Employee e) {
        e.setEpfNo(trim(e.getEpfNo(), 10));
        e.setPlantCode(code(e.getPlantCode()));
        e.setBusinessCenter(code(e.getBusinessCenter()));
        if ((e.getHiredMonth() == null || e.getHiredMonth().isBlank()) && e.getHiredDate() != null) {
            String monthName = e.getHiredDate().getMonth().name();
            e.setHiredMonth(monthName.substring(0, 1).toUpperCase() + monthName.substring(1).toLowerCase());
        }
        e.setHiredMonth(trim(e.getHiredMonth(), 15));
    }

    private String code(String value) {
        return value == null ? null : value.split(" / ", 2)[0].trim();
    }

    private String trim(String value, int max) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.substring(0, Math.min(max, normalized.length()));
    }

    private String rootMessage(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage() == null ? error.getClass().getSimpleName() : cause.getMessage();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable String id, @RequestBody Employee e) {
        return service.findById(id).map(existing -> {
            e.setEpfNo(id);
            return ResponseEntity.ok(service.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id.trim());
        return ResponseEntity.noContent().build();
    }
}

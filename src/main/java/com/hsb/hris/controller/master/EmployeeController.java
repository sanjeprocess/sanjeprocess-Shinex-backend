package com.hsb.hris.controller.master;

import com.hsb.hris.entity.Employee;
import com.hsb.hris.service.master.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    public Employee create(@RequestBody Employee e) { return service.save(e); }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable String id, @RequestBody Employee e) {
        return service.findById(id).map(existing -> {
            e.setEpfNo(id);
            return ResponseEntity.ok(service.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

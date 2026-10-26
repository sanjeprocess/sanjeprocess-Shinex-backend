package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.DeductionTransaction;
import com.hsb.hris.repository.DeductionTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/deduction-transactions")
public class DeductionTransactionController {

    private final DeductionTransactionRepository repository;

    public DeductionTransactionController(DeductionTransactionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<DeductionTransaction> list(
            @RequestParam(required = false) String businessCenter,
            @RequestParam(required = false) String epfNo) {
        return repository.findAll().stream()
                .filter(row -> matches(row.getBusinessCenter(), businessCenter))
                .filter(row -> epfNo == null || epfNo.isBlank() || epfNo.equalsIgnoreCase(row.getEpfNo()))
                .toList();
    }

    @GetMapping("/monthly-deduction")
    public List<Map<String, Object>> monthlyDeduction(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String businessCenter) {
        if (month < 1 || month > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Month must be between 1 and 12.");
        }
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
        for (DeductionTransaction transaction : repository.findAll()) {
            if (!matches(transaction.getBusinessCenter(), businessCenter)
                    || !"ACTIVE".equalsIgnoreCase(transaction.getStatus())
                    || transaction.getStartDate().isAfter(monthEnd)
                    || transaction.getEndDate().isBefore(monthStart)) {
                continue;
            }
            Map<String, Object> row = grouped.computeIfAbsent(transaction.getEpfNo(), key -> {
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("epfNo", key);
                value.put("employeeName", transaction.getEmployeeName());
                value.put("businessCenter", transaction.getBusinessCenter());
                value.put("monthlyDeduction", BigDecimal.ZERO);
                return value;
            });
            BigDecimal amount = (BigDecimal) row.get("monthlyDeduction");
            row.put("monthlyDeduction", amount.add(transaction.getMonthlyCut()));
        }
        return new ArrayList<>(grouped.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeductionTransaction> get(@PathVariable String id) {
        return repository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DeductionTransaction> create(@RequestBody DeductionTransaction transaction) {
        validate(transaction);
        if (transaction.getId() == null || transaction.getId().isBlank()) {
            transaction.setId(UUID.randomUUID().toString());
        }
        if (transaction.getStatus() == null || transaction.getStatus().isBlank()) {
            transaction.setStatus("Active");
        }
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(java.time.LocalDateTime.now());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(transaction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeductionTransaction> update(
            @PathVariable String id,
            @RequestBody DeductionTransaction update) {
        return repository.findById(id).map(existing -> {
            update.setId(id);
            validate(update);
            update.setCreatedAt(existing.getCreatedAt());
            return ResponseEntity.ok(repository.save(update));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void validate(DeductionTransaction row) {
        if (row.getEpfNo() == null || row.getEpfNo().isBlank()
                || row.getDidCode() == null || row.getDidCode().isBlank()
                || row.getBusinessCenter() == null || row.getBusinessCenter().isBlank()
                || row.getStartDate() == null || row.getEndDate() == null
                || row.getTotalAmount() == null || row.getMonthlyCut() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required deduction transaction fields are missing.");
        }
        if (row.getEndDate().isBefore(row.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must not be before start date.");
        }
    }

    private boolean matches(String value, String filter) {
        return filter == null || filter.isBlank() || "ALL".equalsIgnoreCase(filter.trim())
                || (value != null && value.trim().equalsIgnoreCase(filter.trim()));
    }
}

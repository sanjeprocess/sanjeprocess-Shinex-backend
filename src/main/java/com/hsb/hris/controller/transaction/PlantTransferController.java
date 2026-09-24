package com.hsb.hris.controller.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping({"/api/plant-transfers", "/api/transaction/plant-transfers"})
public class PlantTransferController {

    public static class PlantTransferDto {
        private String id;
        private String epfNo;
        private String employeeName;
        private String fromPlant;
        private String toPlant;
        private String transferDate;
        private String endDate;
        private Integer daysWorked;
        private String profitCenter;
        private String transferType;
        private String status;
        private String workDetails;
        private String businessCenter;
        private String createdAt;

        public PlantTransferDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEpfNo() { return epfNo; }
        public void setEpfNo(String epfNo) { this.epfNo = epfNo; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getFromPlant() { return fromPlant; }
        public void setFromPlant(String fromPlant) { this.fromPlant = fromPlant; }

        public String getToPlant() { return toPlant; }
        public void setToPlant(String toPlant) { this.toPlant = toPlant; }

        public String getTransferDate() { return transferDate; }
        public void setTransferDate(String transferDate) { this.transferDate = transferDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public Integer getDaysWorked() { return daysWorked; }
        public void setDaysWorked(Integer daysWorked) { this.daysWorked = daysWorked; }

        public String getProfitCenter() { return profitCenter; }
        public void setProfitCenter(String profitCenter) { this.profitCenter = profitCenter; }

        public String getTransferType() { return transferType; }
        public void setTransferType(String transferType) { this.transferType = transferType; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getWorkDetails() { return workDetails; }
        public void setWorkDetails(String workDetails) { this.workDetails = workDetails; }

        public String getBusinessCenter() { return businessCenter; }
        public void setBusinessCenter(String businessCenter) { this.businessCenter = businessCenter; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    private static final Map<String, PlantTransferDto> transferStore = new ConcurrentHashMap<>();

    @GetMapping
    public List<PlantTransferDto> list(@RequestParam(value = "businessCenter", required = false) String businessCenter) {
        List<PlantTransferDto> list = new ArrayList<>(transferStore.values());
        if (businessCenter != null && !businessCenter.isBlank() && !"ALL".equalsIgnoreCase(businessCenter.trim())) {
            String cleanBc = businessCenter.trim().toUpperCase();
            return list.stream().filter(t -> {
                String tBc = t.getBusinessCenter();
                if (tBc == null || tBc.isBlank()) return false;
                String up = tBc.trim().toUpperCase();
                return up.equals(cleanBc) || up.startsWith(cleanBc) || cleanBc.startsWith(up);
            }).toList();
        }
        return list;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantTransferDto> get(@PathVariable String id) {
        PlantTransferDto dto = transferStore.get(id.trim());
        if (dto != null) return ResponseEntity.ok(dto);
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<PlantTransferDto> create(@RequestBody PlantTransferDto dto) {
        if (dto.getId() == null || dto.getId().isBlank()) {
            dto.setId("tr-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (dto.getCreatedAt() == null) {
            dto.setCreatedAt(new java.util.Date().toString());
        }
        transferStore.put(dto.getId().trim(), dto);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlantTransferDto> update(@PathVariable String id, @RequestBody PlantTransferDto dto) {
        String cleanId = id.trim();
        dto.setId(cleanId);
        transferStore.put(cleanId, dto);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        transferStore.remove(id.trim());
        return ResponseEntity.noContent().build();
    }
}

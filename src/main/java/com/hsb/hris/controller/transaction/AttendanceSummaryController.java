package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.AttendanceSummary;
import com.hsb.hris.entity.id.AttSummaryId;
import com.hsb.hris.repository.AttendanceSummaryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports/attendance-summary")
public class AttendanceSummaryController {

    private final AttendanceSummaryRepository repo;

    public AttendanceSummaryController(AttendanceSummaryRepository repo) { this.repo = repo; }

    @GetMapping
    public ResponseEntity<List<AttendanceSummary>> get(@RequestParam String businessCenter,
                                                        @RequestParam String year,
                                                        @RequestParam String month) {
        List<AttendanceSummary> rows = repo.findByBusinessCenterAndAttYearAndAttMonth(businessCenter, year, month);
        return ResponseEntity.ok(rows);
    }
}

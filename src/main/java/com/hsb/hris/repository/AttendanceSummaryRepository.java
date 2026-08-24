package com.hsb.hris.repository;

import com.hsb.hris.entity.AttendanceSummary;
import com.hsb.hris.entity.id.AttSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, AttSummaryId> {
    List<AttendanceSummary> findByBusinessCenterAndAttYearAndAttMonth(String businessCenter, String year, String month);
}

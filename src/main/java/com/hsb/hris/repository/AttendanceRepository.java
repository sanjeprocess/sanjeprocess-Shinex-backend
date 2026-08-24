package com.hsb.hris.repository;

import com.hsb.hris.entity.Attendance;
import com.hsb.hris.entity.id.AttendanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, AttendanceId> {
    List<Attendance> findByBusinessCenterAndAttYearAndAttMonth(String businessCenter, String year, String month);
    List<Attendance> findByEpfNoAndAttYearAndAttMonth(String epfNo, String year, String month);
    List<Attendance> findByDayInBetween(LocalDate start, LocalDate end);
}

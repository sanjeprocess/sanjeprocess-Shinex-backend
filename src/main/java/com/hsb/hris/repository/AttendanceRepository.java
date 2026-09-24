package com.hsb.hris.repository;

import com.hsb.hris.entity.Attendance;
import com.hsb.hris.entity.id.AttendanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, AttendanceId> {
    List<Attendance> findByBusinessCenterAndAttYearAndAttMonth(String businessCenter, String year, String month);
    List<Attendance> findByEpfNoAndAttYearAndAttMonth(String epfNo, String year, String month);

    @Query("SELECT DISTINCT a FROM Attendance a WHERE " +
           "UPPER(TRIM(a.businessCenter)) = UPPER(TRIM(:bc)) OR " +
           "UPPER(TRIM(a.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), ' %')) OR " +
           "UPPER(TRIM(a.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), '/%')) OR " +
           "EXISTS (SELECT 1 FROM Employee e WHERE TRIM(e.epfNo) = TRIM(a.epfNo) AND " +
           "        (UPPER(TRIM(e.businessCenter)) = UPPER(TRIM(:bc)) OR UPPER(TRIM(e.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), '%')))) OR " +
           "EXISTS (SELECT 1 FROM BusinessCenter c WHERE " +
           "        (UPPER(TRIM(c.companyId)) = UPPER(TRIM(:bc)) OR UPPER(TRIM(c.companyName)) = UPPER(TRIM(:bc))) AND " +
           "        (UPPER(TRIM(a.businessCenter)) = UPPER(TRIM(c.companyId)) OR UPPER(TRIM(a.businessCenter)) = UPPER(TRIM(c.companyName))))")
    List<Attendance> findByBusinessCenterSmart(@Param("bc") String bc);
}

package com.hsb.hris.repository;

import com.hsb.hris.entity.TLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TLeaveRepository extends JpaRepository<TLeave, Integer> {
    @Query("SELECT DISTINCT l FROM TLeave l WHERE " +
           "UPPER(TRIM(l.businessCenter)) = UPPER(TRIM(:bc)) OR " +
           "UPPER(TRIM(l.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), ' %')) OR " +
           "UPPER(TRIM(l.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), '/%')) OR " +
           "EXISTS (SELECT 1 FROM Employee e WHERE TRIM(e.epfNo) = TRIM(l.empNo) AND " +
           "        (UPPER(TRIM(e.businessCenter)) = UPPER(TRIM(:bc)) OR UPPER(TRIM(e.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), '%')))) OR " +
           "EXISTS (SELECT 1 FROM BusinessCenter c WHERE " +
           "        (UPPER(TRIM(c.companyId)) = UPPER(TRIM(:bc)) OR UPPER(TRIM(c.companyName)) = UPPER(TRIM(:bc))) AND " +
           "        (UPPER(TRIM(l.businessCenter)) = UPPER(TRIM(c.companyId)) OR UPPER(TRIM(l.businessCenter)) = UPPER(TRIM(c.companyName))))")
    List<TLeave> findByBusinessCenterSmart(@Param("bc") String bc);
}

package com.hsb.hris.repository;

import com.hsb.hris.entity.TDeduction;
import com.hsb.hris.entity.id.TDeductionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TDeductionRepository extends JpaRepository<TDeduction, TDeductionId> {
    @Query("SELECT DISTINCT d FROM TDeduction d WHERE " +
           "UPPER(TRIM(d.businessCenter)) = UPPER(TRIM(:bc)) OR " +
           "UPPER(TRIM(d.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), ' %')) OR " +
           "UPPER(TRIM(d.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), '/%')) OR " +
           "EXISTS (SELECT 1 FROM Employee e WHERE TRIM(e.epfNo) = TRIM(d.epfNo) AND " +
           "        (UPPER(TRIM(e.businessCenter)) = UPPER(TRIM(:bc)) OR UPPER(TRIM(e.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), '%')))) OR " +
           "EXISTS (SELECT 1 FROM BusinessCenter c WHERE " +
           "        (UPPER(TRIM(c.companyId)) = UPPER(TRIM(:bc)) OR UPPER(TRIM(c.companyName)) = UPPER(TRIM(:bc))) AND " +
           "        (UPPER(TRIM(d.businessCenter)) = UPPER(TRIM(c.companyId)) OR UPPER(TRIM(d.businessCenter)) = UPPER(TRIM(c.companyName))))")
    List<TDeduction> findByBusinessCenterSmart(@Param("bc") String bc);
}

package com.hsb.hris.repository;

import com.hsb.hris.entity.BCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BCardRepository extends JpaRepository<BCard, String> {
    @Query("SELECT DISTINCT b FROM BCard b WHERE " +
           "UPPER(TRIM(b.businessCenter)) = UPPER(TRIM(:bc)) OR " +
           "UPPER(TRIM(b.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), ' %')) OR " +
           "UPPER(TRIM(b.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), '/%')) OR " +
           "EXISTS (SELECT 1 FROM Employee e WHERE TRIM(e.epfNo) = TRIM(b.epfNo) AND " +
           "        (UPPER(TRIM(e.businessCenter)) = UPPER(TRIM(:bc)) OR UPPER(TRIM(e.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), '%')))) OR " +
           "EXISTS (SELECT 1 FROM BusinessCenter c WHERE " +
           "        (UPPER(TRIM(c.companyId)) = UPPER(TRIM(:bc)) OR UPPER(TRIM(c.companyName)) = UPPER(TRIM(:bc))) AND " +
           "        (UPPER(TRIM(b.businessCenter)) = UPPER(TRIM(c.companyId)) OR UPPER(TRIM(b.businessCenter)) = UPPER(TRIM(c.companyName))))")
    List<BCard> findByBusinessCenterSmart(@Param("bc") String bc);
}

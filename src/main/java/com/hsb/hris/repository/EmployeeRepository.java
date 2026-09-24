package com.hsb.hris.repository;

import com.hsb.hris.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    List<Employee> findByBusinessCenter(String businessCenter);

    @Query("SELECT DISTINCT e FROM Employee e WHERE " +
           "UPPER(TRIM(e.businessCenter)) = UPPER(TRIM(:bc)) OR " +
           "UPPER(TRIM(e.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), ' %')) OR " +
           "UPPER(TRIM(e.businessCenter)) LIKE UPPER(CONCAT(TRIM(:bc), '/%')) OR " +
           "EXISTS (SELECT 1 FROM BusinessCenter c WHERE " +
           "        (UPPER(TRIM(c.companyId)) = UPPER(TRIM(:bc)) OR UPPER(TRIM(c.companyName)) = UPPER(TRIM(:bc)) OR " +
           "         UPPER(TRIM(:bc)) LIKE UPPER(CONCAT(TRIM(c.companyId), '%')) OR UPPER(TRIM(:bc)) LIKE UPPER(CONCAT(TRIM(c.companyName), '%'))) AND " +
           "        (UPPER(TRIM(e.businessCenter)) = UPPER(TRIM(c.companyId)) OR UPPER(TRIM(e.businessCenter)) = UPPER(TRIM(c.companyName)) OR " +
           "         UPPER(TRIM(e.businessCenter)) LIKE UPPER(CONCAT(TRIM(c.companyId), '%')) OR UPPER(TRIM(e.businessCenter)) LIKE UPPER(CONCAT(TRIM(c.companyName), '%')))" +
           ")")
    List<Employee> findByBusinessCenterSmart(@Param("bc") String bc);

    @Query(value = "select e from Employee e where trim(e.epfNo) = :epfNo")
    Optional<Employee> findByTrimmedEpfNo(@Param("epfNo") String epfNo);

    @Query("SELECT e.epfNo FROM Employee e")
    List<String> findAllEpfNos();
}

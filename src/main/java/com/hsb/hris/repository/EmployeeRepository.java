package com.hsb.hris.repository;

import com.hsb.hris.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    List<Employee> findByBusinessCenter(String businessCenter);

    @Query(value = "select e from Employee e where trim(e.epfNo) = :epfNo")
    java.util.Optional<Employee> findByTrimmedEpfNo(@Param("epfNo") String epfNo);
}

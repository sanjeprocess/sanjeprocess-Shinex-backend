package com.hsb.hris.repository;

import com.hsb.hris.entity.SalaryCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryCalculationRepository extends JpaRepository<SalaryCalculation, Long> {
    List<SalaryCalculation> findByYearAndMonth(String year, String month);
    List<SalaryCalculation> findByYearAndMonthAndPlant(String year, String month, String plant);
    List<SalaryCalculation> findByPlant(String plant);
    Optional<SalaryCalculation> findByEpfNoAndPlantAndYearAndMonth(String epfNo, String plant, String year, String month);
}

package com.hsb.hris.repository;

import com.hsb.hris.entity.StatutoryHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StatutoryHolidayRepository extends JpaRepository<StatutoryHoliday, LocalDate> {
    List<StatutoryHoliday> findByHolidayDateBetweenOrderByHolidayDateAsc(LocalDate from, LocalDate to);
}

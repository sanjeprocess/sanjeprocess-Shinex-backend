package com.hsb.hris.repository;

import com.hsb.hris.entity.StatutoryHolidayStatus;
import com.hsb.hris.entity.id.StatutoryHolidayStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StatutoryHolidayStatusRepository extends JpaRepository<StatutoryHolidayStatus, StatutoryHolidayStatusId> {
    List<StatutoryHolidayStatus> findByHolidayDateBetween(LocalDate from, LocalDate to);

    @Transactional
    @Modifying
    @Query("DELETE FROM StatutoryHolidayStatus s WHERE s.holidayDate = :date")
    void deleteByHolidayDate(@Param("date") LocalDate date);
}

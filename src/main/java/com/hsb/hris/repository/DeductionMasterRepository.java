package com.hsb.hris.repository;

import com.hsb.hris.entity.DeductionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeductionMasterRepository extends JpaRepository<DeductionMaster, String> {
}

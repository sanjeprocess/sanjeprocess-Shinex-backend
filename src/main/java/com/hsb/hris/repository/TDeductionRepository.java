package com.hsb.hris.repository;

import com.hsb.hris.entity.TDeduction;
import com.hsb.hris.entity.id.TDeductionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TDeductionRepository extends JpaRepository<TDeduction, TDeductionId> {
}

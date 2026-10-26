package com.hsb.hris.repository;

import com.hsb.hris.entity.DeductionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeductionTransactionRepository extends JpaRepository<DeductionTransaction, String> {
}

package com.hsb.hris.repository;

import com.hsb.hris.entity.TLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TLoanRepository extends JpaRepository<TLoan, String> {
}

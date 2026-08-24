package com.hsb.hris.repository;

import com.hsb.hris.entity.RateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateIdRepository extends JpaRepository<RateId, String> {
}

package com.hsb.hris.repository;

import com.hsb.hris.entity.OtType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtTypeRepository extends JpaRepository<OtType, String> {
}

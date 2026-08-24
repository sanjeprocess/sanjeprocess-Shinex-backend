package com.hsb.hris.repository;

import com.hsb.hris.entity.BusinessCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessCenterRepository extends JpaRepository<BusinessCenter, String> {
}

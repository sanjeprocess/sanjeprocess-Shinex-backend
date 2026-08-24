package com.hsb.hris.repository;

import com.hsb.hris.entity.AdditionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionMasterRepository extends JpaRepository<AdditionMaster, String> {
}

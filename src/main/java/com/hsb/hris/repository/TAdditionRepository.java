package com.hsb.hris.repository;

import com.hsb.hris.entity.TAddition;
import com.hsb.hris.entity.id.TAdditionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TAdditionRepository extends JpaRepository<TAddition, TAdditionId> {
}

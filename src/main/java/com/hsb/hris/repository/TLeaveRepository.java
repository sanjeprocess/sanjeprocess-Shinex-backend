package com.hsb.hris.repository;

import com.hsb.hris.entity.TLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TLeaveRepository extends JpaRepository<TLeave, Integer> {
}

package com.hsb.hris.repository;

import com.hsb.hris.entity.BCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BCardRepository extends JpaRepository<BCard, String> {
}

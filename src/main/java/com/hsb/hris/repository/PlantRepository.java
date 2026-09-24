package com.hsb.hris.repository;

import com.hsb.hris.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<Plant, String> {
}

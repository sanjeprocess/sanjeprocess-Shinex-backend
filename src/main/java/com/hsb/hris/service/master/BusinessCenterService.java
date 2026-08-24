package com.hsb.hris.service.master;

import com.hsb.hris.entity.BusinessCenter;

import java.util.List;
import java.util.Optional;

public interface BusinessCenterService {
    List<BusinessCenter> findAll();
    Optional<BusinessCenter> findById(String id);
    BusinessCenter save(BusinessCenter bc);
    void deleteById(String id);
}

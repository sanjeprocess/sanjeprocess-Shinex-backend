package com.hsb.hris.service.master.impl;

import com.hsb.hris.entity.BusinessCenter;
import com.hsb.hris.repository.BusinessCenterRepository;
import com.hsb.hris.service.master.BusinessCenterService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BusinessCenterServiceImpl implements BusinessCenterService {

    private final BusinessCenterRepository repo;

    public BusinessCenterServiceImpl(BusinessCenterRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<BusinessCenter> findAll() { return repo.findAll(); }

    @Override
    public Optional<BusinessCenter> findById(String id) { return repo.findById(id); }

    @Override
    public BusinessCenter save(BusinessCenter bc) { return repo.save(bc); }

    @Override
    public void deleteById(String id) { repo.deleteById(id); }
}

package com.hsb.hris.service.master.impl;

import com.hsb.hris.service.master.GenericMasterService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public class GenericMasterServiceImpl<T, ID> implements GenericMasterService<T, ID> {

    protected final JpaRepository<T, ID> repo;

    public GenericMasterServiceImpl(JpaRepository<T, ID> repo) { this.repo = repo; }

    @Override
    public List<T> findAll() { return repo.findAll(); }

    @Override
    public Optional<T> findById(ID id) { return repo.findById(id); }

    @Override
    public T save(T entity) { return repo.save(entity); }

    @Override
    public void deleteById(ID id) { repo.deleteById(id); }
}

package com.hsb.hris.service.master;

import java.util.List;
import java.util.Optional;

public interface GenericMasterService<T, ID> {
    List<T> findAll();
    Optional<T> findById(ID id);
    T save(T entity);
    void deleteById(ID id);
}

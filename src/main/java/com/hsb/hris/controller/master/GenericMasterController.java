package com.hsb.hris.controller.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * A lightweight generic controller that can be used for simple master entities
 * by wiring a repository bean into the constructor in a @Configuration class or
 * by subclassing and providing the proper path.
 */
public abstract class GenericMasterController<T, ID> {

    protected final JpaRepository<T, ID> repo;

    protected GenericMasterController(JpaRepository<T, ID> repo) { this.repo = repo; }

    @GetMapping
    public List<T> list() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<T> get(@PathVariable ID id) {
        Optional<T> o = repo.findById(id);
        return o.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public T create(@RequestBody T entity) { return repo.save(entity); }

    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable ID id, @RequestBody T entity) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(repo.save(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

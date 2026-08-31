package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.TAddition;
import com.hsb.hris.entity.id.TAdditionId;
import com.hsb.hris.repository.TAdditionRepository;
import com.hsb.hris.controller.master.GenericMasterController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/transaction-additions", "/api/transaction/additions"})
public class TAdditionController extends GenericMasterController<TAddition, TAdditionId> {
    public TAdditionController(TAdditionRepository repo) { super(repo); }

    @GetMapping("/{epfNo}/{addCode}")
    public ResponseEntity<TAddition> get(@PathVariable String epfNo, @PathVariable String addCode) {
        TAdditionId id = new TAdditionId(epfNo, addCode);
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{epfNo}/{addCode}")
    public ResponseEntity<TAddition> update(@PathVariable String epfNo,
                                           @PathVariable String addCode,
                                           @RequestBody TAddition entity) {
        TAdditionId id = new TAdditionId(epfNo, addCode);
        return repo.findById(id).map(existing -> {
            existing.setBusinessCenter(entity.getBusinessCenter());
            existing.setAddAmount(entity.getAddAmount());
            existing.setEveryMonth(entity.getEveryMonth());
            existing.setAddMonth(entity.getAddMonth());
            existing.setAddYear(entity.getAddYear());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{epfNo}/{addCode}")
    public ResponseEntity<Void> delete(@PathVariable String epfNo, @PathVariable String addCode) {
        TAdditionId id = new TAdditionId(epfNo, addCode);
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

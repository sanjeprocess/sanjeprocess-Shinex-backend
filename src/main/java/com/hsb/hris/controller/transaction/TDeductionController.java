package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.TDeduction;
import com.hsb.hris.entity.id.TDeductionId;
import com.hsb.hris.repository.TDeductionRepository;
import com.hsb.hris.controller.master.GenericMasterController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/transaction-deductions", "/api/transaction/deductions"})
public class TDeductionController extends GenericMasterController<TDeduction, TDeductionId> {
    public TDeductionController(TDeductionRepository repo) { super(repo); }

    @GetMapping("/{epfNo}/{didCode}")
    public ResponseEntity<TDeduction> get(@PathVariable String epfNo, @PathVariable String didCode) {
        TDeductionId id = new TDeductionId(epfNo, didCode);
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{epfNo}/{didCode}")
    public ResponseEntity<TDeduction> update(@PathVariable String epfNo,
                                           @PathVariable String didCode,
                                           @RequestBody TDeduction entity) {
        TDeductionId id = new TDeductionId(epfNo, didCode);
        return repo.findById(id).map(existing -> {
            existing.setBusinessCenter(entity.getBusinessCenter());
            existing.setDidAmount(entity.getDidAmount());
            existing.setEveryMonth(entity.getEveryMonth());
            existing.setAddMonth(entity.getAddMonth());
            existing.setAddYear(entity.getAddYear());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{epfNo}/{didCode}")
    public ResponseEntity<Void> delete(@PathVariable String epfNo, @PathVariable String didCode) {
        TDeductionId id = new TDeductionId(epfNo, didCode);
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

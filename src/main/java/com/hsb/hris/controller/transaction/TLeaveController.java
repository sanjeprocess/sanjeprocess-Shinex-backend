package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.TLeave;
import com.hsb.hris.repository.TLeaveRepository;
import com.hsb.hris.controller.master.GenericMasterController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping({"/api/leaves", "/api/transaction/leaves"})
public class TLeaveController extends GenericMasterController<TLeave, Integer> {
    private static final Logger log = LoggerFactory.getLogger(TLeaveController.class);
    public TLeaveController(TLeaveRepository repo) { super(repo); }

    @Override
    @PostMapping
    public TLeave create(@RequestBody TLeave leave) {
        try {
            enforceDateAccess(leave.getLeaveStartDate());
            validateDateRange(leave);
            return repo.save(leave);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Failed to create leave transaction: {}", describe(leave), ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave transaction could not be saved.", ex);
        }
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<TLeave> update(@PathVariable Integer id, @RequestBody TLeave leave) {
        try {
            enforceDateAccess(leave.getLeaveStartDate());
            validateDateRange(leave);
            if (!repo.existsById(id)) return ResponseEntity.notFound().build();
            leave.setId(id);
            return ResponseEntity.ok(repo.save(leave));
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Failed to update leave transaction {}: {}", id, describe(leave), ex);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leave transaction could not be saved.", ex);
        }
    }

    private void validateDateRange(TLeave leave) {
        if (leave.getLeaveStartDate() != null && leave.getLeaveEndDate() != null
                && leave.getLeaveEndDate().isBefore(leave.getLeaveStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "End date cannot be earlier than start date.");
        }
    }

    private String describe(TLeave leave) {
        return "employee=" + leave.getEmpNo() + ", type=" + leave.getLeaveType()
                + ", start=" + leave.getLeaveStartDate() + ", end=" + leave.getLeaveEndDate();
    }

    private void enforceDateAccess(LocalDate startDate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean superAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPERADMIN".equalsIgnoreCase(authority.getAuthority())
                        || "ROLE_SUPER_ADMIN".equalsIgnoreCase(authority.getAuthority()));
        if (startDate != null && startDate.isBefore(LocalDate.now()) && !superAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only Super Admins are authorized to apply for backdated leave.");
        }
    }
}

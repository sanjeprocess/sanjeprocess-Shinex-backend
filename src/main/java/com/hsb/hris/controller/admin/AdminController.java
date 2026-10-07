package com.hsb.hris.controller.admin;

import com.hsb.hris.dto.AuthDtos;
import com.hsb.hris.entity.LoginUser;
import com.hsb.hris.repository.LoginUserRepository;
import com.hsb.hris.repository.BusinessCenterRepository;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final LoginUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final BusinessCenterRepository businessCenterRepo;

    public AdminController(LoginUserRepository userRepo, PasswordEncoder passwordEncoder,
                           BusinessCenterRepository businessCenterRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.businessCenterRepo = businessCenterRepo;
    }
    private void verifySuperAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN") || a.getAuthority().equals("SUPERADMIN"));
        if (!isSuperAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only SUPERADMIN can access this endpoint");
        }
    }

    @GetMapping
    public List<AuthDtos.AdminDto> listAdmins() {
        verifySuperAdmin();
        List<AuthDtos.AdminDto> list = new ArrayList<>();
        boolean hasSuperAdmin = false;

        try {
            List<LoginUser> all = userRepo.findAll();
            for (LoginUser u : all) {
                AuthDtos.AdminDto dto = new AuthDtos.AdminDto();
                dto.loginName = u.getLoginName();
                dto.clientBusinessCode = u.getClientBusinessCode();
                dto.fullName = u.getFullName();
                dto.nicNumber = u.getNicNumber();
                dto.role = u.getRole();
                dto.blocked = u.isBlocked();
                dto.canViewSite = u.isCanViewSite();
                dto.accessLevel = u.getAccessLevel();
                dto.canManageUsers = u.isCanManageUsers();
                if (u.getModulePermissions() != null) {
                    try {
                        dto.modulePermissions = MAPPER.readValue(u.getModulePermissions(), Object.class);
                    } catch (Exception e) {
                        dto.modulePermissions = u.getModulePermissions();
                    }
                }
                if ("superadmin".equalsIgnoreCase(u.getLoginName())) {
                    hasSuperAdmin = true;
                    dto.role = "SUPERADMIN";
                }
                list.add(dto);
            }
        } catch (Exception e) {
            System.err.println("Error listing users from repository: " + e.getMessage());
        }

        // Ensure superadmin is always included in the returned list
        if (!hasSuperAdmin) {
            AuthDtos.AdminDto superAdminDto = new AuthDtos.AdminDto();
            superAdminDto.loginName = "superadmin";
            superAdminDto.role = "SUPERADMIN";
            superAdminDto.clientBusinessCode = "ALL";
            list.add(0, superAdminDto);
        }

        return list;
    }

    @PostMapping
    public ResponseEntity<?> createAdmin(@RequestBody AuthDtos.AdminDto req) {
        try {
            verifySuperAdmin();
            if (req == null || req.loginName == null || req.loginName.trim().isBlank()) {
                return ResponseEntity.badRequest().body("Username is required");
            }
            if (req.password == null || req.password.isBlank()) {
                return ResponseEntity.badRequest().body("Password is required");
            }
            String loginName = req.loginName.trim();
            if (loginName.length() > 50) return ResponseEntity.badRequest().body("Username must not exceed 50 characters");
            if (userRepo.findByLoginName(loginName).isPresent() || "superadmin".equalsIgnoreCase(loginName)) {
                return ResponseEntity.badRequest().body("User with this username already exists");
            }
            String businessCode = req.clientBusinessCode == null || req.clientBusinessCode.isBlank()
                    ? businessCenterRepo.findAll().stream().findFirst().map(b -> b.getCompanyId().trim()).orElse(null)
                    : req.clientBusinessCode.split(" - ", 2)[0].trim();
            if (businessCode == null || businessCode.isBlank()) {
                return ResponseEntity.badRequest().body("A valid business center code is required");
            }
            if (businessCode.length() > 100) return ResponseEntity.badRequest().body("Business center code must not exceed 100 characters");

            LoginUser user = new LoginUser();
            user.setLoginName(loginName);
            user.setPassword(passwordEncoder.encode(req.password.trim()));
            user.setRole(req.role != null && !req.role.isBlank() ? req.role.trim().toUpperCase() : "ADMIN");
            user.setClientBusinessCode(businessCode);
            user.setFullName(trimOptional(req.fullName, 100));
            user.setNicNumber(trimOptional(req.nicNumber, 20));
            applyPermissions(user, req);

            LoginUser saved = userRepo.save(user);
            AuthDtos.AdminDto resp = new AuthDtos.AdminDto();
            resp.loginName = saved.getLoginName();
            resp.role = saved.getRole();
            resp.clientBusinessCode = saved.getClientBusinessCode();
            resp.fullName = saved.getFullName();
            resp.nicNumber = saved.getNicNumber();
            copyPermissions(saved, resp);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (DataIntegrityViolationException | ConstraintViolationException ex) {
            System.err.println("Database constraint failed while creating administrator");
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Unable to create administrator: " + rootMessage(ex));
        } catch (Exception ex) {
            System.err.println("Failed to create administrator: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Unable to create administrator: " + rootMessage(ex));
        }
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage() == null ? ex.getClass().getSimpleName() : cause.getMessage();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable("id") String id, @RequestBody AuthDtos.AdminDto req) {
        verifySuperAdmin();
        LoginUser user = userRepo.findByLoginName(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if ("superadmin".equalsIgnoreCase(id) && req.role != null && !"SUPERADMIN".equalsIgnoreCase(req.role)) {
            return ResponseEntity.badRequest().body("The primary superadmin account cannot be demoted");
        }

        if (req.role != null && !req.role.isBlank()) {
            user.setRole(req.role.toUpperCase());
        }
        if (req.clientBusinessCode != null) {
            user.setClientBusinessCode(req.clientBusinessCode.trim());
        }
        if (req.fullName != null) {
            user.setFullName(trimOptional(req.fullName, 100));
        }
        if (req.nicNumber != null) {
            user.setNicNumber(trimOptional(req.nicNumber, 20));
        }
        applyPermissions(user, req);

        LoginUser updated = userRepo.save(user);

        AuthDtos.AdminDto resp = new AuthDtos.AdminDto();
        resp.loginName = updated.getLoginName();
        resp.role = updated.getRole();
        resp.clientBusinessCode = updated.getClientBusinessCode();
        resp.fullName = updated.getFullName();
        resp.nicNumber = updated.getNicNumber();
        copyPermissions(updated, resp);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable("id") String id) {
        id = id.trim();
        verifySuperAdmin();
        if ("superadmin".equalsIgnoreCase(id)) {
            return ResponseEntity.badRequest().body("The primary superadmin account cannot be deleted");
        }
        LoginUser user = userRepo.findByLoginName(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if ("SUPERADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.badRequest().body("SUPERADMIN accounts cannot be deleted");
        }

        userRepo.delete(user);
        return ResponseEntity.noContent().build();
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private void applyPermissions(LoginUser user, AuthDtos.AdminDto req) {
        if (!"SUPERADMIN".equalsIgnoreCase(user.getRole())) {
            if (req.blocked != null) user.setBlocked(req.blocked);
            if (req.canViewSite != null) user.setCanViewSite(req.canViewSite);
            if (req.accessLevel != null) user.setAccessLevel(req.accessLevel);
            if (req.canManageUsers != null) user.setCanManageUsers(req.canManageUsers);
            if (req.modulePermissions != null) {
                if (req.modulePermissions instanceof String str) {
                    user.setModulePermissions(str);
                } else {
                    try {
                        user.setModulePermissions(MAPPER.writeValueAsString(req.modulePermissions));
                    } catch (Exception e) {
                        user.setModulePermissions(req.modulePermissions.toString());
                    }
                }
            }
        } else {
            user.setBlocked(false);
            user.setCanViewSite(true);
            user.setAccessLevel("READ_WRITE");
            user.setCanManageUsers(true);
            user.setModulePermissions(null);
        }
    }

    private void copyPermissions(LoginUser user, AuthDtos.AdminDto dto) {
        dto.blocked = user.isBlocked();
        dto.canViewSite = user.isCanViewSite();
        dto.accessLevel = user.getAccessLevel();
        dto.canManageUsers = user.isCanManageUsers();
        if (user.getModulePermissions() != null) {
            try {
                dto.modulePermissions = MAPPER.readValue(user.getModulePermissions(), Object.class);
            } catch (Exception e) {
                dto.modulePermissions = user.getModulePermissions();
            }
        } else {
            dto.modulePermissions = null;
        }
    }

    private String trimOptional(String value, int max) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.substring(0, Math.min(max, trimmed.length()));
    }
}

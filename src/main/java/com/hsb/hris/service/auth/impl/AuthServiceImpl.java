package com.hsb.hris.service.auth.impl;

import com.hsb.hris.dto.AuthDtos;
import com.hsb.hris.entity.BusinessCenter;
import com.hsb.hris.entity.LoginUser;
import com.hsb.hris.repository.BusinessCenterRepository;
import com.hsb.hris.security.BusinessCenterScope;
import com.hsb.hris.repository.LoginUserRepository;
import com.hsb.hris.security.JwtUtil;
import com.hsb.hris.service.auth.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthServiceImpl implements AuthService {

    private final LoginUserRepository repo;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final BusinessCenterRepository centers;

    public AuthServiceImpl(LoginUserRepository repo, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, BusinessCenterRepository centers) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.centers = centers;
    }

    // The selected center must be the account's own; the account may hold its code or its name
    private boolean isOwnCenter(String assigned, String selected) {
        if (selected == null || selected.isBlank()) return false;
        if (BusinessCenterScope.sameCenter(assigned, selected)) return true;
        String assignedCode = BusinessCenterScope.codeOf(assigned);
        return centers.findById(BusinessCenterScope.codeOf(selected))
                .map(BusinessCenter::getCompanyName)
                .filter(name -> name != null && name.trim().equalsIgnoreCase(assignedCode))
                .isPresent();
    }

    @Override
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        AuthDtos.LoginResponse resp = new AuthDtos.LoginResponse();
        if (req == null || req.loginName == null || req.password == null) {
            resp.message = "Username and password are required";
            return resp;
        }

        String rawLoginName = req.loginName.trim();
        String loginName = rawLoginName.endsWith("@local") 
                ? rawLoginName.substring(0, rawLoginName.length() - "@local".length()).trim() 
                : rawLoginName;
        String password = req.password;

        LoginUser user = repo.findByLoginName(loginName).orElse(null);
        if (user == null && !loginName.equals(rawLoginName)) {
            user = repo.findByLoginName(rawLoginName).orElse(null);
        }

        if (user != null && user.isBlocked()) {
            resp.message = "Your account has been blocked. Please contact a Super Admin.";
            return resp;
        }

        if (user == null) {
            resp.message = "Invalid username or password";
            return resp;
        }

        String storedPassword = user.getPassword() == null ? "" : user.getPassword().trim();
        boolean validPassword = passwordEncoder.matches(password, storedPassword)
                || storedPassword.equals(password);

        if (!validPassword) {
            resp.message = "Invalid username or password";
            return resp;
        }

            String role = user.getRole() != null ? user.getRole() : "ADMIN";
            boolean isSuperAdminRole = "SUPERADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role);

            // Scope verification: non-superadmin users assigned to a specific Business Center cannot log into other Business Centers
            String assignedBc = user.getClientBusinessCode() != null ? user.getClientBusinessCode().trim() : "";
            if (!isSuperAdminRole && !assignedBc.isEmpty() && !"ALL".equalsIgnoreCase(assignedBc)) {
                if (!isOwnCenter(assignedBc, req.clientBusinessCode)) {
                    resp.message = AuthDtos.CENTER_NOT_ALLOWED;
                    return resp;
                }
            }

            String token = jwtUtil.generateToken(user.getLoginName(), role);
            resp.token = token;
            resp.loginName = user.getLoginName();
            resp.clientBusinessCode = (user.getClientBusinessCode() != null && !user.getClientBusinessCode().isBlank())
                    ? user.getClientBusinessCode()
                    : (req.clientBusinessCode != null && !req.clientBusinessCode.isBlank() ? req.clientBusinessCode : "ALL");
            resp.fullName = user.getFullName();
            resp.nicNumber = user.getNicNumber();
            resp.role = role;
            resp.blocked = user.isBlocked();
            resp.canViewSite = user.isCanViewSite();
            resp.accessLevel = user.getAccessLevel();
            resp.canManageUsers = user.isCanManageUsers();
            if (user.getModulePermissions() != null) {
                try {
                    resp.modulePermissions = new com.fasterxml.jackson.databind.ObjectMapper().readValue(user.getModulePermissions(), Object.class);
                } catch (Exception e) {
                    resp.modulePermissions = user.getModulePermissions();
                }
            }
        return resp;
    }
}

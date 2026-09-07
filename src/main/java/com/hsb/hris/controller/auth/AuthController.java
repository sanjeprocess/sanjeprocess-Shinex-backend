package com.hsb.hris.controller.auth;

import com.hsb.hris.dto.AuthDtos;
import com.hsb.hris.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping(value = {"/login", "/login/"}, consumes = "application/json")
    public ResponseEntity<AuthDtos.LoginResponse> login(@RequestBody AuthDtos.LoginRequest req) {
        if (req == null || req.loginName == null || req.loginName.trim().isEmpty()
                || req.password == null || req.password.isEmpty()) {
            AuthDtos.LoginResponse response = new AuthDtos.LoginResponse();
            response.message = "Username and password are required";
            return ResponseEntity.badRequest().body(response);
        }
        AuthDtos.LoginResponse resp = authService.login(req);
        if (resp.token == null) return ResponseEntity.status(401).body(resp);
        return ResponseEntity.ok(resp);
    }
}

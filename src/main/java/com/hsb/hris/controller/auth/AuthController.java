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

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.LoginResponse> login(@RequestBody AuthDtos.LoginRequest req) {
        AuthDtos.LoginResponse resp = authService.login(req);
        if (resp.token == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(resp);
    }
}

package com.hsb.hris.service.auth;

import com.hsb.hris.dto.AuthDtos;

public interface AuthService {
    AuthDtos.LoginResponse login(AuthDtos.LoginRequest req);
}

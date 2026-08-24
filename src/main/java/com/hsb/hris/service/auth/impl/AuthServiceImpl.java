package com.hsb.hris.service.auth.impl;

import com.hsb.hris.dto.AuthDtos;
import com.hsb.hris.entity.LoginUser;
import com.hsb.hris.repository.LoginUserRepository;
import com.hsb.hris.security.JwtUtil;
import com.hsb.hris.service.auth.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final LoginUserRepository repo;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(LoginUserRepository repo, JwtUtil jwtUtil) {
        this.repo = repo;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        LoginUser user = repo.findByLoginName(req.loginName).orElse(null);
        AuthDtos.LoginResponse resp = new AuthDtos.LoginResponse();
        if (user == null) return resp;
        // Legacy DB uses short plain passwords; replace with hashing in production
        if (!user.getPassword().equals(req.password)) return resp;
        String token = jwtUtil.generateToken(user.getLoginName());
        resp.token = token;
        resp.loginName = user.getLoginName();
        resp.clientBusinessCode = user.getClientBusinessCode();
        return resp;
    }
}

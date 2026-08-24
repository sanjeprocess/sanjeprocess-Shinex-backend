package com.hsb.hris.dto;

public class AuthDtos {
    public static class LoginRequest {
        public String loginName;
        public String password;
    }
    public static class LoginResponse {
        public String token;
        public String loginName;
        public String clientBusinessCode;
    }
}

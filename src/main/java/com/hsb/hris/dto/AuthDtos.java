package com.hsb.hris.dto;

public class AuthDtos {
    public static class LoginRequest {
        public String loginName;
        public String password;
        public String clientBusinessCode;
    }

    public static class LoginResponse {
        public String token;
        public String loginName;
        public String clientBusinessCode;
        public String fullName;
        public String nicNumber;
        public String role;
        public String message;
        public Boolean blocked;
        public Boolean canViewSite;
        public String accessLevel;
        public Boolean canManageUsers;
    }

    public static class AdminDto {
        public String loginName;
        public String password;
        public String clientBusinessCode;
        public String fullName;
        public String nicNumber;
        public String role;
        public Boolean blocked;
        public Boolean canViewSite;
        public String accessLevel;
        public Boolean canManageUsers;
    }
}

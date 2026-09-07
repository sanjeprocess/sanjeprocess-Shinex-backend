package com.hsb.hris.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.hsb.hris.entity.LoginUser;
import com.hsb.hris.repository.LoginUserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final LoginUserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, LoginUserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsername(token);
                String role = jwtUtil.getRole(token);
                
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                // If SUPERADMIN, also grant ROLE_ADMIN
                if ("SUPERADMIN".equalsIgnoreCase(role)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                LoginUser user = userRepository.findByLoginName(username).orElse(null);
                boolean superAdmin = "SUPERADMIN".equalsIgnoreCase(role);
                if (user != null && user.isBlocked() && !superAdmin) {
                    reject(response, HttpServletResponse.SC_FORBIDDEN, "This account is blocked.");
                    return;
                }
                if (!superAdmin && user != null && !user.isCanViewSite()
                        && !request.getRequestURI().startsWith(request.getContextPath() + "/api/auth")) {
                    reject(response, HttpServletResponse.SC_FORBIDDEN, "This account does not have site access.");
                    return;
                }
                if (!superAdmin && user != null && isWrite(request)
                        && "READ_ONLY".equalsIgnoreCase(user.getAccessLevel())) {
                    reject(response, HttpServletResponse.SC_FORBIDDEN, "This account has read-only access.");
                    return;
                }
                if (!superAdmin && user != null && isUserManagementWrite(request)
                        && !user.isCanManageUsers()) {
                    reject(response, HttpServletResponse.SC_FORBIDDEN, "User management permission is required.");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isWrite(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
    }

    private boolean isUserManagementWrite(HttpServletRequest request) {
        return request.getRequestURI().startsWith(request.getContextPath() + "/api/admins")
                && isWrite(request);
    }

    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}

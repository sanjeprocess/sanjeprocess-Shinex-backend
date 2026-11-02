package com.hsb.hris.service;

import com.hsb.hris.controller.master.BusinessCenterController;
import com.hsb.hris.dto.AuthDtos;
import com.hsb.hris.entity.BusinessCenter;
import com.hsb.hris.entity.LoginUser;
import com.hsb.hris.repository.BusinessCenterRepository;
import com.hsb.hris.repository.EmployeeRepository;
import com.hsb.hris.repository.LoginUserRepository;
import com.hsb.hris.security.BusinessCenterScope;
import com.hsb.hris.security.JwtUtil;
import com.hsb.hris.service.auth.impl.AuthServiceImpl;
import com.hsb.hris.service.master.BusinessCenterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A login must select its own business center. Only the super admin may select any. */
class LoginBusinessCenterTest {

    private final LoginUserRepository users = mock(LoginUserRepository.class);
    private final JwtUtil jwt = mock(JwtUtil.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final BusinessCenterRepository centers = mock(BusinessCenterRepository.class);
    private final AuthServiceImpl auth = new AuthServiceImpl(users, jwt, encoder, centers);

    private static BusinessCenter center(String id, String name) {
        BusinessCenter c = new BusinessCenter();
        c.setCompanyId(id);
        c.setCompanyName(name);
        return c;
    }

    @BeforeEach
    void setUp() {
        BusinessCenter shinex = center("B001", "Shinex");
        BusinessCenter alfa = center("B002", "Alfa");
        when(centers.findAll()).thenReturn(List.of(shinex, alfa));
        when(centers.findById("B001")).thenReturn(Optional.of(shinex));
        when(centers.findById("B002")).thenReturn(Optional.of(alfa));
        when(encoder.matches("right", "right")).thenReturn(true);
        when(jwt.generateToken(anyString(), any())).thenReturn("token");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    private void account(String name, String center) {
        LoginUser user = new LoginUser();
        user.setLoginName(name);
        user.setPassword("right");
        user.setClientBusinessCode(center);
        when(users.findByLoginName(name)).thenReturn(Optional.of(user));
    }

    private AuthDtos.LoginResponse login(String name, String password, String selected) {
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest();
        request.loginName = name;
        request.password = password;
        request.clientBusinessCode = selected;
        return auth.login(request);
    }

    @Test
    void shinexLoginSelectingShinexSignsIn() {
        account("shinex", "B001");
        assertEquals("token", login("shinex", "right", "B001").token);
    }

    @Test
    void shinexLoginSelectingAlfaIsRefused() {
        account("shinex", "B001");
        AuthDtos.LoginResponse response = login("shinex", "right", "B002");
        assertNull(response.token);
        assertEquals(AuthDtos.CENTER_NOT_ALLOWED, response.message);
    }

    @Test
    void accountSavedWithNameOrCodeAndNameMatchesTheSelectedCode() {
        account("byName", "Shinex");
        account("both", "B001 / Shinex");
        account("dash", "B001 - Shinex");
        assertEquals("token", login("byName", "right", "B001").token);
        assertNull(login("byName", "right", "B002").token);
        assertEquals("token", login("both", "right", "B001").token);
        assertNull(login("both", "right", "B002").token);
        assertEquals("token", login("dash", "right", "B001").token);
    }

    @Test
    void selectingNoCenterIsRefusedExceptForSuperAdmin() {
        account("shinex", "B001");
        account("superadmin", "ALL");
        assertNull(login("shinex", "right", null).token);
        assertNull(login("shinex", "right", "").token);
        assertEquals("token", login("superadmin", "right", null).token);
    }

    @Test
    void superAdminCanSelectAnyCenter() {
        account("superadmin", "ALL");
        assertEquals("token", login("superadmin", "right", "B001").token);
        assertEquals("token", login("superadmin", "right", "B002").token);
        account("SuperAdmin", "B001");
        assertEquals("token", login("SuperAdmin", "right", "B002").token);
    }

    @Test
    void wrongPasswordDoesNotRevealTheCenter() {
        account("shinex", "B001");
        AuthDtos.LoginResponse response = login("shinex", "wrong", "B001");
        assertNull(response.token);
        assertEquals("Invalid username or password", response.message);
        assertEquals("Invalid username or password", login("shinex", "wrong", "B002").message);
    }

    @Test
    void lockWorksForAnAccountSavedWithTheCenterName() {
        LoginUser user = new LoginUser();
        user.setLoginName("byName");
        user.setClientBusinessCode("Shinex");
        when(centers.findById("Shinex")).thenReturn(Optional.empty());
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String code = new BusinessCenterScope(mock(EmployeeRepository.class), centers, users).restrict(request, user);

        assertEquals("B001", code);
        assertTrue(BusinessCenterScope.allows("B001"));
        assertTrue(BusinessCenterScope.allows("Shinex"));
        assertFalse(BusinessCenterScope.allows("B002"));
    }

    @Test
    void loginPageGetsOnlyCodeAndName() {
        BusinessCenter full = center("B001", "Shinex");
        full.setCompanyAddress("Private address");
        BusinessCenterService service = mock(BusinessCenterService.class);
        when(service.findAll()).thenReturn(List.of(full));
        BusinessCenterController controller = new BusinessCenterController(service);

        BusinessCenter shown = controller.list().get(0);
        assertEquals("B001", shown.getCode());
        assertEquals("Shinex", shown.getName());
        assertNull(shown.getCompanyAddress());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("superadmin", null, List.of()));
        assertEquals("Private address", controller.list().get(0).getCompanyAddress());
    }
}

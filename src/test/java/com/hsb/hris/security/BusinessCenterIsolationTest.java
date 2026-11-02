package com.hsb.hris.security;

import com.hsb.hris.config.BusinessCenterRequestAdvice;
import com.hsb.hris.config.BusinessCenterResponseAdvice;
import com.hsb.hris.entity.*;
import com.hsb.hris.repository.BusinessCenterRepository;
import com.hsb.hris.repository.EmployeeRepository;
import com.hsb.hris.repository.LoginUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A business center login sees only its own center, whatever the browser asks for. A super admin sees any. */
class BusinessCenterIsolationTest {

    private final EmployeeRepository employees = mock(EmployeeRepository.class);
    private final BusinessCenterRepository centers = mock(BusinessCenterRepository.class);
    private final LoginUserRepository users = mock(LoginUserRepository.class);
    private final JwtUtil jwt = mock(JwtUtil.class);
    private final BusinessCenterScope scope = new BusinessCenterScope(employees, centers, users);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, users, scope);

    @BeforeEach
    void setUp() {
        Employee own = employee("E1", "001");
        Employee other = employee("E2", "002");
        Employee ownByName = employee("E3", "Company One");
        Employee longer = employee("E4", "0012");
        when(employees.findAll()).thenReturn(List.of(own, other, ownByName, longer));
        BusinessCenter one = new BusinessCenter();
        one.setCompanyId("001");
        one.setCompanyName("Company One");
        when(centers.findById("001")).thenReturn(Optional.of(one));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private static Employee employee(String epf, String center) {
        Employee e = new Employee();
        e.setEpfNo(epf);
        e.setBusinessCenter(center);
        return e;
    }

    private static LoginUser user(String name, String role, String center) {
        LoginUser u = new LoginUser();
        u.setLoginName(name);
        u.setRole(role);
        u.setClientBusinessCode(center);
        return u;
    }

    /** Runs a request through the real filter and returns the request the controllers would receive. */
    private jakarta.servlet.ServletRequest login(LoginUser user, String askedCenter) throws Exception {
        when(jwt.validateToken("t")).thenReturn(true);
        when(jwt.getUsername("t")).thenReturn(user.getLoginName());
        when(jwt.getRole("t")).thenReturn(user.getRole());
        when(users.findByLoginName(user.getLoginName())).thenReturn(Optional.of(user));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/employees");
        request.setServletPath("/api/employees");
        request.addHeader("Authorization", "Bearer t");
        if (askedCenter != null) request.setParameter("businessCenter", askedCenter);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, new MockHttpServletResponse(), chain);
        return chain.getRequest();
    }

    private void lockedTo001() throws Exception {
        login(user("one@shinex.lk", "ADMIN", "001"), null);
    }

    @Test
    void browserCannotAskForAnotherCenter() throws Exception {
        LoginUser one = user("one@shinex.lk", "ADMIN", "001");
        assertEquals("001", login(one, "002").getParameter("businessCenter"));
        assertEquals("001", login(one, "ALL").getParameter("businessCenter"));
        assertEquals("001", login(one, null).getParameter("businessCenter"));
        assertEquals("001", login(user("two", "ADMIN", "001 - Company One"), "002").getParameter("businessCenter"));
    }

    @Test
    void superAdminCanViewAnyCenter() throws Exception {
        LoginUser root = user("root", "SUPERADMIN", "ALL");
        assertEquals("002", login(root, "002").getParameter("businessCenter"));
        assertEquals("ALL", login(root, "ALL").getParameter("businessCenter"));
        assertFalse(BusinessCenterScope.isRestricted());
        assertTrue(BusinessCenterScope.isVisible(employee("E2", "002")));
        // A super admin assigned to one center still sees any
        assertEquals("002", login(user("root2", "SUPER_ADMIN", "001"), "002").getParameter("businessCenter"));
        assertFalse(BusinessCenterScope.isRestricted());
    }

    @Test
    void unknownUsersTokenIsRejected() throws Exception {
        when(jwt.validateToken("t")).thenReturn(true);
        when(jwt.getUsername("t")).thenReturn("gone");
        when(jwt.getRole("t")).thenReturn("ADMIN");
        when(users.findByLoginName("gone")).thenReturn(Optional.empty());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/employees");
        request.setServletPath("/api/employees");
        request.addHeader("Authorization", "Bearer t");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void plantsOfOtherCentersAndUnassignedPlantsAreHidden() throws Exception {
        lockedTo001();
        Plant own = new Plant();
        own.setBusinessCenter("001");
        Plant other = new Plant();
        other.setBusinessCenter("002");
        Plant unassigned = new Plant();
        assertTrue(BusinessCenterScope.isVisible(own));
        assertFalse(BusinessCenterScope.isVisible(other));
        assertFalse(BusinessCenterScope.isVisible(unassigned));
    }

    @Test
    void employeesOnlyFromOwnCenterWithNoPrefixMatch() throws Exception {
        lockedTo001();
        assertTrue(BusinessCenterScope.isVisible(employee("E1", "001")));
        assertTrue(BusinessCenterScope.isVisible(employee("E3", "Company One")));
        assertFalse(BusinessCenterScope.isVisible(employee("E2", "002")));
        assertFalse(BusinessCenterScope.isVisible(employee("E4", "0012")), "0012 must not match 001");
    }

    @Test
    void transactionsFollowTheEmployee() throws Exception {
        lockedTo001();
        Attendance mine = new Attendance();
        mine.setEpfNo("E1");
        mine.setBusinessCenter("ALL");
        Attendance theirs = new Attendance();
        theirs.setEpfNo("E2");
        theirs.setBusinessCenter("002");
        TDeduction allButTheirs = new TDeduction();
        allButTheirs.setEpfNo("E2");
        allButTheirs.setBusinessCenter("ALL");
        assertTrue(BusinessCenterScope.isVisible(mine));
        assertFalse(BusinessCenterScope.isVisible(theirs));
        assertFalse(BusinessCenterScope.isVisible(allButTheirs));
    }

    @Test
    void reportRowsAndOtherCentersListAreFiltered() throws Exception {
        lockedTo001();
        BusinessCenter one = new BusinessCenter();
        one.setCompanyId("001");
        BusinessCenter two = new BusinessCenter();
        two.setCompanyId("002");
        assertTrue(BusinessCenterScope.isVisible(one));
        assertFalse(BusinessCenterScope.isVisible(two));
        assertFalse(BusinessCenterScope.isVisible(Map.of("epfNo", "E2", "amount", 100)));
        assertTrue(BusinessCenterScope.isVisible(Map.of("epfNo", "E1", "amount", 100)));
        assertFalse(BusinessCenterScope.isVisible(Map.of("businessCenter", "002", "amount", 100)));
    }

    @Test
    void sharedTypesStayVisible() throws Exception {
        lockedTo001();
        assertTrue(BusinessCenterScope.isVisible(new DeductionMaster()));
        assertTrue(BusinessCenterScope.isVisible(new AdditionMaster()));
    }

    @Test
    void auditLogShowsOnlyUsersOfTheSameCenter() throws Exception {
        when(users.findAll()).thenReturn(List.of(user("one@shinex.lk", "ADMIN", "001"), user("two@shinex.lk", "ADMIN", "002")));
        lockedTo001();
        AuditLog mine = new AuditLog();
        mine.setPerformedBy("one@shinex.lk");
        AuditLog theirs = new AuditLog();
        theirs.setPerformedBy("two@shinex.lk");
        assertTrue(BusinessCenterScope.isVisible(mine));
        assertFalse(BusinessCenterScope.isVisible(theirs));
    }

    @Test
    void responsesDropOtherCentersRowsAndRefuseSingleRecords() throws Exception {
        lockedTo001();
        BusinessCenterResponseAdvice advice = new BusinessCenterResponseAdvice();
        Object result = advice.beforeBodyWrite(new ArrayList<>(List.of(employee("E1", "001"), employee("E2", "002"))), null, null, null, null, null);
        assertEquals(1, ((List<?>) result).size());
        assertEquals("E1", ((Employee) ((List<?>) result).get(0)).getEpfNo());
        assertThrows(ResponseStatusException.class, () -> advice.beforeBodyWrite(employee("E2", "002"), null, null, null, null, null));
    }

    @Test
    void savesGoToOwnCenterAndOtherCentersEmployeesAreRefused() throws Exception {
        lockedTo001();
        BusinessCenterRequestAdvice advice = new BusinessCenterRequestAdvice();

        Plant plant = new Plant();
        plant.setBusinessCenter("002");
        advice.afterBodyRead(plant, null, null, null, null);
        assertEquals("001", plant.getBusinessCenter());

        Attendance own = new Attendance();
        own.setEpfNo("E1");
        own.setBusinessCenter("002");
        advice.afterBodyRead(new ArrayList<>(List.of(own)), null, null, null, null);
        assertEquals("001", own.getBusinessCenter());

        Attendance other = new Attendance();
        other.setEpfNo("E2");
        assertThrows(ResponseStatusException.class, () -> advice.afterBodyRead(List.of(other), null, null, null, null));

        Map<String, Object> filters = new HashMap<>();
        advice.afterBodyRead(filters, null, null, null, null);
        assertEquals("001", filters.get("businessCenter"));

        Map<String, String> status = new HashMap<>(Map.of("epfNo", "E2", "status", "W"));
        assertThrows(ResponseStatusException.class, () -> advice.afterBodyRead(status, null, null, null, null));

        // New employee keeps its new number, in the user's center
        Employee created = employee("NEW1", "002");
        advice.afterBodyRead(created, null, null, null, null);
        assertEquals("001", created.getBusinessCenter());
    }

    @Test
    void wholeCodeComparison() {
        assertTrue(BusinessCenterScope.sameCenter("001", "001 / Company One"));
        assertTrue(BusinessCenterScope.sameCenter("001 - Company One", "001"));
        assertFalse(BusinessCenterScope.sameCenter("0012", "001"));
        assertFalse(BusinessCenterScope.sameCenter("001", "0012"));
        assertFalse(BusinessCenterScope.sameCenter("", "001"));
    }
}

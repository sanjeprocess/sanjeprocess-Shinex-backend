package com.hsb.hris.security;

import com.hsb.hris.entity.AuditLog;
import com.hsb.hris.entity.BusinessCenter;
import com.hsb.hris.entity.Employee;
import com.hsb.hris.entity.LoginUser;
import com.hsb.hris.repository.BusinessCenterRepository;
import com.hsb.hris.repository.EmployeeRepository;
import com.hsb.hris.repository.LoginUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The business center a request may see. A normal user is locked to the center of their account,
 * whatever the browser asks for. A super admin, or an account assigned "ALL", is not restricted.
 * The lock is set by {@link JwtAuthenticationFilter} from the database, never from the request.
 */
@Component
public class BusinessCenterScope {

    private static final String CODE = "hsb.bc.code";
    private static final String ALIASES = "hsb.bc.aliases";
    private static final String EMPLOYEES = "hsb.bc.employees";
    private static final String USERS = "hsb.bc.users";
    private static final Object NO_PROPERTY = new Object();

    private static BusinessCenterScope instance;

    private final EmployeeRepository employees;
    private final BusinessCenterRepository centers;
    private final LoginUserRepository users;

    public BusinessCenterScope(EmployeeRepository employees, BusinessCenterRepository centers, LoginUserRepository users) {
        this.employees = employees;
        this.centers = centers;
        this.users = users;
        instance = this;
    }

    /** "001 - Name" or "001 / Name" to "001". */
    public static String codeOf(String value) {
        if (value == null) return "";
        return value.split(" - ", 2)[0].split("/", 2)[0].trim();
    }

    public static boolean isSuperAdminRole(String role) {
        return "SUPERADMIN".equalsIgnoreCase(role) || "SUPER_ADMIN".equalsIgnoreCase(role);
    }

    /** Whole-code comparison, so "001" never matches "0012". */
    public static boolean sameCenter(String value, String center) {
        String left = codeOf(value);
        String right = codeOf(center);
        return !left.isEmpty() && left.equalsIgnoreCase(right);
    }

    /** Locks this request to the user's center. Returns the code, or null when the user is not restricted. */
    public String restrict(HttpServletRequest request, LoginUser user) {
        String code = codeOf(user.getClientBusinessCode());
        if (code.isEmpty() || "ALL".equalsIgnoreCase(code)) return null;
        Set<String> aliases = new HashSet<>();
        aliases.add(code.toUpperCase(Locale.ROOT));
        try {
            BusinessCenter center = centers.findById(code).orElse(null);
            if (center == null) {
                String assigned = code;
                center = centers.findAll().stream()
                        .filter(c -> c.getCompanyName() != null && c.getCompanyName().trim().equalsIgnoreCase(assigned))
                        .findFirst().orElse(null);
                if (center != null) code = center.getCompanyId().trim();
            }
            if (center != null) {
                aliases.add(code.toUpperCase(Locale.ROOT));
                if (center.getCompanyName() != null && !center.getCompanyName().isBlank()) {
                    aliases.add(center.getCompanyName().trim().toUpperCase(Locale.ROOT));
                }
            }
        } catch (RuntimeException ex) {
            // The code alone still locks the request
        }
        request.setAttribute(CODE, code);
        request.setAttribute(ALIASES, aliases);
        return code;
    }

    private static RequestAttributes attributes() {
        return RequestContextHolder.getRequestAttributes();
    }

    /** The locked center code, or null when this request is not restricted. */
    public static String restrictedCode() {
        RequestAttributes attrs = attributes();
        return attrs == null ? null : (String) attrs.getAttribute(CODE, RequestAttributes.SCOPE_REQUEST);
    }

    public static boolean isRestricted() {
        return restrictedCode() != null;
    }

    @SuppressWarnings("unchecked")
    private static Set<String> aliases() {
        return (Set<String>) attributes().getAttribute(ALIASES, RequestAttributes.SCOPE_REQUEST);
    }

    /** True when the value names the locked center (its code, "code / name", or its name). */
    public static boolean allows(String businessCenter) {
        if (!isRestricted()) return true;
        if (businessCenter == null) return false;
        String text = businessCenter.trim().toUpperCase(Locale.ROOT);
        return aliases().contains(text) || aliases().contains(codeOf(text).toUpperCase(Locale.ROOT));
    }

    // Employee number to "belongs to the locked center", loaded once per request
    @SuppressWarnings("unchecked")
    private static Map<String, Boolean> employeeCenters() {
        RequestAttributes attrs = attributes();
        Map<String, Boolean> map = (Map<String, Boolean>) attrs.getAttribute(EMPLOYEES, RequestAttributes.SCOPE_REQUEST);
        if (map == null) {
            map = new HashMap<>();
            for (Employee e : instance.employees.findAll()) {
                if (e.getEpfNo() != null) map.put(e.getEpfNo().trim().toUpperCase(Locale.ROOT), allows(e.getBusinessCenter()));
            }
            attrs.setAttribute(EMPLOYEES, map, RequestAttributes.SCOPE_REQUEST);
        }
        return map;
    }

    /** False only when the employee exists and belongs to another center. */
    public static boolean allowsEmployee(String epfNo) {
        if (!isRestricted() || epfNo == null || epfNo.isBlank()) return true;
        return employeeCenters().getOrDefault(epfNo.trim().toUpperCase(Locale.ROOT), true);
    }

    public static void assertEmployee(String epfNo) {
        if (!allowsEmployee(epfNo)) throw forbidden();
    }

    public static void assertVisible(Object record) {
        if (record != null && !isVisible(record)) throw forbidden();
    }

    /** For actions only a super admin may take, such as creating a business center. */
    public static void assertNotRestricted() {
        if (isRestricted()) throw forbidden();
    }

    public static ResponseStatusException forbidden() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "This record belongs to another business center.");
    }

    /**
     * Whether a record may be shown to the locked center. Records with no center or employee of their
     * own (deduction types and the like) are shared. A plant or section with no center is not.
     */
    public static boolean isVisible(Object record) {
        if (!isRestricted() || record == null) return true;
        if (record instanceof BusinessCenter center) return allows(center.getCompanyId());
        if (record instanceof AuditLog log) return sameCenterUsers().contains(String.valueOf(log.getPerformedBy()).trim().toUpperCase(Locale.ROOT));

        Object center = read(record, "businessCenter");
        Object epf = read(record, "epfNo");
        if (epf == NO_PROPERTY) epf = read(record, "empNo");

        boolean hasEpf = epf != NO_PROPERTY && epf != null && !String.valueOf(epf).isBlank();
        String centerText = center == NO_PROPERTY || center == null ? "" : String.valueOf(center).trim();
        boolean hasCenter = !centerText.isEmpty() && !"ALL".equalsIgnoreCase(centerText);

        if (hasCenter && allows(centerText)) return true;
        if (hasEpf) {
            Boolean known = employeeCenters().get(String.valueOf(epf).trim().toUpperCase(Locale.ROOT));
            if (known != null) return known;
        }
        if (hasCenter) return false;
        // No center and no known employee: shared only if the record has no such fields at all
        return center == NO_PROPERTY && epf == NO_PROPERTY;
    }

    @SuppressWarnings("unchecked")
    private static Set<String> sameCenterUsers() {
        RequestAttributes attrs = attributes();
        Set<String> names = (Set<String>) attrs.getAttribute(USERS, RequestAttributes.SCOPE_REQUEST);
        if (names == null) {
            names = new HashSet<>();
            for (LoginUser user : instance.users.findAll()) {
                if (user.getLoginName() != null && sameCenter(user.getClientBusinessCode(), restrictedCode())) {
                    names.add(user.getLoginName().trim().toUpperCase(Locale.ROOT));
                }
            }
            attrs.setAttribute(USERS, names, RequestAttributes.SCOPE_REQUEST);
        }
        return names;
    }

    /** Value of a map key, getter, record accessor or public field; NO_PROPERTY when there is none. */
    public static Object read(Object record, String name) {
        if (record instanceof Map<?, ?> map) return map.containsKey(name) ? map.get(name) : NO_PROPERTY;
        String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (String method : new String[] {getter, name}) {
            try {
                return record.getClass().getMethod(method).invoke(record);
            } catch (ReflectiveOperationException ex) {
                // try the next form
            }
        }
        try {
            Field field = record.getClass().getField(name);
            if (Modifier.isPublic(field.getModifiers())) return field.get(record);
        } catch (ReflectiveOperationException ex) {
            // no such field
        }
        return NO_PROPERTY;
    }

    public static boolean isMissing(Object value) {
        return value == NO_PROPERTY;
    }
}

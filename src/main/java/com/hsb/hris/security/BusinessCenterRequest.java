package com.hsb.hris.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Every endpoint's "businessCenter" parameter reads as the user's own center, whatever was sent. */
class BusinessCenterRequest extends HttpServletRequestWrapper {

    private static final String PARAMETER = "businessCenter";

    private final String code;

    BusinessCenterRequest(HttpServletRequest request, String code) {
        super(request);
        this.code = code;
    }

    @Override
    public String getParameter(String name) {
        return PARAMETER.equals(name) ? code : super.getParameter(name);
    }

    @Override
    public String[] getParameterValues(String name) {
        return PARAMETER.equals(name) ? new String[] {code} : super.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new HashMap<>(super.getParameterMap());
        map.put(PARAMETER, new String[] {code});
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Set<String> names = new HashSet<>(super.getParameterMap().keySet());
        names.add(PARAMETER);
        return Collections.enumeration(names);
    }
}

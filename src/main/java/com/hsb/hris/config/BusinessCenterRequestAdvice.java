package com.hsb.hris.config;

import com.hsb.hris.security.BusinessCenterScope;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/** Saves by a locked user: records go into their own center, and other centers' employees are refused. */
@ControllerAdvice
public class BusinessCenterRequestAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        if (!BusinessCenterScope.isRestricted()) return body;
        if (body instanceof Collection<?> rows) rows.forEach(this::lock);
        else lock(body);
        return body;
    }

    @SuppressWarnings("unchecked")
    private void lock(Object record) {
        if (record == null) return;
        for (String name : new String[] {"epfNo", "empNo"}) {
            Object epf = BusinessCenterScope.read(record, name);
            if (!BusinessCenterScope.isMissing(epf) && epf != null) BusinessCenterScope.assertEmployee(String.valueOf(epf));
        }
        String code = BusinessCenterScope.restrictedCode();
        if (record instanceof Map<?, ?> map) {
            ((Map<String, Object>) map).put("businessCenter", code);
            return;
        }
        try {
            record.getClass().getMethod("setBusinessCenter", String.class).invoke(record, code);
        } catch (ReflectiveOperationException ex) {
            // Read-only center (a record type) must already name the user's own center
            Object center = BusinessCenterScope.read(record, "businessCenter");
            if (!BusinessCenterScope.isMissing(center) && center != null && !BusinessCenterScope.allows(String.valueOf(center))) {
                throw BusinessCenterScope.forbidden();
            }
        }
    }
}

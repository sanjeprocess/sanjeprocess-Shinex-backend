package com.hsb.hris.config;

import com.hsb.hris.security.BusinessCenterScope;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Collection;

/** Last check on every response: a locked user never receives another business center's records. */
@ControllerAdvice
public class BusinessCenterResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null || !BusinessCenterScope.isRestricted()) return body;
        if (body instanceof Collection<?> rows) {
            return rows.stream().filter(BusinessCenterScope::isVisible).toList();
        }
        BusinessCenterScope.assertVisible(body);
        return body;
    }
}

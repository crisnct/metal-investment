package com.investment.metal;

import com.investment.metal.encryption.AbstractHandShakeEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CallsInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private AbstractHandShakeEncryptor handShakeEncryptor;

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String hs = request.getHeader("hs");
        this.handShakeEncryptor.check(hs);
        return true;
    }
}

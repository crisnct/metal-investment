package com.investment.metal;

import com.investment.metal.encryption.AbstractHandShakeEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CallsInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallsInterceptor.class);

    @Autowired
    private AbstractHandShakeEncryptor handShakeEncryptor;

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod hm = (HandlerMethod) handler;
        LOGGER.info("Start request:" + hm.getShortLogMessage());

        final String hs = request.getHeader("hs");
        this.handShakeEncryptor.check(hs);
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        HandlerMethod hm = (HandlerMethod) handler;
        LOGGER.info("End request:" + hm.getShortLogMessage());
        if (ex != null) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}

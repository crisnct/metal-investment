package com.investment.metal;

import com.investment.metal.encryption.AbstractHandShakeEncryptor;
import com.investment.metal.service.exception.ExceptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class CallsInterceptor implements HandlerInterceptor {

    public static final String HANDSHAKE_HEADER = "hs";

    private static final Logger LOGGER = LoggerFactory.getLogger(CallsInterceptor.class);

    private static final int MAX_HEADER_SIZE = 500;

    @Autowired
    protected ExceptionService exceptionService;

    @Autowired
    private AbstractHandShakeEncryptor handShakeEncryptor;

    @Override
    @SuppressWarnings("NullableProblems")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod hm = (HandlerMethod) handler;
        LOGGER.info("Start request: " + hm.getShortLogMessage());

        //Reject requests with empty headers or with headers which are too long
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            String value = request.getHeader(header);
            if (StringUtils.isEmpty(value)) {
                throw this.exceptionService
                        .createBuilder(MessageKey.INVALID_REQUEST)
                        .setArguments("Invalid header: " + header)
                        .build();
            } else if (value.length() > MAX_HEADER_SIZE) {
                throw this.exceptionService
                        .createBuilder(MessageKey.INVALID_REQUEST)
                        .setArguments("Too long header: " + header)
                        .build();
            }
        }

        //Validate handshake token
        final String hs = request.getHeader(HANDSHAKE_HEADER);
        this.handShakeEncryptor.check(hs);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerMethod hm = (HandlerMethod) handler;
        LOGGER.info("End request:" + hm.getShortLogMessage());
        if (ex != null) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}

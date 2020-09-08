package com.investment.metal;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class Util {
    //1ounce = 0.0283495231 kg
    public static final double ounce = 0.0283495231;

    @Getter
    private static final Random randomGenerator = new Random();

    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getClientIpAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    public static String getTokenFromRequest(HttpServletRequest httpServletRequest){
        String token = httpServletRequest.getHeader(AUTHORIZATION);
        if (token == null) {
            token = "";
        }
        return StringUtils.removeStart(token, "Bearer").trim();
    }


}

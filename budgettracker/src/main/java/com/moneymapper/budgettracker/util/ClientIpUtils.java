package com.moneymapper.budgettracker.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Utility class for extracting client IP addresses from HTTP requests.
 * Handles various proxy headers and forwarded IP scenarios.
 */
@Component
public class ClientIpUtils {

    private static final String[] IP_HEADER_NAMES = {
            "X-Forwarded-For",
            "X-Real-IP",
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

    /**
     * Extracts the client's real IP address from the HTTP request.
     * Checks various proxy headers before falling back to remote address.
     *
     * @param request the HTTP servlet request
     * @return the client's IP address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADER_NAMES) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // X-Forwarded-For can contain multiple IPs, get the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Checks if the IP string is valid (not null, empty, or "unknown").
     *
     * @param ip the IP string to validate
     * @return true if the IP is valid
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
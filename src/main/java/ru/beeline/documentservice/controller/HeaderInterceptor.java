package ru.beeline.documentservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.beeline.documentservice.exception.ForbiddenException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ru.beeline.documentservice.utils.Constants.*;

public class HeaderInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(HeaderInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String uri = request.getRequestURI();

            if (uri.contains("/swagger") ||
                    uri.contains("/error") || uri.contains("/api-docs") || uri.contains("/documentations") ||
                    uri.contains("/versions") ||
                    Pattern.compile("/export/\\d+").matcher(uri).find() ||
                    (uri.contains("/documents") &&
                            !uri.equals("/api/v1/documents/import") &&
                            !uri.equals("/api/v1/documents/export"))
            ) {
                return true;
            }

            Map<String, Object> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                logger.debug(headerName + ": " + headerValue);
            }
            logger.info(USER_ID_HEADER + request.getHeader(USER_ID_HEADER));
            headers.put(USER_ID_HEADER, request.getHeader(USER_ID_HEADER).toString());
            logger.info(USER_PERMISSION_HEADER + toList(request.getHeader(USER_PERMISSION_HEADER)));
            headers.put(USER_PERMISSION_HEADER, toList(request.getHeader(USER_PERMISSION_HEADER).toString()));
            logger.info(USER_PRODUCTS_IDS_HEADER + toList(request.getHeader(USER_PRODUCTS_IDS_HEADER)));
            headers.put(USER_PRODUCTS_IDS_HEADER, toList(request.getHeader(USER_PRODUCTS_IDS_HEADER).toString()));
            logger.info(USER_ROLES_HEADER + toList(request.getHeader(USER_ROLES_HEADER)));
            headers.put(USER_ROLES_HEADER, toList(request.getHeader(USER_ROLES_HEADER).toString()));
            RequestContext.setHeaders(headers);
            logger.info("Set headers complete");
            return true;
        } catch (Exception e) {
            logger.info("403 Forbidden.");
            throw new ForbiddenException("403 Forbidden.");
        }
    }

    private List<String> toList(String value) {
        return Arrays.stream(value.split(","))
                .map(str -> str.substring(0))
                .map(str -> str.replaceAll("\"", ""))
                .map(str -> str.replaceAll("]", ""))
                .map(str -> str.replaceAll("\\[", ""))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
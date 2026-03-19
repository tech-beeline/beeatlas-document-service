/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.beeline.documentservice.utils.Constants.USER_ID_HEADER;
import static ru.beeline.documentservice.utils.Constants.USER_PERMISSION_HEADER;
import static ru.beeline.documentservice.utils.Constants.USER_PRODUCTS_IDS_HEADER;
import static ru.beeline.documentservice.utils.Constants.USER_ROLES_HEADER;

public class RequestContext {
    private static final ThreadLocal<Map<String, Object>> headersThreadLocal = new ThreadLocal<>();

    public static void setHeaders(Map<String, Object> headers) {
        headersThreadLocal.set(headers);
    }

    public static Map<String, Object> getHeaders() {
        return headersThreadLocal.get();
    }

    public static List<String> getUserPermissions() {
        return (List<String>) getHeaders().get(USER_PERMISSION_HEADER);
    }

    public static List<String> getRoles() {
        List<String> roles = (List<String>) getHeaders().get(USER_ROLES_HEADER);
        if (roles == null) {
            return new ArrayList<>();
        }
        return roles.stream()
                .map(role -> role.replaceAll("^[^a-zA-Z]+|[^a-zA-Z]+$", ""))
                .collect(Collectors.toList());
    }

    public static String getUserId() {
        return getHeaders().get(USER_ID_HEADER).toString();
    }

    public static List<Long> getUserProducts() {
        List<String> stringList = (List<String>) getHeaders().get(USER_PRODUCTS_IDS_HEADER);
        boolean hasValidEntries = stringList.stream()
                .anyMatch(s -> s != null && !s.trim().isEmpty());
        if (!hasValidEntries) {
            return Collections.emptyList();
        }
        List<Long> longList = stringList.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return longList;
    }
}

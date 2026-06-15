/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.beeline.documentservice.dto.UserInfoDto;

@Slf4j
@Service
public class AuthClient {

    private final RestTemplate restTemplate;
    private final String authServerUrl;

    public AuthClient(RestTemplate restTemplate, @Value("${integration.auth-server-url}") String authServerUrl) {
        this.restTemplate = restTemplate;
        this.authServerUrl = authServerUrl;
    }

    public UserInfoDto getUserInfo(Integer userId) {
        try {
            String url = authServerUrl + "/api/admin/v1/user/" + userId + "/user-info";
            return restTemplate.getForObject(url, UserInfoDto.class);
        } catch (Exception e) {
            log.error("Error fetching user info for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }
}

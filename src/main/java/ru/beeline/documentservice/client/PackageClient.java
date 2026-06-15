/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.beeline.documentservice.dto.PackageV2DTO;
import ru.beeline.documentservice.dto.PackageV2DTOPageWrapper;

import java.util.ArrayList;
import java.util.List;

import static ru.beeline.documentservice.utils.Constants.USER_ID_HEADER;

@Slf4j
@Service
public class PackageClient {

    private ObjectMapper objectMapper;
    RestTemplate restTemplate;
    private final String packLoaderServerUrl;

    public PackageClient(@Value("${integration.pack-loader-server-url}") String packLoaderServerUrl,
                         RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.packLoaderServerUrl = packLoaderServerUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<PackageV2DTO> getPackagesList(Integer userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(USER_ID_HEADER, userId != null ? userId.toString() : "");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = packLoaderServerUrl + "/api/v2/packages-list?source=excel";

            String response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
            PackageV2DTOPageWrapper pageWrapper = objectMapper.readValue(response, PackageV2DTOPageWrapper.class);
            return pageWrapper != null ? pageWrapper.toPage().getContent() : new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching packages list: ", e);
        }
        return null;
    }
}

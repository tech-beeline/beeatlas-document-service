package ru.beeline.documentservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.beeline.documentservice.dto.CamundaProcessRequestDTO;

@Slf4j
@Service
public class CamundaClient {
    RestTemplate restTemplate;
    private final String camundaUrl;

    public CamundaClient(
            @Value("${integration.camunda.server.url}") String camundaUrl,
            RestTemplate restTemplate) {
        this.camundaUrl = camundaUrl;
        this.restTemplate = restTemplate;
    }

    public String postCamunda(CamundaProcessRequestDTO requestBody) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<CamundaProcessRequestDTO> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    camundaUrl + "/engine-rest/process-definition/key/Process_0m2lqgf/start?async=true",
                    HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("Failed to start Camunda process: {}", response.getBody());
                return null;
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error occurred: {}", e.getStatusCode());
            log.error("Error body: {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Exception occurred: ", e);
        }
        return null;
        }
    }

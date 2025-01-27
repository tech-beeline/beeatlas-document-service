package ru.beeline.documentservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.beeline.documentservice.dto.CamundaProcessRequestDTO;

@Slf4j
@Service
public class CamundaClient {
    RestTemplate restTemplate;


    public CamundaClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String postCamunda(CamundaProcessRequestDTO requestBody) {

        return "Start process";
    }
}

package ru.beeline.documentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CamundaProcessRequestDTO {

    @JsonProperty("variables")
    private Map<String, CamundaVariableDTO> variables;

    @JsonProperty("businessKey")
    private Integer businessKey;
}

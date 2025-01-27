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

    private Map<String, CamundaVariableDTO> variables;

    private Integer businessKey;
}

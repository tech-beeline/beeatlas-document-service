package ru.beeline.documentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CamundaProcessRequestExportDTO {

    private Map<String, CamundaVariableDTO> variables;
    private String businessKey;
}

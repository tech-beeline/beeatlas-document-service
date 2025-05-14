package ru.beeline.documentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersionDTO {
    private Integer id;
    private String key;

    @JsonProperty("created_date")
    private LocalDateTime createdDate;
}

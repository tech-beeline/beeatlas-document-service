package ru.beeline.documentservice.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageV2DTO {

    private Integer packageId;
    private String operation;
    private String status;
    private Integer allParts;
    private Integer successParts;
    private Integer errorParts;
    private Integer processParts;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date createdDate;
    private String source;
    private Integer sourceId;
}

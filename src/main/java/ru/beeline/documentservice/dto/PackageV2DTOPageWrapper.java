/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageV2DTOPageWrapper {
    private List<PackageV2DTO> content;
    private int totalElements;
    private int totalPages;
    private int number;
    private int size;

    public Page<PackageV2DTO> toPage() {
        Pageable pageable = PageRequest.of(number, size);
        return new PageImpl<>(content, pageable, totalElements);
    }
}

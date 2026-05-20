/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "documentation_type", schema = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(name = "target_entity_type", nullable = false)
    private String targetEntityType;

    @Column(nullable = false)
    private Integer ttl;

    @Column(nullable = false)
    private String folder;

    @Column(name = "doc_type", nullable = false)
    private String docType;
}
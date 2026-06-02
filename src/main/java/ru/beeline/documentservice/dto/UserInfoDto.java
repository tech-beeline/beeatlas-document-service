/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.documentservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {
    private Integer id;
    private List<Long> productsIds;
    private List<String> roles;
    private List<String> permissions;
    private List<String> permissionNames;
}

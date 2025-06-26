package com.example.demo_project.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSimpleDto {
    private Integer id;
    private String firstName;
    private String surName;
    private String email;
    private Role role;
    private Boolean enabled;
    private Boolean active;
}
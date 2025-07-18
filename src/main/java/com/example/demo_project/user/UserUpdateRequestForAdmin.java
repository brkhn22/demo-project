package com.example.demo_project.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class UserUpdateRequestForAdmin {
    private String firstName;
    private String lastName;
    private String email;
    private Integer userId;
    private Integer departmentId;
    private String roleName;
    private Boolean enabled;
}

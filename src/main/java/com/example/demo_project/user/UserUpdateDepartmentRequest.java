package com.example.demo_project.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDepartmentRequest {
    private Integer userId;
    private Integer departmentId;
}

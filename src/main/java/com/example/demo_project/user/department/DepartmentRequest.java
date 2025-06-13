package com.example.demo_project.user.department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {
    private String name;
    private Integer companyId;
    private Integer typeId;
    private Integer townId;
    private String address;
}
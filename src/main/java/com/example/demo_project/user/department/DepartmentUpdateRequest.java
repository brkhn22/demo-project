package com.example.demo_project.user.department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentUpdateRequest {
    private Integer id;
    private String newName;
    private Integer newCompanyId;
    private Integer newTypeId;
    private Integer newTownId;
    private String newAddress;
}
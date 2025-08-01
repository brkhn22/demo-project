package com.example.demo_project.user.company.company_type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyTypeUpdateRequest {
    private String newName;
    private Integer id;
}

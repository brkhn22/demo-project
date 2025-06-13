package com.example.demo_project.user.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyUpdateRequest {
    private String newName;
    private String newAddress;
    private String newShortName;
    private Integer newTownId;
    private Integer newTypeId;
    private Integer id;

}

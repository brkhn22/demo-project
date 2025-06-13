package com.example.demo_project.user.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRequest {
    private String name;
    private String shortName;
    private String address;
    private Integer typeId;
    private Integer townId;
}

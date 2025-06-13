package com.example.demo_project.user.town;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRegionRequest {
    private Integer id;
    private String name;
    private Integer cityId;
}
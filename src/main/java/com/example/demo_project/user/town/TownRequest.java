package com.example.demo_project.user.town;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TownRequest {
    private String name;
    private Integer regionId;
    private Integer cityId;
}

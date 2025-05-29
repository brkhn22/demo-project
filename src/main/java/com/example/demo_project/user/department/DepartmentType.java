package com.example.demo_project.user.department;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Company_Type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentType {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Active")
    private Boolean active;

    @Column(name = "Created_At")
    private LocalDateTime createdAt;

    @Column(name = "created_At")
    private LocalDateTime deletedAt;
}

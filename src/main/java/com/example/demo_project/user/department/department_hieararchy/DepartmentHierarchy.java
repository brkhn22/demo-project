package com.example.demo_project.user.department.department_hieararchy;

import com.example.demo_project.user.department.Department;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Department_Hierarchy")
@IdClass(DepartmentHierarchyId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentHierarchy {

    @Id
    @ManyToOne
    @JoinColumn(name = "Parent_Department_ID", referencedColumnName = "ID")
    private Department parentDepartment;
    
    @Id
    @ManyToOne
    @JoinColumn(name = "Child_Department_ID", referencedColumnName = "ID")
    private Department childDepartment;
}

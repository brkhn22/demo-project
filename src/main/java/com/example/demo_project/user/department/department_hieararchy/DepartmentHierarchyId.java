package com.example.demo_project.user.department.department_hieararchy;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentHierarchyId implements Serializable {
    
    // These field names must match exactly with the field names in DepartmentHierarchy entity
    private Integer parentDepartment;  // Changed from Department to Integer
    private Integer childDepartment;   // Changed from Department to Integer

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DepartmentHierarchyId that = (DepartmentHierarchyId) o;
        return Objects.equals(parentDepartment, that.parentDepartment) &&
               Objects.equals(childDepartment, that.childDepartment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentDepartment, childDepartment);
    }
}

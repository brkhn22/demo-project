package com.example.demo_project.user.department.department_hieararchy;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo_project.user.department.Department;

public interface DepartmentHierarchyRepository extends JpaRepository<DepartmentHierarchy, DepartmentHierarchyId> {
    
    // Find all child departments of a parent
    Optional<List<DepartmentHierarchy>> findByParentDepartment(Department parentDepartment);
    
    // Find all parent departments of a child
    Optional<List<DepartmentHierarchy>> findByChildDepartment(Department childDepartment);
    
    // Find by parent department ID
    @Query("SELECT dh FROM DepartmentHierarchy dh WHERE dh.parentDepartment.id = :parentId")
    Optional<List<DepartmentHierarchy>> findByParentDepartmentId(@Param("parentId") Integer parentId);
    
    // Find by child department ID
    @Query("SELECT dh FROM DepartmentHierarchy dh WHERE dh.childDepartment.id = :childId")
    Optional<List<DepartmentHierarchy>> findByChildDepartmentId(@Param("childId") Integer childId);
    
    // Check if hierarchy relationship exists
    boolean existsByParentDepartmentAndChildDepartment(Department parentDepartment, Department childDepartment);
    
    // Get all children of a parent by parent ID
    @Query("SELECT dh.childDepartment FROM DepartmentHierarchy dh WHERE dh.parentDepartment.id = :parentId")
    Optional<List<Department>> findChildDepartmentsByParentId(@Param("parentId") Integer parentId);
    
    // Get all parents of a child by child ID
    @Query("SELECT dh.parentDepartment FROM DepartmentHierarchy dh WHERE dh.childDepartment.id = :childId")
    Optional<List<Department>> findParentDepartmentsByChildId(@Param("childId") Integer childId);
}

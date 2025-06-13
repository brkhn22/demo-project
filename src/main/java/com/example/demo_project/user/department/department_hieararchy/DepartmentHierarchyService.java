package com.example.demo_project.user.department.department_hieararchy;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo_project.user.department.Department;
import com.example.demo_project.user.department.DepartmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentHierarchyService {

    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final DepartmentRepository departmentRepository;

    public ResponseEntity<DepartmentHierarchy> addHierarchy(DepartmentHierarchyRequest request) {
        var parentDepartment = departmentRepository.findById(request.getParentDepartmentId())
                .orElseThrow(() -> new DepartmentHierarchyServiceException("Parent department not found with id: " + request.getParentDepartmentId()));

        var childDepartment = departmentRepository.findById(request.getChildDepartmentId())
                .orElseThrow(() -> new DepartmentHierarchyServiceException("Child department not found with id: " + request.getChildDepartmentId()));

        // Check if the relationship already exists
        if (departmentHierarchyRepository.existsByParentDepartmentAndChildDepartment(parentDepartment, childDepartment)) {
            throw new DepartmentHierarchyServiceException("Hierarchy relationship already exists between departments");
        }

        // Prevent self-referencing
        if (parentDepartment.getId().equals(childDepartment.getId())) {
            throw new DepartmentHierarchyServiceException("A department cannot be its own parent");
        }

        // Check for circular dependency
        if (wouldCreateCircularDependency(parentDepartment, childDepartment)) {
            throw new DepartmentHierarchyServiceException("This relationship would create a circular dependency");
        }

        DepartmentHierarchy hierarchy = DepartmentHierarchy.builder()
                .parentDepartment(parentDepartment)
                .childDepartment(childDepartment)
                .build();

        DepartmentHierarchy savedHierarchy = departmentHierarchyRepository.save(hierarchy);
        return ResponseEntity.ok().body(savedHierarchy);
    }

    public ResponseEntity<List<DepartmentHierarchy>> getChildDepartments(Integer parentId) {
        return ResponseEntity.ok().body(departmentHierarchyRepository.findByParentDepartmentId(parentId)
                .orElseThrow(() -> new DepartmentHierarchyServiceException("No child departments found for parent ID: " + parentId)));
    }

    public ResponseEntity<List<DepartmentHierarchy>> getParentDepartments(Integer childId) {
        return ResponseEntity.ok().body(departmentHierarchyRepository.findByChildDepartmentId(childId)
                .orElseThrow(() -> new DepartmentHierarchyServiceException("No parent departments found for child ID: " + childId)));
    }

    public ResponseEntity<List<Department>> getChildDepartmentsOnly(Integer parentId) {
        return ResponseEntity.ok().body(departmentHierarchyRepository.findChildDepartmentsByParentId(parentId)
                .orElseThrow(() -> new DepartmentHierarchyServiceException("No child departments found for parent ID: " + parentId)));
    }

    public ResponseEntity<List<Department>> getParentDepartmentsOnly(Integer childId) {
        return ResponseEntity.ok().body(departmentHierarchyRepository.findParentDepartmentsByChildId(childId)
                .orElseThrow(() -> new DepartmentHierarchyServiceException("No parent departments found for child ID: " + childId)));
    }

    public ResponseEntity<String> removeHierarchy(DepartmentHierarchyRequest request) {
        var parentDepartment = departmentRepository.findById(request.getParentDepartmentId())
                .orElseThrow(() -> new DepartmentHierarchyServiceException("Parent department not found with id: " + request.getParentDepartmentId()));

        var childDepartment = departmentRepository.findById(request.getChildDepartmentId())
                .orElseThrow(() -> new DepartmentHierarchyServiceException("Child department not found with id: " + request.getChildDepartmentId()));

        // Create the composite key using the IDs, not the entities
        DepartmentHierarchyId id = new DepartmentHierarchyId(
            request.getParentDepartmentId(), 
            request.getChildDepartmentId()
        );
        
        if (!departmentHierarchyRepository.existsById(id)) {
            throw new DepartmentHierarchyServiceException("Hierarchy relationship does not exist between these departments");
        }

        departmentHierarchyRepository.deleteById(id);
        return ResponseEntity.ok().body("Hierarchy relationship removed successfully");
    }

    public ResponseEntity<List<DepartmentHierarchy>> getAllHierarchies() {
        return ResponseEntity.ok().body(departmentHierarchyRepository.findAll());
    }

    private boolean wouldCreateCircularDependency(Department parent, Department child) {
        // Check if the parent department is already a child of the proposed child department
        List<Department> parentsOfParent = departmentHierarchyRepository.findParentDepartmentsByChildId(parent.getId()).orElse(List.of());
        return parentsOfParent.contains(child) || hasTransitiveParent(parent, child);
    }

    private boolean hasTransitiveParent(Department department, Department targetParent) {
        List<Department> parents = departmentHierarchyRepository.findParentDepartmentsByChildId(department.getId()).orElse(List.of());
        for (Department parent : parents) {
            if (parent.getId().equals(targetParent.getId()) || hasTransitiveParent(parent, targetParent)) {
                return true;
            }
        }
        return false;
    }
}

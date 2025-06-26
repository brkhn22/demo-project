package com.example.demo_project.user.department.department_hieararchy;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo_project.user.department.Department;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/department-hierarchy")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DepartmentHierarchyController {
    
    private final DepartmentHierarchyService departmentHierarchyService;

    @PostMapping("/add")
    public ResponseEntity<DepartmentHierarchy> addHierarchy(@RequestBody DepartmentHierarchyRequest request) {
        return departmentHierarchyService.addHierarchy(request);
    }

    @PostMapping("/get-children")
    public ResponseEntity<List<DepartmentHierarchy>> getChildDepartments(@RequestBody DepartmentIdRequest request) {
        return departmentHierarchyService.getChildDepartments(request.getId());
    }

    @PostMapping("/get-parents")
    public ResponseEntity<List<DepartmentHierarchy>> getParentDepartments(@RequestBody DepartmentIdRequest request) {
        return departmentHierarchyService.getParentDepartments(request.getId());
    }

    @PostMapping("/get-children-only")
    public ResponseEntity<List<Department>> getChildDepartmentsOnly(@RequestBody DepartmentIdRequest request) {
        return departmentHierarchyService.getChildDepartmentsOnly(request.getId());
    }

    @PostMapping("/get-parents-only")
    public ResponseEntity<List<Department>> getParentDepartmentsOnly(@RequestBody DepartmentIdRequest request) {
        return departmentHierarchyService.getParentDepartmentsOnly(request.getId());
    }

    @PostMapping("/remove")
    public ResponseEntity<String> removeHierarchy(@RequestBody DepartmentHierarchyRequest request) {
        return departmentHierarchyService.removeHierarchy(request);
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<DepartmentHierarchy>> getAllHierarchies() {
        return departmentHierarchyService.getAllHierarchies();
    }
}

package com.example.demo_project.user.department;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DepartmentController {
    
    private final DepartmentService departmentService;

    @PostMapping("/get-id")
    public ResponseEntity<Department> getDepartmentById(@RequestBody DepartmentIdRequest request) {
        return departmentService.getDepartmentById(request.getId());
    }

    @PostMapping("/get-name")
    public ResponseEntity<Department> getDepartmentByName(@RequestBody DepartmentNameRequest request) {
        return departmentService.getDepartmentByName(request.getName());
    }

    @PostMapping("/add")
    public ResponseEntity<Department> addDepartment(@RequestBody DepartmentRequest request) {
        return departmentService.addDepartment(request);
    }

    @PostMapping("/update")
    public ResponseEntity<Department> updateDepartment(@RequestBody DepartmentUpdateRequest request) {
        return departmentService.updateDepartment(request);
    }
    
    @PostMapping("/delete")
    public ResponseEntity<Department> deleteDepartmentById(@RequestBody DepartmentIdRequest request) {
        return departmentService.deleteDepartmentById(request.getId());
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return departmentService.getAllDepartments();
    }
}

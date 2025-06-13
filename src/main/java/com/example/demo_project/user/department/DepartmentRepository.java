package com.example.demo_project.user.department;


import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Optional<Department> findById(Integer id);
    Optional<Department> findByName(String name);
    Optional<List<Department>> findByCompanyId(Integer companyId);
    
    
}

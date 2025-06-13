package com.example.demo_project.user.department;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentTypeRepository extends JpaRepository<DepartmentType, Integer> {
    Optional<DepartmentType> findByName(String name);
}
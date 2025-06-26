package com.example.demo_project.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer>{
    Optional<User> findByEmail(String email);
    Optional<List<User>> findByDepartmentName(String departmentName);
    Optional<List<User>> findByDepartmentId(Integer departmentId);
}

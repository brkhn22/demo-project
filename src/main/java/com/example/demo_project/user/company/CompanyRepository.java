package com.example.demo_project.user.company;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Optional<Company> findByName(String name);
    Optional<Company> findById(Integer id);
    Optional<List<Company>> findByTypeId(Integer typeId);
}

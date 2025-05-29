package com.example.demo_project.user.company.company_type;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyTypeRepository extends JpaRepository<CompanyType, Integer> {

    public Optional<CompanyType> findByName(String name);
    public Optional<CompanyType> findById(Integer id);
}

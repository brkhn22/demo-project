package com.example.demo_project.user.town;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Integer> {
    
    Optional<City> findByName(String name);
    Optional<City> findById(Integer id);
}

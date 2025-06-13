package com.example.demo_project.user.town;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TownRepository extends JpaRepository<Town, Integer> {

    Optional<Town> findByName(String name);
    Optional<Town> findById(Integer id);
    Optional<List<Town>> findByRegionId(Integer regionId);
    
}

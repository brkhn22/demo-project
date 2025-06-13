package com.example.demo_project.user.town;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Integer> {

    Optional<Region> findByName(String name);
    Optional<Region> findById(Integer id);
    Optional<List<Region>> findByCityId(Integer cityId);
    Optional<List<Region>> findByCityName(String cityName);
}

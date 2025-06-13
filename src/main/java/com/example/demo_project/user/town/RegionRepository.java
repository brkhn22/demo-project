package com.example.demo_project.user.town;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RegionRepository extends JpaRepository<Region, Integer> {
    Optional<List<Region>> findByCityId(Integer cityId);
    Optional<List<Region>> findByCityName(String cityName);
    
    @Query("SELECT r FROM Region r WHERE r.city.id = :cityId AND r.deletedAt IS NULL")
    Optional<List<Region>> findActiveByCityId(Integer cityId);
    
    @Query("SELECT r FROM Region r WHERE r.city.name = :cityName AND r.deletedAt IS NULL")
    Optional<List<Region>> findActiveByCityName(String cityName);
}

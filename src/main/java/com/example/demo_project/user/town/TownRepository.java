package com.example.demo_project.user.town;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TownRepository extends JpaRepository<Town, Integer> {
    Optional<List<Town>> findByRegionId(Integer regionId);
    Optional<List<Town>> findByRegionIdAndRegionCityId(Integer regionId, Integer cityId);
    
    @Query("SELECT t FROM Town t WHERE t.region.id = :regionId AND t.deletedAt IS NULL")
    Optional<List<Town>> findActiveByRegionId(Integer regionId);
    
    @Query("SELECT t FROM Town t WHERE t.region.id = :regionId AND t.region.city.id = :cityId AND t.deletedAt IS NULL")
    Optional<List<Town>> findActiveByRegionIdAndRegionCityId(Integer regionId, Integer cityId);
}

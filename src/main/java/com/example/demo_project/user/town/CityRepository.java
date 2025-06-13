package com.example.demo_project.user.town;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CityRepository extends JpaRepository<City, Integer> {
    Optional<City> findByName(String name);
    
    @Query("SELECT c FROM City c WHERE c.deletedAt IS NULL")
    List<City> findAllActive();
    
    @Query("SELECT c FROM City c WHERE c.name = :name AND c.deletedAt IS NULL")
    Optional<City> findActiveByName(String name);
}

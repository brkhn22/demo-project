package com.example.demo_project.user.town;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TownService {

    private final TownRepository townRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    
    public ResponseEntity<City> getCityByName(String cityName) {
        return ResponseEntity.ok().body(cityRepository.findByName(cityName).orElseThrow(() -> 
            new TownServiceException("City not found with name: " + cityName)));
    }

    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok().body(cityRepository.findAll());
    }

    public ResponseEntity<List<Region>> getRegionsByCityId(Integer cityId) {
        return ResponseEntity.ok().body(regionRepository.findByCityId(cityId).orElseThrow(() -> 
            new TownServiceException("No regions found for city with ID: " + cityId)));
    }

    public ResponseEntity<List<Region>> getRegionsByCityName(String cityName) {
        return ResponseEntity.ok().body(regionRepository.findByCityName(cityName).orElseThrow(() -> 
            new TownServiceException("No regions found for city with name: " + cityName)));
    }

    public ResponseEntity<List<Town>> getTownsByRegionId(Integer regionId) {
        return ResponseEntity.ok().body(townRepository.findByRegionId(regionId).orElseThrow(() -> 
            new TownServiceException("No towns found for region with ID: " + regionId)));
    }
    
    public ResponseEntity<City> addCity(CityNameRequest request) {
        City city = City.builder()
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();
        cityRepository.save(city);
        return ResponseEntity.ok().body(city);
    }

    public ResponseEntity<Region> addRegion(RegionRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new TownServiceException("City not found with id: " + request.getCityId()));
        
        Region region = Region.builder()
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .city(city)
                .build();
        regionRepository.save(region);
        return ResponseEntity.ok().body(region);
    }

    public ResponseEntity<Town> addTown(TownRequest request) {
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new TownServiceException("Region not found with id: " + request.getRegionId()));
        Town town = Town.builder()
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .region(region)
                .build();
        townRepository.save(town);
        return ResponseEntity.ok().body(town);
    }

    public ResponseEntity<Town> updateTown(UpdateTownRequest request){
        
        Town town = townRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("Town not found with id: " + request.getId()));
        
        if(request.getName() != null && !request.getName().isEmpty()) 
            town.setName(request.getName());
        if(request.getRegionId() != null && request.getRegionId() > 0){
            var region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new TownServiceException("Region not found with id: " + request.getRegionId()));
            town.setRegion(region);
        }
        townRepository.save(town);
        return ResponseEntity.ok().body(town);
    }

    public ResponseEntity<Town> deleteTown(IdRequest request) {
        Town town = townRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("Town not found with id: " + request.getId()));
        townRepository.delete(town);
        return ResponseEntity.ok().body(town);
    }

    // City methods
    public ResponseEntity<City> updateCity(UpdateCityRequest request) {
        City city = cityRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("City not found with id: " + request.getId()));
        
        if(request.getName() != null && !request.getName().isEmpty()) {
            city.setName(request.getName());
        }
        
        cityRepository.save(city);
        return ResponseEntity.ok().body(city);
    }

    public ResponseEntity<City> deleteCity(IdRequest request) {
        City city = cityRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("City not found with id: " + request.getId()));
        cityRepository.delete(city);
        return ResponseEntity.ok().body(city);
    }

    // Region methods
    public ResponseEntity<Region> updateRegion(UpdateRegionRequest request) {
        Region region = regionRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("Region not found with id: " + request.getId()));
        
        if(request.getName() != null && !request.getName().isEmpty()) {
            region.setName(request.getName());
        }
        
        if(request.getCityId() != null && request.getCityId() > 0) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new TownServiceException("City not found with id: " + request.getCityId()));
            region.setCity(city);
        }
        
        regionRepository.save(region);
        return ResponseEntity.ok().body(region);
    }

    public ResponseEntity<Region> deleteRegion(IdRequest request) {
        Region region = regionRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("Region not found with id: " + request.getId()));
        regionRepository.delete(region);
        return ResponseEntity.ok().body(region);
    }
}

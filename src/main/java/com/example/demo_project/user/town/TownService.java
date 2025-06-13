package com.example.demo_project.user.town;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TownService {

    private final TownRepository townRepository;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    
    public ResponseEntity<CityResponse> getCityByName(String cityName) {
        City city = cityRepository.findActiveByName(cityName)
                .orElseThrow(() -> new TownServiceException("City not found with name: " + cityName));
        
        CityResponse response = CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .createdAt(city.getCreatedAt())
                .build();
        
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<List<CityResponse>> getAllCities() {
        List<City> cities = cityRepository.findAllActive();
        List<CityResponse> cityResponses = cities.stream()
                .map(city -> CityResponse.builder()
                        .id(city.getId())
                        .name(city.getName())
                        .createdAt(city.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok().body(cityResponses);
    }

    public ResponseEntity<List<RegionResponse>> getRegionsByCityId(Integer cityId) {
        List<Region> regions = regionRepository.findActiveByCityId(cityId)
                .orElseThrow(() -> new TownServiceException("No regions found for city with ID: " + cityId));
        
        List<RegionResponse> regionResponses = regions.stream()
                .map(region -> RegionResponse.builder()
                        .id(region.getId())
                        .name(region.getName())
                        .createdAt(region.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok().body(regionResponses);
    }

    public ResponseEntity<List<RegionResponse>> getRegionsByCityName(String cityName) {
        List<Region> regions = regionRepository.findActiveByCityName(cityName)
                .orElseThrow(() -> new TownServiceException("No regions found for city with name: " + cityName));
        
        List<RegionResponse> regionResponses = regions.stream()
                .map(region -> RegionResponse.builder()
                        .id(region.getId())
                        .name(region.getName())
                        .createdAt(region.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok().body(regionResponses);
    }

    public ResponseEntity<List<TownResponse>> getTownsByRegionId(Integer regionId) {
        List<Town> towns = townRepository.findActiveByRegionId(regionId)
                .orElseThrow(() -> new TownServiceException("No towns found for region with ID: " + regionId));
        
        List<TownResponse> townResponses = towns.stream()
                .map(town -> TownResponse.builder()
                        .id(town.getId())
                        .name(town.getName())
                        .createdAt(town.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok().body(townResponses);
    }

    public ResponseEntity<List<TownResponse>> getTownsByRegionIdAndCityId(Integer regionId, Integer cityId) {
        List<Town> towns = townRepository.findActiveByRegionIdAndRegionCityId(regionId, cityId)
                .orElseThrow(() -> new TownServiceException("No towns found for region with ID: " + regionId));
        
        List<TownResponse> townResponses = towns.stream()
                .map(town -> TownResponse.builder()
                        .id(town.getId())
                        .name(town.getName())
                        .createdAt(town.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok().body(townResponses);
    }
    
    public ResponseEntity<CityResponse> addCity(CityNameRequest request) {
        // Check if city name already exists (including soft-deleted ones)
        if (cityRepository.findByName(request.getName()).isPresent()) {
            throw new TownServiceException("City already exists with name: " + request.getName());
        }
        
        City city = City.builder()
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();
        
        City savedCity = cityRepository.save(city);
        
        CityResponse response = CityResponse.builder()
                .id(savedCity.getId())
                .name(savedCity.getName())
                .createdAt(savedCity.getCreatedAt())
                .build();
        
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<RegionResponse> addRegion(RegionRequest request) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new TownServiceException("City not found with id: " + request.getCityId()));
        
        Region region = Region.builder()
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .city(city)
                .build();
        
        Region savedRegion = regionRepository.save(region);
        
        RegionResponse response = RegionResponse.builder()
                .id(savedRegion.getId())
                .name(savedRegion.getName())
                .createdAt(savedRegion.getCreatedAt())
                .build();
        
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<TownResponse> addTown(TownRequest request) {
        var region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new TownServiceException("Region not found with id: " + request.getRegionId()));
        
        if (request.getCityId() != null && request.getCityId() > 0) {
                var city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new TownServiceException("City not found with id: " + request.getCityId()));
                if(region.getCity() == null || !region.getCity().getId().equals(city.getId())) {
                    throw new TownServiceException("Region does not belong to the specified city");
                }
        }

        Town town = Town.builder()
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .region(region)
                .build();
        
        Town savedTown = townRepository.save(town);
        
        TownResponse response = TownResponse.builder()
                .id(savedTown.getId())
                .name(savedTown.getName())
                .createdAt(savedTown.getCreatedAt())
                .build();
        
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<TownResponse> updateTown(UpdateTownRequest request) {
        Town town = townRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("Town not found with id: " + request.getId()));
        
        if(request.getName() != null && !request.getName().isEmpty()) 
            town.setName(request.getName());
        if(request.getRegionId() != null && request.getRegionId() > 0){
            Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new TownServiceException("Region not found with id: " + request.getRegionId()));
            town.setRegion(region);
        }
        
        Town updatedTown = townRepository.save(town);
        
        TownResponse response = TownResponse.builder()
                .id(updatedTown.getId())
                .name(updatedTown.getName())
                .createdAt(updatedTown.getCreatedAt())
                .build();
        
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<TownResponse> deleteTown(IdRequest request) {
        Town town = townRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("Town not found with id: " + request.getId()));
        
        // Check if already deleted
        if (town.getDeletedAt() != null) {
            throw new TownServiceException("Town with id " + request.getId() + " is already deleted");
        }
        
        // Soft delete - set deletedAt timestamp
        town.setDeletedAt(LocalDateTime.now());
        townRepository.save(town);
        
        TownResponse response = TownResponse.builder()
                .id(town.getId())
                .name(town.getName())
                .createdAt(town.getCreatedAt())
                .build();
        
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<CityResponse> updateCity(UpdateCityRequest request) {
        City city = cityRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("City not found with id: " + request.getId()));
        
        if(request.getName() != null && !request.getName().isEmpty()) {
            // Check if the new name already exists and is not the current city's name
            if (cityRepository.findByName(request.getName()).isPresent() &&
                !city.getName().equals(request.getName())) {
                throw new TownServiceException("City already exists with name: " + request.getName());
            }
            city.setName(request.getName());
        }
        
        City updatedCity = cityRepository.save(city);
        
        CityResponse response = CityResponse.builder()
                .id(updatedCity.getId())
                .name(updatedCity.getName())
                .createdAt(updatedCity.getCreatedAt())
                .build();
        
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<CityResponse> deleteCity(IdRequest request) {
        City city = cityRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("City not found with id: " + request.getId()));
        
        // Check if already deleted
        if (city.getDeletedAt() != null) {
            throw new TownServiceException("City with id " + request.getId() + " is already deleted");
        }
        
        // First, soft delete all regions belonging to this city
        List<Region> regions = regionRepository.findByCityId(request.getId()).orElse(List.of());
        for (Region region : regions) {
            // Only delete if not already deleted
            if (region.getDeletedAt() == null) {
                // Soft delete all towns belonging to this region
                List<Town> towns = townRepository.findByRegionId(region.getId()).orElse(List.of());
                for (Town town : towns) {
                    if (town.getDeletedAt() == null) {
                        town.setDeletedAt(LocalDateTime.now());
                        townRepository.save(town);
                    }
                }
                
                // Soft delete the region
                region.setDeletedAt(LocalDateTime.now());
                regionRepository.save(region);
            }
        }
        
        // Finally, soft delete the city
        city.setDeletedAt(LocalDateTime.now());
        cityRepository.save(city);
        
        CityResponse response = CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .createdAt(city.getCreatedAt())
                .build();
        
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<RegionResponse> updateRegion(UpdateRegionRequest request) {
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
        
        Region updatedRegion = regionRepository.save(region);
        
        RegionResponse response = RegionResponse.builder()
                .id(updatedRegion.getId())
                .name(updatedRegion.getName())
                .createdAt(updatedRegion.getCreatedAt())
                .build();
        
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<RegionResponse> deleteRegion(IdRequest request) {
        Region region = regionRepository.findById(request.getId())
                .orElseThrow(() -> new TownServiceException("Region not found with id: " + request.getId()));
        
        // Check if already deleted
        if (region.getDeletedAt() != null) {
            throw new TownServiceException("Region with id " + request.getId() + " is already deleted");
        }
        
        // First, soft delete all towns belonging to this region
        List<Town> towns = townRepository.findByRegionId(request.getId()).orElse(List.of());
        for (Town town : towns) {
            // Only delete if not already deleted
            if (town.getDeletedAt() == null) {
                town.setDeletedAt(LocalDateTime.now());
                townRepository.save(town);
            }
        }
        
        // Then, soft delete the region
        region.setDeletedAt(LocalDateTime.now());
        regionRepository.save(region);
        
        RegionResponse response = RegionResponse.builder()
                .id(region.getId())
                .name(region.getName())
                .createdAt(region.getCreatedAt())
                .build();
        
        return ResponseEntity.ok().body(response);
    }
}

package com.example.demo_project.user.town;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/town")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TownController {

    private final TownService townService;
    
    // City endpoints
    @GetMapping("/get-all-cities")
    public ResponseEntity<List<City>> getAllCities() {
        return townService.getAllCities();
    }
    
    @GetMapping("/get-city-by-name")
    public ResponseEntity<City> getCityByName(@RequestParam String cityName) {
        return townService.getCityByName(cityName);
    }
    
    @PostMapping("/add-city")
    public ResponseEntity<City> addCity(@RequestBody CityNameRequest request) {
        return townService.addCity(request);
    }
    
    @PostMapping("/update-city")
    public ResponseEntity<City> updateCity(@RequestBody UpdateCityRequest request) {
        return townService.updateCity(request);
    }
    
    @PostMapping("/delete-city")
    public ResponseEntity<City> deleteCity(@RequestBody IdRequest request) {
        return townService.deleteCity(request);
    }
    
    // Region endpoints
    @GetMapping("/get-regions-by-city-id")
    public ResponseEntity<List<Region>> getRegionsByCityId(@RequestParam IdRequest request) {
        return townService.getRegionsByCityId(request.getId());
    }
    
    @PostMapping("/get-regions-by-city-name")
    public ResponseEntity<List<Region>> getRegionsByCityName(@RequestBody CityNameRequest request) {
        return townService.getRegionsByCityName(request.getName());
    }
    
    @PostMapping("/add-region")
    public ResponseEntity<Region> addRegion(@RequestBody RegionRequest request) {
        return townService.addRegion(request);
    }
    
    @PostMapping("/update-region")
    public ResponseEntity<Region> updateRegion(@RequestBody UpdateRegionRequest request) {
        return townService.updateRegion(request);
    }
    
    @PostMapping("/delete-region")
    public ResponseEntity<Region> deleteRegion(@RequestBody IdRequest request) {
        return townService.deleteRegion(request);
    }
    
    // Town endpoints
    @GetMapping("/get-towns-by-region-id")
    public ResponseEntity<List<Town>> getTownsByRegionId(@RequestParam Integer regionId) {
        return townService.getTownsByRegionId(regionId);
    }
    
    @PostMapping("/add-town")
    public ResponseEntity<Town> addTown(@RequestBody TownRequest request) {
        return townService.addTown(request);
    }
    
    @PostMapping("/update-town")
    public ResponseEntity<Town> updateTown(@RequestBody UpdateTownRequest request) {
        return townService.updateTown(request);
    }
    
    @PostMapping("/delete-town")
    public ResponseEntity<Town> deleteTown(@RequestBody IdRequest request) {
        return townService.deleteTown(request);
    }
}

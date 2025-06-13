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
@RequestMapping("/admin/town")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TownController {

    private final TownService townService;
    
    // City endpoints
    @GetMapping("/get-all-cities")
    public ResponseEntity<List<CityResponse>> getAllCities() {
        return townService.getAllCities();
    }
    
    @GetMapping("/get-city-by-name")
    public ResponseEntity<CityResponse> getCityByName(@RequestParam String cityName) {
        return townService.getCityByName(cityName);
    }
    
    @PostMapping("/add-city")
    public ResponseEntity<CityResponse> addCity(@RequestBody CityNameRequest request) {
        return townService.addCity(request);
    }
    
    @PostMapping("/update-city")
    public ResponseEntity<CityResponse> updateCity(@RequestBody UpdateCityRequest request) {
        return townService.updateCity(request);
    }
    
    @PostMapping("/delete-city")
    public ResponseEntity<CityResponse> deleteCity(@RequestBody IdRequest request) {
        return townService.deleteCity(request);
    }
    
    // Region endpoints
    @GetMapping("/get-regions-by-city-id")
    public ResponseEntity<List<RegionResponse>> getRegionsByCityId(@RequestParam Integer cityId) {
        return townService.getRegionsByCityId(cityId);
    }
    
    @PostMapping("/get-regions-by-city-name")
    public ResponseEntity<List<RegionResponse>> getRegionsByCityName(@RequestParam String cityName) {
        return townService.getRegionsByCityName(cityName);
    }
    
    @PostMapping("/add-region")
    public ResponseEntity<RegionResponse> addRegion(@RequestBody RegionRequest request) {
        return townService.addRegion(request);
    }
    
    @PostMapping("/update-region")
    public ResponseEntity<RegionResponse> updateRegion(@RequestBody UpdateRegionRequest request) {
        return townService.updateRegion(request);
    }
    
    @PostMapping("/delete-region")
    public ResponseEntity<RegionResponse> deleteRegion(@RequestBody IdRequest request) {
        return townService.deleteRegion(request);
    }
    
    // Town endpoints
    @GetMapping("/get-towns-by-region-id-city-id")
    public ResponseEntity<List<TownResponse>> getTownsByRegionIdAndCityId(
        @RequestParam Integer regionId,
        @RequestParam Integer cityId) {
        return townService.getTownsByRegionIdAndCityId(regionId, cityId);
    }
    
    @PostMapping("/add-town")
    public ResponseEntity<TownResponse> addTown(@RequestBody TownRequest request) {
        return townService.addTown(request);
    }
    
    @PostMapping("/update-town")
    public ResponseEntity<TownResponse> updateTown(@RequestBody UpdateTownRequest request) {
        return townService.updateTown(request);
    }
    
    @PostMapping("/delete-town")
    public ResponseEntity<TownResponse> deleteTown(@RequestBody IdRequest request) {
        return townService.deleteTown(request);
    }
}

package com.example.demo_project.user.company.company_type;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/company-type")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompanyTypeController {
    
    private final CompanyTypeService companyTypeService;

    @PostMapping("/get-by-id")
    public ResponseEntity<CompanyType> getCompanyTypeById(@RequestBody CompanyTypeIdRequest request) {
        return companyTypeService.getCompanyTypeById(request.getTypeId());
    }

    @PostMapping("/get-by-name")
    public ResponseEntity<CompanyType> getCompanyTypeByName(@RequestBody CompanyTypeNameRequest request) {
        return companyTypeService.getCompanyTypeByName(request.getName());
    }

    @PostMapping("/create")
    public ResponseEntity<CompanyType> createCompanyType(@RequestBody CreateCompanyTypeRequest companyType) {
        return companyTypeService.createCompanyType(companyType);
    }

    @PostMapping("/soft-delete")
    public ResponseEntity<CompanyType> softDeleteCompanyTypeById(@RequestBody CompanyTypeIdRequest request) {
        return companyTypeService.softDeleteCompanyTypeById(request.getTypeId());
    }

    @DeleteMapping ("/delete")
    public ResponseEntity<CompanyType> deleteCompanyTypeById(@RequestBody CompanyTypeIdRequest request) {
        return companyTypeService.deleteCompanyTypeById(request.getTypeId());
    }

    @PostMapping("/update")
    public ResponseEntity<CompanyType> updateCompanyType(@RequestBody CompanyTypeUpdateRequest request) {
        return companyTypeService.updateCompanyType(request);
    }
    
    @GetMapping("/get-all")
    public ResponseEntity<List<CompanyType>> getAllCompanyTypes() {
        return companyTypeService.getAllCompanyTypes();
    }
}

package com.example.demo_project.user.company;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo_project.user.company.company_type.CompanyTypeIdRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompanyController {
    
    private final CompanyService companyService;

    @PostMapping("/get-id")
    public ResponseEntity<Company> getCompanyById(@RequestBody CompanyIdRequest request) {
        return companyService.getCompanyById(request.getId());
    }

    @PostMapping("/get-name")
    public ResponseEntity<Company> getCompanyByName(@RequestBody CompanyNameRequest request) {
        return companyService.getCompanyByName(request.getName());
    }

    @PostMapping("/get-type-id")
    public ResponseEntity<List<Company>> getCompaniesByTypeId(@RequestBody CompanyTypeIdRequest request) {
        return companyService.getCompaniesByTypeId(request.getTypeId());
    }

    @PostMapping("/add")
    public ResponseEntity<Company> addCompany(@RequestBody CompanyRequest company) {
        return companyService.addCompany(company);
    }

    @PostMapping("/update")
    public ResponseEntity<Company> updateCompany(@RequestBody CompanyUpdateRequest companyUpdateRequest) {
        return companyService.updateCompany(companyUpdateRequest);
    }

    @DeleteMapping("/soft-delete")
    public ResponseEntity<Company> softDeleteCompanyById(@RequestBody CompanyIdRequest request) {
        return companyService.softDeleteCompanyById(request.getId());
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<Company>> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        return companyService.getAllCompanies(page, size);
    }
}

package com.example.demo_project.user.company;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
    @PostMapping("/delete")
    public ResponseEntity<Company> deleteCompanyById(@RequestBody CompanyIdRequest request) {
        return companyService.deleteCompanyById(request.getId());
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<Company>> getAllCompanies() {
        return companyService.getAllCompanies();
    }
}

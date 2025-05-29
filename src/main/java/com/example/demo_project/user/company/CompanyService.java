package com.example.demo_project.user.company;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo_project.user.company.company_type.CompanyType;
import com.example.demo_project.user.company.company_type.CompanyTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyTypeRepository companyTypeRepository;

    public ResponseEntity<Company> getCompanyById(Integer id) {
        return ResponseEntity.ok().body(companyRepository.findById(id)
                .orElseThrow(() -> new CompanyServiceException("Company not found with id: " + id)));
    }

    public ResponseEntity<Company> getCompanyByName(String name) {
        return ResponseEntity.ok().body(companyRepository.findByName(name)
                .orElseThrow(() -> new CompanyServiceException("Company not found with name: " + name)));
    }

    public ResponseEntity<List<Company>> getCompaniesByTypeId(Integer companyTypeId) {
        return ResponseEntity.ok().body(companyRepository.findByTypeId(companyTypeId)
                .orElseThrow(() -> new CompanyServiceException("No companies found for type ID: " + companyTypeId)));
    }
    public ResponseEntity<Company> createCompany(CreateCompanyRequest company) {
        if (companyRepository.findByName(company.getName()).isPresent()) {
            throw new CompanyServiceException("Company already exists with name: " + company.getName());
        }
        CompanyType companyType = companyTypeRepository.findById(company.getTypeId())
                .orElseThrow(() -> new CompanyServiceException("Company Type not found with id: " + company.getTypeId()));
        Company newCompany = Company.builder()
                .name(company.getName())
                .type(companyType)
                .active(true)
                .createdAt(LocalDateTime.now())
                .deletedAt(null)
                .build();
        return ResponseEntity.ok().body(companyRepository.save(newCompany));
    }

    public ResponseEntity<Company> updateCompany(CompanyUpdateRequest companyUpdateRequest) {
        Company existingCompany = companyRepository.findById(companyUpdateRequest.getId())
                .orElseThrow(() -> new CompanyServiceException("Company not found with id: " + companyUpdateRequest.getId()));
        
        if(companyUpdateRequest.getNewName() != null) {
            if (companyRepository.findByName(companyUpdateRequest.getNewName()).isPresent() &&
                !existingCompany.getName().equals(companyUpdateRequest.getNewName())) {
                throw new CompanyServiceException("Company already exists with name: " + companyUpdateRequest.getNewName());
            }
            existingCompany.setName(companyUpdateRequest.getNewName());
        }

        if(companyUpdateRequest.getNewTypeId() != null){
            CompanyType type = companyTypeRepository.findById(companyUpdateRequest.getNewTypeId())
                .orElseThrow(() -> new CompanyServiceException("Company Type not found with id: " + companyUpdateRequest.getNewTypeId()));
            existingCompany.setType(type);

        }
        if(companyUpdateRequest.getNewAddress() != null){
            existingCompany.setAddress(companyUpdateRequest.getNewAddress());
        }
        if(companyUpdateRequest.getNewShortName() != null){
            existingCompany.setShortName(companyUpdateRequest.getNewShortName());
        }
        
        return ResponseEntity.ok().body(companyRepository.save(existingCompany));
    }

    public ResponseEntity<Company> deleteCompanyById(Integer id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyServiceException("Company not found with id: " + id));
        company.setActive(false);
        company.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok().body(companyRepository.save(company));
    }

        public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok().body(companyRepository.findAll());
    }
}

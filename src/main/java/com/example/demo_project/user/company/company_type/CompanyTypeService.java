package com.example.demo_project.user.company.company_type;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyTypeService {

    private final CompanyTypeRepository companyTypeRepository;

    public ResponseEntity<CompanyType> getCompanyTypeById(Integer id) {
        return ResponseEntity.ok().body(companyTypeRepository.findById(id)
                .orElseThrow(() -> new CompanyTypeException("Company Type not found with id: " + id)));
    }

    public ResponseEntity<CompanyType> getCompanyTypeByName(String name) {
        return ResponseEntity.ok().body(companyTypeRepository.findByName(name)
                .orElseThrow(() -> new CompanyTypeException("Company Type not found with name: " + name)));
    }

    public ResponseEntity<CompanyType> createCompanyType(CreateCompanyTypeRequest companyType) {
        if (companyTypeRepository.findByName(companyType.getName()).isPresent()) {
            throw new CompanyTypeException("Company Type already exists with name: " + companyType.getName());
        }
        CompanyType type = CompanyType.builder()
                .name(companyType.getName())
                .active(true)
                .createdAt(LocalDateTime.now())
                .deletedAt(null)
                .build();

        return ResponseEntity.ok().body(companyTypeRepository.save(type));
    }

    public ResponseEntity<List<CompanyType>> getAllCompanyTypes() {
        return ResponseEntity.ok().body(companyTypeRepository.findAll());
    }

    public ResponseEntity<CompanyType> deleteCompanyTypeById(Integer id) {
        CompanyType companyType = companyTypeRepository.findById(id)
                .orElseThrow(() -> new CompanyTypeException("Company Type not found with id: " + id));
        companyType.setActive(false);
        companyType.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok().body(companyTypeRepository.save(companyType));
    }

    public ResponseEntity<CompanyType> updateCompanyType(CompanyTypeUpdateRequest request) {
        CompanyType existingCompanyType = companyTypeRepository.findById(request.getId())
                .orElseThrow(() -> new CompanyTypeException("Company Type not found with id: " + request.getId()));
        
        existingCompanyType.setName(request.getNewName());
        existingCompanyType.setActive(true);
        
        return ResponseEntity.ok().body(companyTypeRepository.save(existingCompanyType));
    }
}

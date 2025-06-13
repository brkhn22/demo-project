package com.example.demo_project.user.company;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo_project.user.company.company_type.CompanyType;
import com.example.demo_project.user.company.company_type.CompanyTypeRepository;
import com.example.demo_project.user.town.TownRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyTypeRepository companyTypeRepository;
    private final TownRepository townRepository;

    public ResponseEntity<Company> addCompany(CompanyRequest request){
        if (companyRepository.findByName(request.getName()).isPresent()) 
            throw new CompanyServiceException("Company already exists with name: " + request.getName());
        
        
        CompanyType companyType = companyTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new CompanyServiceException("Company Type not found with id: " + request.getTypeId()));
        
        var town = townRepository.findById(request.getTownId())
                    .orElseThrow(() -> new RuntimeException());

        Company newCompany = Company.builder()
                .name(request.getName())
                .shortName(request.getShortName())
                .type(companyType)
                .address(request.getAddress())
                .active(true)
                .town(town)
                .createdAt(LocalDateTime.now())
                .deletedAt(null)
                .build();
        
        Company savedCompany = companyRepository.save(newCompany);
        return ResponseEntity.ok().body(savedCompany);
    }

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

    public ResponseEntity<Company> updateCompany(CompanyUpdateRequest companyUpdateRequest) {
        Company existingCompany = companyRepository.findById(companyUpdateRequest.getId())
                .orElseThrow(() -> new CompanyServiceException("Company not found with id: " + companyUpdateRequest.getId()));
        
        if(companyUpdateRequest.getNewName() != null && !companyUpdateRequest.getNewName().isEmpty()) {
            if (companyRepository.findByName(companyUpdateRequest.getNewName()).isPresent() &&
                !existingCompany.getName().equals(companyUpdateRequest.getNewName())) {
                throw new CompanyServiceException("Company already exists with name: " + companyUpdateRequest.getNewName());
            }
            existingCompany.setName(companyUpdateRequest.getNewName());
        }

        if(companyUpdateRequest.getNewTypeId() != null && companyUpdateRequest.getNewTypeId() > 0){
            CompanyType type = companyTypeRepository.findById(companyUpdateRequest.getNewTypeId())
                .orElseThrow(() -> new CompanyServiceException("Company Type not found with id: " + companyUpdateRequest.getNewTypeId()));
            existingCompany.setType(type);

        }

        if(companyUpdateRequest.getNewAddress() != null && !companyUpdateRequest.getNewAddress().isEmpty()){
            existingCompany.setAddress(companyUpdateRequest.getNewAddress());
        }

        if(companyUpdateRequest.getNewShortName() != null && !companyUpdateRequest.getNewShortName().isEmpty()){
            existingCompany.setShortName(companyUpdateRequest.getNewShortName());
        }
        
        if(companyUpdateRequest.getNewTownId() != null &&
            companyUpdateRequest.getNewTownId() > 0 &&
            companyUpdateRequest.getNewTownId() != existingCompany.getId()){
            var town = townRepository.findById(companyUpdateRequest.getNewTownId())
                        .orElseThrow(()-> new CompanyServiceException("New Town not found with id: "+companyUpdateRequest.getNewTownId()));
            existingCompany.setTown(town);
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

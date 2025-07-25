package com.example.demo_project.user.company;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo_project.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo_project.user.company.company_type.CompanyType;
import com.example.demo_project.user.company.company_type.CompanyTypeRepository;
import com.example.demo_project.user.town.Town;
import com.example.demo_project.user.town.TownRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyTypeRepository companyTypeRepository;
    private final TownRepository townRepository;

    public ResponseEntity<Company> addCompany(CompanyRequest request){
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getRole().getName().equals("Admin"))
            throw new  CompanyServiceException("Only Admins can create a company");

        if (companyRepository.findByName(request.getName()).isPresent())
            throw new CompanyServiceException("Company already exists with name: " + request.getName());
        
        CompanyType companyType = companyTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new CompanyServiceException("Company Type not found with id: " + request.getTypeId()));
        
        Town town = townRepository.findById(request.getTownId())
                .orElseThrow(() -> new CompanyServiceException("Town not found with id: " + request.getTownId()));
        
        // Check if town is active
        if (town.getDeletedAt() != null) {
            throw new CompanyServiceException("Cannot use deleted town with id: " + request.getTownId());
        }

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
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getRole().getName().equals("Admin") && !user.getRole().getName().equals("Manager"))
            throw new  CompanyServiceException("Only Admins and Managers can read companies");

        return ResponseEntity.ok().body(companyRepository.findById(id)
                .orElseThrow(() -> new CompanyServiceException("Company not found with id: " + id)));
    }

    public ResponseEntity<Company> getCompanyByName(String name) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getRole().getName().equals("Admin") && !user.getRole().getName().equals("Manager"))
            throw new  CompanyServiceException("Only Admins and Managers can read companies");

        return ResponseEntity.ok().body(companyRepository.findByName(name)
                .orElseThrow(() -> new CompanyServiceException("Company not found with name: " + name)));
    }

    public ResponseEntity<List<Company>> getCompaniesByTypeId(Integer companyTypeId) {
        return ResponseEntity.ok().body(companyRepository.findByTypeId(companyTypeId)
                .orElseThrow(() -> new CompanyServiceException("No companies found for type ID: " + companyTypeId)));
    }

    public ResponseEntity<Company> updateCompany(CompanyUpdateRequest companyUpdateRequest) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getRole().getName().equals("Admin"))
            throw new  CompanyServiceException("Only Admins can soft update a company");

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
            companyUpdateRequest.getNewTownId() != existingCompany.getTown().getId()){
            Town town = townRepository.findById(companyUpdateRequest.getNewTownId())
                        .orElseThrow(()-> new CompanyServiceException("New Town not found with id: "+companyUpdateRequest.getNewTownId()));
            
            // Check if town is active
            if (town.getDeletedAt() != null) {
                throw new CompanyServiceException("Cannot use deleted town with id: " + companyUpdateRequest.getNewTownId());
            }
            
            existingCompany.setTown(town);
        }
        return ResponseEntity.ok().body(companyRepository.save(existingCompany));
    }

    public ResponseEntity<Company> softDeleteCompanyById(Integer id) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getRole().getName().equals("Admin"))
            throw new  CompanyServiceException("Only Admins can soft delete a company");
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyServiceException("Company not found with id: " + id));
        company.setActive(false);
        company.setDeletedAt(LocalDateTime.now());
        return ResponseEntity.ok().body(companyRepository.save(company));
    }

    public ResponseEntity<Company> deleteCompanyById(Integer id) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getRole().getName().equals("Admin"))
            throw new  CompanyServiceException("Only Admins can delete a company");

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyServiceException("Company not found with id: " + id));
        companyRepository.delete(company);
        return ResponseEntity.ok().body(company);
    }

    public ResponseEntity<List<Company>> getAllCompanies(int page, int size) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getRole().getName().equals("Admin") && !user.getRole().getName().equals("Manager"))
            throw new  CompanyServiceException("Only Admins and Managers can read companies");
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok().body(companyRepository.findAll(pageable).toList());
    }
}

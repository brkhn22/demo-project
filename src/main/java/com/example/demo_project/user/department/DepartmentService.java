package com.example.demo_project.user.department;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo_project.user.company.CompanyServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.demo_project.user.User;
import com.example.demo_project.user.UserRepository;
import com.example.demo_project.user.company.Company;
import com.example.demo_project.user.company.CompanyRepository;
import com.example.demo_project.user.town.Town;
import com.example.demo_project.user.town.TownRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentTypeRepository departmentTypeRepository;
    private final TownRepository townRepository;
    private final UserRepository userRepository;

    public ResponseEntity<Department> addDepartment(DepartmentRequest request) {
        if(!isCurrrentUserAdmin())
            throw new DepartmentServiceException("Only Admins can create a department");

        if (departmentRepository.findByName(request.getName()).isPresent()) {
            throw new DepartmentServiceException("Department already exists with name: " + request.getName());
        }

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new DepartmentServiceException("Company not found with id: " + request.getCompanyId()));

        DepartmentType departmentType = departmentTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new DepartmentServiceException("Department Type not found with id: " + request.getTypeId()));

        Town town = townRepository.findById(request.getTownId())
                .orElseThrow(() -> new DepartmentServiceException("Town not found with id: " + request.getTownId()));

        // Check if town is active
        if (town.getDeletedAt() != null) {
            throw new DepartmentServiceException("Cannot use deleted town with id: " + request.getTownId());
        }

        Department newDepartment = Department.builder()
                .name(request.getName())
                .company(company)
                .type(departmentType)
                .town(town)
                .address(request.getAddress())
                .active(true)
                .createdAt(LocalDateTime.now())
                .deletedAt(null)
                .build();

        Department savedDepartment = departmentRepository.save(newDepartment);
        return ResponseEntity.ok().body(savedDepartment);
    }

    public ResponseEntity<Department> getDepartmentById(Integer id) {
        if(!isCurrrentUserAdminOrManager())
            throw new DepartmentServiceException("Only Admins and Managers can get a department");

        return ResponseEntity.ok().body(departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentServiceException("Department not found with id: " + id)));
    }

    public ResponseEntity<Department> getDepartmentByName(String name) {
        if(!isCurrrentUserAdminOrManager())
            throw new DepartmentServiceException("Only Admins and Managers can get a department");

        return ResponseEntity.ok().body(departmentRepository.findByName(name)
                .orElseThrow(() -> new DepartmentServiceException("Department not found with name: " + name)));
    }

    public ResponseEntity<Department> updateDepartment(DepartmentUpdateRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails)
            username = ((UserDetails) principal).getUsername();

        if(username == null)
            throw new DepartmentServiceException("Username is not valid.");

        var user = userRepository.findByEmail(username).orElseThrow();

        if(user.getRole().getName().equals("Manager")){
            if(!user.getDepartment().getId().equals(request.getId()))
                throw new DepartmentServiceException("Manager can only update their own department.");
        }else if (!user.getRole().getName().equals("Admin"))
            throw new DepartmentServiceException("Only Admin and Managers can update a department.");

        Department existingDepartment = departmentRepository.findById(request.getId())
                .orElseThrow(() -> new DepartmentServiceException("Department not found with id: " + request.getId()));

        if(!existingDepartment.getActive() || existingDepartment.getDeletedAt() != null)
            throw new DepartmentServiceException("Cannot update a deleted department.");

        if (request.getNewName() != null && !request.getNewName().isEmpty()) {
            if (departmentRepository.findByName(request.getNewName()).isPresent() &&
                !existingDepartment.getName().equals(request.getNewName())) {
                throw new DepartmentServiceException("Department already exists with name: " + request.getNewName());
            }
            existingDepartment.setName(request.getNewName());
        }

        if (request.getNewCompanyId() != null && request.getNewCompanyId() > 0) {
            Company company = companyRepository.findById(request.getNewCompanyId())
                    .orElseThrow(() -> new DepartmentServiceException("Company not found with id: " + request.getNewCompanyId()));
            existingDepartment.setCompany(company);
        }

        if (request.getNewTypeId() != null && request.getNewTypeId() > 0) {
            DepartmentType type = departmentTypeRepository.findById(request.getNewTypeId())
                    .orElseThrow(() -> new DepartmentServiceException("Department Type not found with id: " + request.getNewTypeId()));
            existingDepartment.setType(type);
        }

        if (request.getNewTownId() != null && request.getNewTownId() > 0) {
            Town town = townRepository.findById(request.getNewTownId())
                    .orElseThrow(() -> new DepartmentServiceException("Town not found with id: " + request.getNewTownId()));
            
            // Check if town is active
            if (town.getDeletedAt() != null) {
                throw new DepartmentServiceException("Cannot use deleted town with id: " + request.getNewTownId());
            }
            
            existingDepartment.setTown(town);
        }

        if (request.getNewAddress() != null && !request.getNewAddress().isEmpty()) {
            existingDepartment.setAddress(request.getNewAddress());
        }

        return ResponseEntity.ok().body(departmentRepository.save(existingDepartment));
    }

    public ResponseEntity<Department> softDeleteDepartmentById(Integer id) {
        if(!isCurrrentUserAdmin())
            throw new DepartmentServiceException("Only Admins can delete department");

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentServiceException("Department not found with id: " + id));
        department.setActive(false);
        department.setDeletedAt(LocalDateTime.now());
        var users = userRepository.findByDepartmentId(id);
        if(users.isPresent()){
            for (User user : users.get()) {
                user.setDeletedAt(LocalDateTime.now());
                user.setEnabled(false);
                userRepository.save(user);
            }
        }
        return ResponseEntity.ok().body(departmentRepository.save(department));
    }

    public ResponseEntity<Department> deleteDepartmentById(Integer id) {
        if(!isCurrrentUserAdmin())
            throw new DepartmentServiceException("Only Admins can delete department");

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentServiceException("Department not found with id: " + id));

        department.setActive(false);
        department.setDeletedAt(LocalDateTime.now());
        var users = userRepository.findByDepartmentId(id);
        users.ifPresent(userRepository::deleteAll);
        departmentRepository.delete(department);

        return ResponseEntity.ok().body(department);
    }

    public ResponseEntity<List<Department>> getAllDepartments(int page, int size) {
        if(!isCurrrentUserAdminOrManager())
            throw new DepartmentServiceException("Only Admins and Managers can get all departments");
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok().body(departmentRepository.findAll(pageable).toList());
    }

    private boolean isCurrrentUserAdmin(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails)
            username = ((UserDetails) principal).getUsername();

        if(username == null)
            throw new DepartmentServiceException("Username is not valid.");

        var user = userRepository.findByEmail(username).orElseThrow();
        return user.getRole().getName().equals("Admin");
    }

    private boolean isCurrrentUserAdminOrManager(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails)
            username = ((UserDetails) principal).getUsername();

        if(username == null)
            throw new DepartmentServiceException("Username is not valid.");

        var user = userRepository.findByEmail(username).orElseThrow();
        return user.getRole().getName().equals("Admin") || user.getRole().getName().equals("Manager");
    }
}

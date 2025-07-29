package com.example.demo_project.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.demo_project.auth.Validator;
import com.example.demo_project.user.department.DepartmentServiceException;
import com.example.demo_project.user.department.department_hieararchy.DepartmentHierarchy;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.demo_project.user.department.Department;
import com.example.demo_project.user.department.DepartmentRepository;
import com.example.demo_project.user.department.department_hieararchy.DepartmentHierarchyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    public ResponseEntity<User> getSelf(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            throw new UserServiceException("User not found in security context.");
        }
        
        return ResponseEntity.ok()
                .body(user);
    }

    public ResponseEntity<User> getById(int id){
        var currentUser = getCurrrentUserIfAdminOrManager();
        if(currentUser == null)
            throw new UserServiceException("This user is not authorized to read users.");

        if(id <= 0)
            throw new UserServiceException("User ID cannot be less than or equal to zero.");

        var targetUser = userRepository.findById(id).orElseThrow(() -> new UserServiceException("User with id " + id + " not found."));

        if(currentUser.getRole().getName().equals("Manager")){
            if(!currentUser.getDepartment().getId().equals(id)){
                List<Department> depts = getChildDepartments(currentUser.getDepartment().getId())
                        .stream()
                        .filter(department->department.getId().equals(id))
                        .toList();
                if(depts.isEmpty())
                    throw new UserServiceException("You can only read users from your own department or child departments.");
            }
        }

        return ResponseEntity.ok()
                .body(targetUser);
    }


    public ResponseEntity<UserDeletedResponse> softDeleteUserById(Integer id) {
        var currentUser = getCurrrentUserIfAdminOrManager();
        if(currentUser == null)
            throw new UserServiceException("This user is not authorized to read users.");

        if (currentUser.getId().equals(id)) {
            throw new UserServiceException("You cannot delete your own account.");
        }

        var userToDelete = userRepository.findById(id).orElseThrow(() -> new UserServiceException("User with id " + id + " not found."));

        if(userToDelete.getDeletedAt() != null || !userToDelete.getEnabled())
            throw new UserServiceException("User is already deleted");

        if(currentUser.getRole().getName().equals("Manager")) {
            if (!currentUser.getDepartment().getId().equals(userToDelete.getDepartment().getId()))
                throw new UserServiceException("You can only delete users from your own department.");
            // Manager cannot delete admins
            if (userToDelete.getRole().getName().equals("Admin"))
                throw new UserServiceException("You cannot delete admin users.");
        }
        
        userToDelete.setDeletedAt(LocalDateTime.now());
        userToDelete.setEnabled(false);
        userRepository.save(userToDelete);
        
        return ResponseEntity.ok()
                .body(UserDeletedResponse.builder()
                        .user(userToDelete)
                        .build());
    }

    public ResponseEntity<UserListResponse<User>> getAllUsersDetailed(int page, int size) {
        var currentUser = getCurrrentUserIfAdminOrManager();
        if(currentUser == null)
            throw new UserServiceException("This user is not authorized to read users.");

        List<User> users = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, size);

        if(currentUser.getRole().getName().equals("Manager")){
            List<Department> depts = new ArrayList<>(getChildDepartmentsIncludingSelf(currentUser.getDepartment().getId()));

            if(!depts.isEmpty()) {
                List<Integer> ids = depts.stream().map(Department::getId).toList();
                users.addAll(
                        userRepository.findByDepartmentIdIn(ids, pageable).toList()
                );
            }
        }else
            users.addAll(userRepository.findAll(pageable).toList());

        return ResponseEntity.ok()
                .body(new UserListResponse<User>(users));
    }

    
    public ResponseEntity<UserListResponse<UserSimpleDto>> getAllUsers(int page, int size) {
        var currentUser = getCurrrentUserIfAdminOrManager();
        if(currentUser == null)
            throw new UserServiceException("This user is not authorized to read users.");

        List<User> users = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, size);

        if(currentUser.getRole().getName().equals("Manager")){
            List<Department> depts = new ArrayList<>(getChildDepartmentsIncludingSelf(currentUser.getDepartment().getId()));


            if(!depts.isEmpty()) {
                List<Integer> ids = depts.stream().map(Department::getId).toList();
                users.addAll(
                        userRepository.findByDepartmentIdIn(ids, pageable).toList()
                );
            }
        }else
            users.addAll(userRepository.findAll(pageable).toList());
        
        List<UserSimpleDto> simpleUsers = users.stream()
            .map(this::convertToUserSimpleDto)
            .toList();
        
        return ResponseEntity.ok()
                .body(new UserListResponse<UserSimpleDto>(simpleUsers));
    }


    private List<Department> getChildDepartments(Integer parentId) {
        List<Department> directChildren = departmentHierarchyRepository
                .findChildDepartmentsByParentId(parentId)
                .orElse(List.of());

        if (directChildren.isEmpty()) {
            return List.of();
        }

        return directChildren.stream()
                .flatMap(child -> {
                    List<Department> childAndDescendants = new ArrayList<>(getChildDepartments(child.getId()));
                    childAndDescendants.add(child); // include the current child
                    return childAndDescendants.stream();
                })
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Department> getChildDepartmentsIncludingSelf(Integer parentId) {
        List<Department> all = new ArrayList<>(getChildDepartments(parentId));
        departmentRepository.findById(parentId).ifPresent(all::add); // include self
        return all.stream().distinct().collect(Collectors.toList());
    }


    private List<UserSimpleDto> getUsersByDepartmentId(Integer departmentId) {
        return userRepository.findByDepartmentId(departmentId)
            .orElse(List.of())
            .stream()
            .map(this::convertToUserSimpleDto)
            .toList();
    }

    private UserSimpleDto convertToUserSimpleDto(User user) {
        return UserSimpleDto.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .surName(user.getSurName())
            .email(user.getEmail())
            .role(user.getRole())
            .enabled(user.getEnabled())
                .departmentId(user.getDepartment().getId())
            .active(user.getActive())
            .build();
    }

    public ResponseEntity<UserSimpleDto> updateUser(UserUpdateRequestForAdmin request) {
        var currentUser = getCurrrentUserIfAdminOrManager();
        if(currentUser == null)
            throw new UserServiceException("This user is not authorized to update.");

        if(request.getUserId() == null || request.getUserId() <= 0)
            throw new UserServiceException("User ID cannot be null or less than or equal to zero.");

        var updateUser = userRepository.findById(request.getUserId()).orElseThrow(() -> new UserServiceException("User not found with ID: " + request.getUserId()));

        if(currentUser.getRole().getName().equals("Manager"))
            if(!currentUser.getDepartment().getId().equals(updateUser.getDepartment().getId()))
                throw new UserServiceException("You cannot update other users from other departments.");

        var toUser = userRepository.findById(request.getUserId()).orElseThrow(() -> new UserServiceException("User not found with ID: " + request.getUserId()));

        if(toUser.getDeletedAt() != null || !toUser.getEnabled())
            throw new UserServiceException("User is not active. Cannot update.");

        Validator.isValidEmail(request.getEmail());
        Validator.isValidName(request.getFirstName());
        Validator.isValidName(request.getLastName());

        if(request.getFirstName() != null && !request.getFirstName().isEmpty())
            toUser.setFirstName(request.getFirstName());
        if(request.getLastName() != null && !request.getLastName().isEmpty())
            toUser.setSurName(request.getLastName());
        if(request.getEmail() != null && !request.getEmail().isEmpty())
            toUser.setEmail(request.getEmail());



        if(request.getDepartmentId() != null && request.getDepartmentId() > 0){
            var department = departmentRepository.findById(request.getDepartmentId()).orElseThrow(()  -> new UserServiceException("Department not found with ID: " + request.getDepartmentId()));
            toUser.setDepartment(department);
        }
        if(request.getRoleName() != null && !request.getRoleName().isEmpty()){
            var role = roleRepository.findByName(request.getRoleName()).orElseThrow(()-> new UserServiceException("Role not found with name: " + request.getRoleName()));
            toUser.setRole(role);
        }
        if(request.getEnabled() != null)
            toUser.setEnabled(request.getEnabled());

        userRepository.save(toUser);

        return ResponseEntity.ok().body(convertToUserSimpleDto(toUser));
    }

    @Nullable
    private User getCurrrentUserIfAdminOrManager(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails)
            username = ((UserDetails) principal).getUsername();

        if(username == null)
            throw new DepartmentServiceException("Username is not valid.");

        var user = userRepository.findByEmail(username).orElseThrow();
        return (user.getRole().getName().equals("Admin") || user.getRole().getName().equals("Manager")) ? user : null;
    }
}

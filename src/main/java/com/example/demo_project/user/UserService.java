package com.example.demo_project.user;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public ResponseEntity<UserSimpleDto> updateUser(UserUpdateRequest request) {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) 
            throw new UserServiceException("User not found in security context.");
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getSurName() != null) {
            user.setSurName(request.getSurName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        userRepository.save(user);
        
        return ResponseEntity.ok()
                .body(convertToUserSimpleDto(user));
    }

    public ResponseEntity<UserSimpleDto> updateUserDepartmentForAdmin(UserUpdateDepartmentRequest request) {        
        if( request.getDepartmentId() == null || request.getDepartmentId() <= 0) 
            throw new UserServiceException("Department ID cannot be null or less than or equal to zero.");
        
        if( request.getUserId() == null || request.getUserId() <= 0) 
            throw new UserServiceException("User ID cannot be null or less than or equal to zero.");
        
        var userToUpdate = getUserById(request.getUserId());
        var targetDepartment = getDepartmentById(request.getDepartmentId());
        
        if( request.getDepartmentId().equals(userToUpdate.getDepartment().getId()) )
            throw new UserServiceException("User is already in the specified department.");

        // 🔥 FIXED: Actually set the department!
        userToUpdate.setDepartment(targetDepartment);
        userRepository.save(userToUpdate);
        return ResponseEntity.ok().body(convertToUserSimpleDto(userToUpdate));
    }

    public ResponseEntity<UserSimpleDto> updateUserDepartmentForManager(UserUpdateDepartmentRequest request){
        var manager = getCurrentUser();
        validateManagerAuthorization(manager);
        validateUpdateRequest(request);
        
        User userToUpdate = getUserById(request.getUserId());
        Department targetDepartment = getDepartmentById(request.getDepartmentId());
        
        validateUserUpdatePermissions(manager, userToUpdate, targetDepartment);
        
        // Perform the update
        userToUpdate.setDepartment(targetDepartment);
        userRepository.save(userToUpdate);

        return ResponseEntity.ok().body(convertToUserSimpleDto(userToUpdate));
    }

    public ResponseEntity<UserDeletedResponse> deleteUserById(Integer id) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("User not found with ID: " + id));

        // Admin cannot delete themselves
        if (currentUser.getRole().getName().equals("Admin") && currentUser.getId().equals(id)) {
            throw new UserServiceException("Admin cannot delete their own account.");
        }

        // Only Admin can use this method
        if (!currentUser.getRole().getName().equals("Admin")) {
            throw new UserServiceException("You are not authorized to delete users.");
        }

        userToDelete.setDeletedAt(LocalDateTime.now());
        userToDelete.setEnabled(false);
        userRepository.save(userToDelete);
        
        return ResponseEntity.ok()
                .body(UserDeletedResponse.builder()
                        .user(userToDelete)
                        .build());
    }

    public ResponseEntity<UserDeletedResponse> deleteUserByIdForManager(Integer id) {
        var currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new UserServiceException("User not found with ID: " + id));
        
        // Manager cannot delete themselves
        if (currentUser.getId().equals(id)) {
            throw new UserServiceException("You cannot delete your own account.");
        }
        
        // Only Manager can use this method
        if (!currentUser.getRole().getName().equals("Manager")) {
            throw new UserServiceException("You are not authorized to delete users.");
        }
        
        // Manager cannot delete users from other departments
        if (!currentUser.getDepartment().getId().equals(userToDelete.getDepartment().getId())) {
            throw new UserServiceException("You can only delete users from your own department.");
        }
        
        // Manager cannot delete other managers
        if (userToDelete.getRole().getName().equals("Manager")) {
            throw new UserServiceException("Managers cannot delete other managers.");
        }
        
        // Manager cannot delete admins
        if (userToDelete.getRole().getName().equals("Admin")) {
            throw new UserServiceException("Managers cannot delete admin users.");
        }
        
        userToDelete.setDeletedAt(LocalDateTime.now());
        userToDelete.setEnabled(false);
        userRepository.save(userToDelete);
        
        return ResponseEntity.ok()
                .body(UserDeletedResponse.builder()
                        .user(userToDelete)
                        .build());
    }

    public ResponseEntity<UserListResponse<User>> getAllUsersDetailed() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new UserServiceException("No users found.");
        }
        return ResponseEntity.ok()
                .body(new UserListResponse<User>(users));
    }

    
    public ResponseEntity<UserListResponse<UserSimpleDto>> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new UserServiceException("No users found.");
        }
        
        List<UserSimpleDto> simpleUsers = users.stream()
            .map(this::convertToUserSimpleDto)
            .toList();
        
        return ResponseEntity.ok()
                .body(new UserListResponse<UserSimpleDto>(simpleUsers));
    }

    public ResponseEntity<UserListResponse<UserSimpleDto>> getUsersByDepartment(UserDepartmentRequest request) {
        if( request.getDepartmentId() == null || request.getDepartmentId() <= 0) 
            throw new UserServiceException("Department ID cannot be null or less than or equal to zero.");
        
        List<UserSimpleDto> users = getUsersByDepartmentId(request.getDepartmentId());
        if (users.isEmpty()) {
            throw new UserServiceException("No users found in department: " + request.getDepartmentId());
        }
        
        return ResponseEntity.ok()
                .body(new UserListResponse<UserSimpleDto>(users));
    }

    public ResponseEntity<UserListResponse<UserSimpleDto>> getUsersOfDepartmentByManager(){
        var user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (user == null) {
            throw new UserServiceException("User is not authenticated.");
        }
        
        List<UserSimpleDto> users = getUsersByDepartmentId(user.getDepartment().getId());
        if (users.isEmpty()) {
            throw new UserServiceException("No users found in your department.");
        }
        
        return ResponseEntity.ok()
                .body(new UserListResponse<UserSimpleDto>(users));
    }

    public ResponseEntity<Map<String, List<UserSimpleDto>>> getUsersOfDepartmentAndChildsByManager() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (user == null) {
            throw new UserServiceException("User is not authenticated.");
        }
        
        Map<String, List<UserSimpleDto>> departmentUsersMap = new HashMap<>();
        
        // Add manager's own department users
        addManagerDepartmentUsers(user, departmentUsersMap);
        
        // Add child departments users
        addChildDepartmentsUsers(user, departmentUsersMap);
        
        if (departmentUsersMap.isEmpty()) {
            throw new UserServiceException("No users found in your department or child departments.");
        }
        
        return ResponseEntity.ok().body(departmentUsersMap);
    }

    private void addManagerDepartmentUsers(User manager, Map<String, List<UserSimpleDto>> departmentUsersMap) {
        Department managerDepartment = manager.getDepartment();
        List<UserSimpleDto> managerDeptUsers = getUsersByDepartmentId(managerDepartment.getId());
        departmentUsersMap.put(managerDepartment.getName(), managerDeptUsers);
    }

    private void addChildDepartmentsUsers(User manager, Map<String, List<UserSimpleDto>> departmentUsersMap) {
        List<Department> childDepartments = getChildDepartments(manager.getDepartment().getId());
        
        for (Department childDept : childDepartments) {
            List<UserSimpleDto> childDeptUsers = getUsersByDepartmentId(childDept.getId());
            departmentUsersMap.put(childDept.getName(), childDeptUsers);
        }
    }

    private List<Department> getChildDepartments(Integer parentDepartmentId) {
        return departmentHierarchyRepository
            .findChildDepartmentsByParentId(parentDepartmentId)
            .orElse(List.of());
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
            .role(user.getRole()) // 🔥 ADDED
            .enabled(user.getEnabled())
            .active(user.getActive())
            .build();
    }

    // Helper methods for validation and business logic

    private User getCurrentUser() {
        var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            throw new UserServiceException("User not found in security context.");
        }
        return user;
    }

    private void validateManagerAuthorization(User user) {
        if (!user.getRole().getName().equals("Manager")) {
            throw new UserServiceException("You are not authorized to update user departments.");
        }
    }

    private void validateUpdateRequest(UserUpdateDepartmentRequest request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new UserServiceException("User ID cannot be null or less than or equal to zero.");
        }
        
        if (request.getDepartmentId() == null || request.getDepartmentId() <= 0) {
            throw new UserServiceException("Department ID cannot be null or less than or equal to zero.");
        }
    }

    private User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException("User not found with ID: " + userId));
    }

    private Department getDepartmentById(Integer departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new UserServiceException("Department not found with ID: " + departmentId));
    }

    private void validateUserUpdatePermissions(User manager, User userToUpdate, Department targetDepartment) {
        validateNotUpdatingAdmin(userToUpdate);
        validateUserIsInManagerScope(manager, userToUpdate);
        validateNotSameDepartment(userToUpdate, targetDepartment);
        validateCanMoveToTargetDepartment(manager, targetDepartment);
    }

    private void validateNotUpdatingAdmin(User userToUpdate) {
        if (userToUpdate.getRole().getName().equals("Admin")) {
            throw new UserServiceException("You cannot change the department of an Admin");
        }
    }

    private void validateUserIsInManagerScope(User manager, User userToUpdate) {
        boolean canUpdateThisUser = isUserInManagerDepartment(manager, userToUpdate) || 
                                   isUserInChildDepartment(manager, userToUpdate);
        
        if (!canUpdateThisUser) {
            throw new UserServiceException("You can only update users from your own department or child departments.");
        }
    }

    private boolean isUserInManagerDepartment(User manager, User userToUpdate) {
        return manager.getDepartment().getId().equals(userToUpdate.getDepartment().getId());
    }

    private boolean isUserInChildDepartment(User manager, User userToUpdate) {
        List<Department> childDepartments = getChildDepartments(manager.getDepartment().getId());
        
        return childDepartments.stream()
                .anyMatch(childDept -> childDept.getId().equals(userToUpdate.getDepartment().getId()));
    }

    private void validateNotSameDepartment(User userToUpdate, Department targetDepartment) {
        if (userToUpdate.getDepartment().getId().equals(targetDepartment.getId())) {
            throw new UserServiceException("User is already in the specified department.");
        }
    }

    private void validateCanMoveToTargetDepartment(User manager, Department targetDepartment) {
        boolean canMoveToTarget = isManagerOwnDepartment(manager, targetDepartment) || 
                                 isTargetChildDepartment(manager, targetDepartment);
        
        if (!canMoveToTarget) {
            throw new UserServiceException("You can only move users to your own department or child departments.");
        }
    }

    private boolean isManagerOwnDepartment(User manager, Department targetDepartment) {
        return manager.getDepartment().getId().equals(targetDepartment.getId());
    }

    private boolean isTargetChildDepartment(User manager, Department targetDepartment) {
        List<Department> childDepartments = getChildDepartments(manager.getDepartment().getId());
        
        return childDepartments.stream()
                .anyMatch(childDept -> childDept.getId().equals(targetDepartment.getId()));
    }

    public ResponseEntity<UserSimpleDto> updateUserRoleForAdmin(UserUpdateRoleRequest request) {
        validateAdminAuthorization();
        validateRoleUpdateRequest(request);
        
        User userToUpdate = getUserById(request.getUserId());
        Role targetRole = getRoleById(request.getRoleId());
        
        validateNotSameRole(userToUpdate, targetRole);
        
        // Perform the update
        userToUpdate.setRole(targetRole);
        userRepository.save(userToUpdate);
        
        return ResponseEntity.ok().body(convertToUserSimpleDto(userToUpdate));
    }

    public ResponseEntity<UserSimpleDto> updateUserRoleForManager(UserUpdateRoleRequest request) {
        var manager = getCurrentUser();
        validateManagerAuthorization(manager);
        validateRoleUpdateRequest(request);
        
        User userToUpdate = getUserById(request.getUserId());
        Role targetRole = getRoleById(request.getRoleId());
        
        validateManagerRoleUpdatePermissions(manager, userToUpdate, targetRole);
        
        // Perform the update
        userToUpdate.setRole(targetRole);
        userRepository.save(userToUpdate);
        
        return ResponseEntity.ok().body(convertToUserSimpleDto(userToUpdate));
    }

    // Helper methods for role validation

    private void validateAdminAuthorization() {
        var currentUser = getCurrentUser();
        if (!currentUser.getRole().getName().equals("Admin")) {
            throw new UserServiceException("You are not authorized to update user roles.");
        }
    }

    private void validateRoleUpdateRequest(UserUpdateRoleRequest request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new UserServiceException("User ID cannot be null or less than or equal to zero.");
        }
        
        if (request.getRoleId() == null || request.getRoleId() <= 0) {
            throw new UserServiceException("Role ID cannot be null or less than or equal to zero.");
        }
    }

    private Role getRoleById(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new UserServiceException("Role not found with ID: " + roleId));
    }

    private void validateNotSameRole(User userToUpdate, Role targetRole) {
        if (userToUpdate.getRole().getId().equals(targetRole.getId())) {
            throw new UserServiceException("User already has the specified role.");
        }
    }

    private void validateManagerRoleUpdatePermissions(User manager, User userToUpdate, Role targetRole) {
        validateUserIsInManagerScope(manager, userToUpdate);
        validateManagerCannotUpdateOtherManagersFromOtherDepartments(manager, userToUpdate);
        validateManagerCannotAssignAdminRole(targetRole);
        validateNotSameRole(userToUpdate, targetRole);
    }

    private void validateManagerCannotUpdateOtherManagersFromOtherDepartments(User manager, User userToUpdate) {
        // If user is a manager and NOT in manager's department or child departments
        if (userToUpdate.getRole().getName().equals("Manager")) {
            boolean isInManagerScope = isUserInManagerDepartment(manager, userToUpdate) || 
                                  isUserInChildDepartment(manager, userToUpdate);
            
            if (!isInManagerScope) {
                throw new UserServiceException("You cannot change the role of managers from other departments.");
            }
        }
    }

    private void validateManagerCannotAssignAdminRole(Role targetRole) {
        if (targetRole.getName().equals("Admin")) {
            throw new UserServiceException("Managers cannpt assign Admin role to users.");
        }
    }
}

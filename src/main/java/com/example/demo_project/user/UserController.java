package com.example.demo_project.user;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @PostMapping("/user/update-self")
    public ResponseEntity<UserSimpleDto> updateUser(
        @RequestBody UserUpdateRequest request
    ) {
        return userService.updateUser(request);
    }

    @GetMapping("/user/get-self")
    public ResponseEntity<User> getSelf() {
        return userService.getSelf();
    }

    @PostMapping("/admin/update-user")
    public ResponseEntity<UserSimpleDto> updateOtherUser(
            @RequestBody UserUpdateRequestForAdmin request
    ){
        return userService.updateOtherUser(request);
    }

    // 🔥 FIXED: Admin delete method
    @PostMapping("/admin/soft-delete-user")
    public ResponseEntity<UserDeletedResponse> deleteUserAdmin(
        @RequestBody UserIdRequest request
    ) {
        return userService.softDeleteUserById(request.getId());
    }

    @PostMapping("/admin/delete-user")
    public ResponseEntity<UserDeletedResponse> deleteUserById(
            @RequestBody UserIdRequest request
    ) {
        return userService.deleteUserById(request.getId());
    }

    // 🔥 FIXED: Manager delete method
    @PostMapping("/manager/soft-delete-user")
    public ResponseEntity<UserDeletedResponse> deleteUserManager(
        @RequestBody UserIdRequest request
    ) {
        return userService.softDeleteUserByIdForManager(request.getId());
    }

    @PostMapping("/admin/get-users-by-department")
    public ResponseEntity<UserListResponse<UserSimpleDto>> getUsersByDepartment(
        @RequestBody UserDepartmentRequest request
    ) {
        return userService.getUsersByDepartment(request);
    }
    
    @GetMapping("/admin/get-all-users-detailed")
    public ResponseEntity<UserListResponse<User>> getAllUsersDetailed() {
        return userService.getAllUsersDetailed();
    }
    
    @GetMapping("/admin/get-all-users")
    public ResponseEntity<UserListResponse<UserSimpleDto>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/manager/get-users-of-department-by-manager")
    public ResponseEntity<UserListResponse<UserSimpleDto>> getUsersOfDepartmentByManager() {
        return userService.getUsersOfDepartmentByManager();
    }

    @GetMapping("/manager/get-users-of-department-and-childs-by-manager")
    public ResponseEntity<Map<String, List<UserSimpleDto>>> getUsersOfDepartmentAndChildsByManager() {
        return userService.getUsersOfDepartmentAndChildsByManager();
    }


    @PostMapping("/manager/update-user-department")
    public ResponseEntity<UserSimpleDto> updateUserDepartmentManager(
        @RequestBody UserUpdateDepartmentRequest request
    ) {
        return userService.updateUserDepartmentForManager(request);
    }

    @PostMapping("/manager/update-user-role")
    public ResponseEntity<UserSimpleDto> updateUserRoleManager(
        @RequestBody UserUpdateRoleRequest request
    ) {
        return userService.updateUserRoleForManager(request);
    }
}

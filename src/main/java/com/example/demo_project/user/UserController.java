package com.example.demo_project.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @PostMapping("/delete-user")
    public ResponseEntity<UserDeletedResponse> deleteUser(
        @RequestBody UserEmailRequest request
    ) {
        return userService.deleteUserByEmail(request);
    }

    @PostMapping("/get-users-by-department")
    public ResponseEntity<UserListResponse<User>> getUsersByDepartment(
        @RequestBody UserDepartmentRequest request
    ) {
        return userService.getUsersByDepartment(request);
    }
    
    @GetMapping("/get-all-users-detailed")
    public ResponseEntity<UserListResponse<User>> getAllUsersDetailed() {
        return userService.getAllUsersDetailed();
    }
    
    @GetMapping("/get-all-users")
    public ResponseEntity<UserListResponse<UserSimpleDto>> getAllUsers() {
        return userService.getAllUsers();
    }
}

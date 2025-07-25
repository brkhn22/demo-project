package com.example.demo_project.user;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @GetMapping("/get-self")
    public ResponseEntity<User> getSelf() {
        return userService.getSelf();
    }

    @PostMapping("/update-user")
    public ResponseEntity<UserSimpleDto> updateUser(
            @RequestBody UserUpdateRequestForAdmin request
    ){
        return userService.updateUser(request);
    }

    @DeleteMapping("/soft-delete-user")
    public ResponseEntity<UserDeletedResponse> deleteUser(
        @RequestBody UserIdRequest request
    ) {
        return userService.softDeleteUserById(request.getId());
    }

    
    @GetMapping("/get-all-users-detailed")
    public ResponseEntity<UserListResponse<User>> getAllUsersDetailed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.getAllUsersDetailed(page, size);
    }
    
    @GetMapping("/get-all-users")
    public ResponseEntity<UserListResponse<UserSimpleDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.getAllUsers(page, size);
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<User> getUserById(
            @RequestParam int id) {
        return userService.getById(id);
    }


}

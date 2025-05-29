package com.example.demo_project.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserServiceException("User not found with email: " + email));
    }

    public ResponseEntity<UserDeletedResponse> deleteUserByEmail(UserEmailRequest request) {
        User user = getUserByEmail(request.getEmail());

        if (user == null) {
            throw new UserServiceException("User not found with email: " + request.getEmail());
        }
        user.setDeletedAt(LocalDateTime.now());
        user.setEnabled(false);
        userRepository.save(user);
        return ResponseEntity.ok()
                .body(UserDeletedResponse.builder()
                .user(user)
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
        .map(user -> UserSimpleDto.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .surName(user.getSurName())
            .email(user.getEmail())
            .enabled(user.getEnabled())
            .active(user.getActive())
            .build())
        .toList();
    
        return ResponseEntity.ok()
                .body(new UserListResponse<UserSimpleDto>(simpleUsers));
    }

    public ResponseEntity<UserListResponse<User>> getUsersByDepartment(UserDepartmentRequest request) {
        if( request.getDepartmentName() == null || request.getDepartmentName().isEmpty()) 
            throw new UserServiceException("Company name and department name must be provided.");
        
        List<User> users = userRepository.findByDepartmentName(request.getDepartmentName())
        .orElseThrow(() -> new UserServiceException("No users found in department: " + request.getDepartmentName()));

        return ResponseEntity.ok()
                .body(new UserListResponse<User>(users));
    }
    
}

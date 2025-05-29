package com.example.demo_project.auth;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo_project.auth.token.ConfirmRequest;
import com.example.demo_project.auth.token.ConfirmationTokenService;
import com.example.demo_project.auth.token.ResendRequest;
import com.example.demo_project.config.JwtService;
import com.example.demo_project.email.EmailSender;
import com.example.demo_project.user.RoleRepository;
import com.example.demo_project.user.User;
import com.example.demo_project.user.UserRepository;
import com.example.demo_project.user.department.DepartmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final String MAIN_PATH = "https://demo-project-6-env.eba-bub3hufq.eu-north-1.elasticbeanstalk.com";

    public AuthenticationResponse register(RegisterRequest request){
        Validator.isValidEmail(request.getEmail());

        if(userRepository.findByEmail(request.getEmail()).isPresent())
            throw new AuthenticationException("Email already in use");

        var role = roleRepository.findByName(request.getRoleName())
        .orElseThrow();
        var department = departmentRepository.findByName(request.getDepartmentName())
        .orElseThrow();

        var user = User.builder()
        .firstName(request.getFirstName())
        .surName(request.getSurName())
        .email(request.getEmail())
        .password(null)
        .role(role)
        .department(department)
        .active(false)
        .enabled(false)
        .createdAt(LocalDateTime.now())
        .deletedAt(null)
        .build();

        userRepository.save(user);
        String token = confirmationTokenService.saveConfirmationToken(user);
        String link = MAIN_PATH+"/api/v1/auth/activation?token=" + token;
        String message = "<p>Click the link to activate your account:</p>" +
        "<a href=\"" + link + "\">" + link + "</a>";
        emailSender.send(request.getEmail(), message);

        return AuthenticationResponse.builder()
        .token(token)
        .build();

    }

    public AuthenticationResponse resendToken(ResendRequest request){
        var user = userRepository.findByEmail(request.getEmail())
        .orElseThrow();

        Validator.isValidEmail(request.getEmail());

        if(user.getActive())
            throw new AuthenticationException("User already active");

        String token = confirmationTokenService.saveConfirmationToken(user);
        return AuthenticationResponse.builder()
        .token(token)
        .build();
    }

    public AuthenticationResponse activate(String token){
        String validToken = confirmationTokenService.isTokenValidEmail(token);
        return AuthenticationResponse.builder()
        .token(validToken)
        .build();
    }

    public AuthenticationResponse confirm(ConfirmRequest request){
        var user = confirmationTokenService.confirmTokenEmail(request.getToken());

        Validator.isValidEmail(request.getEmail());
        Validator.isValidPassword(request.getPassword());

        user.setActive(true);
        user.setEnabled(true);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // authentication token
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
        .token(jwtToken)
        .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request){

        Validator.isValidEmail(request.getEmail());
        Validator.isValidPassword(request.getPassword());
        
        authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword()
            )
        );

        var user = userRepository.findByEmail(request.getEmail())
        .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
        .token(jwtToken)
        .build();
    }

    public AuthenticationResponse forgotPassword(ForgotPasswordRequest request){
        Validator.isValidEmail(request.getEmail());

        var user = userRepository.findByEmail(request.getEmail())
        .orElseThrow();

        String token = confirmationTokenService.saveConfirmationToken(user);
        String link = MAIN_PATH+"/api/v1/auth/activate-forgot-password?token=" + token;
        String message = "<p>Click the link to reset your password:</p>" +
        "<a href=\"" + link + "\">" + link + "</a>";
        emailSender.send(request.getEmail(), message);

        return AuthenticationResponse.builder()
        .token(token)
        .build();
    }

    public AuthenticationResponse activateForgotPassword(String token){
        String validToken = confirmationTokenService.isTokenValidPassword(token);
        return AuthenticationResponse.builder()
        .token(validToken)
        .build();
    }

    public AuthenticationResponse resetPassword(ResetPasswordRequest request){
        Validator.isValidPassword(request.getPassword());

        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new AuthenticationException("Passwords do not match");
        
        var user = confirmationTokenService.confirmTokenPassword(request.getToken());
        
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        
        String jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
        .token(jwtToken)
        .build();
    }
}

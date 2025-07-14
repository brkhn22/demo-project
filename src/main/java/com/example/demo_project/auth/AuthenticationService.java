package com.example.demo_project.auth;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.example.demo_project.user.department.Department;
import com.example.demo_project.user.department.DepartmentRepository;
import com.example.demo_project.user.department.department_hieararchy.DepartmentHierarchyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private final String MAIN_PATH = "http://localhost:5173";
    private final String ACTIVATION_PATH = "/set-initial-password";

    @Transactional
    public AuthenticationResponse register(RegisterRequest request){
        Validator.isValidEmail(request.getEmail());
        Validator.isValidDepartmentId(request.getDepartmentId());
        Validator.isValidName(request.getFirstName());
        Validator.isValidName(request.getSurName());
        Validator.isValidRoleName(request.getRoleName());

        if(userRepository.findByEmail(request.getEmail()).isPresent())
            throw new AuthenticationException("Email already in use");
        
        var role = roleRepository.findByName(request.getRoleName())
        .orElseThrow();
        var department = departmentRepository.findById(request.getDepartmentId())
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
        String link = MAIN_PATH+ACTIVATION_PATH+"?token=" + token;
        
        // 🎨 Beautiful HTML email
        String message = createAccountActivationEmail(request.getFirstName(), link);
        emailSender.send(request.getEmail(), message);

        return AuthenticationResponse.builder()
        .token(token)
        .build();

    }
    @Transactional
    public AuthenticationResponse registerByManager(RegisterRequest request){
        Validator.isValidEmail(request.getEmail());
        Validator.isValidName(request.getFirstName());
        Validator.isValidName(request.getSurName());
        Validator.isValidDepartmentId(request.getDepartmentId());
        Validator.isValidRoleName(request.getRoleName());
        
        var manager = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (manager == null) {
            throw new AuthenticationException("User not found in security context.");
        }

        // Only managers can use this method
        if (!manager.getRole().getName().equals("Manager")) {
            throw new AuthenticationException("You are not authorized to register users.");
        }

        if(userRepository.findByEmail(request.getEmail()).isPresent())
            throw new AuthenticationException("Email already in use");
        
        var role = roleRepository.findByName(request.getRoleName())
            .orElseThrow(() -> new AuthenticationException("Role not found: " + request.getRoleName()));

        var targetDepartment = departmentRepository.findById(request.getDepartmentId())
            .orElseThrow(() -> new AuthenticationException("Department not found with ID: " + request.getDepartmentId()));

        // Validate manager can only assign Employee role
        if (role.getName().equals("Admin")) {
            throw new AuthenticationException("Managers cannot register Admin users.");
        }

        // Validate target department is manager's department or child department
        validateManagerCanRegisterToTargetDepartment(manager, targetDepartment);

        var newUser = User.builder()
            .firstName(request.getFirstName())
            .surName(request.getSurName())
            .email(request.getEmail())
            .password(null)
            .role(role)
            .department(targetDepartment)
            .active(false)
            .enabled(false)
            .createdAt(LocalDateTime.now())
            .deletedAt(null)
            .build();

        userRepository.save(newUser);
        String token = confirmationTokenService.saveConfirmationToken(newUser);
        String link = MAIN_PATH+ACTIVATION_PATH+"?token=" + token;
        
        // 🎨 Beautiful HTML email for manager registration
        String message = createManagerInvitationEmail(request.getFirstName(), manager.getFirstName() + " " + manager.getSurName(), targetDepartment.getName(), link);
        emailSender.send(request.getEmail(), message);

        return AuthenticationResponse.builder()
            .token(token)
            .build();
    }

    private void validateManagerCanRegisterToTargetDepartment(User manager, Department targetDepartment) {
        // Check if target is manager's own department
        if (manager.getDepartment().getId().equals(targetDepartment.getId())) {
            return; // Valid - manager's own department
        }
        
        // Check if target is a child department
        List<Department> childDepartments = departmentHierarchyRepository
            .findChildDepartmentsByParentId(manager.getDepartment().getId())
            .orElse(List.of());
        
        boolean isChildDepartment = childDepartments.stream()
            .anyMatch(childDept -> childDept.getId().equals(targetDepartment.getId()));
        
        if (!isChildDepartment) {
            throw new AuthenticationException("You can only register users to your own department or child departments.");
        }
    }

    public AuthenticationResponse resendToken(ResendRequest request){
        var user = userRepository.findByEmail(request.getEmail())
        .orElseThrow();

        Validator.isValidEmail(request.getEmail());

        if(user.getActive())
            throw new AuthenticationException("User already active");

        String token = confirmationTokenService.saveConfirmationToken(user);
        String link = MAIN_PATH+ACTIVATION_PATH+"?token=" + token;

        String message = createAccountActivationEmail(user.getFirstName(), link);
        emailSender.send(request.getEmail(), message);

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

    @Transactional
    public AuthenticationResponse confirm(ConfirmRequest request){
        Validator.isValidEmail(request.getEmail());
        Validator.isValidPassword(request.getPassword());

        var user = confirmationTokenService.confirmTokenEmail(request.getToken());

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

        if (!user.getActive())
            throw new AuthenticationException("User is not active");
        if (!user.getEnabled())
            throw new AuthenticationException("User is not enabled");
        if (user.getDeletedAt() != null)
            throw new AuthenticationException("User is deleted");

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
        .token(jwtToken)
        .build();
    }

    @Transactional
    public AuthenticationResponse forgotPassword(ForgotPasswordRequest request){
        Validator.isValidEmail(request.getEmail());

        var user = userRepository.findByEmail(request.getEmail())
        .orElseThrow();
        if(!user.getActive())
           throw new AuthenticationException("User is not active. Please do the activation.");

        String token = confirmationTokenService.saveConfirmationToken(user);
        String link = MAIN_PATH+"/auth/activate-forgot-password?token=" + token;
        
        // 🎨 Beautiful HTML email for password reset
        String message = createPasswordResetEmail(user.getFirstName(), link);
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

    @Transactional
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

    // 🎨 Email Template Methods

    private String createAccountActivationEmail(String firstName, String activationLink) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.05); }
                .header { text-align: center; margin-bottom: 20px; }
                .header h1 { margin: 0; font-size: 24px; color: #333; }
                .message { font-size: 16px; color: #555; margin-bottom: 20px; line-height: 1.5; }
                .btn { display: inline-block; background-color: #4a63f0; color: #fff; padding: 12px 20px; border-radius: 5px; text-decoration: none; font-weight: bold; }
                .footer { margin-top: 30px; font-size: 13px; color: #888; text-align: center; }
                a.link { color: #4a63f0; word-break: break-all; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Welcome, %s!</h1>
                </div>
                <div class="message">
                    We're happy to have you on board. Please activate your account by clicking the button below:
                </div>
                <div style="text-align: center; margin-bottom: 20px;">
                    <a href="%s" class="btn">Activate My Account</a>
                </div>
                <div class="message">
                    If the button doesn't work, copy and paste this link into your browser:<br>
                    <a href="%s" class="link">%s</a>
                </div>
                <div class="footer">
                    If you didn’t request this account, you can safely ignore this email.<br>
                    &copy; 2025 Demo Project
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName, activationLink, activationLink, activationLink);
    }


    private String createManagerInvitationEmail(String firstName, String managerName, String departmentName, String activationLink) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                .container { max-width: 600px; margin: 0 auto; background-color: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.05); }
                .header { text-align: center; margin-bottom: 20px; }
                .header h1 { margin: 0; font-size: 22px; color: #11998e; }
                .message { font-size: 16px; color: #333; margin-bottom: 20px; line-height: 1.5; }
                .info-box { background: #e8f5e8; padding: 15px; border-left: 4px solid #4caf50; border-radius: 5px; margin-bottom: 20px; font-size: 14px; }
                .btn { display: inline-block; background-color: #38ef7d; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; }
                .footer { text-align: center; margin-top: 30px; font-size: 13px; color: #999; }
                .link { color: #11998e; word-break: break-all; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>You've Been Invited, %s!</h1>
                </div>
                <div class="message">
                    Your manager <strong>%s</strong> has invited you to join <strong>Demo Project</strong>.
                </div>
                <div class="info-box">
                    👤 Name: %s<br>
                    🏢 Department: %s<br>
                    👔 Manager: %s
                </div>
                <div class="message">
                    Click below to activate your account:
                </div>
                <div style="text-align: center;">
                    <a href="%s" class="btn">Activate My Account</a>
                </div>
                <div class="message">
                    Or copy this link into your browser:<br>
                    <a href="%s" class="link">%s</a>
                </div>
                <div class="footer">
                    © 2025 Demo Project. If this was a mistake, please contact your manager.
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName, managerName, firstName, departmentName, managerName, activationLink, activationLink, activationLink);
    }


    private String createPasswordResetEmail(String firstName, String resetLink) {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                .container { max-width: 600px; margin: 0 auto; background-color: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.05); }
                .header { text-align: center; margin-bottom: 20px; }
                .header h1 { margin: 0; font-size: 22px; color: #ff6b6b; }
                .message { font-size: 16px; color: #333; margin-bottom: 20px; line-height: 1.5; }
                .warning { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; border-radius: 5px; font-size: 14px; margin-bottom: 20px; }
                .btn { display: inline-block; background-color: #ff6b6b; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-weight: bold; }
                .footer { text-align: center; margin-top: 30px; font-size: 13px; color: #999; }
                .link { color: #ff6b6b; word-break: break-all; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Password Reset</h1>
                </div>
                <div class="message">
                    Hello %s,<br>
                    We received a request to reset your password for your <strong>Demo Project</strong> account.
                </div>
                <div style="text-align: center;">
                    <a href="%s" class="btn">Reset My Password</a>
                </div>
                <div class="warning">
                    ⚠️ This link will expire in 24 hours. If you didn't request a password reset, just ignore this email.
                </div>
                <div class="message">
                    Or copy this link into your browser:<br>
                    <a href="%s" class="link">%s</a>
                </div>
                <div class="footer">
                    © 2025 Demo Project. This is an automated message — please do not reply.
                </div>
            </div>
        </body>
        </html>
        """.formatted(firstName, resetLink, resetLink, resetLink);
    }

}

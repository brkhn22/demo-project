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

    private final String MAIN_PATH = "https://demo-project-6-env.eba-bub3hufq.eu-north-1.elasticbeanstalk.com";

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
        String link = MAIN_PATH+"/auth/activation?token=" + token;
        
        // 🎨 Beautiful HTML email
        String message = createAccountActivationEmail(request.getFirstName(), link);
        emailSender.send(request.getEmail(), message);

        return AuthenticationResponse.builder()
        .token(token)
        .build();

    }

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
        String link = MAIN_PATH+"/auth/activation?token=" + token;
        
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

    public AuthenticationResponse forgotPassword(ForgotPasswordRequest request){
        Validator.isValidEmail(request.getEmail());

        var user = userRepository.findByEmail(request.getEmail())
        .orElseThrow();

        String token = confirmationTokenService.saveConfirmationToken(user);
        String link = MAIN_PATH+"/api/v1/auth/activate-forgot-password?token=" + token;
        
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
                    .container { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .logo { font-size: 32px; font-weight: bold; margin-bottom: 10px; }
                    .content { background: #f8f9fa; padding: 40px; border-radius: 0 0 10px 10px; }
                    .welcome-text { font-size: 24px; color: #333; margin-bottom: 20px; }
                    .message { font-size: 16px; color: #666; line-height: 1.6; margin-bottom: 30px; }
                    .btn { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 50px; font-weight: bold; text-align: center; margin: 20px 0; transition: transform 0.2s; }
                    .btn:hover { transform: translateY(-2px); }
                    .footer { text-align: center; margin-top: 30px; color: #999; font-size: 14px; }
                    .security-note { background: #e3f2fd; padding: 15px; border-left: 4px solid #2196f3; margin: 20px 0; border-radius: 4px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🚀 Demo Project</div>
                        <div>Welcome to Our Platform!</div>
                    </div>
                    <div class="content">
                        <div class="welcome-text">Hello %s! 👋</div>
                        <div class="message">
                            Welcome to <strong>Demo Project</strong>! We're excited to have you join our team.
                            <br><br>
                            To get started, please activate your account by clicking the button below:
                        </div>
                        <div style="text-align: center;">
                            <a href="%s" class="btn">✨ Activate My Account</a>
                        </div>
                        <div class="security-note">
                            <strong>🔒 Security Note:</strong> This activation link will expire in 24 hours for your security.
                        </div>
                        <div class="message">
                            If the button doesn't work, you can copy and paste this link into your browser:<br>
                            <a href="%s" style="color: #667eea; word-break: break-all;">%s</a>
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2025 Demo Project. All rights reserved.</p>
                        <p>If you didn't request this account, please ignore this email.</p>
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
                    .container { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .logo { font-size: 32px; font-weight: bold; margin-bottom: 10px; }
                    .content { background: #f8f9fa; padding: 40px; border-radius: 0 0 10px 10px; }
                    .welcome-text { font-size: 24px; color: #333; margin-bottom: 20px; }
                    .message { font-size: 16px; color: #666; line-height: 1.6; margin-bottom: 30px; }
                    .btn { display: inline-block; background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 50px; font-weight: bold; text-align: center; margin: 20px 0; transition: transform 0.2s; }
                    .btn:hover { transform: translateY(-2px); }
                    .info-box { background: #e8f5e8; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #4caf50; }
                    .footer { text-align: center; margin-top: 30px; color: #999; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🏢 Demo Project</div>
                        <div>You've Been Invited!</div>
                    </div>
                    <div class="content">
                        <div class="welcome-text">Hello %s! 🎉</div>
                        <div class="message">
                            You have been invited to join <strong>Demo Project</strong> by your manager <strong>%s</strong>.
                        </div>
                        <div class="info-box">
                            <strong>📋 Your Details:</strong><br>
                            👤 Name: %s<br>
                            🏢 Department: %s<br>
                            👔 Manager: %s
                        </div>
                        <div class="message">
                            To activate your account and set your password, please click the button below:
                        </div>
                        <div style="text-align: center;">
                            <a href="%s" class="btn">🚀 Activate My Account</a>
                        </div>
                        <div class="message">
                            If the button doesn't work, you can copy and paste this link into your browser:<br>
                            <a href="%s" style="color: #11998e; word-break: break-all;">%s</a>
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2025 Demo Project. All rights reserved.</p>
                        <p>If you believe this was sent to you by mistake, please contact your manager.</p>
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
                    .container { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .logo { font-size: 32px; font-weight: bold; margin-bottom: 10px; }
                    .content { background: #f8f9fa; padding: 40px; border-radius: 0 0 10px 10px; }
                    .welcome-text { font-size: 24px; color: #333; margin-bottom: 20px; }
                    .message { font-size: 16px; color: #666; line-height: 1.6; margin-bottom: 30px; }
                    .btn { display: inline-block; background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%); color: white; padding: 15px 30px; text-decoration: none; border-radius: 50px; font-weight: bold; text-align: center; margin: 20px 0; transition: transform 0.2s; }
                    .btn:hover { transform: translateY(-2px); }
                    .warning-box { background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; border-radius: 4px; }
                    .footer { text-align: center; margin-top: 30px; color: #999; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🔐 Demo Project</div>
                        <div>Password Reset Request</div>
                    </div>
                    <div class="content">
                        <div class="welcome-text">Hello %s! 🔑</div>
                        <div class="message">
                            We received a request to reset your password for your <strong>Demo Project</strong> account.
                            <br><br>
                            If you requested this password reset, click the button below to create a new password:
                        </div>
                        <div style="text-align: center;">
                            <a href="%s" class="btn">🔄 Reset My Password</a>
                        </div>
                        <div class="warning-box">
                            <strong>⚠️ Security Notice:</strong> This reset link will expire in 24 hours. If you didn't request a password reset, please ignore this email and your account will remain secure.
                        </div>
                        <div class="message">
                            If the button doesn't work, you can copy and paste this link into your browser:<br>
                            <a href="%s" style="color: #ff6b6b; word-break: break-all;">%s</a>
                        </div>
                        <div class="message">
                            <strong>Need help?</strong> Contact your system administrator if you're having trouble accessing your account.
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2025 Demo Project. All rights reserved.</p>
                        <p>This is an automated message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName, resetLink, resetLink, resetLink);
    }
}

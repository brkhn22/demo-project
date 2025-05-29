package com.example.demo_project.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo_project.auth.token.ConfirmRequest;
import com.example.demo_project.auth.token.ResendRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
        @RequestBody RegisterRequest request
        ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/activation/resend")
    public ResponseEntity<AuthenticationResponse> resend(
        @RequestBody ResendRequest request
        ) {
        return ResponseEntity.ok(authenticationService.resendToken(request));
    }

    @PostMapping("/activation/confirm")
    public ResponseEntity<AuthenticationResponse> confirm(
        @RequestBody ConfirmRequest request
        ) {
        return ResponseEntity.ok(authenticationService.confirm(request));
    }

    @GetMapping("/activation")
    public ResponseEntity<AuthenticationResponse> confirm(
        @RequestParam String token
        ) {
        return ResponseEntity.ok(authenticationService.activate(token));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
        @RequestBody AuthenticationRequest request
        ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/activate-forgot-password")
    public ResponseEntity<AuthenticationResponse> activateForgotPassword(
        @RequestParam String token
        ) {
        return ResponseEntity.ok(authenticationService.activateForgotPassword(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthenticationResponse> forgotPassword(
        @RequestBody ForgotPasswordRequest request
        ) {
        return ResponseEntity.ok(authenticationService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthenticationResponse> resetPassword(
        @RequestBody ResetPasswordRequest request
        ) {
        return ResponseEntity.ok(authenticationService.resetPassword(request));
    }
}
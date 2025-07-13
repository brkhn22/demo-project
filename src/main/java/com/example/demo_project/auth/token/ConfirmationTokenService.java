package com.example.demo_project.auth.token;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.demo_project.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenService {
    
    private final ConfirmationTokenRepository confirmationTokenRepository;

    public String saveConfirmationToken(User user) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null, user);
        confirmationTokenRepository.save(confirmationToken);
        return token;
    }

    public String isTokenValidEmail(String token) {
        if (token == null || token.isEmpty())
        throw new ConfirmationException("Token cannot be null or empty");

        var confirmationToken = confirmationTokenRepository.findByToken(token)
        .orElseThrow(() -> new ConfirmationException("Token not found"));

        if (confirmationToken == null)
            throw new ConfirmationException("Token not found");
        if (confirmationToken.getConfirmedAt() != null)
            throw new ConfirmationException("Token already confirmed");
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ConfirmationException("Token expired");
        if (confirmationToken.getUser() == null)
            throw new ConfirmationException("User not found");
        if (confirmationToken.getUser().getDeletedAt() != null)
            throw new ConfirmationException("User deleted");
        if (confirmationToken.getUser().getActive())
            throw new ConfirmationException("User already active");

        return confirmationToken.getToken();
    }

    public String isTokenValidPassword(String token) {
        if (token == null || token.isEmpty())
        throw new ConfirmationException("Token cannot be null or empty");

        var confirmationToken = confirmationTokenRepository.findByToken(token)
        .orElseThrow();

        if (confirmationToken == null)
            throw new ConfirmationException("Token not found");
        if (confirmationToken.getConfirmedAt() != null)
            throw new ConfirmationException("Token already confirmed");
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ConfirmationException("Token expired");
        if (confirmationToken.getUser() == null)
            throw new ConfirmationException("User not found");
        if (confirmationToken.getUser().getDeletedAt() != null)
            throw new ConfirmationException("User deleted");

        return confirmationToken.getToken();
    }

    public User confirmTokenEmail(String token) {
        var confirmationToken = isTokenValidEmailHelper(token);

        confirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);
        return confirmationToken.getUser();
    }

    public User confirmTokenPassword(String token) {
        var confirmationToken = isTokenValidPasswordHelper(token);

        confirmationToken.setConfirmedAt(LocalDateTime.now());
        confirmationTokenRepository.save(confirmationToken);
        return confirmationToken.getUser();
    }

    private ConfirmationToken isTokenValidEmailHelper(String token) {
        if (token == null || token.isEmpty())
            throw new ConfirmationException("Token cannot be null or empty");

        var confirmationToken = confirmationTokenRepository.findByToken(token)
        .orElseThrow(() -> new ConfirmationException("Token not found"));

        if (confirmationToken == null)
            throw new ConfirmationException("Token not found");
        if (confirmationToken.getConfirmedAt() != null)
            throw new ConfirmationException("Token already confirmed");
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ConfirmationException("Token expired");
        if (confirmationToken.getUser() == null)
            throw new ConfirmationException("User not found");
        if (confirmationToken.getUser().getDeletedAt() != null)
            throw new ConfirmationException("User deleted");
        if (confirmationToken.getUser().getActive())
            throw new ConfirmationException("User already active");

        return confirmationToken;
    }

    private ConfirmationToken isTokenValidPasswordHelper(String token) {
        if (token == null || token.isEmpty())
            throw new ConfirmationException("Token cannot be null or empty");

        var confirmationToken = confirmationTokenRepository.findByToken(token)
        .orElseThrow();

        if (confirmationToken == null)
            throw new ConfirmationException("Token not found");
        if (confirmationToken.getConfirmedAt() != null)
            throw new ConfirmationException("Token already confirmed");
        if (confirmationToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ConfirmationException("Token expired");
        if (confirmationToken.getUser() == null)
            throw new ConfirmationException("User not found");
        if (confirmationToken.getUser().getDeletedAt() != null)
            throw new ConfirmationException("User deleted");

        return confirmationToken;
    }
}

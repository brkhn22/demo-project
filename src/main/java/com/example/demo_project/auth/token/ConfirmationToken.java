package com.example.demo_project.auth.token;

import java.time.LocalDateTime;

import com.example.demo_project.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "Confirmation_Token")
@Entity
@Data
@NoArgsConstructor
public class ConfirmationToken {

    public ConfirmationToken(String token, LocalDateTime createdAt, LocalDateTime expiresAt, LocalDateTime confirmedAt, User user) {
        this.token = token;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.confirmedAt = confirmedAt;
        this.user = user;
    }

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "Token", nullable = false)
    private String token;

    @Column(name = "Created_At", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "Expires_At", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "Confirmed_At")
    private LocalDateTime confirmedAt;

    @ManyToOne
    @JoinColumn(name = "User_ID", referencedColumnName = "ID")
    private User user;
    
}

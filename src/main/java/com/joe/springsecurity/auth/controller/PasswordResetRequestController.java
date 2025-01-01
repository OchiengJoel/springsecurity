package com.joe.springsecurity.auth.controller;

import com.joe.springsecurity.auth.dto.PasswordResetRequest;
import com.joe.springsecurity.auth.dto.UserPasswordResetRequest;
import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.UserRepository;
import com.joe.springsecurity.auth.service.PasswordResetTokenService;
import com.joe.springsecurity.utils.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/api/v2/request-password-reset")
public class PasswordResetRequestController {

    private final PasswordResetTokenService passwordResetTokenService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public PasswordResetRequestController(PasswordResetTokenService passwordResetTokenService,
                                          UserRepository userRepository, EmailService emailService) {
        this.passwordResetTokenService = passwordResetTokenService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // Endpoint to request password reset
    @PostMapping
    public ResponseEntity<String> requestPasswordReset(@RequestBody UserPasswordResetRequest userPasswordResetRequest) {
        String email = userPasswordResetRequest.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Generate password reset token
        String token = passwordResetTokenService.createPasswordResetToken(user);

        // Send reset link to user's email
        String resetLink = "http://localhost:8080/api/v2/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String text = "To reset your password, click on the following link: " + resetLink;

        try {
            emailService.sendEmail(email, subject, text);
            return ResponseEntity.ok("Password reset link has been sent to your email.");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending email.");
        }
    }
}

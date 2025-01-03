package com.joe.springsecurity.auth.controller;

import com.joe.springsecurity.auth.dto.PasswordResetRequest;
import com.joe.springsecurity.auth.model.PasswordResetToken;
import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.UserRepository;
import com.joe.springsecurity.auth.service.PasswordResetTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/reset-password")
@CrossOrigin(origins = "http://localhost:4200")
public class PasswordResetController {

    private final PasswordResetTokenService passwordResetTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(PasswordResetTokenService passwordResetTokenService,
                                   UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.passwordResetTokenService = passwordResetTokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Endpoint to reset the password
    @PostMapping
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();

        // Validate token
        if (passwordResetTokenService.validateToken(token)) {
            PasswordResetToken resetToken = passwordResetTokenService.getTokenByValue(token);
            User user = resetToken.getUser();

            // Update the password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Optional: Delete or invalidate the token after use
            passwordResetTokenService.delete(resetToken);

            return ResponseEntity.ok("Password has been reset successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }
    }
}

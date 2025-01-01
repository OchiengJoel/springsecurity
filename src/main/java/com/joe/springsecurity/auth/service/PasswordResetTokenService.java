package com.joe.springsecurity.auth.service;

import com.joe.springsecurity.auth.model.PasswordResetToken;
import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.PasswordResetTokenRepository;
import com.joe.springsecurity.auth.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public PasswordResetTokenService(PasswordResetTokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    // Generate a password reset token
    public String createPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();  // Generate a random token

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpirationDate(LocalDateTime.now().plusHours(1));  // Set expiration time (1 hour)

        tokenRepository.save(resetToken);

        return token;
    }

    // Validate the token
    public boolean validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        // Check if the token is expired
        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }
        return true;
    }

    // Get the token by its value
    public PasswordResetToken getTokenByValue(String token) {
        return tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }

    // Delete the token after it is used
    public void delete(PasswordResetToken token) {
        tokenRepository.delete(token);
    }
}

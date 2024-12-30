package com.joe.springsecurity.auth.service;


import com.joe.springsecurity.auth.dto.AuthenticationResponse;
import com.joe.springsecurity.auth.model.Role;
import com.joe.springsecurity.auth.model.Token;
import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.TokenRepository;
import com.joe.springsecurity.auth.repo.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, TokenRepository tokenRepository, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
    }

    // User Authentication
    public AuthenticationResponse authenticate(User request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Get user details from database
        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate new JWT tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Revoke any existing tokens for the user
        revokeAllTokensByUser(user);

        // Save new tokens in the database
        saveUserToken(accessToken, refreshToken, user);

        // Return authentication response with tokens
        return new AuthenticationResponse(accessToken, refreshToken, "User login was successful");
    }

    // User Registration
    public AuthenticationResponse register(User request) {
        if (repository.findByUsername(request.getUsername()).isPresent()) {
            return new AuthenticationResponse(null, null, "User already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // If no roles are provided, default to ROLE_USER
        Set<Role> roles = request.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add(Role.ROLE_USER);  // Default to ROLE_USER
        }
        user.setRoles(roles);  // Set the roles for the user

        // Save user to the database
        user = repository.save(user); // Ensure that roles are persisted

        // Generate JWT Tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(accessToken, refreshToken, user);

        return new AuthenticationResponse(accessToken, refreshToken, "User registration was successful");
    }

    // Assign a new role to a user
//    public String assignRoleToUser(Long userId, Role role) {
//        User user = repository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Check if role already exists
//        if (user.getRoles().contains(role)) {
//            return "User already has the role: " + role;
//        }
//
//        user.getRoles().add(role);  // Add the role to the user
//        repository.save(user);
//
//        return "Role " + role + " assigned to user " + user.getUsername();
//    }

    public String assignRoleToUser(Long userId, Role role) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Ensure that roles are added correctly
        if (user.getRoles().contains(role)) {
            return "User already has the role: " + role;
        }

        user.getRoles().add(role);  // Add the role
        repository.save(user);  // Save user to persist the role

        return "Role " + role + " assigned to user " + user.getUsername();
    }

    // Remove a role from a user
    public String removeRoleFromUser(Long userId, Role role) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Debug log to inspect the roles in the user
        System.out.println("User roles: " + user.getRoles());

        if (!user.getRoles().contains(role)) {
            return "User does not have the role: " + role;
        }

        // Remove the role from the user's roles set
        user.getRoles().remove(role);

        if (user.getRoles().isEmpty()) {
            user.getRoles().add(Role.ROLE_USER);  // Assign default role
        }

        repository.save(user);
        return "Role " + role + " removed from user " + user.getUsername();
    }

    // Revoke all existing tokens for a user
    private void revokeAllTokensByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllAccessTokensByUser(user.getId());
        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t -> t.setLoggedOut(true));
        tokenRepository.saveAll(validTokens);
    }

    // Save new tokens for the user
    private void saveUserToken(String accessToken, String refreshToken, User user) {
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
    }

    // Refresh token
    public ResponseEntity refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7); // Extract token

        String username = jwtService.extractUsername(token); // Extract username from token

        User user = repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("No user found"));

        // Check if the refresh token is valid
        if (jwtService.isValidRefreshToken(token, user)) {
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllTokensByUser(user);
            saveUserToken(accessToken, refreshToken, user);

            return ResponseEntity.ok(new AuthenticationResponse(accessToken, refreshToken, "New tokens generated"));
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}
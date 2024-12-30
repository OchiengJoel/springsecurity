package com.joe.springsecurity.auth.controller;

import com.joe.springsecurity.auth.dto.AuthenticationResponse;
import com.joe.springsecurity.auth.dto.UserDTO;
import com.joe.springsecurity.auth.model.Role;
import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.UserRepository;
import com.joe.springsecurity.auth.service.AuthenticationService;
import com.joe.springsecurity.auth.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/auth")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthenticationController(AuthenticationService authService, UserRepository userRepository, JwtService jwtService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody User request) {
        try {
            AuthenticationResponse response = authService.register(request);
            if ("User already exists".equals(response.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthenticationResponse(null, null, "Error registering user: " + e.getMessage()));
        }
    }

    // Login user
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody User request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    // Refresh access token
    @PostMapping("/refresh_token")
    public ResponseEntity refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return authService.refreshToken(request, response);
    }

    // Get User Details
    @GetMapping("/user_details")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN' )")
    public ResponseEntity<UserDTO> getUserDetails(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDTO userDTO = new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getUsername(), user.getRoles());

        return ResponseEntity.ok(userDTO);
    }

    // Assign role to user
    @PostMapping("/assign_role/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> assignRole(@PathVariable Long userId, @RequestParam Role role) {
        String message = authService.assignRoleToUser(userId, role);
        return ResponseEntity.ok(message);
    }

    // Remove role from user
    @PostMapping("/remove_role/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> removeRole(@PathVariable Long userId, @RequestParam Role role) {
        String message = authService.removeRoleFromUser(userId, role);
        return ResponseEntity.ok(message);
    }
}

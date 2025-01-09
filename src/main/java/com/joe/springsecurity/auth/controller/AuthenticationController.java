package com.joe.springsecurity.auth.controller;

import com.joe.springsecurity.auth.dto.AuthenticationResponse;
import com.joe.springsecurity.auth.dto.SwitchCompanyRequest;
import com.joe.springsecurity.auth.dto.UserDTO;
import com.joe.springsecurity.auth.model.Role;
import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.UserRepository;
import com.joe.springsecurity.auth.service.AuthenticationService;
import com.joe.springsecurity.auth.service.JwtService;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.company.repo.CompanyRepository;
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
@CrossOrigin(origins = "http://localhost:4200")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CompanyRepository companyRepository;

    public AuthenticationController(AuthenticationService authService, UserRepository userRepository, JwtService jwtService, CompanyRepository companyRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.companyRepository = companyRepository;
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
//                    .body(new AuthenticationResponse(null, null, "Error registering user: " + e.getMessage()));
                    .body(new AuthenticationResponse(null, null, "Error registering user: " + e.getMessage(), null, null, null, null, null, null));
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

        UserDTO userDTO = new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail(), user.getRoles(), user.getCompanies());

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

    @PostMapping("/switch_company")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<String> switchCompany(@RequestBody SwitchCompanyRequest request) {
        User user = authService.getCurrentUser();

        Company newCompany = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Ensure the user belongs to the company they are switching to
        if (!user.getCompanies().contains(newCompany)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not belong to this company");
        }

        // Set the new company for the user's session (you may store it in a session or token)
        // For simplicity, this example assumes a request body that contains company info
        return ResponseEntity.ok("Switched to company: " + newCompany.getName());
    }
}

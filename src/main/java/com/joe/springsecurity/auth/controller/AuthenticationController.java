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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

/**
 * Handles authentication-related endpoints such as login, registration, and company switching.
 * Uses JWT for access tokens and refresh tokens stored in HttpOnly cookies for security.
 */
@RestController
@RequestMapping("/api/v2/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true",
        allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final CompanyRepository companyRepository;

    @Autowired
    public AuthenticationController(AuthenticationService authService, UserRepository userRepository,
                                    JwtService jwtService, CompanyRepository companyRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.companyRepository = companyRepository;
    }

    /**
     * Registers a new user and returns access/refresh tokens.
     * @param request User registration details.
     * @param response HTTP response to set refresh token cookie.
     * @return ResponseEntity with AuthenticationResponse or error details.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody User request, HttpServletResponse response) {
        logger.info("Register request received for username: {}", request.getUsername());
        try {
            AuthenticationResponse authResponse = authService.register(request);
            setRefreshTokenCookie(response, authResponse.getRefreshToken());
            if ("User already exists".equals(authResponse.getMessage())) {
                logger.warn("Registration failed: User {} already exists", request.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(authResponse);
            }
            logger.info("User {} registered successfully", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (Exception e) {
            logger.error("Error registering user {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthenticationResponse(null, null, "Error registering user: " + e.getMessage()));
        }
    }

    /**
     * Authenticates a user and returns access/refresh tokens.
     * @param request User login credentials.
     * @param response HTTP response to set refresh token cookie.
     * @return ResponseEntity with AuthenticationResponse.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody User request, HttpServletResponse response) {
        logger.info("Login request received for username: {}", request.getUsername());
        AuthenticationResponse authResponse = authService.authenticate(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        logger.info("User {} logged in successfully", request.getUsername());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Refreshes the access token using the refresh token from cookies.
     * @param request HTTP request containing refresh token cookie.
     * @param response HTTP response to update refresh token cookie.
     * @return ResponseEntity with new tokens or error status.
     */
    @PostMapping("/refresh_token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Refresh token request received");
        ResponseEntity<AuthenticationResponse> authResponse = authService.refreshToken(request, response);
        if (authResponse.getStatusCode().is2xxSuccessful()) {
            setRefreshTokenCookie(response, authResponse.getBody().getRefreshToken());
            logger.info("Tokens refreshed successfully for user");
        } else {
            logger.warn("Refresh token failed with status: {}", authResponse.getStatusCode());
        }
        return authResponse;
    }

    /**
     * Retrieves details of the authenticated user.
     * @param authentication Spring Security Authentication object.
     * @return ResponseEntity with UserDTO or error.
     */
    @GetMapping("/user_details")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<UserDTO> getUserDetails(Authentication authentication) {
        logger.info("Fetching user details for username: {}", authentication.getName());
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found: {}", username);
                        return new RuntimeException("User not found");
                    });
            UserDTO userDTO = new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getUsername(),
                    user.getEmail(), user.getRoles(), user.getCompanies());
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            logger.error("Error fetching user details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Switches the user's active company and issues new tokens.
     * @param request Request containing the new company ID.
     * @param response HTTP response to set refresh token cookie.
     * @return ResponseEntity with AuthenticationResponse or error.
     */
    @PostMapping("/switch_company")
    public ResponseEntity<AuthenticationResponse> switchCompany(@RequestBody SwitchCompanyRequest request, HttpServletResponse response) {
        logger.info("Switch company request received for companyId: {}", request.getCompanyId());
        try {
            AuthenticationResponse authResponse = authService.switchCompany(request.getCompanyId());
            setRefreshTokenCookie(response, authResponse.getRefreshToken());
            logger.info("Company switched successfully to ID: {}", request.getCompanyId());
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            logger.error("Error switching company: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new AuthenticationResponse(null, null, e.getMessage()));
        }
    }

    /**
     * Utility method to set the refresh token in an HttpOnly, Secure cookie.
     * @param response HTTP response to add the cookie.
     * @param refreshToken The refresh token to set.
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader("Set-Cookie",
                "refresh_token=" + refreshToken + "; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=604800");
    }
}

//    @PostMapping("/switch_company")
//    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
//    public ResponseEntity<String> switchCompany(@RequestBody SwitchCompanyRequest request) {
//        User user = authService.getCurrentUser();
//
//        Company newCompany = companyRepository.findById(request.getCompanyId())
//                .orElseThrow(() -> new RuntimeException("Company not found"));
//
//        // Ensure the user belongs to the company they are switching to
//        if (!user.getCompanies().contains(newCompany)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not belong to this company");
//        }
//
//        // Set the new company for the user's session (you may store it in a session or token)
//        // For simplicity, this example assumes a request body that contains company info
//        return ResponseEntity.ok("Switched to company: " + newCompany.getName());
//    }


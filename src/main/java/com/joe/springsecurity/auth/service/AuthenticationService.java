package com.joe.springsecurity.auth.service;


import com.joe.springsecurity.auth.dto.AuthenticationResponse;
import com.joe.springsecurity.auth.model.Role;
import com.joe.springsecurity.auth.model.Token;
import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.TokenRepository;
import com.joe.springsecurity.auth.repo.UserRepository;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.company.repo.CompanyRepository;
import com.joe.springsecurity.utils.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling user authentication, registration, and company switching.
 * Manages JWT token generation and persistence.
 */
@Service
@Transactional
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final CompanyRepository companyRepository;
    private final EmailService emailService;

    @Autowired
    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService,
                                 TokenRepository tokenRepository, AuthenticationManager authenticationManager,
                                 CompanyRepository companyRepository, EmailService emailService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
        this.companyRepository = companyRepository;
        this.emailService = emailService;
    }

    /**
     * Registers a new user, assigns a default company, and generates tokens.
     * @param request The user data to register.
     * @return AuthenticationResponse with tokens and user details.
     */
    public AuthenticationResponse register(User request) {
        logger.info("Registering new user: {}", request.getUsername());

        // Check if username or email already exists
        if (repository.findByUsername(request.getUsername()).isPresent()) {
            logger.error("Username already exists: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            logger.error("Email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        // Encode password and set default role if not provided
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            request.setRoles(Collections.singleton(Role.ROLE_USER)); // Default role
        }

        // Assign a default company (e.g., first available or create a new one)
        Company defaultCompany = assignDefaultCompany(request);
        request.setCompanies(Collections.singleton(defaultCompany)); // Changed to singleton (Set)

        // Save the new user
        User savedUser = repository.save(request);
        logger.info("User registered successfully: {}", savedUser.getUsername());

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(savedUser, defaultCompany);
        String refreshToken = jwtService.generateRefreshToken(savedUser, defaultCompany);

        // Save tokens
        saveUserToken(accessToken, refreshToken, savedUser);

        // Send welcome email
        sendWelcomeEmail(savedUser);

        return buildAuthResponse(savedUser, accessToken, refreshToken, "User registered successfully", defaultCompany);
    }

    /**
     * Authenticates a user and generates access/refresh tokens with the default company.
     * @param request User login credentials.
     * @return AuthenticationResponse with tokens and user details.
     */
    public AuthenticationResponse authenticate(User request) {
        logger.info("Authenticating user: {}", request.getUsername());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found: {}", request.getUsername());
                    return new RuntimeException("User not found");
                });

        Company defaultCompany = getDefaultCompany(user);
        String accessToken = jwtService.generateAccessToken(user, defaultCompany);
        String refreshToken = jwtService.generateRefreshToken(user, defaultCompany);

        revokeAllTokensByUser(user);
        saveUserToken(accessToken, refreshToken, user);

        sendLoginNotification(user);
        return buildAuthResponse(user, accessToken, refreshToken, "User login was successful", defaultCompany);
    }

    /**
     * Switches the user's active company and generates new tokens.
     * @param companyId The ID of the company to switch to.
     * @return AuthenticationResponse with new tokens and updated company details.
     */
    public AuthenticationResponse switchCompany(Long companyId) {
        logger.info("Switching company to ID: {}", companyId);
        User user = getCurrentUser();

        Company newCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> {
                    logger.error("Company not found with ID: {}", companyId);
                    return new RuntimeException("Company not found with ID: " + companyId);
                });

        if (!user.getCompanies().contains(newCompany)) {
            logger.error("User {} not associated with company: {}", user.getUsername(), newCompany.getName());
            throw new RuntimeException("User is not associated with company: " + newCompany.getName());
        }

        revokeAllTokensByUser(user);
        String accessToken = jwtService.generateAccessToken(user, newCompany);
        String refreshToken = jwtService.generateRefreshToken(user, newCompany);
        saveUserToken(accessToken, refreshToken, user);

        sendCompanySwitchNotification(user, newCompany);
        return buildAuthResponse(user, accessToken, refreshToken, "Company switched to " + newCompany.getName(), newCompany);
    }

    /**
     * Assigns a default company to a new user (e.g., a placeholder or existing company).
     * @param user The user to assign a company to.
     * @return The default Company entity.
     */
    private Company assignDefaultCompany(User user) {
        // Example: Create a new default company or fetch an existing one
        Company defaultCompany = new Company();
        defaultCompany.setName(user.getUsername() + "'s Default Company"); // Customize as needed
        defaultCompany.setPrimaryEmail(user.getEmail());
        defaultCompany.setStatus(true);
        return companyRepository.save(defaultCompany);
    }

    /**
     * Gets the currently authenticated from the security context.
     * @return The current User entity.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("No authenticated user found");
            throw new RuntimeException("User is not authenticated");
        }
        String username = authentication.getName();
        return repository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found in repository: {}", username);
                    return new RuntimeException("User not found");
                });
    }

    /**
     * Retrieves the default company for a user (first in the list).
     * @param user The user to get the default company for.
     * @return The default Company entity.
     */
    private Company getDefaultCompany(User user) {
        if (user.getCompanies().isEmpty()) {
            logger.error("User {} has no associated companies", user.getUsername());
            throw new RuntimeException("User does not belong to any company");
        }
        return user.getCompanies().stream()
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("No companies assigned to user: {}", user.getUsername());
                    return new RuntimeException("No companies assigned");
                });
    }

    /**
     * Builds an AuthenticationResponse with user and company details.
     * @param user The authenticated user.
     * @param accessToken The new access token.
     * @param refreshToken The new refresh token.
     * @param message The response message.
     * @param company The active company.
     * @return AuthenticationResponse object.
     */
    private AuthenticationResponse buildAuthResponse(User user, String accessToken, String refreshToken,
                                                     String message, Company company) {
        List<String> companyNames = user.getCompanies().stream().map(Company::getName).collect(Collectors.toList());
        List<Long> companyIds = user.getCompanies().stream().map(Company::getId).collect(Collectors.toList());
        return new AuthenticationResponse(
                accessToken, refreshToken, message, user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRoles().stream().map(Role::name).collect(Collectors.toList()),
                companyNames, company.getName(), companyIds);
    }

    /**
     * Revokes all existing tokens for a user.
     * @param user The user whose tokens should be revoked.
     */
    private void revokeAllTokensByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllAccessTokensByUser(user.getId());
        if (!validTokens.isEmpty()) {
            validTokens.forEach(t -> t.setLoggedOut(true));
            tokenRepository.saveAll(validTokens);
            logger.info("Revoked {} tokens for user: {}", validTokens.size(), user.getUsername());
        }
    }

    /**
     * Saves new access and refresh tokens for a user.
     * @param accessToken The access token.
     * @param refreshToken The refresh token.
     * @param user The user to associate the tokens with.
     */
    private void saveUserToken(String accessToken, String refreshToken, User user) {
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);
        token.setExpirationDate(jwtService.extractExpiration(accessToken));
        tokenRepository.save(token);
        logger.debug("Saved new token for user: {}", user.getUsername());
    }

    /**
     * Sends a welcome email to the newly registered user.
     * @param user The user to notify.
     */
    private void sendWelcomeEmail(User user) {
        String subject = "Welcome to CMS";
        String text = String.format("Dear %s,\n\nWelcome to CMS! Your account has been successfully created.\n\n" +
                        "Username: %s\nEmail: %s\n\nPlease log in to get started.\n\nBest Regards.",
                user.getFirstName(), user.getUsername(), user.getEmail());
        try {
            emailService.sendEmail(user.getEmail(), subject, text);
            logger.info("Welcome email sent to: {}", user.getEmail());
        } catch (MessagingException e) {
            logger.error("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Sends a login notification email to the user.
     * @param user The user to notify.
     */
    private void sendLoginNotification(User user) {
        String subject = "Login Notification";
        String text = String.format("Dear %s,\n\nWe noticed a successful sign-in to your CMS account: %s.\n\n" +
                        "If you signed in recently, relax and know that you are safe!\n\n" +
                        "If you don’t recognize this sign-in, change your password immediately or contact support.\n\nBest Regards.",
                user.getFirstName(), user.getEmail());
        try {
            emailService.sendEmail(user.getEmail(), subject, text);
            logger.info("Login notification sent to: {}", user.getEmail());
        } catch (MessagingException e) {
            logger.error("Failed to send login notification to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    /**
     * Sends a company switch notification email to the user.
     * @param user The user to notify.
     * @param company The newly selected company.
     */
    private void sendCompanySwitchNotification(User user, Company company) {
        String subject = "Company Switch Notification";
        String text = String.format("Dear %s,\n\nYou have successfully switched to company: %s.\n\n" +
                        "If this was not you, please contact support immediately.\n\nBest Regards.",
                user.getFirstName(), company.getName());
        try {
            emailService.sendEmail(user.getEmail(), subject, text);
            logger.info("Company switch notification sent to: {}", user.getEmail());
        } catch (MessagingException e) {
            logger.error("Failed to send company switch notification to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }


    /**
     * Refreshes the access token using the refresh token from the request cookie.
     * @param request HTTP request containing the refresh token cookie.
     * @param response HTTP response to update the refresh token if needed.
     * @return ResponseEntity with new AuthenticationResponse or error status.
     */
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Processing refresh token request");

        // Extract refresh token from cookie
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null || refreshToken.isEmpty()) {
            logger.warn("No refresh token found in request cookies");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticationResponse(null, null, "Refresh token is missing"));
        }

        // Extract username from token
        String username = jwtService.extractUsername(refreshToken);
        if (username == null) {
            logger.warn("Invalid refresh token: no username found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticationResponse(null, null, "Invalid refresh token"));
        }

        // Fetch user
        User user = repository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found for refresh token: {}", username);
                    return new RuntimeException("User not found");
                });

        // Validate refresh token using JwtService's isValidRefreshToken
        if (!jwtService.isValidRefreshToken(refreshToken, user)) {
            logger.warn("Invalid or expired refresh token for user: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticationResponse(null, null, "Invalid or expired refresh token"));
        }

        // Get current company from token (since your tokens include companyId)
        Long companyId = jwtService.extractCompanyId(refreshToken);
        Company currentCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> {
                    logger.error("Company not found for ID: {}", companyId);
                    return new RuntimeException("Company not found");
                });

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user, currentCompany);
        String newRefreshToken = jwtService.generateRefreshToken(user, currentCompany);

        // Revoke old token and save new ones
        revokeAllTokensByUser(user);
        saveUserToken(newAccessToken, newRefreshToken, user);

        logger.info("Tokens refreshed successfully for user: {}", username);
        return ResponseEntity.ok(buildAuthResponse(user, newAccessToken, newRefreshToken, "Tokens refreshed successfully", currentCompany));
    }


    /**
     * Extracts the refresh token from the request cookies.
     * @param request HTTP request containing cookies.
     * @return The refresh token string or null if not found.
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}



//    // User Authentication
//    public AuthenticationResponse authenticate(User request) {
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getUsername(),
//                        request.getPassword()
//                )
//        );
//
//        // Get user details from database
//        User user = repository.findByUsername(request.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Ensure the user has companies assigned (i.e., they are linked to a company)
//        if (user.getCompanies().isEmpty()) {
//            throw new RuntimeException("User does not belong to any company");
//        }
//
//        // Generate new JWT tokens
//        String accessToken = jwtService.generateAccessToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user);
//
//        // Revoke any existing tokens for the user
//        revokeAllTokensByUser(user);
//
//        // Save new tokens in the database
//        saveUserToken(accessToken, refreshToken, user);
//
//        // Send email notification for successful login
//        String subject = "Login Notification";
//        String text = "Dear " + user.getFirstName() + ",\n\n" +
//                "We noticed a successfull sign-in to your CMS account: " + user.getEmail() +".\n\n" +
//                "If you signed-in recently, relax and know that you are safe! \n\n" +
//                "But if you don’t recognize this sign-in, we recommend you change your password immediately or " +
//                "contact your System Admin or support team. \n\n" +
//                "Best Regards.";
//        try {
//            emailService.sendEmail(user.getEmail(), subject, text);
//        } catch (MessagingException e) {
//            logger.error("Error sending email to " + user.getEmail(), e);  // Handle email sending errors
//        }
//
//        // Return authentication response with tokens
//        //return new AuthenticationResponse(accessToken, refreshToken, "User login was successful");
//        return new AuthenticationResponse(
//                accessToken,
//                refreshToken,
//                "User login was successful",
//                user.getEmail(),
//                user.getFirstName(),
//                user.getLastName(),
//                user.getRoles().stream().map(Role::name).collect(Collectors.toList()),
//                user.getCompanies().stream().map(Company::getName).collect(Collectors.toList())
//        );
//    }


// User Registration
//    public AuthenticationResponse register(User request) {
//        if (repository.findByUsername(request.getUsername()).isPresent()) {
//            return new AuthenticationResponse(null, null, "User already exists");
//        }
//
//        User user = new User();
//        user.setFirstName(request.getFirstName());
//        user.setLastName(request.getLastName());
//        user.setUsername(request.getUsername());
//        user.setEmail(request.getEmail());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//
//        // If no roles are provided, default to ROLE_USER
//        Set<Role> roles = request.getRoles();
//        if (roles == null || roles.isEmpty()) {
//            roles = new HashSet<>();
//            roles.add(Role.ROLE_USER);  // Default to ROLE_USER
//        }
//        user.setRoles(roles);  // Set the roles for the user
//
//        // Check if this is the first registration and assign the default companies
//        user = repository.save(user); // Ensure that roles are persisted
//
//        // Automatically assign the default companies
//        if (companyRepository.count() == 0) {
//            Company companyA = new Company();
//            companyA.setName("Company A");
//            companyRepository.save(companyA);
//
//            Company companyB = new Company();
//            companyB.setName("Company B");
//            companyRepository.save(companyB);
//        }
//
//        // Assign default companies to the user
//        Company companyA = companyRepository.findByName("Company A").orElseThrow(() -> new RuntimeException("Company Not Found"));
//        Company companyB = companyRepository.findByName("Company B").orElseThrow(() -> new RuntimeException("Company Not Found"));
//        user.getCompanies().add(companyA);
//        user.getCompanies().add(companyB);
//        repository.save(user); // Ensure companies are saved
//
//        // Send email notification to the new user
//        String subject = "Welcome to the system!";
//        String text = "Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n" +
//                "Your account has been created successfully.\n\n" +
//                "Username: " + user.getUsername() + "\n" +
//                "Password: " + request.getPassword();  // You may want to generate a password reset link instead.
//        try {
//            emailService.sendEmail(user.getEmail(), subject, text);
//        } catch (MessagingException e) {
//            e.printStackTrace();  // Handle email sending errors
//        }
//
//        // Generate JWT Tokens
//        String accessToken = jwtService.generateAccessToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user);
//
//        saveUserToken(accessToken, refreshToken, user);
//
//        return new AuthenticationResponse(accessToken, refreshToken, "User registration was successful");
//    }

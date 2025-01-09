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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {

        private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

        private final UserRepository repository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final TokenRepository tokenRepository;
        private final AuthenticationManager authenticationManager;
        private final CompanyRepository companyRepository; // Inject CompanyRepository to manage companies
        private final UserRepository userRepository;
        private final EmailService emailService;
        private final PasswordResetTokenService passwordResetTokenService;

        public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, TokenRepository tokenRepository, AuthenticationManager authenticationManager, CompanyRepository companyRepository, UserRepository userRepository, EmailService emailService, PasswordResetTokenService passwordResetTokenService) {
            this.repository = repository;
            this.passwordEncoder = passwordEncoder;
            this.jwtService = jwtService;
            this.tokenRepository = tokenRepository;
            this.authenticationManager = authenticationManager;
            this.companyRepository = companyRepository; // Initialize the Company repository
            this.userRepository = userRepository;
            this.emailService = emailService;
            this.passwordResetTokenService = passwordResetTokenService;
        }

        public AuthenticationResponse authenticate(User request) {
            // Authenticate the user's credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Retrieve the user from the database
            User user = repository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Ensure the user has companies assigned (they must be linked to at least one company)
            if (user.getCompanies().isEmpty()) {
                throw new RuntimeException("User does not belong to any company");
            }

            // Set the default company (you can customize this logic based on your needs)
            Company defaultCompany = user.getCompanies().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No companies assigned"));

            // Generate new JWT tokens, passing both the user and the default company
            String accessToken = jwtService.generateAccessToken(user, defaultCompany);
            String refreshToken = jwtService.generateRefreshToken(user, defaultCompany);

            // Revoke any existing tokens for the user
            revokeAllTokensByUser(user);

            // Save the new tokens in the database
            saveUserToken(accessToken, refreshToken, user);

            // Send email notification for successful login
            String subject = "Login Notification";
            String text = "Dear " + user.getFirstName() + ",\n\n" +
                    "We noticed a successful sign-in to your CMS account: " + user.getEmail() + ".\n\n" +
                    "If you signed-in recently, relax and know that you are safe! \n\n" +
                    "But if you don’t recognize this sign-in, we recommend you change your password immediately or " +
                    "contact your System Admin or support team. \n\n" +
                    "Best Regards.";
            try {
                emailService.sendEmail(user.getEmail(), subject, text);
            } catch (MessagingException e) {
                logger.error("Error sending email to " + user.getEmail(), e);  // Handle email sending errors
            }

            // Return authentication response with tokens and the default company information
            return new AuthenticationResponse(
                    accessToken,
                    refreshToken,
                    "User login was successful",
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRoles().stream().map(Role::name).collect(Collectors.toList()),
                    user.getCompanies().stream().map(Company::getName).collect(Collectors.toList()),
                    defaultCompany.getName() // Include default company in the response
            );
        }

        public AuthenticationResponse register(User request) throws MessagingException {
            // Check if the user already exists
            if (repository.findByUsername(request.getUsername()).isPresent()) {
                return new AuthenticationResponse(null, null, "User already exists", null, null,
                        null, null, null, null);
            }

            // Create a new User
            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));  // Encode password securely

            // Set roles for the user
            Set<Role> roles = request.getRoles();
            if (roles == null || roles.isEmpty()) {
                roles = new HashSet<>();
                roles.add(Role.ROLE_USER);  // Default to ROLE_USER
            }
            user.setRoles(roles);

            // Save the user to the database
            user = repository.save(user);

            // Automatically assign a default company if none exists
            if (companyRepository.count() == 0) {
                Company companyA = new Company();
                companyA.setName("Test Company Ltd");
                companyRepository.save(companyA);
            }

            // Assign the default company to the user
            Company companyA = companyRepository.findByName("Test Company Ltd")
                    .orElseThrow(() -> new RuntimeException("Company Not Found"));
            user.getCompanies().add(companyA);
            repository.save(user);

            // Generate a password reset token
            String resetToken = passwordResetTokenService.createPasswordResetToken(user);

            // Create the password reset URL
            String resetLink = "https://yourdomain.com/reset-password?token=" + resetToken;

            // Send email with the password reset link
            String subject = "Welcome to the system! Please reset your password";
            String text = "Dear " + user.getFirstName() + " " + user.getLastName() + ",\n\n" +
                    "Your account has been created successfully. Please click the link below to set your password:\n\n" +
                    resetLink;

            emailService.sendEmail(user.getEmail(), subject, text);  // Send email asynchronously

            // Generate JWT Tokens with the user and the assigned company
            String accessToken = jwtService.generateAccessToken(user, companyA); // Pass the user and the company
            String refreshToken = jwtService.generateRefreshToken(user, companyA); // Pass the user and the company

            // Save the generated tokens
            saveUserToken(accessToken, refreshToken, user);

            // Return the authentication response with user and company details
            return new AuthenticationResponse(
                    accessToken,
                    refreshToken,
                    "User registration was successful",
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRoles().stream().map(Role::name).collect(Collectors.toList()),
                    user.getCompanies().stream().map(Company::getName).collect(Collectors.toList()),
                    companyA.getName()  // Pass the default company here
            );
        }

        // Assign a new role to a user
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

        // Ensure the user has at least one company assigned
        if (user.getCompanies().isEmpty()) {
            throw new RuntimeException("User does not belong to any company");
        }

        // Set the default company (you can customize this logic based on your needs)
        Company defaultCompany = user.getCompanies().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No companies assigned"));

        // Check if the refresh token is valid
        if (jwtService.isValidRefreshToken(token, user)) {
            // Generate new JWT tokens, passing both the user and the default company
            String accessToken = jwtService.generateAccessToken(user, defaultCompany);
            String refreshToken = jwtService.generateRefreshToken(user, defaultCompany);

            revokeAllTokensByUser(user);
            saveUserToken(accessToken, refreshToken, user);

            return ResponseEntity.ok(new AuthenticationResponse(
                    accessToken,
                    refreshToken,
                    "New tokens generated", // Adjusted message to be more appropriate
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRoles().stream().map(Role::name).collect(Collectors.toList()),
                    user.getCompanies().stream().map(Company::getName).collect(Collectors.toList()),
                    defaultCompany.getName() // Pass the default company in the response
            ));
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }



    // This method will get the current user's username from the JWT token
        public String getCurrentUserUsername() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                throw new RuntimeException("User is not authenticated");
            }
            return authentication.getName();  // Get the username from the SecurityContext
        }

        // You can also add this method to get the current user, if needed
        public User getCurrentUser() {
            String username = getCurrentUserUsername();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
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

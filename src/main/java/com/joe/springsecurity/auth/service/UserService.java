package com.joe.springsecurity.auth.service;

import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.UserRepository;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.company.repo.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JwtService jwtService;

    @Autowired
    public UserService(AuthenticationService authenticationService, UserRepository userRepository, CompanyRepository companyRepository, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jwtService = jwtService;
    }

    // Retrieve the current logged-in user based on JWT or session
    public User getCurrentUser() {
        String username = authenticationService.getCurrentUser().getUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Retrieve the current user's associated company (assuming a user has one or more companies)
    public Company getCurrentUserCompany() {
        User user = getCurrentUser();
        if (user.getCompanies().isEmpty()) {
            throw new RuntimeException("User does not belong to any company");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getCredentials() == null) {
            throw new IllegalStateException("Authentication context is missing or invalid");
        }
        String token = authentication.getCredentials().toString();
        Long jwtCompanyId = jwtService.extractCompanyId(token);
        return user.getCompanies().stream()
                .filter(company -> company.getId().equals(jwtCompanyId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not associated with company ID from JWT: " + jwtCompanyId));
    }

    // Assign a company to a user
    public String assignCompanyToUser(Long userId, Long companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (user.getCompanies().contains(company)) {
            return "User is already assigned to this company.";
        }

        user.getCompanies().add(company);  // Add the company to the user's companies set
        userRepository.save(user);  // Save the updated user

        return "Company " + company.getName() + " assigned to user " + user.getUsername();
    }

    // Remove a company from a user
    public String removeCompanyFromUser(Long userId, Long companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        if (!user.getCompanies().contains(company)) {
            return "User is not assigned to this company.";
        }

        user.getCompanies().remove(company);  // Remove the company from the user's companies set

        // Ensure user has at least one company or assign a default company
        if (user.getCompanies().isEmpty()) {
            // Optionally, assign a default company or return an error
            Company defaultCompany = companyRepository.findByName("Default Company")
                    .orElseThrow(() -> new RuntimeException("Default company not found"));
            user.getCompanies().add(defaultCompany);
        }

        userRepository.save(user);  // Save the updated user

        return "Company " + company.getName() + " removed from user " + user.getUsername();
    }
}

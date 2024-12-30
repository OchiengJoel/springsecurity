package com.joe.springsecurity.init;

import com.joe.springsecurity.auth.model.Role;
import com.joe.springsecurity.auth.model.User;
import com.joe.springsecurity.auth.repo.UserRepository;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.company.repo.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class InitialDataService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(InitialDataService.class);

    @Autowired
    public InitialDataService(CompanyRepository companyRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void initDefaultCompaniesAndUser() {
        // Add default companies if none exist
        if (!companyRepository.findAll().iterator().hasNext()) {
            Company companyA = new Company();
            companyA.setName("Company A");
            companyRepository.save(companyA);

            Company companyB = new Company();
            companyB.setName("Company B");
            companyRepository.save(companyB);

            logger.info("Default companies added: Company A and Company B");
        } else {
            logger.info("Companies already exist in the database.");
        }

        // Add default super admin user if no user exists
        if (!userRepository.findByUsername("Johnny").isPresent()) {
            User newUser = createSuperAdminUser();
            userRepository.save(newUser);

            logger.info("Default super admin user created: Johnny Doe");
        } else {
            logger.info("User already exists in the database.");
        }
    }

    private User createSuperAdminUser() {
        // Create the user
        User user = new User();
        user.setFirstName("Johnny");
        user.setLastName("Doe");
        user.setUsername("Johnny");
        user.setPassword(passwordEncoder.encode("Admin123*"));

        // Set role for super admin
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_SUPER_ADMIN);
        user.setRoles(roles);

        // Assign the user to Company A (ensure Company A exists)
        Company companyA = companyRepository.findByName("Company A")
                .orElseThrow(() -> new IllegalStateException("Company A not found"));
        user.getCompanies().add(companyA);

        return user;
    }
}

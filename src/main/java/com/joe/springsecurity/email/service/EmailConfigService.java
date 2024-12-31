package com.joe.springsecurity.email.service;

import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.company.repo.CompanyRepository;
import com.joe.springsecurity.email.dto.EmailConfigDTO;
import com.joe.springsecurity.email.model.EmailConfig;
import com.joe.springsecurity.email.repo.EmailConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailConfigService {

    private final EmailConfigRepository emailConfigRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public EmailConfigService(EmailConfigRepository emailConfigRepository, CompanyRepository companyRepository) {
        this.emailConfigRepository = emailConfigRepository;
        this.companyRepository = companyRepository;
    }

    public EmailConfigDTO createEmailConfig(Long companyId, EmailConfigDTO emailConfigDTO) {
        // Check if the company exists
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("Company does not exist"));

        // Create the email config
        EmailConfig emailConfig = new EmailConfig();
        emailConfig.setCompany(company);
        emailConfig.setSmtpHost(emailConfigDTO.getSmtpHost());
        emailConfig.setSmtpPort(emailConfigDTO.getSmtpPort());
        emailConfig.setSmtpUsername(emailConfigDTO.getSmtpUsername());
        emailConfig.setSmtpPassword(emailConfigDTO.getSmtpPassword());
        emailConfig.setFromAddress(emailConfigDTO.getFromAddress());
        emailConfig.setActive(emailConfigDTO.isActive()); // Set the active status

        EmailConfig savedEmailConfig = emailConfigRepository.save(emailConfig);

        // Return the EmailConfigDTO with the active status
        return new EmailConfigDTO(
                savedEmailConfig.getId(),
                savedEmailConfig.getSmtpHost(),
                savedEmailConfig.getSmtpPort(),
                savedEmailConfig.getSmtpUsername(),
                savedEmailConfig.getSmtpPassword(),
                savedEmailConfig.getFromAddress(),
                savedEmailConfig.getCompany().getId(),
                savedEmailConfig.isActive() // Pass the active status to the DTO
        );
    }

    public EmailConfigDTO updateEmailConfig(Long companyId, EmailConfigDTO emailConfigDTO) {
        // Check if the company exists
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("Company does not exist"));

        // Find the email config for the company
        EmailConfig existingEmailConfig = emailConfigRepository.findByCompanyId(companyId);
        if (existingEmailConfig == null) {
            throw new RuntimeException("Email configuration does not exist for this company.");
        }

        // Update the email config
        existingEmailConfig.setSmtpHost(emailConfigDTO.getSmtpHost());
        existingEmailConfig.setSmtpPort(emailConfigDTO.getSmtpPort());
        existingEmailConfig.setSmtpUsername(emailConfigDTO.getSmtpUsername());
        existingEmailConfig.setSmtpPassword(emailConfigDTO.getSmtpPassword());
        existingEmailConfig.setFromAddress(emailConfigDTO.getFromAddress());
        existingEmailConfig.setActive(emailConfigDTO.isActive()); // Update the active status

        EmailConfig updatedEmailConfig = emailConfigRepository.save(existingEmailConfig);

        // Return the updated EmailConfigDTO with the active status
        return new EmailConfigDTO(
                updatedEmailConfig.getId(),
                updatedEmailConfig.getSmtpHost(),
                updatedEmailConfig.getSmtpPort(),
                updatedEmailConfig.getSmtpUsername(),
                updatedEmailConfig.getSmtpPassword(),
                updatedEmailConfig.getFromAddress(),
                updatedEmailConfig.getCompany().getId(),
                updatedEmailConfig.isActive() // Pass the active status to the DTO
        );
    }

    // Activate the email configuration for a company
    public EmailConfigDTO activateEmailConfig(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company does not exist"));

        EmailConfig emailConfig = emailConfigRepository.findByCompany(company)
                .orElseThrow(() -> new RuntimeException("Email configuration does not exist for this company"));

        // Activate the email configuration
        emailConfig.setActive(true);
        EmailConfig updatedEmailConfig = emailConfigRepository.save(emailConfig);

        return new EmailConfigDTO(updatedEmailConfig);
    }

    // Deactivate the email configuration for a company
    public EmailConfigDTO deactivateEmailConfig(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company does not exist"));

        EmailConfig emailConfig = emailConfigRepository.findByCompany(company)
                .orElseThrow(() -> new RuntimeException("Email configuration does not exist for this company"));

        // Deactivate the email configuration
        emailConfig.setActive(false);
        EmailConfig updatedEmailConfig = emailConfigRepository.save(emailConfig);

        return new EmailConfigDTO(updatedEmailConfig);
    }
}

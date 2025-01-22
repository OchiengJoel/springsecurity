package com.joe.springsecurity.company.service;

import com.joe.springsecurity.company.dto.CompanyDTO;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.company.repo.CompanyRepository;
import com.joe.springsecurity.country.model.Country;
import com.joe.springsecurity.country.repo.CountryRepository;
import com.joe.springsecurity.email.dto.EmailConfigDTO;
import com.joe.springsecurity.email.model.EmailConfig;
import com.joe.springsecurity.email.repo.EmailConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CompanyService {

    @Autowired
    private final CompanyRepository companyRepository;
    private final EmailConfigRepository emailConfigRepository;
    private final CountryRepository countryRepository;

    public CompanyService(CompanyRepository companyRepository, EmailConfigRepository emailConfigRepository, CountryRepository countryRepository) {
        this.companyRepository = companyRepository;
        this.emailConfigRepository = emailConfigRepository;
        this.countryRepository = countryRepository;
    }

    // Create a new company
    public CompanyDTO createCompany(CompanyDTO companyDTO) {
        Optional<Company> existingCompany = companyRepository.findByName(companyDTO.getName());
        if (existingCompany.isPresent()) {
            throw new RuntimeException("Company with name " + companyDTO.getName() + " already exists.");
        }

        // Fetch the associated Country from the database based on CountryDTO
        //Country country = countryRepository.findById(companyDTO.getCountry().getId())
        Country country = countryRepository.findById(companyDTO.getCountryId())
                .orElseThrow(() -> new RuntimeException("Country not found"));


        Company company = new Company();
        company.setName(companyDTO.getName());
        company.setPrimaryEmail(companyDTO.getPrimaryEmail());
        company.setSecondaryEmail(companyDTO.getSecondaryEmail());
        company.setPrimaryContact(companyDTO.getPrimaryContact());
        company.setSecondaryContact(companyDTO.getSecondaryContact());
        company.setTown(companyDTO.getTown());
        company.setAddress(companyDTO.getAddress());
        company.setRegistration(companyDTO.getRegistration());
        company.setTax_id(companyDTO.getTax_id());
        company.setCountry(country); // Set the Country entity
        company.setStatus(companyDTO.isStatus()); // Set status

        Company savedCompany = companyRepository.save(company);

        return new CompanyDTO(savedCompany); // Convert saved Company to DTO
    }

    public CompanyDTO updateCompany(Long companyId, CompanyDTO companyDTO) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company with ID " + companyId + " not found."));

        // Fetch the associated Country from the database based on CountryDTO
//        Country country = countryRepository.findById(companyDTO.getCountry().getId())
//                .orElseThrow(() -> new RuntimeException("Country not found"));

        Country country = countryRepository.findById(companyDTO.getCountryId())
                .orElseThrow(() -> new RuntimeException("Country not found"));

        company.setName(companyDTO.getName());
        company.setPrimaryEmail(companyDTO.getPrimaryEmail());
        company.setSecondaryEmail(companyDTO.getSecondaryEmail());
        company.setPrimaryContact(companyDTO.getPrimaryContact());
        company.setSecondaryContact(companyDTO.getSecondaryContact());
        company.setTown(companyDTO.getTown());
        company.setAddress(companyDTO.getAddress());
        company.setRegistration(companyDTO.getRegistration());
        company.setTax_id(companyDTO.getTax_id());
        company.setCountry(country); // Set the Country entity
        company.setStatus(companyDTO.isStatus()); // Set status

        Company updatedCompany = companyRepository.save(company);

        return new CompanyDTO(updatedCompany); // Return the updated CompanyDTO
    }




    public Page<CompanyDTO> getAllCompanies(Pageable pageable) {
        Page<Company> companies = companyRepository.findAll(pageable);

        // Convert Page<Company> to Page<CompanyDTO>
        return companies.map(CompanyDTO::new);
    }

    // Get a single company by ID
    public CompanyDTO getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with ID " + id));

        return new CompanyDTO(company);
    }

    // Delete a company by ID
    public void deleteCompany(Long id) {
        Optional<Company> company = companyRepository.findById(id);
        if (!company.isPresent()) {
            throw new RuntimeException("Company not found with ID " + id);
        }

        companyRepository.deleteById(id);
    }

    // Enable a company
    public CompanyDTO enableCompany(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() ->
                new RuntimeException("Company not found with ID: " + companyId));

        company.setStatus(true);
        Company updatedCompany = companyRepository.save(company);
        return new CompanyDTO(updatedCompany);
    }

    // Disable a company
    public CompanyDTO disableCompany(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() ->
                new RuntimeException("Company not found with ID: " + companyId));

        company.setStatus(false);
        Company updatedCompany = companyRepository.save(company);
        return new CompanyDTO(updatedCompany);
    }

    // Get email configuration for a specific company
    public EmailConfigDTO getEmailConfig(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company does not exist"));

        EmailConfig emailConfig = emailConfigRepository.findByCompany(company)
                .orElseThrow(() -> new RuntimeException("Email configuration does not exist for this company"));

        return new EmailConfigDTO(emailConfig);
    }
}


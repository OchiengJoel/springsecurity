package com.joe.springsecurity.company.service;

import com.joe.springsecurity.company.dto.CompanyDTO;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.company.repo.CompanyRepository;
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

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // Create a new company
    public CompanyDTO createCompany(CompanyDTO companyDTO) {
        // Check for existing company with the same name to avoid duplicates
        Optional<Company> existingCompany = companyRepository.findByName(companyDTO.getName());
        if (existingCompany.isPresent()) {
            throw new RuntimeException("Company with name " + companyDTO.getName() + " already exists.");
        }

        // Create and save new company
        Company company = new Company();
        company.setName(companyDTO.getName());
        Company savedCompany = companyRepository.save(company);

        return new CompanyDTO(savedCompany);
    }

    // Update an existing company
    public CompanyDTO updateCompany(Long companyId, CompanyDTO companyDTO) {
        // Check if the company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company with ID " + companyId + " not found."));

        // Update company details
        company.setName(companyDTO.getName());
        Company updatedCompany = companyRepository.save(company);

        return new CompanyDTO(updatedCompany);
    }

    // Get all companies
//    public Iterable<CompanyDTO> getAllCompanies() {
//        Iterable<Company> companies = companyRepository.findAll();
//        // Convert Iterable to List and then use stream()
//        return StreamSupport.stream(companies.spliterator(), false)
//                .map(CompanyDTO::new)
//                .collect(Collectors.toList());
//    }

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



}

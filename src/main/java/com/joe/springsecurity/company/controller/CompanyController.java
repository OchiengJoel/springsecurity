package com.joe.springsecurity.company.controller;

import com.joe.springsecurity.company.dto.CompanyDTO;
import com.joe.springsecurity.company.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/companies")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // Create a new company
    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<CompanyDTO> createCompany(@RequestBody CompanyDTO companyDTO) {
        try {
            CompanyDTO createdCompany = companyService.createCompany(companyDTO);
            return ResponseEntity.ok(createdCompany);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Update an existing company
    @PutMapping("/update/{companyId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<CompanyDTO> updateCompany(@PathVariable Long companyId, @RequestBody CompanyDTO companyDTO) {
        try {
            CompanyDTO updatedCompany = companyService.updateCompany(companyId, companyDTO);
            return ResponseEntity.ok(updatedCompany);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Get all companies
//    @GetMapping
//    public ResponseEntity<List<CompanyDTO>> getAllCompanies() {
//        List<CompanyDTO> companies = (List<CompanyDTO>) companyService.getAllCompanies();
//        return ResponseEntity.ok(companies);
//    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Page<CompanyDTO>> getAllCompanies(Pageable pageable) {
        Page<CompanyDTO> companyDTOs = companyService.getAllCompanies(pageable);
        return ResponseEntity.ok(companyDTOs);
    }

    // Get a single company by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<CompanyDTO> getCompanyById(@PathVariable Long id) {
        try {
            CompanyDTO company = companyService.getCompanyById(id);
            return ResponseEntity.ok(company);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a company by ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        try {
            companyService.deleteCompany(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
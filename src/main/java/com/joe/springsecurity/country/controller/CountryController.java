package com.joe.springsecurity.country.controller;


import com.joe.springsecurity.country.dto.CountryDTO;
import com.joe.springsecurity.country.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/countries")
@CrossOrigin(origins = "http://localhost:4200")
public class CountryController {

    private final CountryService countryService;

    @Autowired
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    // Create a new country
    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<CountryDTO> createCountry(@RequestBody CountryDTO countryDTO) {
        try {
            CountryDTO createdCountry = countryService.createCountry(countryDTO);
            return ResponseEntity.ok(createdCountry);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/update/{countryId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<CountryDTO> updateCountry(@PathVariable Long countryId, @RequestBody CountryDTO countryDTO) {
        try {
            CountryDTO updatedCountry = countryService.updateCountry(countryId, countryDTO);
            return ResponseEntity.ok(updatedCountry);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Page<CountryDTO>> getAllCountries(Pageable pageable) {
        Page<CountryDTO> countryDTOs = countryService.getAllCountries(pageable);
        return ResponseEntity.ok(countryDTOs);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<CountryDTO> getCountryById(@PathVariable Long id) {
        try {
            CountryDTO country = countryService.getCountryById(id);
            return ResponseEntity.ok(country);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCountry(@PathVariable Long id) {
        try {
            countryService.deleteCountry(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}

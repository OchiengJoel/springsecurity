package com.joe.springsecurity.country.service;


import com.joe.springsecurity.country.dto.CountryDTO;
import com.joe.springsecurity.country.model.Country;
import com.joe.springsecurity.country.repo.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CountryService {

    @Autowired
    public final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    // Create a new Country
    public CountryDTO createCountry(CountryDTO countryDTO) {
        // Check for existing country with the same name to avoid duplicates
        Optional<Country> existingCountry = countryRepository.findByName(countryDTO.getName());
        if (existingCountry.isPresent()) {
            throw new RuntimeException("Country with name " + countryDTO.getName() + " already exists.");
        }

        // Create and save new country
        Country country = new Country();
        country.setName(countryDTO.getName());
        country.setCode(countryDTO.getCode());
        country.setContinent(countryDTO.getContinent());
        Country savedCountry = countryRepository.save(country);

        return new CountryDTO(savedCountry);
    }

    // Update an existing country
    public CountryDTO updateCountry(Long countryId, CountryDTO countryDTO) {
        // Check if the country exists
        Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new RuntimeException("Country with ID " + countryId + " not found."));

        // Update country details
        country.setName(countryDTO.getName());
        country.setCode(countryDTO.getCode());
        country.setContinent(countryDTO.getContinent());
        Country updatedCountry = countryRepository.save(country);

        return new CountryDTO(updatedCountry);
    }

    public Page<CountryDTO> getAllCountries(Pageable pageable) {
        Page<Country> countries = countryRepository.findAll(pageable);

        // Convert Page<Country> to Page<CountryDTO>
        return countries.map(CountryDTO::new);
    }

    // Get a single Country by ID
    public CountryDTO getCountryById(Long id) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Country not found with ID " + id));

        return new CountryDTO(country);
    }

    // Delete a Country by ID
    public void deleteCountry(Long id) {
        Optional<Country> country = countryRepository.findById(id);
        if (!country.isPresent()) {
            throw new RuntimeException("Country not found with ID " + id);
        }

        countryRepository.deleteById(id);
    }

}

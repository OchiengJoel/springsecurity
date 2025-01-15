package com.joe.springsecurity.country.repo;


import com.joe.springsecurity.country.model.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByName(String name);

    // Paginate through all companies
    Page<Country> findAll(Pageable pageable);
}

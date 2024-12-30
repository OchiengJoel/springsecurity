package com.joe.springsecurity.company.repo;

import com.joe.springsecurity.company.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    // Paginate through all companies
    Page<Company> findAll(Pageable pageable);
}

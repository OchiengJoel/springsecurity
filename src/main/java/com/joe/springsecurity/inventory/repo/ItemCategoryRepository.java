package com.joe.springsecurity.inventory.repo;

import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.inventory.enums.ItemType;
import com.joe.springsecurity.inventory.model.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    Optional<ItemCategory> findByName(String name);

    Page<ItemCategory> findByCompany(Company company, Pageable pageable);

    //Checks for an ItemCategory by both name and company:
    Optional<ItemCategory> findByCompanyAndName(Company company, String name);

    long countByCompany(Company company);
}

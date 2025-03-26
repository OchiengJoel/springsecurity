package com.joe.springsecurity.inventory.repo;

import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.model.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

//    List<InventoryItem> findByCompany(Company company);

    // Method to find all inventory items by company with pagination
    Page<InventoryItem> findByCompany(Company company, Pageable pageable);

    // Method to count inventory items by company
    long countByCompany(Company company);

    long countByCompanyId(Long companyId);

    List<InventoryItem> findByCompanyId(Long companyId);

    // Custom method to find InventoryItems by Company and ItemCategory with pagination
    Page<InventoryItem> findByCompanyAndItemCategory(Company company, ItemCategory itemCategory, Pageable pageable);

}

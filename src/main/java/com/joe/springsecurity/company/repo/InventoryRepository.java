package com.joe.springsecurity.company.repo;

import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.inventory.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByCompany(Company company);
}

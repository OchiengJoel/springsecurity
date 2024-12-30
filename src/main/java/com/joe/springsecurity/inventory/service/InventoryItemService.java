package com.joe.springsecurity.inventory.service;

import com.joe.springsecurity.auth.service.UserService;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.errorhandling.UnauthorizedAccessException;
import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.repo.InventoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final UserService userService;

    @Autowired
    public InventoryItemService(InventoryItemRepository inventoryItemRepository, UserService userService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.userService = userService;
    }

    @Transactional
    public InventoryItem createInventoryItem(InventoryItem inventoryItem) {
        Company userCompany = userService.getCurrentUserCompany();
        if (userCompany == null) {
            throw new UnauthorizedAccessException("User does not belong to any company.");
        }
        inventoryItem.setCompany(userCompany); // Set the company of the inventory item to the logged-in user's company.
        return inventoryItemRepository.save(inventoryItem);
    }

    // Get all inventory items of the current user's company
    public Page<InventoryItem> getAllInventoryItems(Pageable pageable) {
        Company userCompany = userService.getCurrentUserCompany();
        if (userCompany == null) {
            throw new UnauthorizedAccessException("User does not belong to any company.");
        }
        return inventoryItemRepository.findByCompany(userCompany, pageable);  // This will now work
    }

    // Get inventory item count for the user's company
    public long getInventoryItemCount() {
        Company userCompany = userService.getCurrentUserCompany();
        if (userCompany == null) {
            throw new UnauthorizedAccessException("User does not belong to any company.");
        }
        return inventoryItemRepository.countByCompany(userCompany);  // This method works as expected
    }

    public InventoryItem getInventoryItemById(Long id) {
        InventoryItem item = inventoryItemRepository.findById(id).orElse(null);
        if (item != null && !item.getCompany().equals(userService.getCurrentUserCompany())) {
            throw new UnauthorizedAccessException("This inventory item does not belong to your company.");
        }
        return item;
    }

    @Transactional
    public InventoryItem updateInventoryItem(Long id, InventoryItem inventoryItem) {
        InventoryItem existingItem = inventoryItemRepository.findById(id).orElse(null);
        if (existingItem == null || !existingItem.getCompany().equals(userService.getCurrentUserCompany())) {
            throw new UnauthorizedAccessException("This inventory item does not belong to your company.");
        }
        inventoryItem.setId(id);  // Keep the original ID
        return inventoryItemRepository.save(inventoryItem);
    }

    @Transactional
    public boolean deleteInventoryItem(Long id) {
        InventoryItem existingItem = inventoryItemRepository.findById(id).orElse(null);
        if (existingItem == null || !existingItem.getCompany().equals(userService.getCurrentUserCompany())) {
            throw new UnauthorizedAccessException("This inventory item does not belong to your company.");
        }
        inventoryItemRepository.deleteById(id);
        return true;
    }
}

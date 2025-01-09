package com.joe.springsecurity.inventory.service;

import com.joe.springsecurity.auth.service.UserService;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.errorhandling.UnauthorizedAccessException;
import com.joe.springsecurity.inventory.dto.InventoryItemDTO;
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
    public InventoryItemDTO createInventoryItem(InventoryItem inventoryItem) {
        Company userCompany = userService.getCurrentUserCompany();
        if (userCompany == null) {
            throw new UnauthorizedAccessException("User does not belong to any company.");
        }
        inventoryItem.setCompany(userCompany); // Set the company for the inventory item

        // Save the inventory item
        InventoryItem createdItem = inventoryItemRepository.save(inventoryItem);

        // Convert the created InventoryItem to InventoryItemDTO and return it
        return new InventoryItemDTO(
                createdItem.getId(),
                createdItem.getName(),
                createdItem.getQuantity(),
                createdItem.getPrice(),
                createdItem.getDescription()
        );
    }

    public Page<InventoryItemDTO> getAllInventoryItems(Pageable pageable) {
        Company userCompany = userService.getCurrentUserCompany();
        if (userCompany == null) {
            throw new UnauthorizedAccessException("User does not belong to any company.");
        }
        Page<InventoryItem> inventoryItems = inventoryItemRepository.findByCompany(userCompany, pageable);

        // Convert InventoryItem entities to InventoryItemDTOs
        return inventoryItems.map(item -> new InventoryItemDTO(
                item.getId(),
                item.getName(),
                item.getQuantity(),
                item.getPrice(),
                item.getDescription()
        ));
    }

    public long getInventoryItemCount() {
        Company userCompany = userService.getCurrentUserCompany();
        if (userCompany == null) {
            throw new UnauthorizedAccessException("User does not belong to any company.");
        }
        return inventoryItemRepository.countByCompany(userCompany);
    }

    public InventoryItemDTO getInventoryItemById(Long id) {
        InventoryItem item = inventoryItemRepository.findById(id).orElse(null);
        if (item == null || !item.getCompany().equals(userService.getCurrentUserCompany())) {
            throw new UnauthorizedAccessException("This inventory item does not belong to your company.");
        }

        // Return the converted DTO
        return new InventoryItemDTO(
                item.getId(),
                item.getName(),
                item.getQuantity(),
                item.getPrice(),
                item.getDescription()
        );
    }

    @Transactional
    public InventoryItemDTO updateInventoryItem(Long id, InventoryItem inventoryItem) {
        InventoryItem existingItem = inventoryItemRepository.findById(id).orElse(null);
        if (existingItem == null || !existingItem.getCompany().equals(userService.getCurrentUserCompany())) {
            throw new UnauthorizedAccessException("This inventory item does not belong to your company.");
        }

        inventoryItem.setId(id); // Keep the original ID
        InventoryItem updatedItem = inventoryItemRepository.save(inventoryItem);

        // Convert the updated entity to DTO
        return new InventoryItemDTO(
                updatedItem.getId(),
                updatedItem.getName(),
                updatedItem.getQuantity(),
                updatedItem.getPrice(),
                updatedItem.getDescription()
        );
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


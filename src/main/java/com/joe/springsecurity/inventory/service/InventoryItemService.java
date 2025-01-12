package com.joe.springsecurity.inventory.service;

import com.joe.springsecurity.auth.service.UserService;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.errorhandling.UnauthorizedAccessException;
import com.joe.springsecurity.inventory.dto.InventoryItemDTO;
import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.model.ItemCategory;
import com.joe.springsecurity.inventory.repo.InventoryItemRepository;
import com.joe.springsecurity.inventory.repo.ItemCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class InventoryItemService {


    private final InventoryItemRepository inventoryItemRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final UserService userService;

    @Autowired
    public InventoryItemService(InventoryItemRepository inventoryItemRepository, ItemCategoryRepository itemCategoryRepository, UserService userService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.userService = userService;
    }

    @Transactional
    public InventoryItemDTO createInventoryItem(InventoryItem inventoryItem, Long itemCategoryId) {
        Company userCompany = userService.getCurrentUserCompany();
        if (userCompany == null) {
            throw new UnauthorizedAccessException("User does not belong to any company.");
        }
        inventoryItem.setCompany(userCompany); // Set the company for the inventory item

        // Ensure the ItemCategory exists
        ItemCategory itemCategory = itemCategoryRepository.findById(itemCategoryId).orElse(null);
        if (itemCategory == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ItemCategory not found.");
        }

        inventoryItem.setItemCategory(itemCategory); // Set the ItemCategory for the InventoryItem

        // Save the inventory item
        InventoryItem createdItem = inventoryItemRepository.save(inventoryItem);

        // Convert the created InventoryItem to InventoryItemDTO and return it
        return new InventoryItemDTO(
                createdItem.getId(),
                createdItem.getName(),
                createdItem.getQuantity(),
                createdItem.getPrice(),
                createdItem.getDescription(),
                createdItem.getItemCategory().getId()
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
                item.getDescription(),
                item.getItemCategory().getId()
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
                item.getDescription(),
                item.getItemCategory().getId()
        );
    }

    @Transactional
    public InventoryItemDTO updateInventoryItem(Long id, InventoryItem inventoryItem, Long itemCategoryId) {
        InventoryItem existingItem = inventoryItemRepository.findById(id).orElse(null);
        if (existingItem == null || !existingItem.getCompany().equals(userService.getCurrentUserCompany())) {
            throw new UnauthorizedAccessException("This inventory item does not belong to your company.");
        }

        // Ensure the ItemCategory exists
        ItemCategory itemCategory = itemCategoryRepository.findById(itemCategoryId).orElse(null);
        if (itemCategory == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ItemCategory not found.");
        }

        inventoryItem.setItemCategory(itemCategory); // Set the new ItemCategory
        inventoryItem.setId(id); // Keep the original ID
        InventoryItem updatedItem = inventoryItemRepository.save(inventoryItem);

        // Convert the updated entity to DTO
        return new InventoryItemDTO(
                updatedItem.getId(),
                updatedItem.getName(),
                updatedItem.getQuantity(),
                updatedItem.getPrice(),
                updatedItem.getDescription(),
                updatedItem.getItemCategory().getId()
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


//    public Set<InventoryItem> getInventoryItemsByCategory(Long categoryId) {
//        ItemCategory itemCategory = itemCategoryRepository.findById(categoryId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategory not found"));
//
//        return itemCategory.getInventoryItems(); // Fetch associated InventoryItems
//    }

    public Page<InventoryItemDTO> getInventoryItemsByCategory(Long categoryId, Pageable pageable) {
        Company userCompany = userService.getCurrentUserCompany();
        if (userCompany == null) {
            throw new UnauthorizedAccessException("User does not belong to any company.");
        }

        // Fetch the ItemCategory based on the provided categoryId
        ItemCategory itemCategory = itemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategory not found"));

        // Fetch the paginated list of InventoryItems belonging to the category
        Page<InventoryItem> inventoryItems = inventoryItemRepository.findByCompanyAndItemCategory(userCompany, itemCategory, pageable);

        // Convert InventoryItem entities to InventoryItemDTOs
        return inventoryItems.map(item -> new InventoryItemDTO(
                item.getId(),
                item.getName(),
                item.getQuantity(),
                item.getPrice(),
                item.getDescription(),
                item.getItemCategory().getId()
        ));
    }

}


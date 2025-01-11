package com.joe.springsecurity.inventory.service;

import com.joe.springsecurity.auth.service.UserService;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.errorhandling.UnauthorizedAccessException;
import com.joe.springsecurity.inventory.dto.InventoryItemDTO;
import com.joe.springsecurity.inventory.dto.ItemCategoryDTO;
import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.model.ItemCategory;
import com.joe.springsecurity.inventory.repo.ItemCategoryRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemCategoryService {

    private final ItemCategoryRepository itemCategoryRepository;
    private final UserService userService;

    @Autowired
    public ItemCategoryService(ItemCategoryRepository itemCategoryRepository, UserService userService) {
        this.itemCategoryRepository = itemCategoryRepository;
        this.userService = userService;
    }

    /**
     * Ensures the current user belongs to a company and throws an UnauthorizedAccessException if not.
     */
    private Company ensureUserBelongsToCompany() {
        Company userCompany = userService.getCurrentUserCompany();
        if (userCompany == null) {
            throw new UnauthorizedAccessException("User does not belong to any company.");
        }
        return userCompany;
    }

    /**
     * Create a new ItemCategory and return its DTO representation.
     *
     * @param itemCategoryDTO Data Transfer Object (DTO) for creating an ItemCategory
     * @return ItemCategoryDTO created ItemCategory in DTO format
     */
    @Transactional
    public ItemCategoryDTO createItemCategory(@Valid ItemCategoryDTO itemCategoryDTO) {

        System.out.println("Creating ItemCategory from DTO: " + itemCategoryDTO);

        // Ensure the user belongs to a company
        Company userCompany = ensureUserBelongsToCompany();

        // Check if category with the same name already exists
        if (itemCategoryRepository.findByName(itemCategoryDTO.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ItemCategory with this name already exists.");
        }

        // Create the ItemCategory entity from the DTO
        ItemCategory itemCategory = new ItemCategory(
                itemCategoryDTO.getName(),
                itemCategoryDTO.getDescription(),
                itemCategoryDTO.getItemType()
        );

        itemCategory.setCompany(userCompany); // Set the company for the ItemCategory

        // Save the ItemCategory entity to the database
        ItemCategory createdItemCategory = itemCategoryRepository.save(itemCategory);

        // Return the DTO version of the created ItemCategory
        return new ItemCategoryDTO(
                createdItemCategory.getId(),
                createdItemCategory.getName(),
                createdItemCategory.getItemType(),
                createdItemCategory.getDescription()
        );
    }

    /**
     * Get all ItemCategories in the system.
     *
     * @return List of ItemCategoryDTOs
     */
    public Page<ItemCategoryDTO> getAllItemCategories(Pageable pageable) {
        // Ensure the user belongs to a company before fetching the data
        Company userCompany = ensureUserBelongsToCompany();

        // Fetch the ItemCategories associated with the user's company
        Page<ItemCategory> itemCategories = itemCategoryRepository.findByCompany(userCompany, pageable);

        // Convert ItemCategory entities to ItemCategoryDTOs before returning
        return itemCategories.map(itemCategory -> new ItemCategoryDTO(
                itemCategory.getId(),
                itemCategory.getName(),
                itemCategory.getItemType(),
                itemCategory.getDescription()
        ));
    }

    /**
     * Get an ItemCategory by its ID.
     *
     * @param id ItemCategory ID
     * @return ItemCategoryDTO
     */
    public ItemCategoryDTO getItemCategoryById(Long id) {
        // Ensure the user belongs to a company before fetching the data
        ensureUserBelongsToCompany();

        ItemCategory itemCategory = itemCategoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategory not found"));

        // Convert ItemCategory entity to DTO before returning
        return new ItemCategoryDTO(
                itemCategory.getId(),
                itemCategory.getName(),
                itemCategory.getItemType(),
                itemCategory.getDescription()
        );
    }

    /**
     * Update an existing ItemCategory.
     *
     * @param id ItemCategory ID
     * @param itemCategoryDTO Data Transfer Object with updated details
     * @return ItemCategoryDTO Updated ItemCategory in DTO format
     */
    @Transactional
    public ItemCategoryDTO updateItemCategory(Long id, @Valid ItemCategoryDTO itemCategoryDTO) {
        // Ensure the user belongs to a company before updating the data
        ensureUserBelongsToCompany();

        // Retrieve the existing ItemCategory to update
        ItemCategory existingItemCategory = itemCategoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategory not found"));

        // Check if the name is already used by another category
        if (itemCategoryRepository.findByName(itemCategoryDTO.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ItemCategory with this name already exists.");
        }

        // Update the entity with new values
        existingItemCategory.setName(itemCategoryDTO.getName());
        existingItemCategory.setDescription(itemCategoryDTO.getDescription());
        existingItemCategory.setItemType(itemCategoryDTO.getItemType());

        // Save the updated entity to the database
        ItemCategory updatedItemCategory = itemCategoryRepository.save(existingItemCategory);

        // Return the updated DTO
        return new ItemCategoryDTO(
                updatedItemCategory.getId(),
                updatedItemCategory.getName(),
                updatedItemCategory.getItemType(),
                updatedItemCategory.getDescription()
        );
    }

    /**
     * Delete an existing ItemCategory by ID.
     *
     * @param id ItemCategory ID
     */
    public boolean deleteItemCategory(Long id) {
        // Ensure the user belongs to a company before deleting the data
        Company userCompany = ensureUserBelongsToCompany();

        ItemCategory existingItemCategory = itemCategoryRepository.findById(id).orElse(null);

        if (existingItemCategory == null || !existingItemCategory.getCompany().equals(userCompany)) {
            throw new UnauthorizedAccessException("This item category does not belong to your company.");
        }

        // Proceed with deletion
        itemCategoryRepository.deleteById(id);
        return true;
    }

    @Transactional
    public List<ItemCategoryDTO> batchCreateItemCategories(@Valid List<ItemCategoryDTO> itemCategoryDTOs) {
        // Ensure the user belongs to a company
        Company userCompany = ensureUserBelongsToCompany();

        // Check for duplicate names before proceeding
        for (ItemCategoryDTO itemCategoryDTO : itemCategoryDTOs) {
            if (itemCategoryRepository.findByName(itemCategoryDTO.getName()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ItemCategory with name '" + itemCategoryDTO.getName() + "' already exists.");
            }
        }

        // Convert DTOs to entities and associate the user's company
        List<ItemCategory> itemCategories = itemCategoryDTOs.stream()
                .map(dto -> {
                    ItemCategory itemCategory = new ItemCategory(
                            dto.getName(),
                            dto.getDescription(),
                            dto.getItemType()
                    );
                    itemCategory.setCompany(userCompany);
                    return itemCategory;
                })
                .collect(Collectors.toList());

        // Save all entities at once
        List<ItemCategory> createdItemCategories = itemCategoryRepository.saveAll(itemCategories);

        // Convert entities back to DTOs
        return createdItemCategories.stream()
                .map(itemCategory -> new ItemCategoryDTO(
                        itemCategory.getId(),
                        itemCategory.getName(),
                        itemCategory.getItemType(),
                        itemCategory.getDescription()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void batchDeleteItemCategories(List<Long> ids) {
        // Ensure the user belongs to a company
        Company userCompany = ensureUserBelongsToCompany();

        // Fetch the ItemCategories by their IDs and check for ownership
        List<ItemCategory> itemCategories = itemCategoryRepository.findAllById(ids);

        if (itemCategories.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategories not found.");
        }

        for (ItemCategory itemCategory : itemCategories) {
            if (!itemCategory.getCompany().equals(userCompany)) {
                throw new UnauthorizedAccessException("You do not have permission to delete this item category.");
            }
        }

        // Proceed with deletion
        itemCategoryRepository.deleteAll(itemCategories);
    }
}

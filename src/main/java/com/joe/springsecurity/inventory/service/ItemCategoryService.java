package com.joe.springsecurity.inventory.service;

import com.joe.springsecurity.auth.service.JwtService;
import com.joe.springsecurity.auth.service.UserService;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.errorhandling.UnauthorizedAccessException;
import com.joe.springsecurity.inventory.dto.ItemCategoryDTO;
import com.joe.springsecurity.inventory.model.ItemCategory;
import com.joe.springsecurity.inventory.repo.ItemCategoryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemCategoryService {
    private static final Logger logger = LoggerFactory.getLogger(ItemCategoryService.class);

    private final ItemCategoryRepository itemCategoryRepository;
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public ItemCategoryService(
            ItemCategoryRepository itemCategoryRepository,
            UserService userService,
            JwtService jwtService) {
        this.itemCategoryRepository = itemCategoryRepository;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Validates the company context by checking the user's company against the JWT token.
     * @return The validated Company object.
     * @throws UnauthorizedAccessException if company context is invalid.
     */
    private Company validateCompanyContext() {
        logger.debug("Validating company context for current user");
        Company userCompany = userService.getCurrentUserCompany();
        if (userCompany == null) {
            logger.error("User does not belong to any company");
            throw new UnauthorizedAccessException("User does not belong to any company.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.error("No authentication object found in security context");
            throw new IllegalStateException("Authentication context is missing");
        }

        String token;
        Object credentials = authentication.getCredentials();
        if (credentials == null) {
            logger.debug("Credentials are null, checking principal for token");
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                token = (String) principal;
            } else {
                logger.error("No valid token found in authentication object");
                throw new IllegalStateException("No valid token found in authentication credentials or principal");
            }
        } else {
            token = credentials.toString();
        }

        Long jwtCompanyId = jwtService.extractCompanyId(token);
        if (jwtCompanyId == null) {
            logger.error("JWT token contains no company ID: {}", token);
            throw new UnauthorizedAccessException("Invalid JWT token: No company ID present.");
        }
        if (!userCompany.getId().equals(jwtCompanyId)) {
            logger.error("Token company ID {} does not match user's company ID {}", jwtCompanyId, userCompany.getId());
            throw new UnauthorizedAccessException("Token company does not match user's current company.");
        }
        logger.debug("Company context validated for company ID: {}", userCompany.getId());
        return userCompany;
    }

    /**
     * Creates a new ItemCategory.
     * @param itemCategoryDTO DTO containing the new category details.
     * @return DTO of the created ItemCategory.
     */
    @Transactional
    public ItemCategoryDTO createItemCategory(@Valid ItemCategoryDTO itemCategoryDTO) {
        logger.debug("Creating item category with name: {}", itemCategoryDTO.getName());
        Company userCompany = validateCompanyContext();

        // Check for name uniqueness within the company
        if (itemCategoryRepository.findByCompanyAndName(userCompany, itemCategoryDTO.getName()).isPresent()) {
            logger.warn("Item category with name '{}' already exists for company ID: {}",
                    itemCategoryDTO.getName(), userCompany.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ItemCategory with this name already exists in your company.");
        }

        try {
            ItemCategory itemCategory = new ItemCategory(
                    itemCategoryDTO.getName(),
                    itemCategoryDTO.getDescription(),
                    itemCategoryDTO.getItemType(),
                    userCompany
            );
            ItemCategory createdItemCategory = itemCategoryRepository.save(itemCategory);
            logger.info("Item category created with ID: {}", createdItemCategory.getId());

            return new ItemCategoryDTO(
                    createdItemCategory.getId(),
                    createdItemCategory.getName(),
                    createdItemCategory.getItemType(),
                    createdItemCategory.getDescription()
            );
        } catch (Exception e) {
            logger.error("Failed to create item category for company ID: {}", userCompany.getId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error creating item category: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches all ItemCategories for the current company.
     * @param pageable Pagination details.
     * @return Page of ItemCategoryDTOs.
     */
    public Page<ItemCategoryDTO> getAllItemCategories(Pageable pageable) {
        logger.debug("Fetching all item categories for page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        Company userCompany = validateCompanyContext();

        try {
            Page<ItemCategory> itemCategories = itemCategoryRepository.findByCompany(userCompany, pageable);
            logger.debug("Found {} item categories for company ID: {}",
                    itemCategories.getTotalElements(), userCompany.getId());
            return itemCategories.map(itemCategory -> new ItemCategoryDTO(
                    itemCategory.getId(),
                    itemCategory.getName(),
                    itemCategory.getItemType(),
                    itemCategory.getDescription()
            ));
        } catch (Exception e) {
            logger.error("Failed to fetch item categories for company ID: {}", userCompany.getId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error fetching item categories: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches an ItemCategory by ID.
     * @param id The ID of the ItemCategory.
     * @return DTO of the ItemCategory.
     */
    public ItemCategoryDTO getItemCategoryById(Long id) {
        logger.debug("Fetching item category by ID: {}", id);
        Company userCompany = validateCompanyContext();

        ItemCategory itemCategory = itemCategoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Item category not found for ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategory not found.");
                });

        if (!itemCategory.getCompany().equals(userCompany)) {
            logger.error("Item category company {} does not match user company {}",
                    itemCategory.getCompany().getId(), userCompany.getId());
            throw new UnauthorizedAccessException("This item category does not belong to your company.");
        }

        return new ItemCategoryDTO(
                itemCategory.getId(),
                itemCategory.getName(),
                itemCategory.getItemType(),
                itemCategory.getDescription()
        );
    }

    /**
     * Updates an existing ItemCategory.
     * @param id The ID of the ItemCategory to update.
     * @param itemCategoryDTO DTO with updated details.
     * @return DTO of the updated ItemCategory.
     */
    @Transactional
    public ItemCategoryDTO updateItemCategory(Long id, @Valid ItemCategoryDTO itemCategoryDTO) {
        logger.debug("Updating item category with ID: {}", id);
        Company userCompany = validateCompanyContext();

        ItemCategory existingItemCategory = itemCategoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Item category not found for ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategory not found.");
                });

        if (!existingItemCategory.getCompany().equals(userCompany)) {
            logger.error("Item category company {} does not match user company {}",
                    existingItemCategory.getCompany().getId(), userCompany.getId());
            throw new UnauthorizedAccessException("This item category does not belong to your company.");
        }

        // Check for name conflict within the same company, excluding the current entity
        Optional<ItemCategory> conflictingCategory = itemCategoryRepository.findByCompanyAndName(
                userCompany, itemCategoryDTO.getName());
        if (conflictingCategory.isPresent() && !conflictingCategory.get().getId().equals(id)) {
            logger.warn("Item category with name '{}' already exists for company ID: {}",
                    itemCategoryDTO.getName(), userCompany.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ItemCategory with this name already exists in your company.");
        }

        existingItemCategory.setName(itemCategoryDTO.getName());
        existingItemCategory.setDescription(itemCategoryDTO.getDescription());
        existingItemCategory.setItemType(itemCategoryDTO.getItemType());

        try {
            ItemCategory updatedItemCategory = itemCategoryRepository.save(existingItemCategory);
            logger.info("Item category updated with ID: {}", updatedItemCategory.getId());
            return new ItemCategoryDTO(
                    updatedItemCategory.getId(),
                    updatedItemCategory.getName(),
                    updatedItemCategory.getItemType(),
                    updatedItemCategory.getDescription()
            );
        } catch (Exception e) {
            logger.error("Failed to update item category ID: {} for company ID: {}", id, userCompany.getId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error updating item category: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an ItemCategory by ID.
     * @param id The ID of the ItemCategory to delete.
     * @return True if deleted successfully.
     */
    @Transactional
    public boolean deleteItemCategory(Long id) {
        logger.debug("Deleting item category with ID: {}", id);
        Company userCompany = validateCompanyContext();

        ItemCategory existingItemCategory = itemCategoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Item category not found for ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategory not found.");
                });

        if (!existingItemCategory.getCompany().equals(userCompany)) {
            logger.error("Item category company {} does not match user company {}",
                    existingItemCategory.getCompany().getId(), userCompany.getId());
            throw new UnauthorizedAccessException("This item category does not belong to your company.");
        }

        itemCategoryRepository.deleteById(id);
        logger.info("Item category deleted with ID: {}", id);
        return true;
    }

    /**
     * Batch creates multiple ItemCategories.
     * @param itemCategoryDTOs List of DTOs for creating ItemCategories.
     * @return List of created ItemCategoryDTOs.
     */
    @Transactional
    public List<ItemCategoryDTO> batchCreateItemCategories(@Valid List<ItemCategoryDTO> itemCategoryDTOs) {
        logger.debug("Batch creating {} item categories", itemCategoryDTOs.size());
        Company userCompany = validateCompanyContext();

        // Check for duplicate names within the company
        for (ItemCategoryDTO dto : itemCategoryDTOs) {
            if (itemCategoryRepository.findByCompanyAndName(userCompany, dto.getName()).isPresent()) {
                logger.warn("Item category with name '{}' already exists for company ID: {}",
                        dto.getName(), userCompany.getId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ItemCategory with name '" + dto.getName() + "' already exists in your company.");
            }
        }

        try {
            List<ItemCategory> itemCategories = itemCategoryDTOs.stream()
                    .map(dto -> new ItemCategory(
                            dto.getName(),
                            dto.getDescription(),
                            dto.getItemType(),
                            userCompany
                    ))
                    .collect(Collectors.toList());

            List<ItemCategory> createdItemCategories = itemCategoryRepository.saveAll(itemCategories);
            logger.info("Batch created {} item categories for company ID: {}",
                    createdItemCategories.size(), userCompany.getId());

            return createdItemCategories.stream()
                    .map(itemCategory -> new ItemCategoryDTO(
                            itemCategory.getId(),
                            itemCategory.getName(),
                            itemCategory.getItemType(),
                            itemCategory.getDescription()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to batch create item categories for company ID: {}", userCompany.getId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error batch creating item categories: " + e.getMessage(), e);
        }
    }

    /**
     * Batch deletes multiple ItemCategories by IDs.
     * @param ids List of ItemCategory IDs to delete.
     */
    @Transactional
    public void batchDeleteItemCategories(List<Long> ids) {
        logger.debug("Batch deleting {} item categories", ids.size());
        Company userCompany = validateCompanyContext();

        List<ItemCategory> itemCategories = itemCategoryRepository.findAllById(ids);
        if (itemCategories.isEmpty()) {
            logger.warn("No item categories found for IDs: {}", ids);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategories not found.");
        }

        for (ItemCategory itemCategory : itemCategories) {
            if (!itemCategory.getCompany().equals(userCompany)) {
                logger.error("Item category ID {} company {} does not match user company {}",
                        itemCategory.getId(), itemCategory.getCompany().getId(), userCompany.getId());
                throw new UnauthorizedAccessException("You do not have permission to delete this item category.");
            }
        }

        itemCategoryRepository.deleteAll(itemCategories);
        logger.info("Batch deleted {} item categories for company ID: {}", itemCategories.size(), userCompany.getId());
    }
}

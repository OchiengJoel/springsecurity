package com.joe.springsecurity.inventory.service;

import com.joe.springsecurity.auth.service.JwtService;
import com.joe.springsecurity.auth.service.UserService;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.errorhandling.UnauthorizedAccessException;
import com.joe.springsecurity.inventory.dto.InventoryItemDTO;
import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.model.ItemCategory;
import com.joe.springsecurity.inventory.repo.InventoryItemRepository;
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

import java.util.Set;

@Service
public class InventoryItemService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryItemService.class);

    private final InventoryItemRepository inventoryItemRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public InventoryItemService(
            InventoryItemRepository inventoryItemRepository,
            ItemCategoryRepository itemCategoryRepository,
            UserService userService,
            JwtService jwtService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.userService = userService;
        this.jwtService = jwtService;
    }

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
                token = (String) principal; // Token might be in principal
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

    public Page<InventoryItemDTO> getAllInventoryItems(Pageable pageable) {
        logger.debug("Fetching all inventory items for page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Company userCompany = validateCompanyContext();
        try {
            Page<InventoryItem> inventoryItems = inventoryItemRepository.findByCompany(userCompany, pageable);
            logger.debug("Found {} inventory items for company ID: {}", inventoryItems.getTotalElements(), userCompany.getId());
            return inventoryItems.map(item -> new InventoryItemDTO(
                    item.getId(), item.getName(), item.getQuantity(),
                    item.getPrice(), item.getDescription(), item.getItemCategory().getId()));
        } catch (Exception e) {
            logger.error("Failed to fetch inventory items for company ID: {}", userCompany.getId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching inventory items: " + e.getMessage(), e);
        }
    }

    @Transactional
    public InventoryItemDTO createInventoryItem(InventoryItem inventoryItem, Long itemCategoryId) {
        logger.debug("Creating inventory item with category ID: {}", itemCategoryId);
        Company userCompany = validateCompanyContext();
        inventoryItem.setCompany(userCompany);

        ItemCategory itemCategory = itemCategoryRepository.findById(itemCategoryId)
                .orElseThrow(() -> {
                    logger.error("ItemCategory not found for ID: {}", itemCategoryId);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "ItemCategory not found.");
                });
        if (!itemCategory.getCompany().equals(userCompany)) {
            logger.error("ItemCategory company {} does not match user company {}", itemCategory.getCompany().getId(), userCompany.getId());
            throw new UnauthorizedAccessException("ItemCategory does not belong to your company.");
        }

        inventoryItem.setItemCategory(itemCategory);
        InventoryItem createdItem = inventoryItemRepository.save(inventoryItem);
        logger.info("Inventory item created with ID: {}", createdItem.getId());

        return new InventoryItemDTO(
                createdItem.getId(), createdItem.getName(), createdItem.getQuantity(),
                createdItem.getPrice(), createdItem.getDescription(), createdItem.getItemCategory().getId());
    }

    public long getInventoryItemCount() {
        logger.debug("Counting inventory items");
        Company userCompany = validateCompanyContext();
        return inventoryItemRepository.countByCompany(userCompany);
    }

    public InventoryItemDTO getInventoryItemById(Long id) {
        logger.debug("Fetching inventory item by ID: {}", id);
        Company userCompany = validateCompanyContext();
        InventoryItem item = inventoryItemRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Inventory item not found for ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found.");
                });
        if (!item.getCompany().equals(userCompany)) {
            logger.error("Inventory item company {} does not match user company {}", item.getCompany().getId(), userCompany.getId());
            throw new UnauthorizedAccessException("This inventory item does not belong to your company: " + userCompany.getName());
        }
        return new InventoryItemDTO(
                item.getId(), item.getName(), item.getQuantity(),
                item.getPrice(), item.getDescription(), item.getItemCategory().getId());
    }

    @Transactional
    public InventoryItemDTO updateInventoryItem(Long id, InventoryItem inventoryItem, Long itemCategoryId) {
        logger.debug("Updating inventory item with ID: {}, category ID: {}", id, itemCategoryId);
        Company userCompany = validateCompanyContext();
        InventoryItem existingItem = inventoryItemRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Inventory item not found for ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found.");
                });
        if (!existingItem.getCompany().equals(userCompany)) {
            logger.error("Inventory item company {} does not match user company {}", existingItem.getCompany().getId(), userCompany.getId());
            throw new UnauthorizedAccessException("This inventory item does not belong to your company: " + userCompany.getName());
        }

        ItemCategory itemCategory = itemCategoryRepository.findById(itemCategoryId)
                .orElseThrow(() -> {
                    logger.error("ItemCategory not found for ID: {}", itemCategoryId);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "ItemCategory not found.");
                });
        if (!itemCategory.getCompany().equals(userCompany)) {
            logger.error("ItemCategory company {} does not match user company {}", itemCategory.getCompany().getId(), userCompany.getId());
            throw new UnauthorizedAccessException("ItemCategory does not belong to your company: " + userCompany.getName());
        }

        existingItem.setName(inventoryItem.getName());
        existingItem.setQuantity(inventoryItem.getQuantity());
        existingItem.setPrice(inventoryItem.getPrice());
        existingItem.setDescription(inventoryItem.getDescription());
        existingItem.setItemCategory(itemCategory);

        InventoryItem updatedItem = inventoryItemRepository.save(existingItem);
        logger.info("Inventory item updated with ID: {}", updatedItem.getId());
        return new InventoryItemDTO(
                updatedItem.getId(), updatedItem.getName(), updatedItem.getQuantity(),
                updatedItem.getPrice(), updatedItem.getDescription(), updatedItem.getItemCategory().getId());
    }

    @Transactional
    public boolean deleteInventoryItem(Long id) {
        logger.debug("Deleting inventory item with ID: {}", id);
        Company userCompany = validateCompanyContext();
        InventoryItem existingItem = inventoryItemRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Inventory item not found for ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found.");
                });
        if (!existingItem.getCompany().equals(userCompany)) {
            logger.error("Inventory item company {} does not match user company {}", existingItem.getCompany().getId(), userCompany.getId());
            throw new UnauthorizedAccessException("This inventory item does not belong to your company: " + userCompany.getName());
        }
        inventoryItemRepository.deleteById(id);
        logger.info("Inventory item deleted with ID: {}", id);
        return true;
    }

    public Page<InventoryItemDTO> getInventoryItemsByCategory(Long categoryId, Pageable pageable) {
        logger.debug("Fetching inventory items by category ID: {}, page: {}, size: {}", categoryId, pageable.getPageNumber(), pageable.getPageSize());
        Company userCompany = validateCompanyContext();
        ItemCategory itemCategory = itemCategoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    logger.error("ItemCategory not found for ID: {}", categoryId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "ItemCategory not found.");
                });
        if (!itemCategory.getCompany().equals(userCompany)) {
            logger.error("ItemCategory company {} does not match user company {}", itemCategory.getCompany().getId(), userCompany.getId());
            throw new UnauthorizedAccessException("ItemCategory does not belong to your company: " + userCompany.getName());
        }

        Page<InventoryItem> inventoryItems = inventoryItemRepository.findByCompanyAndItemCategory(userCompany, itemCategory, pageable);
        logger.debug("Found {} inventory items for category ID: {}", inventoryItems.getTotalElements(), categoryId);
        return inventoryItems.map(item -> new InventoryItemDTO(
                item.getId(), item.getName(), item.getQuantity(),
                item.getPrice(), item.getDescription(), item.getItemCategory().getId()));
    }
}


package com.joe.springsecurity.inventory.controller;

import com.joe.springsecurity.errorhandling.UnauthorizedAccessException;
import com.joe.springsecurity.inventory.dto.InventoryItemDTO;
import com.joe.springsecurity.inventory.dto.ItemCategoryDTO;
import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.model.ItemCategory;
import com.joe.springsecurity.inventory.service.ItemCategoryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v2/item-categories")
@CrossOrigin(origins = "http://localhost:4200")
public class ItemCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(ItemCategoryController.class);

    private final ItemCategoryService itemCategoryService;

    @Autowired
    public ItemCategoryController(ItemCategoryService itemCategoryService) {
        this.itemCategoryService = itemCategoryService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ItemCategoryDTO> createItemCategory(@Valid @RequestBody ItemCategoryDTO itemCategoryDTO) {
        logger.debug("Received request to create item category: {}", itemCategoryDTO);
        try {
            ItemCategoryDTO createdItemCategory = itemCategoryService.createItemCategory(itemCategoryDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItemCategory);
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "UNAUTHORIZED");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", e.getReason() != null ? e.getReason() : "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(null);
        } catch (Exception e) {
            logger.error("Internal server error while creating item category", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "Failed to create item category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllItemCategories(Pageable pageable) {
        logger.info("Received request to list item categories, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<ItemCategoryDTO> page = itemCategoryService.getAllItemCategories(pageable);
            Map<String, Object> response = new HashMap<>();
            response.put("items", page.getContent());
            response.put("currentPage", page.getNumber());
            response.put("totalItems", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("size", page.getSize());
            logger.info("Returning {} item categories", page.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "UNAUTHORIZED");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", e.getReason() != null ? e.getReason() : "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            logger.error("Internal server error while fetching item categories", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "Failed to fetch item categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemCategoryDTO> getItemCategoryById(@PathVariable Long id) {
        logger.debug("Received request to fetch item category with ID: {}", id);
        try {
            ItemCategoryDTO itemCategoryDTO = itemCategoryService.getItemCategoryById(id);
            return ResponseEntity.ok(itemCategoryDTO);
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "UNAUTHORIZED");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", e.getReason() != null ? e.getReason() : "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(null);
        } catch (Exception e) {
            logger.error("Internal server error while fetching item category ID: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "Failed to fetch item category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ItemCategoryDTO> updateItemCategory(
            @PathVariable Long id, @Valid @RequestBody ItemCategoryDTO itemCategoryDTO) {
        logger.debug("Received request to update item category with ID: {}", id);
        try {
            ItemCategoryDTO updatedItemCategory = itemCategoryService.updateItemCategory(id, itemCategoryDTO);
            return ResponseEntity.ok(updatedItemCategory);
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "UNAUTHORIZED");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", e.getReason() != null ? e.getReason() : "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(null);
        } catch (Exception e) {
            logger.error("Internal server error while updating item category ID: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "Failed to update item category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteItemCategory(@PathVariable Long id) {
        logger.debug("Received request to delete item category with ID: {}", id);
        try {
            boolean isDeleted = itemCategoryService.deleteItemCategory(id);
            if (isDeleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "UNAUTHORIZED");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", e.getReason() != null ? e.getReason() : "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatus()).build();
        } catch (Exception e) {
            logger.error("Internal server error while deleting item category ID: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "Failed to delete item category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/batch-create")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ItemCategoryDTO>> batchCreateItemCategories(
            @Valid @RequestBody List<ItemCategoryDTO> itemCategoryDTOs) {
        logger.debug("Received request to batch create {} item categories", itemCategoryDTOs.size());
        try {
            List<ItemCategoryDTO> createdItemCategories = itemCategoryService.batchCreateItemCategories(itemCategoryDTOs);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItemCategories);
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "UNAUTHORIZED");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", e.getReason() != null ? e.getReason() : "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(null);
        } catch (Exception e) {
            logger.error("Internal server error while batch creating item categories", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "Failed to batch create item categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/batch-delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> batchDeleteItemCategories(@RequestBody List<Long> ids) {
        logger.debug("Received request to batch delete {} item categories", ids.size());
        try {
            itemCategoryService.batchDeleteItemCategories(ids);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "UNAUTHORIZED");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", e.getReason() != null ? e.getReason() : "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatus()).build();
        } catch (Exception e) {
            logger.error("Internal server error while batch deleting item categories", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "Failed to batch delete item categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
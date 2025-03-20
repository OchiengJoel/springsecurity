package com.joe.springsecurity.inventory.controller;


import com.joe.springsecurity.errorhandling.UnauthorizedAccessException;
import com.joe.springsecurity.inventory.dto.InventoryItemDTO;
import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.service.InventoryItemService;
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

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/inventoryitem")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class InventoryItemController {
    private static final Logger logger = LoggerFactory.getLogger(InventoryItemController.class);
    private final InventoryItemService inventoryItemService;

    @Autowired
    public InventoryItemController(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllInventoryItems(Pageable pageable) {
        logger.info("Received request to list inventory items, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<InventoryItemDTO> page = inventoryItemService.getAllInventoryItems(pageable);
            Map<String, Object> response = new HashMap<>();
            response.put("items", page.getContent());
            response.put("currentPage", page.getNumber());
            response.put("totalItems", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("size", page.getSize());
            logger.info("Returning {} inventory items", page.getTotalElements());
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
            logger.error("Internal server error while fetching inventory items", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "Failed to fetch inventory items: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/count")
    public ResponseEntity<Long> getInventoryItemCount() {
        logger.debug("Received request to count inventory items");
        try {
            long count = inventoryItemService.getInventoryItemCount();
            return ResponseEntity.ok(count);
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(0L);
        } catch (Exception e) {
            logger.error("Internal server error while counting inventory items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0L);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemDTO> getInventoryItemById(@PathVariable Long id) {
        logger.debug("Received request to fetch inventory item with ID: {}", id);
        try {
            InventoryItemDTO item = inventoryItemService.getInventoryItemById(id);
            return ResponseEntity.ok(item);
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(null);
        } catch (Exception e) {
            logger.error("Internal server error while fetching inventory item ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InventoryItemDTO> createInventoryItem(@Valid @RequestBody InventoryItem inventoryItem, @RequestParam Long itemCategoryId) {
        logger.debug("Received request to create inventory item with category ID: {}", itemCategoryId);
        try {
            InventoryItemDTO createdItem = inventoryItemService.createInventoryItem(inventoryItem, itemCategoryId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(null);
        } catch (Exception e) {
            logger.error("Internal server error while creating inventory item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InventoryItemDTO> updateInventoryItem(@PathVariable Long id, @Valid @RequestBody InventoryItem inventoryItem, @RequestParam Long itemCategoryId) {
        logger.debug("Received request to update inventory item with ID: {}", id);
        try {
            InventoryItemDTO updatedItem = inventoryItemService.updateInventoryItem(id, inventoryItem, itemCategoryId);
            return ResponseEntity.ok(updatedItem);
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(null);
        } catch (Exception e) {
            logger.error("Internal server error while updating inventory item ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long id) {
        logger.debug("Received request to delete inventory item with ID: {}", id);
        try {
            boolean isDeleted = inventoryItemService.deleteInventoryItem(id);
            if (isDeleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (UnauthorizedAccessException e) {
            logger.warn("Unauthorized access: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResponseStatusException e) {
            logger.warn("Response status exception: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus()).build();
        } catch (Exception e) {
            logger.error("Internal server error while deleting inventory item ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getInventoryItemsByCategory(@PathVariable Long categoryId, Pageable pageable) {
        logger.debug("Received request to fetch inventory items by category ID: {}", categoryId);
        try {
            Page<InventoryItemDTO> page = inventoryItemService.getInventoryItemsByCategory(categoryId, pageable);
            Map<String, Object> response = new HashMap<>();
            response.put("items", page.getContent());
            response.put("currentPage", page.getNumber());
            response.put("totalItems", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("size", page.getSize());
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
            errorResponse.put("errorCode", "BAD_REQUEST");
            errorResponse.put("message", e.getReason());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            logger.error("Internal server error while fetching inventory items by category ID: {}", categoryId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("errorCode", "INTERNAL_SERVER_ERROR");
            errorResponse.put("message", "Failed to fetch inventory items by category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

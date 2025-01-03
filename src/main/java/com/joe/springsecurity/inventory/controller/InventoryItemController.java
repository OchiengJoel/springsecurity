package com.joe.springsecurity.inventory.controller;


import com.joe.springsecurity.errorhandling.UnauthorizedAccessException;
import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.service.InventoryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/inventoryitem")
@CrossOrigin(origins = "http://localhost:4200")
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    @Autowired
    public InventoryItemController(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    // Get all inventory items (with pagination), scoped to the current user's company
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllInventoryItems(Pageable pageable) {
        try {
            Page<InventoryItem> page = inventoryItemService.getAllInventoryItems(pageable);
            Map<String, Object> response = new HashMap<>();
            response.put("items", page.getContent());
            response.put("currentPage", page.getNumber());
            response.put("totalItems", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("size", page.getSize());
            return ResponseEntity.ok(response);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);  // Handle unauthorized access
        }
    }

    // Get inventory item count (total number of items), scoped to the current user's company
    @GetMapping("/count")
    public ResponseEntity<Long> getInventoryItemCount() {
        try {
            long count = inventoryItemService.getInventoryItemCount();
            return ResponseEntity.ok(count);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(0L);
        }
    }

    // Get single inventory item by ID (accessible if it belongs to the user's company)
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getInventoryItemById(@PathVariable Long id) {
        try {
            InventoryItem item = inventoryItemService.getInventoryItemById(id);
            return ResponseEntity.ok(item);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    // Create a new inventory item (Admin only)
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InventoryItem> createInventoryItem(@Valid @RequestBody InventoryItem inventoryItem) {
        try {
            InventoryItem createdItem = inventoryItemService.createInventoryItem(inventoryItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    // Update inventory item (Admin only)
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InventoryItem> updateInventoryItem(@PathVariable Long id, @Valid @RequestBody InventoryItem inventoryItem) {
        try {
            InventoryItem updatedItem = inventoryItemService.updateInventoryItem(id, inventoryItem);
            return ResponseEntity.ok(updatedItem);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    // Delete inventory item (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long id) {
        try {
            boolean isDeleted = inventoryItemService.deleteInventoryItem(id);
            if (isDeleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}

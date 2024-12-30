package com.joe.springsecurity.inventory.controller;


import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.service.InventoryItemService;
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
public class InventoryController {

    private final InventoryItemService inventoryItemService;

    public InventoryController(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    // Get all inventory items (accessible by both Admin and User)
//    @GetMapping("/list")
//    public ResponseEntity<List<InventoryItem>> getAllInventoryItems() {
//        List<InventoryItem> items = inventoryItemService.getAllInventoryItems();
//        return ResponseEntity.ok(items);
//    }

    // Get all inventory items (with pagination)
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllInventoryItems(Pageable pageable) {
        Page<InventoryItem> page = inventoryItemService.getAllInventoryItems(pageable);
        Map<String, Object> response = new HashMap<>();
        response.put("items", page.getContent()); // The list of items in the current page
        response.put("currentPage", page.getNumber()); // Current page number
        response.put("totalItems", page.getTotalElements()); // Total number of items
        response.put("totalPages", page.getTotalPages()); // Total number of pages
        response.put("size", page.getSize()); // Number of items per page
        return ResponseEntity.ok(response);
    }

    // Get inventory item count (total number of items)
    @GetMapping("/count")
    public ResponseEntity<Long> getInventoryItemCount() {
        long count = inventoryItemService.getInventoryItemCount();
        return ResponseEntity.ok(count); // Return the total count of inventory items
    }

    // Get single inventory item by ID (accessible by both Admin and User)
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getInventoryItemById(@PathVariable Long id) {
        InventoryItem item = inventoryItemService.getInventoryItemById(id);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
        return ResponseEntity.ok(item);
    }

    // Create a new inventory item (Admin only)
    @PostMapping("/create")
//    @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InventoryItem> createInventoryItem(@Valid @RequestBody InventoryItem inventoryItem) {
        try {
            InventoryItem createdItem = inventoryItemService.createInventoryItem(inventoryItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Update inventory item (Admin only)
    @PutMapping("/update/{id}")
   // @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InventoryItem> updateInventoryItem(@PathVariable Long id, @Valid @RequestBody InventoryItem inventoryItem) {
        InventoryItem updatedItem = inventoryItemService.updateInventoryItem(id, inventoryItem);
        if (updatedItem == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
        return ResponseEntity.ok(updatedItem);
    }

    // Delete inventory item (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable Long id) {
        boolean isDeleted = inventoryItemService.deleteInventoryItem(id);
        if (!isDeleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

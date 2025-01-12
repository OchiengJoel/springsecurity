package com.joe.springsecurity.inventory.controller;


import com.joe.springsecurity.errorhandling.UnauthorizedAccessException;
import com.joe.springsecurity.inventory.dto.InventoryItemDTO;
import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.service.InventoryItemService;
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
@CrossOrigin(origins = "http://localhost:4200")
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    @Autowired
    public InventoryItemController(InventoryItemService inventoryItemService) {
        this.inventoryItemService = inventoryItemService;
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllInventoryItems(Pageable pageable) {
        try {
            Page<InventoryItemDTO> page = inventoryItemService.getAllInventoryItems(pageable);
            Map<String, Object> response = new HashMap<>();
            response.put("items", page.getContent());
            response.put("currentPage", page.getNumber());
            response.put("totalItems", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("size", page.getSize());
            return ResponseEntity.ok(response);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getInventoryItemCount() {
        try {
            long count = inventoryItemService.getInventoryItemCount();
            return ResponseEntity.ok(count);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(0L);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemDTO> getInventoryItemById(@PathVariable Long id) {
        try {
            InventoryItemDTO item = inventoryItemService.getInventoryItemById(id);
            return ResponseEntity.ok(item);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InventoryItemDTO> createInventoryItem(@Valid @RequestBody InventoryItem inventoryItem, @RequestParam Long itemCategoryId) {
        try {
            InventoryItemDTO createdItem = inventoryItemService.createInventoryItem(inventoryItem, itemCategoryId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<InventoryItemDTO> updateInventoryItem(@PathVariable Long id, @Valid @RequestBody InventoryItem inventoryItem, @RequestParam Long itemCategoryId) {
        try {
            InventoryItemDTO updatedItem = inventoryItemService.updateInventoryItem(id, inventoryItem, itemCategoryId);
            return ResponseEntity.ok(updatedItem);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

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

        // New method to get inventory items by category
//        @GetMapping("/category/{categoryId}")
//        public ResponseEntity<Set<InventoryItemDTO>> getInventoryItemsByCategory(@PathVariable Long categoryId) {
//            try {
//                Set<InventoryItem> inventoryItems = inventoryItemService.getInventoryItemsByCategory(categoryId);
//                // Convert InventoryItem entities to InventoryItemDTOs
//                Set<InventoryItemDTO> inventoryItemDTOs = inventoryItems.stream()
//                        .map(item -> new InventoryItemDTO(
//                                item.getId(),
//                                item.getName(),
//                                item.getQuantity(),
//                                item.getPrice(),
//                                item.getDescription(),
//                                item.getItemCategory().getId()
//                        ))
//                        .collect(Collectors.toSet());
//
//                return ResponseEntity.ok(inventoryItemDTOs);
//            } catch (ResponseStatusException e) {
//                return ResponseEntity.status(e.getStatus()).body(null);
//            }
//        }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getInventoryItemsByCategory(@PathVariable Long categoryId, Pageable pageable) {
        try {
            // Fetch paginated InventoryItems by category
            Page<InventoryItemDTO> page = inventoryItemService.getInventoryItemsByCategory(categoryId, pageable);

            // Prepare the response map
            Map<String, Object> response = new HashMap<>();
            response.put("items", page.getContent());
            response.put("currentPage", page.getNumber());
            response.put("totalItems", page.getTotalElements());
            response.put("totalPages", page.getTotalPages());
            response.put("size", page.getSize());

            return ResponseEntity.ok(response);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatus()).body(null);
        }
    }


}

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

        /**
         * Create a new ItemCategory.
         *
         * @param itemCategoryDTO Data Transfer Object (DTO) for creating an ItemCategory
         * @return ResponseEntity with status code and created ItemCategoryDTO
         */
        @PostMapping("/create")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ItemCategoryDTO> createItemCategory(@Valid @RequestBody ItemCategoryDTO itemCategoryDTO) {
            // Log the incoming request to see if it's being correctly parsed
            logger.info("Received ItemCategoryDTO: " + itemCategoryDTO);

            try {
                ItemCategoryDTO createdItemCategory = itemCategoryService.createItemCategory(itemCategoryDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdItemCategory);
            } catch (UnauthorizedAccessException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (ResponseStatusException e) {
                return ResponseEntity.status(e.getStatus()).body(null); // Return status from exception
            }
        }

        /**
         * Get all ItemCategories.
         *
         * @param pageable Pagination information
         * @return ResponseEntity containing a page of ItemCategoryDTOs
         */
        @GetMapping("/list")
        public ResponseEntity<Map<String, Object>> getAllItemCategories(Pageable pageable) {
            try {
                Page<ItemCategoryDTO> page = itemCategoryService.getAllItemCategories(pageable);
                Map<String, Object> response = new HashMap<>();
                response.put("items", page.getContent());
                response.put("currentPage", page.getNumber());
                response.put("totalItems", page.getTotalElements());
                response.put("totalPages", page.getTotalPages());
                response.put("size", page.getSize());
                return ResponseEntity.ok(response);
            } catch (UnauthorizedAccessException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (Exception e) {
                // General error handler for unexpected exceptions
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        /**
         * Get an ItemCategory by its ID.
         *
         * @param id ItemCategory ID
         * @return ResponseEntity with ItemCategoryDTO
         */
        @GetMapping("/{id}")
        public ResponseEntity<ItemCategoryDTO> getItemCategoryById(@PathVariable Long id) {
            try {
                ItemCategoryDTO itemCategoryDTO = itemCategoryService.getItemCategoryById(id);
                return ResponseEntity.ok(itemCategoryDTO);
            } catch (UnauthorizedAccessException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (ResponseStatusException e) {
                return ResponseEntity.status(e.getStatus()).body(null); // Return status from exception
            }
        }

        /**
         * Update an existing ItemCategory.
         *
         * @param id               ItemCategory ID
         * @param itemCategoryDTO  Data Transfer Object with updated details
         * @return ResponseEntity with the updated ItemCategoryDTO
         */
        @PutMapping("/update/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ItemCategoryDTO> updateItemCategory(@PathVariable Long id, @Valid @RequestBody ItemCategoryDTO itemCategoryDTO) {
            try {
                ItemCategoryDTO updatedItemCategory = itemCategoryService.updateItemCategory(id, itemCategoryDTO);
                return ResponseEntity.ok(updatedItemCategory);
            } catch (UnauthorizedAccessException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (ResponseStatusException e) {
                return ResponseEntity.status(e.getStatus()).body(null); // Return status from exception
            }
        }

        /**
         * Delete an existing ItemCategory.
         *
         * @param id ItemCategory ID
         * @return ResponseEntity with status OK if deleted, or NOT_FOUND if not found
         */
        @DeleteMapping("/delete/{id}")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<Void> deleteItemCategory(@PathVariable Long id) {
            try {
                boolean isDeleted = itemCategoryService.deleteItemCategory(id);
                if (isDeleted) {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } catch (UnauthorizedAccessException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } catch (ResponseStatusException e) {
                return ResponseEntity.status(e.getStatus()).build(); // Return status from exception
            } catch (Exception e) {
                // General error handler for unexpected exceptions
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

    /**
     * Batch create ItemCategories.
     *
     * @param itemCategoryDTOs List of Data Transfer Objects (DTOs) for creating ItemCategories
     * @return ResponseEntity with status code and list of created ItemCategoryDTOs
     */
    @PostMapping("/batch-create")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ItemCategoryDTO>> batchCreateItemCategories(@Valid @RequestBody List<ItemCategoryDTO> itemCategoryDTOs) {
        try {
            List<ItemCategoryDTO> createdItemCategories = itemCategoryService.batchCreateItemCategories(itemCategoryDTOs);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdItemCategories);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatus()).body(null); // Return status from exception
        }
    }

    /**
     * Batch delete ItemCategories by their IDs.
     *
     * @param ids List of ItemCategory IDs
     * @return ResponseEntity with status code indicating whether deletion was successful
     */
    @DeleteMapping("/batch-delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> batchDeleteItemCategories(@RequestBody List<Long> ids) {
        try {
            itemCategoryService.batchDeleteItemCategories(ids);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatus()).build();
        } catch (Exception e) {
            // General error handler for unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    }
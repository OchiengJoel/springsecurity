package com.joe.springsecurity.inventory.service;

import com.joe.springsecurity.inventory.model.InventoryItem;
import com.joe.springsecurity.inventory.repo.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryItemService {

    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryItemService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public InventoryItem createInventoryItem(InventoryItem inventoryItem) {
        return inventoryRepository.save(inventoryItem);
    }

//    // Read inventory item - allowed for both Admin and User
//    public List<InventoryItem> getAllInventoryItems() {
//        return inventoryRepository.findAll();
//    }

    // Get all inventory items with pagination and sorting
    public Page<InventoryItem> getAllInventoryItems(Pageable pageable) {
        return inventoryRepository.findAll(pageable); // Using the Pageable object to get a page of inventory items
    }

    // Get inventory item count
    public long getInventoryItemCount() {
        return inventoryRepository.count(); // Get the total count of inventory items
    }

    public InventoryItem getInventoryItemById(Long id) {
        return inventoryRepository.findById(id).orElse(null);
    }

    @Transactional
    public InventoryItem updateInventoryItem(Long id, InventoryItem inventoryItem) {
        if (!inventoryRepository.existsById(id)) {
            return null;
        }
        inventoryItem.setId(id);
        return inventoryRepository.save(inventoryItem);
    }

    @Transactional
    public boolean deleteInventoryItem(Long id) {
        if (!inventoryRepository.existsById(id)) {
            return false;
        }
        inventoryRepository.deleteById(id);
        return true;
    }
}

package com.joe.springsecurity.inventory.dto;

import com.joe.springsecurity.inventory.enums.ItemType;

public class ItemCategoryDTO {

    private Long id;
    private String name;
    private String description;
    private ItemType itemType;

    public ItemCategoryDTO() {
        // Default constructor
    }

    // Constructor without the ID (used during creation)
    public ItemCategoryDTO(String name, String description, ItemType itemType) {
        this.name = name;
        this.description = description;
        this.itemType = itemType;
    }

    // Constructor with ID (used for fetching/updating)
    public ItemCategoryDTO(Long id, String name, ItemType itemType, String description) {
        this.id = id;
        this.name = name;
        this.itemType = itemType;
        this.description = description;
    }

    @Override
    public String toString() {
        return "ItemCategoryDTO{id=" + id + ", name='" + name + "', description='" + description + "', itemType=" + itemType + "}";
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }
}
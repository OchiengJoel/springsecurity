package com.joe.springsecurity.inventory.dto;

import com.joe.springsecurity.inventory.enums.ItemType;

public class ItemCategoryDTO {

    private String name;
    private String description;
    private ItemType itemType;

    // Constructors, Getters, Setters


    public ItemCategoryDTO() {
    }

    public ItemCategoryDTO(String name, String description, ItemType itemType) {
        this.name = name;
        this.description = description;
        this.itemType = itemType;
    }

    public ItemCategoryDTO(Long id, String name, ItemType itemType, String description) {
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
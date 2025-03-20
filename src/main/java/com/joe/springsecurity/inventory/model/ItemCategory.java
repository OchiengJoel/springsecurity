package com.joe.springsecurity.inventory.model;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.inventory.enums.ItemType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cms_item_categories", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "company_id"}))
public class ItemCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    // One-to-many relationship with InventoryItem
    @OneToMany(mappedBy = "itemCategory", fetch = FetchType.LAZY)
    @JsonManagedReference  // Prevents infinite recursion by serializing only the parent side
    private Set<InventoryItem> inventoryItems = new HashSet<>();

// Constructors, Getters, Setters, equals() and hashCode()


public ItemCategory() {
}

// Constructor with all fields, including company
public ItemCategory(String name, String description, ItemType itemType, Company company) {
    this.name = name;
    this.description = description;
    this.itemType = itemType;
    this.company = company;
}

// Constructor without company (for DTO conversion or non-company specific categories)
public ItemCategory(String name, String description, ItemType itemType) {
    this.name = name;
    this.description = description;
    this.itemType = itemType;
}

@Override
public String toString() {
    return "ItemCategory{id=" + id + ", name='" + name + "', description='" + description + "', itemType=" + itemType + ", company=" + (company != null ? company.getName() : "No company") + "}";
}

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

public Company getCompany() {
    return company;
}

public void setCompany(Company company) {
    this.company = company;
}


public Set<InventoryItem> getInventoryItems() {
    return inventoryItems;
}

public void setInventoryItems(Set<InventoryItem> inventoryItems) {
    this.inventoryItems = inventoryItems;
}
}
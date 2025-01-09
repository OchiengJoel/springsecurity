package com.joe.springsecurity.inventory.model;


import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.inventory.enums.ItemType;

import javax.persistence.*;

@Entity
@Table(name = "cms_item_categories")
public class ItemCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    // Constructors, Getters, Setters, equals() and hashCode()


    public ItemCategory() {
    }

    public ItemCategory(String name, String description, ItemType itemType, Company company) {
        this.name = name;
        this.description = description;
        this.itemType = itemType;
        this.company = company;
    }

    public ItemCategory(String name, String description, ItemType itemType) {
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
}
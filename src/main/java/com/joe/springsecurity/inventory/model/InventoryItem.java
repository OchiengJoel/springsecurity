package com.joe.springsecurity.inventory.model;

import com.joe.springsecurity.company.model.Company;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "cms_inventory_item")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    @Min(0)
    private int quantity;

    @DecimalMin("0.0")
    private double price;

    private String description;

    // Marking total_price as transient so it's not persisted in the database
    @Transient
    private double totalPrice;

    @ManyToOne
    @JoinColumn(name = "company_id")  // Foreign key column to map to Company
    private Company company;  // The company this inventory item belongs to

    public InventoryItem() {
    }

    public InventoryItem(String name, int quantity, double price, String description, Company company) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.description = description;
        this.company = company;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateTotalPrice();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        updateTotalPrice();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    // This method calculates the total price dynamically based on quantity and price.
    public double getTotalPrice() {
        return quantity * price;
    }

    // Private method to update the total price when quantity or price changes.
    private void updateTotalPrice() {
        this.totalPrice = getTotalPrice(); // Update the total price whenever quantity or price is changed
    }

}

package com.joe.springsecurity.company.model;


import com.joe.springsecurity.inventory.model.InventoryItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cms_companies")
public class Company implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    @OneToMany(mappedBy = "company")  // The mappedBy should refer to the 'company' field in InventoryItem
    private Set<UserCompany> userCompanies = new HashSet<>();

    @OneToMany(mappedBy = "company")  // Similarly, the 'company' field in InventoryItem
    private Set<InventoryItem> inventories = new HashSet<>();  // The company owns many inventory items

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

    public Set<UserCompany> getUserCompanies() {
        return userCompanies;
    }

    public void setUserCompanies(Set<UserCompany> userCompanies) {
        this.userCompanies = userCompanies;
    }

    public Set<InventoryItem> getInventories() {
        return inventories;
    }

    public void setInventories(Set<InventoryItem> inventories) {
        this.inventories = inventories;
    }
}

package com.joe.springsecurity.company.model;


import com.joe.springsecurity.country.model.Country;
import com.joe.springsecurity.inventory.model.InventoryItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "cms_companies")
public class Company implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    private String primaryEmail;

    private String secondaryEmail;

    private String primaryContact;

    private String secondaryContact;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    private String town;

    private String address;

    private String registration;

    private String tax_id;    

    private boolean status = true; //Default to true(enabled)

    @OneToMany(mappedBy = "company")  // The mappedBy should refer to the 'company' field in InventoryItem
    private Set<UserCompany> userCompanies = new HashSet<>();

    @OneToMany(mappedBy = "company")  // Similarly, the 'company' field in InventoryItem
    private Set<InventoryItem> inventories = new HashSet<>();  // The company owns many inventory items

    public Company() {
    }

    // Constructor with essential fields
    public Company(String name, Country country, String primaryEmail, String secondaryEmail,
                   String primaryContact, String secondaryContact, String town,
                   String address, String registration, String tax_id, boolean status) {
        this.name = name;
        this.country = country;
        this.primaryEmail = primaryEmail;
        this.secondaryEmail = secondaryEmail;
        this.primaryContact = primaryContact;
        this.secondaryContact = secondaryContact;
        this.town = town;
        this.address = address;
        this.registration = registration;
        this.tax_id = tax_id;
        this.status = status;
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

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    public void setSecondaryEmail(String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }

    public String getPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(String primaryContact) {
        this.primaryContact = primaryContact;
    }

    public String getSecondaryContact() {
        return secondaryContact;
    }

    public void setSecondaryContact(String secondaryContact) {
        this.secondaryContact = secondaryContact;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getTax_id() {
        return tax_id;
    }

    public void setTax_id(String tax_id) {
        this.tax_id = tax_id;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
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

    // Override toString
    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", primaryEmail='" + primaryEmail + '\'' +
                ", secondaryEmail='" + secondaryEmail + '\'' +
                ", primaryContact='" + primaryContact + '\'' +
                ", secondaryContact='" + secondaryContact + '\'' +
                ", country=" + country +
                ", town='" + town + '\'' +
                ", address='" + address + '\'' +
                ", registration='" + registration + '\'' +
                ", tax_id='" + tax_id + '\'' +
                ", status=" + status +
                '}';
    }

    // Override equals() and hashCode() for correct comparison and consistency in collections
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Company company = (Company) obj;
        return Objects.equals(id, company.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

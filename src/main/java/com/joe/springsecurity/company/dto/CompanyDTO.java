package com.joe.springsecurity.company.dto;

import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.country.dto.CountryDTO;

public class CompanyDTO {


        private Long id;
        private String name;
        private Long countryId;  // Change to Long to store the country ID
        private String primaryEmail;
        private String secondaryEmail;
        private String primaryContact;
        private String secondaryContact;
        private String town;
        private String address;
        private String registration;
        private String tax_id;
        private boolean status;

        // Default constructor
        public CompanyDTO() {}

        // Constructor to create from entity
        public CompanyDTO(Company company) {
            this.id = company.getId();
            this.name = company.getName();
            this.countryId = company.getCountry() != null ? company.getCountry().getId() : null;
            this.primaryEmail = company.getPrimaryEmail();
            this.secondaryEmail = company.getSecondaryEmail();
            this.primaryContact = company.getPrimaryContact();
            this.secondaryContact = company.getSecondaryContact();
            this.town = company.getTown();
            this.address = company.getAddress();
            this.registration = company.getRegistration();
            this.tax_id = company.getTax_id();
            this.status = company.isStatus();
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getCountryId() { return countryId; }
        public void setCountryId(Long countryId) { this.countryId = countryId; }
        public String getPrimaryEmail() { return primaryEmail; }
        public void setPrimaryEmail(String primaryEmail) { this.primaryEmail = primaryEmail; }
        public String getSecondaryEmail() { return secondaryEmail; }
        public void setSecondaryEmail(String secondaryEmail) { this.secondaryEmail = secondaryEmail; }
        public String getPrimaryContact() { return primaryContact; }
        public void setPrimaryContact(String primaryContact) { this.primaryContact = primaryContact; }
        public String getSecondaryContact() { return secondaryContact; }
        public void setSecondaryContact(String secondaryContact) { this.secondaryContact = secondaryContact; }
        public String getTown() { return town; }
        public void setTown(String town) { this.town = town; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getRegistration() { return registration; }
        public void setRegistration(String registration) { this.registration = registration; }
        public String getTax_id() { return tax_id; }
        public void setTax_id(String tax_id) { this.tax_id = tax_id; }
        public boolean isStatus() { return status; }
        public void setStatus(boolean status) { this.status = status; }

        @Override
        public String toString() {
            return "CompanyDTO{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", countryId=" + countryId +
                    ", primaryEmail='" + primaryEmail + '\'' +
                    ", secondaryEmail='" + secondaryEmail + '\'' +
                    ", primaryContact='" + primaryContact + '\'' +
                    ", secondaryContact='" + secondaryContact + '\'' +
                    ", town='" + town + '\'' +
                    ", address='" + address + '\'' +
                    ", registration='" + registration + '\'' +
                    ", tax_id='" + tax_id + '\'' +
                    ", status=" + status +
                    '}';
        }
}




//    private Long id;
//    private String name;
//    private CountryDTO country;
//    private String primaryEmail;
//    private String secondaryEmail;
//    private String primaryContact;
//    private String secondaryContact;
//    private String town;
//    private String address;
//    private String registration;
//    private String tax_id;
//    private boolean status;
//
//    // Default constructor
//    public CompanyDTO() {
//    }
//
//    // Constructor to create from entity
//    public CompanyDTO(Company company) {
//        this.id = company.getId();
//        this.name = company.getName();
//        this.country = new CountryDTO(company.getCountry()); // Map Country entity to DTO
//        this.primaryEmail = company.getPrimaryEmail();
//        this.secondaryEmail = company.getSecondaryEmail();
//        this.primaryContact = company.getPrimaryContact();
//        this.secondaryContact = company.getSecondaryContact();
//        this.town = company.getTown();
//        this.address = company.getAddress();
//        this.registration = company.getRegistration();
//        this.tax_id = company.getTax_id();
//        this.status = company.isStatus();
//    }
//
//    // Getters and Setters
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public CountryDTO getCountry() {
//        return country;
//    }
//
//    public void setCountry(CountryDTO country) {
//        this.country = country;
//    }
//
//    public String getPrimaryEmail() {
//        return primaryEmail;
//    }
//
//    public void setPrimaryEmail(String primaryEmail) {
//        this.primaryEmail = primaryEmail;
//    }
//
//    public String getSecondaryEmail() {
//        return secondaryEmail;
//    }
//
//    public void setSecondaryEmail(String secondaryEmail) {
//        this.secondaryEmail = secondaryEmail;
//    }
//
//    public String getPrimaryContact() {
//        return primaryContact;
//    }
//
//    public void setPrimaryContact(String primaryContact) {
//        this.primaryContact = primaryContact;
//    }
//
//    public String getSecondaryContact() {
//        return secondaryContact;
//    }
//
//    public void setSecondaryContact(String secondaryContact) {
//        this.secondaryContact = secondaryContact;
//    }
//
//    public String getTown() {
//        return town;
//    }
//
//    public void setTown(String town) {
//        this.town = town;
//    }
//
//    public String getAddress() {
//        return address;
//    }
//
//    public void setAddress(String address) {
//        this.address = address;
//    }
//
//    public String getRegistration() {
//        return registration;
//    }
//
//    public void setRegistration(String registration) {
//        this.registration = registration;
//    }
//
//    public String getTax_id() {
//        return tax_id;
//    }
//
//    public void setTax_id(String tax_id) {
//        this.tax_id = tax_id;
//    }
//
//    public boolean isStatus() {
//        return status;
//    }
//
//    public void setStatus(boolean status) {
//        this.status = status;
//    }
//
//    // toString() Method
//    @Override
//    public String toString() {
//        return "CompanyDTO{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                ", country=" + country +
//                ", primaryEmail='" + primaryEmail + '\'' +
//                ", secondaryEmail='" + secondaryEmail + '\'' +
//                ", primaryContact='" + primaryContact + '\'' +
//                ", secondaryContact='" + secondaryContact + '\'' +
//                ", town='" + town + '\'' +
//                ", address='" + address + '\'' +
//                ", registration='" + registration + '\'' +
//                ", tax_id='" + tax_id + '\'' +
//                ", status=" + status +
//                '}';
//    }
//
//}

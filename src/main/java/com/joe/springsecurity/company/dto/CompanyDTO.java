package com.joe.springsecurity.company.dto;

import com.joe.springsecurity.company.model.Company;

public class CompanyDTO {

    private Long id;
    private String name;

    public CompanyDTO() {
    }

    // Constructor to easily create from entity
    public CompanyDTO(Company company) {
        this.id = company.getId();
        this.name = company.getName();
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

}

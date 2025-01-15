package com.joe.springsecurity.country.dto;

import com.joe.springsecurity.country.model.Country;

public class CountryDTO {

    private Long id;
    private String name;
    private String code;
    private String continent;

    // Default constructor
    public CountryDTO() {
    }

    // Constructor with id, name, code, and continent
    public CountryDTO(Long id, String name, String code, String continent) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.continent = continent;
    }

    // Constructor to initialize from Country entity
    public CountryDTO(Country country) {
        this.id = country.getId();
        this.name = country.getName();
        this.code = country.getCode();
        this.continent = country.getContinent();
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    // toString() Method
    @Override
    public String toString() {
        return "CountryDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", continent='" + continent + '\'' +
                '}';
    }
}

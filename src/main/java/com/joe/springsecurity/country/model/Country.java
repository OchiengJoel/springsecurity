package com.joe.springsecurity.country.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cms_countries")
public class Country implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    private String code;

    private String continent;

    public Country() {
    }


    public Country(String code) {
        this.code = code;
    }

    public Country(String name, String code, String continent) {
        this.name = name;
        this.code = code;
        this.continent = continent;
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


    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

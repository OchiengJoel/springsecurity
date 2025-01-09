package com.joe.springsecurity.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AuthenticationResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("message")
    private String message;

    @JsonProperty("email")
    private String email;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private  String lastName;

    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("companies")
    private List<String> companies;

    private String defaultCompany; // New field to store the default company


    // Constructor updated to include defaultCompany
    public AuthenticationResponse(String accessToken, String refreshToken, String message, String email,
                                  String firstName, String lastName, List<String> roles, List<String> companies,
                                  String defaultCompany) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.message = message;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
        this.companies = companies;
        this.defaultCompany = defaultCompany;
    }


//    public AuthenticationResponse(String accessToken, String refreshToken, String message) {
//        this.accessToken = accessToken;
//        this.message = message;
//        this.refreshToken = refreshToken;
//    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getMessage() {
        return message;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getCompanies() {
        return companies;
    }

    public String getDefaultCompany() {
        return defaultCompany;
    }

    public void setDefaultCompany(String defaultCompany) {
        this.defaultCompany = defaultCompany;
    }
}

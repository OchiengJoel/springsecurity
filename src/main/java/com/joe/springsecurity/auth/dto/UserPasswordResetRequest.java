package com.joe.springsecurity.auth.dto;

public class UserPasswordResetRequest {

    private String email;

    public UserPasswordResetRequest() {
    }

    public UserPasswordResetRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

package com.joe.springsecurity.auth.dto;

import com.joe.springsecurity.auth.model.Role;

import java.util.Set;

public class UserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private Set<Role> roles;  // Make sure roles are included in DTO as a Set or List

    public UserDTO(Long id, String firstName, String lastName, String username, Set<Role> roles) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.roles = roles;
    }

    // Getters and setters for all fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}

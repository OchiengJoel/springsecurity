package com.joe.springsecurity.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SwitchCompanyRequest {

    @JsonProperty("companyId")
    private Long companyId;

    // Default constructor (required by Jackson)
    public SwitchCompanyRequest() {}

    // Parameterized constructor (optional but useful)
    public SwitchCompanyRequest(Long companyId) {
        this.companyId = companyId;
    }

    // Getter and Setter
    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}

package com.joe.springsecurity.auth.dto;

public class SwitchCompanyRequest {

    private Long companyId;

    public SwitchCompanyRequest(Long companyId) {
        this.companyId = companyId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}

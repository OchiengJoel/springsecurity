package com.joe.springsecurity.email.dto;

import com.joe.springsecurity.email.model.EmailConfig;

public class EmailConfigDTO {

    private Long id;
    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String fromAddress;
    private Long companyId;
    private boolean active; // Added active field to indicate if the email config is active or not

    public EmailConfigDTO() {}

    // Constructor to create a DTO from EmailConfig object
    public EmailConfigDTO(EmailConfig emailConfig) {
        this.id = emailConfig.getId();
        this.smtpHost = emailConfig.getSmtpHost();
        this.smtpPort = emailConfig.getSmtpPort();
        this.smtpUsername = emailConfig.getSmtpUsername();
        this.smtpPassword = emailConfig.getSmtpPassword();
        this.fromAddress = emailConfig.getFromAddress();
        this.companyId = emailConfig.getCompany() != null ? emailConfig.getCompany().getId() : null;
        this.active = emailConfig.isActive(); // Set the active status from EmailConfig
    }

    // Constructor with parameters for manual creation
    public EmailConfigDTO(Long id, String smtpHost, int smtpPort, String smtpUsername, String smtpPassword, String fromAddress, Long companyId, boolean active) {
        this.id = id;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
        this.fromAddress = fromAddress;
        this.companyId = companyId;
        this.active = active; // Set the active status
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

package com.joe.springsecurity.email.controller;

import com.joe.springsecurity.email.dto.EmailConfigDTO;
import com.joe.springsecurity.email.service.EmailConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/companies/{companyId}/email-config")
public class EmailConfigController {

    private final EmailConfigService emailConfigService;

    @Autowired
    public EmailConfigController(EmailConfigService emailConfigService) {
        this.emailConfigService = emailConfigService;
    }

    @PostMapping("/create")
    public ResponseEntity<EmailConfigDTO> createEmailConfig(
            @PathVariable Long companyId,
            @RequestBody EmailConfigDTO emailConfigDTO) {
        try {
            EmailConfigDTO createdEmailConfig = emailConfigService.createEmailConfig(companyId, emailConfigDTO);
            return ResponseEntity.ok(createdEmailConfig);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<EmailConfigDTO> updateEmailConfig(
            @PathVariable Long companyId,
            @RequestBody EmailConfigDTO emailConfigDTO) {
        try {
            EmailConfigDTO updatedEmailConfig = emailConfigService.updateEmailConfig(companyId, emailConfigDTO);
            return ResponseEntity.ok(updatedEmailConfig);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Activate email config for a company
    @PutMapping("/activate")
    public ResponseEntity<EmailConfigDTO> activateEmailConfig(@PathVariable Long companyId) {
        try {
            EmailConfigDTO emailConfigDTO = emailConfigService.activateEmailConfig(companyId);
            return ResponseEntity.ok(emailConfigDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Deactivate email config for a company
    @PutMapping("/deactivate")
    public ResponseEntity<EmailConfigDTO> deactivateEmailConfig(@PathVariable Long companyId) {
        try {
            EmailConfigDTO emailConfigDTO = emailConfigService.deactivateEmailConfig(companyId);
            return ResponseEntity.ok(emailConfigDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}

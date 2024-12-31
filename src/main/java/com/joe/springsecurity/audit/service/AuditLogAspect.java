package com.joe.springsecurity.audit.service;

import com.joe.springsecurity.audit.model.ActionType;
import com.joe.springsecurity.audit.model.AuditLog;
import com.joe.springsecurity.audit.repo.AuditLogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Define pointcut for the create, update, and delete methods in InventoryItemService
    @Pointcut("execution(* com.joe.springsecurity.inventory.service.InventoryItemService.createInventoryItem(..)) || " +
            "execution(* com.joe.springsecurity.inventory.service.InventoryItemService.updateInventoryItem(..)) || " +
            "execution(* com.joe.springsecurity.inventory.service.InventoryItemService.deleteInventoryItem(..))")
    public void inventoryItemServiceMethods() {}

    // Define pointcut for the create, update, and delete methods in InventoryItemService
    @Pointcut("execution(* com.joe.springsecurity.company.service.CompanyService.createCompany(..)) || " +
            "execution(* com.joe.springsecurity.company.service.CompanyService.updateCompany(..)) || " +
            "execution(* com.joe.springsecurity.company.service.CompanyService.deleteCompany(..))")
    public void companyServiceMethods() {}

    // Log the action before executing the method for InventoryItemService
    @Before("inventoryItemServiceMethods()")
    public void logBeforeInventoryItemOperations(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String action = "";
        String details = "";

        // Determine the action and details based on method name
        switch (methodName) {
            case "createInventoryItem":
                action = ActionType.CREATE_ITEM.getAction();
                details = "Created a new inventory item.";
                break;
            case "updateInventoryItem":
                action = ActionType.UPDATE_ITEM.getAction();
                details = "Updated an inventory item.";
                break;
            case "deleteInventoryItem":
                action = ActionType.DELETE_ITEM.getAction();
                details = "Deleted an inventory item.";
                break;
        }
        logAction(action, details, joinPoint);
    }

    // Log the action before executing the method for CompanyService
    @Before("companyServiceMethods()")
    public void logBeforeCompanyOperations(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String action = "";
        String details = "";

        // Determine the action and details based on method name
        switch (methodName) {
            case "createCompany":
                action = ActionType.CREATE_COMPANY.getAction();
                details = "Created a new company.";
                break;
            case "updateCompany":
                action = ActionType.UPDATE_COMPANY.getAction();
                details = "Updated a company.";
                break;
            case "deleteCompany":
                action = ActionType.DELETE_COMPANY.getAction();
                details = "Deleted a company.";
                break;
        }
        logAction(action, details, joinPoint);
    }

    // General method for logging actions
    private void logAction(String action, String details, JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "Unknown";

        // Log the details
        try {
            AuditLog auditLog = new AuditLog(action, username, details);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Handle the error gracefully (optional: log it)
            System.err.println("Failed to save audit log: " + e.getMessage());
        }
    }


//    // Log the action before executing the method
//    @Before("inventoryItemServiceMethods()")
//    public void logBeforeOperation(JoinPoint joinPoint) {
//        // Get the currently authenticated user
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication != null ? authentication.getName() : "Unknown";
//
//        // Get the method name being called
//        String methodName = joinPoint.getSignature().getName();
//
//        // Define action and details based on the method
//        String action = "";
//        String details = "";
//
//        if (methodName.equals("createInventoryItem")) {
//            action = "CREATE_ITEM";
//            details = "Created a new inventory item.";
//        } else if (methodName.equals("updateInventoryItem")) {
//            action = "UPDATE_ITEM";
//            details = "Updated an inventory item.";
//        } else if (methodName.equals("deleteInventoryItem")) {
//            action = "DELETE_ITEM";
//            details = "Deleted an inventory item.";
//        }
//
//        // Create and save the audit log entry
//        AuditLog auditLog = new AuditLog(action, username, details);
//        auditLogRepository.save(auditLog); // Save the log in the database
//    }
//
//    // Log the action before executing the method
//    @Before("companyServiceMethods()")
//    public void log1BeforeOperation(JoinPoint joinPoint) {
//        // Get the currently authenticated user
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication != null ? authentication.getName() : "Unknown";
//
//        // Get the method name being called
//        String methodName = joinPoint.getSignature().getName();
//
//        // Define action and details based on the method
//        String action = "";
//        String details = "";
//
//        if (methodName.equals("createCompany")) {
//            action = "CREATE_ITEM";
//            details = "Created a new company.";
//        } else if (methodName.equals("updateCompany")) {
//            action = "UPDATE_ITEM";
//            details = "Updated a company.";
//        } else if (methodName.equals("deleteCompany")) {
//            action = "DELETE_ITEM";
//            details = "Deleted a company.";
//        }
//
//        // Create and save the audit log entry
//        AuditLog auditLog1 = new AuditLog(action, username, details);
//        auditLogRepository.save(auditLog1); // Save the log in the database
//    }
}

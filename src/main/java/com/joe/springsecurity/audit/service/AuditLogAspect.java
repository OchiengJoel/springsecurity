package com.joe.springsecurity.audit.service;

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

    // Log the action before executing the method
    @Before("inventoryItemServiceMethods()")
    public void logBeforeOperation(JoinPoint joinPoint) {
        // Get the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "Unknown";

        // Get the method name being called
        String methodName = joinPoint.getSignature().getName();

        // Define action and details based on the method
        String action = "";
        String details = "";

        if (methodName.equals("createInventoryItem")) {
            action = "CREATE_ITEM";
            details = "Created a new inventory item.";
        } else if (methodName.equals("updateInventoryItem")) {
            action = "UPDATE_ITEM";
            details = "Updated an inventory item.";
        } else if (methodName.equals("deleteInventoryItem")) {
            action = "DELETE_ITEM";
            details = "Deleted an inventory item.";
        }

        // Create and save the audit log entry
        AuditLog auditLog = new AuditLog(action, username, details);
        auditLogRepository.save(auditLog); // Save the log in the database
    }
}

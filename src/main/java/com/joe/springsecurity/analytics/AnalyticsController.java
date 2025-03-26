package com.joe.springsecurity.analytics;

import com.joe.springsecurity.auth.dto.AuthenticationResponse;
import com.joe.springsecurity.company.repo.CompanyRepository;
import com.joe.springsecurity.inventory.repo.InventoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/analytics")
public class AnalyticsController {

    private final CompanyRepository companyRepository;

    private final InventoryItemRepository inventoryItemRepository;

    @Autowired
    public AnalyticsController(CompanyRepository companyRepository, InventoryItemRepository inventoryItemRepository) {
        this.companyRepository = companyRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getCompanyAnalytics() {
        // Get the authenticated user's details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticationResponse userDetails = (AuthenticationResponse) authentication.getPrincipal(); // Assuming AuthResponse is used in JWT
        Long selectedCompanyId = userDetails.getCompanyIds().get(0); // Default to first company, adjust as needed

        Map<String, Object> analytics = new HashMap<>();

        // Counts for the selected company
        analytics.put("totalCompanies", 1); // Single company context, could be omitted or used differently
        long inventoryCount = inventoryItemRepository.countByCompanyId(selectedCompanyId);
        analytics.put("totalInventoryItems", inventoryCount);

        // Bar Chart Data: Inventory by category for the selected company
        Map<String, Long> inventoryByCategory = new HashMap<>();
        inventoryItemRepository.findByCompanyId(selectedCompanyId).forEach(item -> {
            String category = item.getItemCategory().getName();
            inventoryByCategory.merge(category, 1L, Long::sum);
        });
        analytics.put("inventoryByCategory", inventoryByCategory);

        // Pie Chart Data: Inventory distribution by status (example, adjust as needed)
//        Map<String, Long> inventoryByStatus = new HashMap<>();
//        inventoryItemRepository.findByCompanyId(selectedCompanyId).forEach(item -> {
//            String status = item.getStatus(); // Assume status field exists (e.g., "In Stock", "Sold")
//            inventoryByStatus.merge(status, 1L, Long::sum);
//        });
//        analytics.put("inventoryByStatus", inventoryByStatus);

        return ResponseEntity.ok(analytics);
    }

}


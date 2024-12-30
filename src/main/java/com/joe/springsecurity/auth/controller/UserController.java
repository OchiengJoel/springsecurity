package com.joe.springsecurity.auth.controller;

import com.joe.springsecurity.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/users")
public class UserController {


    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{userId}/companies/{companyId}")
    public ResponseEntity<String> assignCompanyToUser(@PathVariable Long userId, @PathVariable Long companyId) {
        String response = userService.assignCompanyToUser(userId, companyId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/companies/{companyId}")
    public ResponseEntity<String> removeCompanyFromUser(@PathVariable Long userId, @PathVariable Long companyId) {
        String response = userService.removeCompanyFromUser(userId, companyId);
        return ResponseEntity.ok(response);
    }
}

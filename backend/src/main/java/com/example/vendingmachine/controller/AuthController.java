package com.example.vendingmachine.controller;

import com.example.vendingmachine.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") 
public class AuthController {

    @Autowired
    private AuthService authService;

    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        String account = loginRequest.get("account");
        String password = loginRequest.get("password");

        
        Map<String, Object> authResult = authService.authenticate(account, password);

        Map<String, Object> response = new HashMap<>();
        if (authResult != null && (Boolean) authResult.get("success")) {
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("data", authResult.get("data"));
        } else {
            response.put("success", false);
            response.put("message", "Invalid account or password");
        }
        return response;
    }
}
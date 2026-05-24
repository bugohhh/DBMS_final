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
    @PostMapping("/register")
public Map<String, Object> register(@RequestBody Map<String, String> request) {
    String userName = request.get("user_name");
    String account = request.get("account");
    String password = request.get("password");

    Map<String, Object> response = new HashMap<>();
    if (userName == null || account == null || password == null) {
        response.put("success", false);
        response.put("message", "缺少必填欄位");
        return response;
    }

    try {
        Map<String, Object> result = authService.registerStaff(userName, account, password);
        response.put("success", true);
        response.put("message", "註冊成功");
        response.put("data", result);
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", e.getMessage());
    }
    return response;
}
}
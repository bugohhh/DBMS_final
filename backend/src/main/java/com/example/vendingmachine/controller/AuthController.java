package com.example.vendingmachine.controller;

import com.example.vendingmachine.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth") // 前端呼叫的網址起頭
public class AuthController {

    @Autowired
    private AuthService authService;

    // 開闢一個讓前端用 POST 呼叫 /api/auth/login 的入口
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginRequest) {
        String account = loginRequest.get("account");
        String password = loginRequest.get("password");

        // 啟動大腦進行驗證
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
package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public ApiResponse<Void> test() {
        return ApiResponse.success("Backend is running", null);
    }
}

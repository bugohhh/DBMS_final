package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.model.SalesRecord;
import com.example.vendingmachine.service.AuthService;
import com.example.vendingmachine.service.SalesRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/sales-records")
public class SalesRecordController {

    private final SalesRecordService salesRecordService;
    private final AuthService authService;

    public SalesRecordController(SalesRecordService salesRecordService, AuthService authService) {
        this.salesRecordService = salesRecordService;
        this.authService = authService;
    }

    @PostMapping
    public ApiResponse<SalesRecord> createSalesRecord(
            @RequestBody SalesRecord salesRecord,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        return ApiResponse.success("Sales record created", salesRecordService.createSalesRecord(salesRecord));
    }

    @GetMapping
    public ApiResponse<List<SalesRecord>> getSalesRecords(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String token = extractBearerToken(authorization);
        if (!authService.isManagerToken(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only managers can read sales records");
        }

        return ApiResponse.success("Sales record list", salesRecordService.getSalesRecords());
    }

    private void requireValidToken(String authorization) {
        String token = extractBearerToken(authorization);
        if (!authService.isValidToken(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or expired token");
        }
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }

        String prefix = "Bearer ";
        if (!authorization.startsWith(prefix)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization must use Bearer token");
        }

        String token = authorization.substring(prefix.length()).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token");
        }
        return token;
    }
}

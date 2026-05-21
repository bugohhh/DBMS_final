package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.dto.DeviceInventoryUpdateRequest;
import com.example.vendingmachine.dto.DeviceSalesRecordRequest;
import com.example.vendingmachine.service.AuthService;
import com.example.vendingmachine.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private final DeviceService deviceService;
    private final AuthService authService;

    public DeviceController(DeviceService deviceService, AuthService authService) {
        this.deviceService = deviceService;
        this.authService = authService;
    }

    @PostMapping("/inventory/update")
    public ApiResponse<Void> updateInventoryFromDevice(
            @RequestBody DeviceInventoryUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        deviceService.updateInventoryFromDevice(request);
        return ApiResponse.success("Device inventory update received", null);
    }

    @PostMapping("/sales-records")
    public ApiResponse<Void> createSalesRecordFromDevice(
            @RequestBody DeviceSalesRecordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        deviceService.createSalesRecordFromDevice(request);
        return ApiResponse.success("Device sales record received", null);
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

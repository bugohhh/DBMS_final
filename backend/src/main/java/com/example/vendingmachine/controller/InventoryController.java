package com.example.vendingmachine.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.model.Inventory;
import com.example.vendingmachine.service.AuthService;
import com.example.vendingmachine.service.InventoryService;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;
    private final AuthService authService;

    public InventoryController(InventoryService inventoryService, AuthService authService) {
        this.inventoryService = inventoryService;
        this.authService = authService;
    }

    @GetMapping("/machines/{machine_id}/inventory")
    public ApiResponse<List<Inventory>> getInventoryByMachineId(
            @PathVariable("machine_id") Long machineId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        return ApiResponse.success("Inventory list", inventoryService.getInventoryByMachineId(machineId));
    }

    @PostMapping("/inventory")
    public ApiResponse<Inventory> createInventory(
            @RequestBody Inventory inventory,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        return ApiResponse.success("Inventory created", inventoryService.createInventory(inventory));
    }

    @PutMapping("/inventory/{inventory_id}")
    public ApiResponse<Inventory> updateInventory(
            @PathVariable("inventory_id") Long inventoryId,
            @RequestBody Inventory inventory,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        requireValidToken(authorization);
        return ApiResponse.success("Inventory updated", inventoryService.updateInventory(inventoryId, inventory));
    }

    // Howard 指定：low-stock 不需要 token 驗證。
    @GetMapping("/inventory/low-stock")
    public ApiResponse<List<Inventory>> getLowStockInventory() {
        return ApiResponse.success("Low stock inventory list", inventoryService.getLowStockInventory());
    }

    // Howard 指定：public inventory 不需要 token 驗證。
    @GetMapping("/public/machines/{machine_id}/inventory")
    public ApiResponse<List<Inventory>> getPublicInventoryByMachineId(@PathVariable("machine_id") Long machineId) {
        return ApiResponse.success("Public inventory list", inventoryService.getInventoryByMachineId(machineId));
    }
    //考慮移到auth controller，因為這裡是專門給前端讀取庫存的，不需要驗證token，直接公開就好。
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

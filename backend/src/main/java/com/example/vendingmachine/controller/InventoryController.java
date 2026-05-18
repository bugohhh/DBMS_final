package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.model.Inventory;
import com.example.vendingmachine.service.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/machines/{machine_id}/inventory")
    public ApiResponse<List<Inventory>> getInventoryByMachineId(@PathVariable("machine_id") Long machineId) {
        return ApiResponse.success("Inventory list", inventoryService.getInventoryByMachineId(machineId));
    }

    @PostMapping("/inventory")
    public ApiResponse<Inventory> createInventory(@RequestBody Inventory inventory) {
        return ApiResponse.success("Inventory created", inventoryService.createInventory(inventory));
    }

    @PutMapping("/inventory/{inventory_id}")
    public ApiResponse<Inventory> updateInventory(
            @PathVariable("inventory_id") Long inventoryId,
            @RequestBody Inventory inventory
    ) {
        return ApiResponse.success("Inventory updated", inventoryService.updateInventory(inventoryId, inventory));
    }

    @GetMapping("/inventory/low-stock")
    public ApiResponse<List<Inventory>> getLowStockInventory() {
        return ApiResponse.success("Low stock inventory list", inventoryService.getLowStockInventory());
    }

    @GetMapping("/public/machines/{machine_id}/inventory")
    public ApiResponse<List<Inventory>> getPublicInventoryByMachineId(@PathVariable("machine_id") Long machineId) {
        return ApiResponse.success("Public inventory list", inventoryService.getInventoryByMachineId(machineId));
    }
}

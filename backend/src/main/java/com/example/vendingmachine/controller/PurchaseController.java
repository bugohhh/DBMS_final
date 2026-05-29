package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.dto.InventoryDetailsDTO;
import com.example.vendingmachine.dto.PurchaseRequest;
import com.example.vendingmachine.service.DeviceService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PurchaseController {

    private final DeviceService deviceService;

    public PurchaseController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/machines/{machine_id}/purchase")
    public ApiResponse<List<InventoryDetailsDTO>> purchaseDrink(
            @PathVariable("machine_id") Long machineId,
            @RequestBody PurchaseRequest request
    ) {
        List<InventoryDetailsDTO> latestInventory = deviceService.purchaseFromSimulator(
                machineId,
                request.getDrinkId(),
                request.getQuantity()
        );
        return ApiResponse.success("Purchase completed", latestInventory);
    }
}

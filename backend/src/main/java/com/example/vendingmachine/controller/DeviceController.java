package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.dto.DeviceInventoryUpdateRequest;
import com.example.vendingmachine.dto.DeviceSalesRecordRequest;
import com.example.vendingmachine.service.DeviceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/inventory/update")
    public ApiResponse<Void> updateInventoryFromDevice(@RequestBody DeviceInventoryUpdateRequest request) {
        deviceService.updateInventoryFromDevice(request);
        return ApiResponse.success("Device inventory update received", null);
    }

    @PostMapping("/sales-records")
    public ApiResponse<Void> createSalesRecordFromDevice(@RequestBody DeviceSalesRecordRequest request) {
        deviceService.createSalesRecordFromDevice(request);
        return ApiResponse.success("Device sales record received", null);
    }
}

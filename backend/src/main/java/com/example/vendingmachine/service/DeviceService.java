package com.example.vendingmachine.service;

import com.example.vendingmachine.dto.DeviceInventoryUpdateRequest;
import com.example.vendingmachine.dto.DeviceSalesRecordRequest;
import com.example.vendingmachine.dto.InventoryDetailsDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DeviceService {

    private final InventoryService inventoryService;
    private final SalesRecordService salesRecordService;

    public DeviceService(InventoryService inventoryService, SalesRecordService salesRecordService) {
        this.inventoryService = inventoryService;
        this.salesRecordService = salesRecordService;
    }

    public void updateInventoryFromDevice(DeviceInventoryUpdateRequest request) {
        inventoryService.updateInventoryFromDevice(
                request.getMachineId(),
                request.getDrinkId(),
                request.getQuantity()
        );
    }

    @Transactional
    public void createSalesRecordFromDevice(DeviceSalesRecordRequest request) {
        salesRecordService.createDeviceSalesRecord(
                request.getMachineId(),
                request.getDrinkId(),
                request.getQuantity(),
                request.getSaleTime()
        );

        // 智慧販賣機回報銷售時，同步扣庫存。
        inventoryService.decreaseInventoryAfterSale(
                request.getMachineId(),
                request.getDrinkId(),
                request.getQuantity()
        );
    }

    @Transactional
    public List<InventoryDetailsDTO> purchaseFromSimulator(Long machineId, Long drinkId, Integer quantity) {
        if (machineId == null || drinkId == null || quantity == null || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId, drinkId and positive quantity are required");
        }

        salesRecordService.createDeviceSalesRecord(machineId, drinkId, quantity, java.time.LocalDateTime.now());
        inventoryService.decreaseInventoryAfterSale(machineId, drinkId, quantity);

        return inventoryService.getPublicInventoryDetailsByMachineId(machineId);
    }

    public void handleDeviceInventoryUpdate() {
        // 保留既有方法，避免影響其他人如果有呼叫；實際功能由 updateInventoryFromDevice 處理。
    }

    public void handleDeviceSalesRecord() {
        // 保留既有方法，避免影響其他人如果有呼叫；實際功能由 createSalesRecordFromDevice 處理。
    }
}

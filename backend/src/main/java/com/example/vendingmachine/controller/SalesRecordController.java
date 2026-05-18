package com.example.vendingmachine.controller;

import com.example.vendingmachine.dto.ApiResponse;
import com.example.vendingmachine.model.SalesRecord;
import com.example.vendingmachine.service.SalesRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sales-records")
public class SalesRecordController {

    private final SalesRecordService salesRecordService;

    public SalesRecordController(SalesRecordService salesRecordService) {
        this.salesRecordService = salesRecordService;
    }

    @PostMapping
    public ApiResponse<SalesRecord> createSalesRecord(@RequestBody SalesRecord salesRecord) {
        return ApiResponse.success("Sales record created", salesRecordService.createSalesRecord(salesRecord));
    }

    @GetMapping
    public ApiResponse<List<SalesRecord>> getSalesRecords() {
        return ApiResponse.success("Sales record list", salesRecordService.getSalesRecords());
    }
}

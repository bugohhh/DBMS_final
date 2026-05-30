package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.SalesRecordDao;
import com.example.vendingmachine.dto.RegionDrinkSalesSummaryDTO;
import com.example.vendingmachine.model.SalesRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SalesRecordService {

    private final SalesRecordDao salesRecordDao;

    public SalesRecordService(SalesRecordDao salesRecordDao) {
        this.salesRecordDao = salesRecordDao;
    }

    public SalesRecord createSalesRecord(SalesRecord salesRecord) {
        validateSalesRecord(salesRecord);
        if (salesRecord.getRecordSource() == null) {
            salesRecord.setRecordSource("Manual");
        }
        return salesRecordDao.create(salesRecord);
    }

    public SalesRecord createDeviceSalesRecord(Long machineId, Long drinkId, Integer quantity, java.time.LocalDateTime saleTime) {
        return createDeviceSalesRecord(machineId, drinkId, quantity, null, saleTime);
    }

    public SalesRecord createDeviceSalesRecord(Long machineId, Long drinkId, Integer quantity, java.math.BigDecimal price, java.time.LocalDateTime saleTime) {
        SalesRecord record = new SalesRecord();
        record.setMachineId(machineId);
        record.setDrinkId(drinkId);
        record.setQuantity(quantity);
        record.setPrice(price);
        record.setSaleTime(saleTime);
        record.setRecordSource("Auto");
        return createSalesRecord(record);
    }

    public SalesRecord createManualSalesRecord(Long machineId, Long drinkId, Integer quantity, java.math.BigDecimal price) {
        SalesRecord record = new SalesRecord();
        record.setMachineId(machineId);
        record.setDrinkId(drinkId);
        record.setQuantity(quantity);
        record.setPrice(price);
        record.setSaleTime(java.time.LocalDateTime.now());
        record.setRecordSource("Manual");
        return createSalesRecord(record);
    }

    public List<SalesRecord> getSalesRecords() {
        return salesRecordDao.findAll();
    }

    public List<SalesRecord> getSalesRecords(Long machineId, Long drinkId) {
        if (machineId == null && drinkId == null) {
            return getSalesRecords();
        }
        return salesRecordDao.findByFilters(machineId, drinkId);
    }

    public List<RegionDrinkSalesSummaryDTO> getDrinkSalesSummaryByRegion(Long regionId) {
        if (regionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "regionId is required");
        }
        return salesRecordDao.sumDrinkSalesByRegion(regionId);
    }

    private void validateSalesRecord(SalesRecord salesRecord) {
        if (salesRecord == null
                || salesRecord.getMachineId() == null
                || salesRecord.getDrinkId() == null
                || salesRecord.getQuantity() == null
                || salesRecord.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "machineId, drinkId and positive quantity are required");
        }
    }
}

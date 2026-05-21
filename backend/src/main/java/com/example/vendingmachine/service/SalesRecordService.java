package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.SalesRecordDao;
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
        SalesRecord record = new SalesRecord();
        record.setMachineId(machineId);
        record.setDrinkId(drinkId);
        record.setQuantity(quantity);
        record.setSaleTime(saleTime);
        record.setRecordSource("Auto");
        return createSalesRecord(record);
    }

    public List<SalesRecord> getSalesRecords() {
        return salesRecordDao.findAll();
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

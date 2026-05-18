package com.example.vendingmachine.service;

import com.example.vendingmachine.dao.SalesRecordDao;
import com.example.vendingmachine.model.SalesRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesRecordService {

    private final SalesRecordDao salesRecordDao;

    public SalesRecordService(SalesRecordDao salesRecordDao) {
        this.salesRecordDao = salesRecordDao;
    }

    public SalesRecord createSalesRecord(SalesRecord salesRecord) {
        return salesRecordDao.create(salesRecord);
    }

    public List<SalesRecord> getSalesRecords() {
        return salesRecordDao.findAll();
    }
}

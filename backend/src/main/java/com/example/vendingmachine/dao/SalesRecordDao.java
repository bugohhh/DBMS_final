package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.SalesRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class SalesRecordDao {

    private final JdbcTemplate jdbcTemplate;

    public SalesRecordDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SalesRecord create(SalesRecord salesRecord) {
        // TODO: Implement INSERT into SalesRecord.
        return salesRecord;
    }

    public List<SalesRecord> findAll() {
        // TODO: Implement SELECT from SalesRecord.
        return Collections.emptyList();
    }
}

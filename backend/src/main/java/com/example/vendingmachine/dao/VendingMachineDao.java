package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.VendingMachine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VendingMachineDao {

    private final JdbcTemplate jdbcTemplate;

    public VendingMachineDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public VendingMachine findById(Long machineId) {
        // TODO: Implement SELECT from VendingMachine by machine_id.
        return null;
    }
}

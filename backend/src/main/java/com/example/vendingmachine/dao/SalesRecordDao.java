package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.SalesRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SalesRecordDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<SalesRecord> salesRecordMapper = new RowMapper<SalesRecord>() {
        @Override
        public SalesRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            SalesRecord record = new SalesRecord();
            record.setRecordId(rs.getLong("record_id"));
            record.setMachineId(rs.getLong("machine_id"));
            record.setDrinkId(rs.getLong("drink_id"));
            record.setQuantity(rs.getInt("quantity"));
            record.setSalesTime(rs.getTimestamp("sales_time"));
            return record;
        }
    };


    public void insert(SalesRecord record) {
        String sql = "INSERT INTO sales_record (machine_id, drink_id, quantity) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, record.getMachineId(), record.getDrinkId(), record.getQuantity());
    }


    public List<SalesRecord> findAll() {
        String sql = "SELECT * FROM sales_record";
        return jdbcTemplate.query(sql, salesRecordMapper);
    }
}
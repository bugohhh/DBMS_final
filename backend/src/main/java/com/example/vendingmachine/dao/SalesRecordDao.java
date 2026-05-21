package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.SalesRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SalesRecordDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<SalesRecord> salesRecordMapper = new RowMapper<SalesRecord>() {
        @Override
        public SalesRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            SalesRecord record = new SalesRecord();

            record.setSalesId(rs.getLong("sales_id")); 
            record.setMachineId(rs.getLong("machine_id"));
            record.setDrinkId(rs.getLong("drink_id"));
            record.setQuantity(rs.getInt("quantity"));
            

            Timestamp timestamp = rs.getTimestamp("sale_time");
            if (timestamp != null) {
                record.setSaleTime(timestamp.toLocalDateTime());
            }
            record.setRecordSource(rs.getString("record_source"));
            return record;
        }
    };


    public SalesRecord create(SalesRecord record) {
        String sql = "INSERT INTO SalesRecord (machine_id, drink_id, quantity, sale_time, record_source) VALUES (?, ?, ?, ?, ?)";
        
        Timestamp timestamp = record.getSaleTime() != null ? Timestamp.valueOf(record.getSaleTime()) : new Timestamp(System.currentTimeMillis());
        
        jdbcTemplate.update(sql, 
            record.getMachineId(), 
            record.getDrinkId(), 
            record.getQuantity(),
            timestamp,
            record.getRecordSource()
        );
        
        return record;
    }

    public List<SalesRecord> findAll() {
        String sql = "SELECT * FROM SalesRecord ORDER BY sale_time DESC, sales_id DESC";
        return jdbcTemplate.query(sql, salesRecordMapper);
    }
}
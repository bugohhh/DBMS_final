package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.SalesRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SalesRecordDao {

    private final JdbcTemplate jdbcTemplate;

    public SalesRecordDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

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
        String sql = """
                INSERT INTO SalesRecord (machine_id, drink_id, quantity, sale_time, record_source)
                VALUES (?, ?, ?, ?, ?)
                """;
        Timestamp timestamp = record.getSaleTime() != null
                ? Timestamp.valueOf(record.getSaleTime())
                : new Timestamp(System.currentTimeMillis());
        String recordSource = record.getRecordSource() != null ? record.getRecordSource() : "Manual";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, record.getMachineId());
            ps.setLong(2, record.getDrinkId());
            ps.setInt(3, record.getQuantity());
            ps.setTimestamp(4, timestamp);
            ps.setString(5, recordSource);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            record.setSalesId(keyHolder.getKey().longValue());
        }
        record.setSaleTime(timestamp.toLocalDateTime());
        record.setRecordSource(recordSource);
        return record;
    }

    public List<SalesRecord> findAll() {
        String sql = """
                SELECT sales_id, machine_id, drink_id, quantity, sale_time, record_source
                FROM SalesRecord
                ORDER BY sale_time DESC, sales_id DESC
                """;
        return jdbcTemplate.query(sql, salesRecordMapper);
    }
}

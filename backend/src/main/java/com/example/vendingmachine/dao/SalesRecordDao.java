package com.example.vendingmachine.dao;

import com.example.vendingmachine.dto.RegionDrinkSalesSummaryDTO;
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

    public List<SalesRecord> findByFilters(Long machineId, Long drinkId) {
        StringBuilder sql = new StringBuilder("""
                SELECT sales_id, machine_id, drink_id, quantity, sale_time, record_source
                FROM SalesRecord
                WHERE 1 = 1
                """);
        java.util.List<Object> params = new java.util.ArrayList<>();
        if (machineId != null) {
            sql.append(" AND machine_id = ?");
            params.add(machineId);
        }
        if (drinkId != null) {
            sql.append(" AND drink_id = ?");
            params.add(drinkId);
        }
        sql.append(" ORDER BY sale_time DESC, sales_id DESC");
        return jdbcTemplate.query(sql.toString(), salesRecordMapper, params.toArray());
    }

    public List<RegionDrinkSalesSummaryDTO> sumDrinkSalesByRegion(Long regionId) {
        String sql = """
                SELECT sr.drink_id, d.drink_name, COALESCE(SUM(sr.quantity), 0) AS total_quantity
                FROM SalesRecord sr
                JOIN VendingMachine vm ON sr.machine_id = vm.machine_id
                JOIN Drink d ON sr.drink_id = d.drink_id
                WHERE vm.region_id = ?
                GROUP BY sr.drink_id, d.drink_name
                ORDER BY total_quantity DESC, d.drink_name, sr.drink_id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RegionDrinkSalesSummaryDTO dto = new RegionDrinkSalesSummaryDTO();
            dto.setDrinkId(rs.getLong("drink_id"));
            dto.setDrinkName(rs.getString("drink_name"));
            dto.setTotalQuantity(rs.getLong("total_quantity"));
            return dto;
        }, regionId);
    }

}

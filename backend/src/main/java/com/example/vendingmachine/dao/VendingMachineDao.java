package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.VendingMachine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@Repository
public class VendingMachineDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<VendingMachine> machineMapper = new RowMapper<VendingMachine>() {
        @Override
        public VendingMachine mapRow(ResultSet rs, int rowNum) throws SQLException {
            VendingMachine machine = new VendingMachine();
            machine.setMachineId(rs.getLong("machine_id"));
            machine.setMachineName(rs.getString("machine_name"));
            machine.setRegionId(rs.getLong("region_id"));
            machine.setMachineType(rs.getString("machine_type"));
            machine.setLocation(rs.getString("location"));
            machine.setStatus(rs.getString("status"));
            return machine;
        }
    };

    public List<VendingMachine> findAll() {
        String sql = "SELECT * FROM VendingMachine";
        return jdbcTemplate.query(sql, machineMapper);
    }

    public Optional<VendingMachine> findById(Long id) {
        String sql = "SELECT * FROM VendingMachine WHERE machine_id = ?";
        List<VendingMachine> list = jdbcTemplate.query(sql, machineMapper, id);
        return list.stream().findFirst();
    }


    public List<VendingMachine> findByRegionId(Long regionId) {
        String sql = "SELECT * FROM VendingMachine WHERE region_id = ?";
        return jdbcTemplate.query(sql, machineMapper, regionId);
    }

    public VendingMachine save(VendingMachine machine) {
        if (machine.getMachineId() == null) {
            String sql = "INSERT INTO VendingMachine (machine_name, machine_type, location, status, region_id) VALUES (?, ?, ?, COALESCE(?, '運行'), ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, machine.getMachineName());
                ps.setString(2, machine.getMachineType() == null ? "Smart" : machine.getMachineType());
                ps.setString(3, machine.getLocation());
                ps.setString(4, machine.getStatus());
                ps.setLong(5, machine.getRegionId());
                return ps;
            }, keyHolder);
            
            Number key = keyHolder.getKey();
            if (key != null) machine.setMachineId(key.longValue());
            return machine;
        } else {
            String sql = "UPDATE VendingMachine SET machine_name = ?, machine_type = COALESCE(?, machine_type), location = COALESCE(?, location), region_id = ? WHERE machine_id = ?";
            jdbcTemplate.update(sql, machine.getMachineName(), machine.getMachineType(), machine.getLocation(), machine.getRegionId(), machine.getMachineId());
            return machine;
        }
    }

    public boolean deleteById(Long machineId) {
        jdbcTemplate.update("DELETE FROM Inventory WHERE machine_id = ?", machineId);
        int deleted = jdbcTemplate.update("DELETE FROM VendingMachine WHERE machine_id = ?", machineId);
        return deleted > 0;
    }
}
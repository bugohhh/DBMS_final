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
            return machine;
        }
    };


    public List<VendingMachine> findAll() {
        String sql = "SELECT * FROM vending_machine";
        return jdbcTemplate.query(sql, machineMapper);
    }


    public Optional<VendingMachine> findById(Long id) {
        String sql = "SELECT * FROM vending_machine WHERE machine_id = ?";
        List<VendingMachine> list = jdbcTemplate.query(sql, machineMapper, id);
        return list.stream().findFirst();
    }


    public VendingMachine save(VendingMachine machine) {
        if (machine.getMachineId() == null) {
            String sql = "INSERT INTO vending_machine (machine_name, region_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, machine.getMachineName(), machine.getRegionId());
            return machine;
        } else {
            String sql = "UPDATE vending_machine SET machine_name = ?, region_id = ? WHERE machine_id = ?";
            jdbcTemplate.update(sql, machine.getMachineName(), machine.getRegionId(), machine.getMachineId());
            return machine;
        }
    }


    public void deleteById(Long id) {
        String sql = "DELETE FROM vending_machine WHERE machine_id = ?";
        jdbcTemplate.update(sql, id);
    }
}
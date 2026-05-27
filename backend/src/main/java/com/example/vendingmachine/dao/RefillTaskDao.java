package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.RefillDetail;
import com.example.vendingmachine.model.RefillTask;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class RefillTaskDao {

    private final JdbcTemplate jdbcTemplate;

    public RefillTaskDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RefillTask> refillTaskMapper = new RowMapper<RefillTask>() {
        @Override
        public RefillTask mapRow(ResultSet rs, int rowNum) throws SQLException {
            RefillTask task = new RefillTask();
            // DB uses `refilltask_id` (no underscore)
            task.setRefillTaskId(rs.getLong("refilltask_id"));
            task.setTeamId(rs.getLong("team_id"));
            task.setRegionId(rs.getLong("region_id"));
            task.setRegionName(rs.getString("region_name"));
            task.setMachineId(rs.getLong("machine_id"));
            task.setMachineNames(rs.getString("machine_name")); // 直接用現有的 machineNames 欄位

            java.sql.Date taskDate = rs.getDate("task_date");
            if (taskDate != null) task.setTaskDate(taskDate.toLocalDate());

            task.setTaskType(rs.getString("task_type"));

            java.sql.Timestamp createdTs = rs.getTimestamp("created_time");
            if (createdTs != null) task.setCreatedTime(createdTs.toLocalDateTime());

            task.setStatus(rs.getString("status"));
            return task;
        }
    };

    public List<RefillTask> findAll() {
        String sql = "SELECT r.refilltask_id, r.team_id, r.region_id, r.machine_id, r.task_date, r.task_type, r.created_time, r.status, " +
             "rg.region_name, vm.machine_name " +
             "FROM RefillTask r " +
             "LEFT JOIN Region rg ON r.region_id = rg.region_id " +
             "LEFT JOIN VendingMachine vm ON r.machine_id = vm.machine_id";        
        return jdbcTemplate.query(sql, refillTaskMapper);
    }

    public List<RefillTask> findByStaffId(Long staffId) {
        String sql = "SELECT r.refilltask_id, r.team_id, r.region_id, r.machine_id, r.task_date, r.task_type, r.created_time, r.status, " +
             "rg.region_name, vm.machine_name " +
             "FROM RefillTask r " +
             "JOIN Staff s ON r.team_id = s.team_id " +
             "LEFT JOIN Region rg ON r.region_id = rg.region_id " +
             "LEFT JOIN VendingMachine vm ON r.machine_id = vm.machine_id " +
             "WHERE s.user_id = ?";
        return jdbcTemplate.query(sql, refillTaskMapper, staffId);
    }

    public Optional<RefillTask> findByRefillTaskId(Long refillTaskId) {
        String sql = "SELECT r.refilltask_id, r.team_id, r.region_id, r.machine_id, r.task_date, r.task_type, r.created_time, r.status, " +
             "rg.region_name, vm.machine_name " +
             "FROM RefillTask r " +
             "LEFT JOIN Region rg ON r.region_id = rg.region_id " +
             "LEFT JOIN VendingMachine vm ON r.machine_id = vm.machine_id " +
             "WHERE r.refilltask_id = ?";
        return jdbcTemplate.query(sql, refillTaskMapper, refillTaskId).stream().findFirst();
    }

    public RefillTask create(RefillTask refillTask) {
        String sql = "INSERT INTO RefillTask (team_id, region_id, machine_id, task_date, task_type, created_time, status) VALUES (?, ?, ?, ?, ?, NOW(), ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, refillTask.getTeamId());
            ps.setLong(2, refillTask.getRegionId());
            if (refillTask.getMachineId() != null) {
                ps.setLong(3, refillTask.getMachineId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            if (refillTask.getTaskDate() != null) {
                ps.setDate(4, java.sql.Date.valueOf(refillTask.getTaskDate()));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            ps.setString(5, refillTask.getTaskType());
            ps.setString(6, refillTask.getStatus());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            refillTask.setRefillTaskId(keyHolder.getKey().longValue());
        }
        return refillTask;
    }

    public boolean updateStatus(Long refillTaskId, String status) {
        String sql = "UPDATE RefillTask SET status = ? WHERE refilltask_id = ?";
        int updated = jdbcTemplate.update(sql, status, refillTaskId);
        return updated > 0;
    }

    public boolean updateTeamAndStatus(Long refillTaskId, Long teamId, String status) {
        String sql = "UPDATE RefillTask SET team_id = ?, status = ? WHERE refilltask_id = ?";
        int updated = jdbcTemplate.update(sql, teamId, status, refillTaskId);
        return updated > 0;
    }

    public void createRefillDetail(Long refillTaskId, Long machineId, Long drinkId, Integer actualQty) {
        String sql = "INSERT INTO RefillDetail (refilltask_id, machine_id, drink_id, actual_quantity, refill_time) VALUES (?, ?, ?, ?, NOW())";
        jdbcTemplate.update(sql, refillTaskId, machineId, drinkId, actualQty);
    }

    public RefillDetail updateRefillDetail(Long refillDetailsId, RefillDetail refillDetail) {
        // Minimal implementation: update actual_quantity and last_restock
        String sql = "UPDATE RefillDetail SET actual_quantity = ?, last_restock = NOW() WHERE refilldetails_id = ?";
        jdbcTemplate.update(sql, refillDetail.getActualQuantity(), refillDetailsId);
        return refillDetail;
    }

    public boolean delete(Long refillTaskId) {
        // 先刪關聯的 RefillDetail
        jdbcTemplate.update("DELETE FROM RefillDetail WHERE refilltask_id = ?", refillTaskId);
        // 再刪任務
        int deleted = jdbcTemplate.update("DELETE FROM RefillTask WHERE refilltask_id = ?", refillTaskId);
        return deleted > 0;
    }
}

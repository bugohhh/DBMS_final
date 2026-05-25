package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.Inventory;
import com.example.vendingmachine.model.RefillTask;
import com.example.vendingmachine.model.RefillDetail;

import org.springframework.http.HttpStatus;
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
            task.setRefillTaskId(rs.getLong("refill_task_id"));
            task.setTeamId(rs.getLong("team_id"));
            task.setRegionId(rs.getLong("region_id"));
            task.setTaskDate(rs.getDate("task_date").toLocalDate());
            task.setTaskType(rs.getString("task_type"));
            task.setCreatedTime(rs.getTimestamp("created_time").toLocalDateTime());
            task.setStatus(rs.getString("status"));
            return task;
        }
    };

    public List<RefillTask> findAll() {
        String sql = """
                SELECT * FROM RefillTask
                """;
        return jdbcTemplate.query(sql, refillTaskMapper);
    }

    public List<RefillTask> findByStaffId(Long staffId) {
        String sql = """
                SELECT * FROM RefillTask 
                OUTER JOIN staff_team ON RefillTask.team_id = staff_team.team_id
                WHERE staff_id = ?
                """;
        return jdbcTemplate.query(sql, refillTaskMapper, staffId);
    }

    public Optional<RefillTask> findByRefillTaskId(Long refillTaskId) {
        String sql = """
                SELECT refill_task_id, team_id, region_id, task_date, task_type, created_time, status
                FROM RefillTask
                WHERE refill_task_id = ?
                ORDER BY refill_task_id
                """;
        return jdbcTemplate.query(sql, refillTaskMapper, refillTaskId).stream().findFirst();
    }

    public RefillTask create(RefillTask refillTask) {
        String sql = """
                INSERT INTO RefillTask (team_id, region_id, task_date, task_type, created_time, status)
                VALUES (?, ?, ?, ?, NOW(), ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, refillTask.getTeamId());
            ps.setLong(2, refillTask.getRegionId());
            ps.setDate(3, java.sql.Date.valueOf(refillTask.getTaskDate()));
            ps.setString(4, refillTask.getTaskType());
            ps.setString(5, refillTask.getStatus());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            refillTask.setRefillTaskId(keyHolder.getKey().longValue());
        }
        return refillTask;
    }

    public boolean updateStatus(Long refillTaskId, String status) {
        String sql = """
                UPDATE RefillTask
                SET status = ?
                WHERE refill_task_id = ?
                """;
        int updated = jdbcTemplate.update(sql, status, refillTaskId);
        return updated > 0;
    }

    public RefillDetail updateRefillDetail(Long refillDetailsId, RefillDetail refillDetail) {
        String sql = """
                UPDATE RefillDetail
                SET refill_task_id = ?, machine_id = ?, drink_id = ?, planned_quantity = ?, actual_quantity = ?, last_restock = NOW(), update_source = 'Manual'
                WHERE refill_details_id = ?
                """;
        int updated = jdbcTemplate.update(sql, refillDetail.getRefillTaskId(), refillDetail.getMachineId(), refillDetail.getDrinkId(), refillDetail.getPlannedQuantity(), refillDetail.getActualQuantity(), refillDetailsId);
        if (updated > 0) {
            return refillDetail;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Refill detail not found");
    }
}

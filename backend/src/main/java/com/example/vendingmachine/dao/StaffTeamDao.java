package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.StaffTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class StaffTeamDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<StaffTeam> staffTeamMapper = new RowMapper<StaffTeam>() {
    @Override
    public StaffTeam mapRow(ResultSet rs, int rowNum) throws SQLException {
        StaffTeam st = new StaffTeam();
        st.setTeamId(rs.getLong("team_id"));
        st.setStaffId(rs.getLong("user_id"));
        return st;
        }
    };

    public StaffTeam save(StaffTeam staffTeam) {
        // 檢查 user 是否存在
        String checkUserSql = "SELECT COUNT(*) FROM `User` WHERE user_id = ?";
        int userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, staffTeam.getStaffId());
        if (userCount == 0) {
            throw new RuntimeException("找不到 User ID: " + staffTeam.getStaffId());
        }

        // 檢查是否已在 Staff 表
        String checkSql = "SELECT COUNT(*) FROM Staff WHERE user_id = ?";
        int count = jdbcTemplate.queryForObject(checkSql, Integer.class, staffTeam.getStaffId());
        if (count > 0) {
            String sql = "UPDATE Staff SET team_id = ? WHERE user_id = ?";
            jdbcTemplate.update(sql, staffTeam.getTeamId(), staffTeam.getStaffId());
        } else {
            String sql = "INSERT INTO Staff (user_id, team_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, staffTeam.getStaffId(), staffTeam.getTeamId());
        }
        return staffTeam;
    }

    public List<StaffTeam> findByTeamId(Long teamId) {
        String sql = "SELECT s.user_id, s.team_id, u.user_name FROM Staff s JOIN User u ON s.user_id = u.user_id WHERE s.team_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            StaffTeam st = new StaffTeam();
            st.setStaffId(rs.getLong("user_id"));
            st.setTeamId(rs.getLong("team_id"));
            st.setStaffName(rs.getString("user_name"));
            return st;
        }, teamId);
    }

    public Optional<StaffTeam> findByTeamIdAndStaffId(Long teamId, Long staffId) {
        String sql = "SELECT user_id, team_id FROM Staff WHERE team_id = ? AND user_id = ?";
        return jdbcTemplate.query(sql, staffTeamMapper, teamId, staffId).stream().findFirst();
    }

    public void delete(StaffTeam staffTeam) {
        // 移除成員 = 把 team_id 設為 NULL
        String sql = "UPDATE Staff SET team_id = NULL WHERE user_id = ? AND team_id = ?";
        jdbcTemplate.update(sql, staffTeam.getStaffId(), staffTeam.getTeamId());
    }
}
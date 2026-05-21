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
            st.setId(rs.getLong("id"));
            st.setTeamId(rs.getLong("team_id"));
            st.setStaffId(rs.getLong("staff_id"));
            return st;
        }
    };

    public StaffTeam save(StaffTeam staffTeam) {
        String sql = "INSERT INTO staff_team (team_id, staff_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, staffTeam.getTeamId(), staffTeam.getStaffId());
        return staffTeam;
    }


    public List<StaffTeam> findByTeamId(Long teamId) {
        String sql = "SELECT * FROM staff_team WHERE team_id = ?";
        return jdbcTemplate.query(sql, staffTeamMapper, teamId);
    }


    public Optional<StaffTeam> findByTeamIdAndStaffId(Long teamId, Long staffId) {
        String sql = "SELECT * FROM staff_team WHERE team_id = ? AND staff_id = ?";
        List<StaffTeam> list = jdbcTemplate.query(sql, staffTeamMapper, teamId, staffId);
        return list.stream().findFirst();
    }


    public void delete(StaffTeam staffTeam) {
        String sql = "DELETE FROM staff_team WHERE team_id = ? AND staff_id = ?";
        jdbcTemplate.update(sql, staffTeam.getTeamId(), staffTeam.getStaffId());
    }
}
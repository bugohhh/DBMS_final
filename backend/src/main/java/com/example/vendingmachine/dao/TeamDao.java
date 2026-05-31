package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class TeamDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Team> teamMapper = new RowMapper<Team>() {
        @Override
        public Team mapRow(ResultSet rs, int rowNum) throws SQLException {
            Team team = new Team();
            team.setTeamId(rs.getLong("team_id"));
            team.setTeamName(rs.getString("team_name"));
            team.setTeamStatus(rs.getString("team_status"));
            long regionId = rs.getLong("region_id");
            team.setRegionId(rs.wasNull() ? null : regionId);
            try { team.setRegionName(rs.getString("region_name")); } catch (SQLException ignored) {}
            return team;
        }
    };

    public List<Team> findAll() {
        String sql = """
                SELECT t.team_id, t.team_name, t.team_status, t.region_id, r.region_name
                FROM Team t
                LEFT JOIN Region r ON t.region_id = r.region_id
                ORDER BY t.team_id
                """;
        return jdbcTemplate.query(sql, teamMapper);
    }

    public Optional<Team> findById(Long id) {
        String sql = """
                SELECT t.team_id, t.team_name, t.team_status, t.region_id, r.region_name
                FROM Team t
                LEFT JOIN Region r ON t.region_id = r.region_id
                WHERE t.team_id = ?
                """;
        List<Team> list = jdbcTemplate.query(sql, teamMapper, id);
        return list.stream().findFirst();
    }

    public Team save(Team team) {
        if (team.getTeamId() == null) {
            String sql = "INSERT INTO Team (team_name, team_status, establish_time, region_id) VALUES (?, COALESCE(?, 'Active'), NOW(), ?)";
            jdbcTemplate.update(sql, team.getTeamName(), team.getTeamStatus(), team.getRegionId());
            return team;
        } else {
            String sql = "UPDATE Team SET team_name = ?, team_status = COALESCE(?, team_status), region_id = ? WHERE team_id = ?";
            jdbcTemplate.update(sql, team.getTeamName(), team.getTeamStatus(), team.getRegionId(), team.getTeamId());
            return team;
        }
    }

    public void clearStaffAssignments(Long teamId) {
        jdbcTemplate.update("UPDATE Staff SET team_id = NULL WHERE team_id = ?", teamId);
    }

    public void deleteRefillTasks(Long teamId) {
        jdbcTemplate.update("""
                DELETE rd FROM RefillDetail rd
                JOIN RefillTask rt ON rd.refilltask_id = rt.refilltask_id
                WHERE rt.team_id = ?
                """, teamId);
        jdbcTemplate.update("DELETE FROM RefillTask WHERE team_id = ?", teamId);
    }

    public boolean deleteById(Long teamId) {
        return jdbcTemplate.update("DELETE FROM Team WHERE team_id = ?", teamId) > 0;
    }
}

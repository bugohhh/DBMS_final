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
            return team;
        }
    };

 
    public List<Team> findAll() {
        String sql = "SELECT team_id, team_name FROM team";
        return jdbcTemplate.query(sql, teamMapper);
    }


    public Optional<Team> findById(Long id) {
        String sql = "SELECT team_id, team_name FROM team WHERE team_id = ?";
        List<Team> list = jdbcTemplate.query(sql, teamMapper, id);
        return list.stream().findFirst();
    }


    public Team save(Team team) {
        if (team.getTeamId() == null) {
            String sql = "INSERT INTO team (team_name) VALUES (?)";
            jdbcTemplate.update(sql, team.getTeamName());

            return team;
        } else {
            String sql = "UPDATE team SET team_name = ? WHERE team_id = ?";
            jdbcTemplate.update(sql, team.getTeamName(), team.getTeamId());
            return team;
        }
    }
}
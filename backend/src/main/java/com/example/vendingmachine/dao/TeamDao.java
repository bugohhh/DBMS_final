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
            team.setId(rs.getLong("id"));
            team.setName(rs.getString("name"));
            return team;
        }
    };

 
    public List<Team> findAll() {
        String sql = "SELECT * FROM team";
        return jdbcTemplate.query(sql, teamMapper);
    }


    public Optional<Team> findById(Long id) {
        String sql = "SELECT * FROM team WHERE id = ?";
        List<Team> list = jdbcTemplate.query(sql, teamMapper, id);
        return list.stream().findFirst();
    }


    public Team save(Team team) {
        if (team.getId() == null) {
            String sql = "INSERT INTO team (name) VALUES (?)";
            jdbcTemplate.update(sql, team.getName());
            return team;
        } else {
            String sql = "UPDATE team SET name = ? WHERE id = ?";
            jdbcTemplate.update(sql, team.getName(), team.getId());
            return team;
        }
    }
}
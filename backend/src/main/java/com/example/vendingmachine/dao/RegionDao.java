package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class RegionDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Region> regionMapper = new RowMapper<Region>() {
        @Override
        public Region mapRow(ResultSet rs, int rowNum) throws SQLException {
            Region region = new Region();
            region.setId(rs.getLong("id"));
            region.setName(rs.getString("name"));
            region.setDescription(rs.getString("description"));
            return region;
        }
    };

    public List<Region> findAll() {
        String sql = "SELECT * FROM region";
        return jdbcTemplate.query(sql, regionMapper);
    }

    public Optional<Region> findById(Long id) {
        String sql = "SELECT * FROM region WHERE id = ?";
        List<Region> list = jdbcTemplate.query(sql, regionMapper, id);
        return list.stream().findFirst();
    }


    public Optional<Region> findByName(String name) {
        String sql = "SELECT * FROM region WHERE name = ?";
        List<Region> list = jdbcTemplate.query(sql, regionMapper, name);
        return list.stream().findFirst();
    }

    public Region save(Region region) {
        if (region.getId() == null) {
            String sql = "INSERT INTO region (name, description) VALUES (?, ?)";
            jdbcTemplate.update(sql, region.getName(), region.getDescription());
            return region;
        } else {
            String sql = "UPDATE region SET name = ?, description = ? WHERE id = ?";
            jdbcTemplate.update(sql, region.getName(), region.getDescription(), region.getId());
            return region;
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM region WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
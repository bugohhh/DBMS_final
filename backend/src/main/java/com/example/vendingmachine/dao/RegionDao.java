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
        region.setId(rs.getLong("region_id"));
        region.setName(rs.getString("region_name"));
        region.setDescription(rs.getString("description"));
        return region;
        }
    };

    public List<Region> findAll() {
        String sql = "SELECT * FROM Region";
        return jdbcTemplate.query(sql, regionMapper);
    }

    public Optional<Region> findById(Long id) {
        String sql = "SELECT * FROM Region WHERE region_id = ?";
        return jdbcTemplate.query(sql, regionMapper, id).stream().findFirst();
    }

    public Optional<Region> findByName(String name) {
        String sql = "SELECT * FROM Region WHERE region_name = ?";
        return jdbcTemplate.query(sql, regionMapper, name).stream().findFirst();
    }

    public Region save(Region region) {
        if (region.getId() == null) {
            String sql = "INSERT INTO Region (region_name, description) VALUES (?, ?)";
            jdbcTemplate.update(sql, region.getName(), region.getDescription());
            return region;
        } else {
            String sql = "UPDATE Region SET region_name = ?, description = ? WHERE region_id = ?";
            jdbcTemplate.update(sql, region.getName(), region.getDescription(), region.getId());
            return region;
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM Region WHERE region_id = ?";
        jdbcTemplate.update(sql, id);
    }
}
package com.example.vendingmachine.dao;

import com.example.vendingmachine.model.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            long managerId = rs.getLong("manager_id");
            region.setManagerId(rs.wasNull() ? null : managerId);
            try { region.setManagerName(rs.getString("manager_name")); } catch (SQLException ignored) {}
            return region;
        }
    };

    private static final String REGION_SELECT = """
            SELECT r.region_id, r.region_name, r.description,
                   m.user_id AS manager_id, u.user_name AS manager_name
            FROM Region r
            LEFT JOIN Manager m ON r.region_id = m.region_id
            LEFT JOIN `User` u ON m.user_id = u.user_id
            """;

    public List<Region> findAll() {
        String sql = REGION_SELECT + " ORDER BY r.region_id";
        return jdbcTemplate.query(sql, regionMapper);
    }

    public Optional<Region> findById(Long id) {
        String sql = REGION_SELECT + " WHERE r.region_id = ?";
        return jdbcTemplate.query(sql, regionMapper, id).stream().findFirst();
    }

    public Optional<Region> findByName(String name) {
        String sql = REGION_SELECT + " WHERE r.region_name = ?";
        return jdbcTemplate.query(sql, regionMapper, name).stream().findFirst();
    }

    public boolean isManagerUser(Long userId) {
        if (userId == null) return false;
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM `User` WHERE user_id = ? AND user_type = 'Manager'",
                Integer.class,
                userId
        );
        return count != null && count > 0;
    }

    public Region save(Region region) {
        if (region.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO Region (region_name, description) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, region.getName());
                ps.setString(2, region.getDescription());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) region.setId(key.longValue());
        } else {
            String sql = "UPDATE Region SET region_name = ?, description = ? WHERE region_id = ?";
            jdbcTemplate.update(sql, region.getName(), region.getDescription(), region.getId());
        }
        saveManagerAssignment(region.getId(), region.getManagerId());
        return findById(region.getId()).orElse(region);
    }

    private void saveManagerAssignment(Long regionId, Long managerId) {
        if (regionId == null) return;
        jdbcTemplate.update("DELETE FROM Manager WHERE region_id = ?", regionId);
        if (managerId != null) {
            jdbcTemplate.update("INSERT INTO Manager (region_id, user_id) VALUES (?, ?)", regionId, managerId);
        }
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM Manager WHERE region_id = ?", id);
        jdbcTemplate.update("DELETE FROM Region WHERE region_id = ?", id);
    }
}

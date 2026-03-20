package com.financetracker.api.repository;

import com.financetracker.api.mapper.ExampleRowMapper;
import com.financetracker.api.model.ExampleEntity;
import com.financetracker.api.query.ExampleQueries;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class ExampleRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ExampleRowMapper rowMapper;

    public ExampleRepository(JdbcTemplate jdbcTemplate, ExampleRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    public List<ExampleEntity> findAll() {
        return jdbcTemplate.query(ExampleQueries.FIND_ALL, rowMapper);
    }

    public Optional<ExampleEntity> findById(Long id) {
        List<ExampleEntity> results = jdbcTemplate.query(
                ExampleQueries.FIND_BY_ID, rowMapper, id
        );
        return results.stream().findFirst();
    }

    public List<ExampleEntity> findByActive(boolean active) {
        return jdbcTemplate.query(ExampleQueries.FIND_BY_ACTIVE, rowMapper, active);
    }

    public long save(ExampleEntity entity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    ExampleQueries.INSERT, new String[]{"id"}
            );
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getEmail());
            ps.setBoolean(3, entity.isActive());
            ps.setTimestamp(4, Timestamp.from(entity.getCreatedAt()));
            ps.setTimestamp(5, Timestamp.from(entity.getUpdatedAt()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            entity.setId(key.longValue());
        }
        return entity.getId();
    }

    public int update(ExampleEntity entity) {
        return jdbcTemplate.update(
                ExampleQueries.UPDATE,
                entity.getName(),
                entity.getEmail(),
                entity.isActive(),
                Timestamp.from(entity.getUpdatedAt()),
                entity.getId()
        );
    }

    public int softDelete(Long id) {
        return jdbcTemplate.update(ExampleQueries.SOFT_DELETE, Timestamp.from(Instant.now()), id);
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                ExampleQueries.EXISTS_BY_EMAIL, Integer.class, email
        );
        return count != null && count > 0;
    }

    public boolean existsByEmailExcludingId(String email, Long id) {
        Integer count = jdbcTemplate.queryForObject(
                ExampleQueries.EXISTS_BY_EMAIL_EXCLUDING_ID, Integer.class, email, id
        );
        return count != null && count > 0;
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject(ExampleQueries.COUNT_ALL, Long.class);
        return count != null ? count : 0;
    }

    public List<ExampleEntity> findAllPaginated(String sortBy, String direction, int limit, int offset) {
        String column = ExampleQueries.resolveColumn(sortBy);
        String dir = ExampleQueries.resolveDirection(direction);
        if (column == null || dir == null) {
            throw new IllegalArgumentException("Invalid sort parameters: " + sortBy + " " + direction);
        }
        return jdbcTemplate.query(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    ExampleQueries.FIND_ALL_PAGINATED_TEMPLATE
                            .replace(":orderColumn", column)
                            .replace(":orderDir", dir)
            );
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            return ps;
        }, rowMapper);
    }
}

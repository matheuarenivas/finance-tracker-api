package com.financetracker.api.mapper;

import com.financetracker.api.model.ExampleEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class ExampleRowMapper implements RowMapper<ExampleEntity> {

    @Override
    public ExampleEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        ExampleEntity entity = new ExampleEntity();
        entity.setId(rs.getLong("id"));
        entity.setName(rs.getString("name"));
        entity.setEmail(rs.getString("email"));
        entity.setActive(rs.getBoolean("active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) entity.setCreatedAt(createdAt.toInstant());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) entity.setUpdatedAt(updatedAt.toInstant());

        return entity;
    }
}

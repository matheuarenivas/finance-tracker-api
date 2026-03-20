package com.financetracker.api.query;

import java.util.Map;
import java.util.Set;

/**
 * Centralized SQL queries for ExampleEntity.
 * One file per entity keeps things organized as the project grows.
 */
public final class ExampleQueries {

    private ExampleQueries() {}

    private static final String COLUMNS = "id, name, email, active, created_at, updated_at";

    /** Whitelist of sortable columns — prevents SQL injection in ORDER BY */
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "id",
            "name", "name",
            "email", "email",
            "active", "active",
            "createdAt", "created_at",
            "updatedAt", "updated_at"
    );

    public static final Set<String> VALID_SORT_COLUMNS = SORT_COLUMNS.keySet();
    public static final Set<String> VALID_DIRECTIONS = Set.of("ASC", "DESC");

    public static final String FIND_ALL =
            "SELECT " + COLUMNS + " FROM examples WHERE active = true";

    public static final String FIND_BY_ID =
            "SELECT " + COLUMNS + " FROM examples WHERE id = ? AND active = true";

    public static final String FIND_BY_ACTIVE =
            "SELECT " + COLUMNS + " FROM examples WHERE active = ?";

    public static final String INSERT =
            "INSERT INTO examples (name, email, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";

    public static final String UPDATE =
            "UPDATE examples SET name = ?, email = ?, active = ?, updated_at = ? WHERE id = ?";

    public static final String SOFT_DELETE =
            "UPDATE examples SET active = false, updated_at = ? WHERE id = ?";

    public static final String EXISTS_BY_EMAIL =
            "SELECT COUNT(*) FROM examples WHERE email = ?";

    public static final String EXISTS_BY_EMAIL_EXCLUDING_ID =
            "SELECT COUNT(*) FROM examples WHERE email = ? AND id != ?";

    public static final String COUNT_ALL =
            "SELECT COUNT(*) FROM examples WHERE active = true";

    /**
     * Builds a safe paginated query using the column whitelist.
     * Only returns active records. Returns null if sort column is invalid.
     */
    public static String buildPaginatedQuery(String sortBy, String direction) {
        String column = SORT_COLUMNS.get(sortBy);
        if (column == null || !VALID_DIRECTIONS.contains(direction)) {
            return null;
        }
        return "SELECT " + COLUMNS + " FROM examples WHERE active = true ORDER BY "
                + column + " " + direction + " LIMIT ? OFFSET ?";
    }
}

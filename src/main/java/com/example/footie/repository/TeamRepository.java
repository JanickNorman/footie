package com.example.footie.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.example.footie.newSimulator.ConcreteTeam;
import com.example.footie.newSimulator.Team;

@Repository
public class TeamRepository {

    private static final String SELECT_ALL = "SELECT name, code, continent, pot FROM teams ORDER BY pot, name";
    private static final String SELECT_BY_CODE = "SELECT name, code, continent, pot FROM teams WHERE code = ?";
    private static final String UPSERT = "INSERT INTO teams (name, code, continent, pot) VALUES (?, ?, ?, ?) "
            + "ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, continent = EXCLUDED.continent, pot = EXCLUDED.pot, updated_at = CURRENT_TIMESTAMP";
    private static final String DELETE_BY_CODE = "DELETE FROM teams WHERE code = ?";

    private static final RowMapper<Team> TEAM_ROW_MAPPER = (rs, rowNum) -> new ConcreteTeam(
            rs.getString("name"),
            rs.getString("continent"),
            0,
            rs.getString("code")
    );

    private final JdbcTemplate jdbc;

    public TeamRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Find all teams ordered by pot then name.
     */
    public List<Team> findAll() {
        return jdbc.query(SELECT_ALL, TEAM_ROW_MAPPER);
    }

    /**
     * Find a team by its 3-letter code.
     */
    public Optional<Team> findByCode(String code) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(SELECT_BY_CODE, TEAM_ROW_MAPPER, code));
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    /**
     * Insert or update a team using Postgres upsert.
     */
    public int save(Team team) {
        return jdbc.update(UPSERT,
                team.getName(),
                team.getCode(),
                team.getContinents().stream().findFirst().orElse(null));
    }

    /**
     * Delete a team by its code.
     */
    public int deleteByCode(String code) {
        return jdbc.update(DELETE_BY_CODE, code);
    }
}

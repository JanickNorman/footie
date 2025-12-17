package com.example.footie.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.example.footie.newSimulator.ConcreteTeam;
import com.example.footie.newSimulator.Team;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ReactiveTeamRepository {

        private static final String SELECT_ALL = "SELECT name, code, continent, flag_url FROM teams ORDER BY name";
        private static final String SELECT_BY_CODE = "SELECT name, code, continent, flag_url FROM teams WHERE code = :code";
        private static final String UPSERT = "INSERT INTO teams (name, code, continent, flag_url) VALUES (:name, :code, :continent, :flag_url) "
            + "ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, continent = EXCLUDED.continent, flag_url = EXCLUDED.flag_url, updated_at = CURRENT_TIMESTAMP";
        private static final String DELETE_BY_CODE = "DELETE FROM teams WHERE code = :code";

    private final DatabaseClient client;

    public ReactiveTeamRepository(DatabaseClient client) {
        this.client = client;
    }

    private Team mapRow(io.r2dbc.spi.Row row) {
        return new ConcreteTeam(
                row.get("name", String.class),
                row.get("continent", String.class),
                0,
                row.get("code", String.class),
                row.get("flag_url", String.class)
        );
    }

    public Flux<Team> findAll() {
        return client.sql(SELECT_ALL)
                .map((row, meta) -> mapRow(row))
                .all();
    }

    public Mono<Team> findByCode(String code) {
        return client.sql(SELECT_BY_CODE)
                .bind("code", code)
                .map((row, meta) -> mapRow(row))
                .one();
    }

    public Mono<Long> save(Team team) {
        return client.sql(UPSERT)
                .bind("name", team.getName())
                .bind("code", team.getCode())
                .bind("continent", team.getContinents().stream().findFirst().orElse(null))
                .bind("flag_url", team.getFlagUrl())
                .fetch()
                .rowsUpdated();
    }

    public Mono<Long> deleteByCode(String code) {
        return client.sql(DELETE_BY_CODE)
                .bind("code", code)
                .fetch()
                .rowsUpdated();
    }
}

package com.example.footie.service;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import static org.springframework.data.relational.core.query.Criteria.where;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

import com.example.footie.newSimulator.ConcreteTeam;
import com.example.footie.newSimulator.Team;
import com.example.footie.repository.TeamEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TeamService {

    private static final String UPSERT_SQL = "INSERT INTO teams (name, code, continent, pot, flag_url) VALUES (:name, :code, :continent, :pot, :flag_url) "
            + "ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, continent = EXCLUDED.continent, pot = EXCLUDED.pot, flag_url = EXCLUDED.flag_url, updated_at = CURRENT_TIMESTAMP";

    private final R2dbcEntityTemplate template;
    private final DatabaseClient client;

    public TeamService(R2dbcEntityTemplate template, DatabaseClient client) {
        this.template = template;
        this.client = client;
    }

    private Team toDomain(TeamEntity e, Long index) {
        if (e == null) return null;
        int pot = (int)(index % 4) + 1;
        return new ConcreteTeam(e.getName(), e.getContinent(), pot, e.getCode(), e.getFlagUrl());
    }

    public Flux<Team> findAll() {
        Query q = Query.empty();
        return template.select(q, TeamEntity.class).index().map(tuple -> toDomain(tuple.getT2(), tuple.getT1()));
    }

    public Mono<Team> findByCode(String code) {
        Query q = Query.query(where("code").is(code));
        return template.selectOne(q, TeamEntity.class).map(e -> toDomain(e, 0L));
    }

    // public Mono<Long> save(Team team) {
    //     return client.sql(UPSERT_SQL)
    //             .bind("name", team.getName())
    //             .bind("code", team.getCode())
    //             .bind("continent", team.getContinents().stream().findFirst().orElse(null))
    //             .bind("pot", team.pot())
    //             .bind("flag_url", team.getFlagUrl())
    //             .fetch()
    //             .rowsUpdated();
    // }

    public Mono<Void> deleteByCode(String code) {
        Query q = Query.query(where("code").is(code));
        return template.delete(q, TeamEntity.class).then();
    }
}

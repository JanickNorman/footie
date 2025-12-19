package com.example.footie.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Query q = Query.query(where("team_type").is("national"));
        return template.select(q, TeamEntity.class).index().map(tuple -> toDomain(tuple.getT2(), tuple.getT1()));
    }

    public Mono<Team> findByCode(String code) {
        Query q = Query.query(where("code").is(code).and("team_type").is("national"));
        return template.selectOne(q, TeamEntity.class).map(e -> toDomain(e, 0L));
    }

    public Flux<Team> getRandomWorldCupTeams(int count) {
        // Fixed quotas per continent for the world cup selection.
        final Map<String, Integer> quotas = Map.of(
                "Europe", 16,
                "Asia", 9,
                "Africa", 9,
                "SouthAmerica", 7,
                "NorthAmerica", 6,
                "Oceania", 1
        );

        return template.select(Query.query(where("team_type").is("national")), TeamEntity.class)
                .collectList()
                .flatMapMany(list -> {
                    if (list.isEmpty()) return Flux.empty();

                    // group by continent (fifa_continent stored in TeamEntity.continent)
                    Map<String, List<TeamEntity>> byContinent = list.stream()
                            .collect(Collectors.groupingBy(e -> e.getContinent() == null ? "" : e.getContinent()));

                    List<TeamEntity> selected = new ArrayList<>();

                    // sample per quota
                    quotas.forEach((continent, q) -> {
                        List<TeamEntity> pool = byContinent.getOrDefault(continent, List.of());
                        if (pool.isEmpty()) return;
                        Collections.shuffle(pool);
                        int take = Math.min(q, pool.size());
                        for (int i = 0; i < take; i++) {
                            TeamEntity e = pool.get(i);
                            selected.add(e);
                        }
                    });

                    List<TeamEntity> finalSelection = selected;
                    if (count > 0 && selected.size() > count) {
                        Collections.shuffle(selected);
                        finalSelection = new ArrayList<>(selected.subList(0, count));
                    }

                    // map to domain and assign pots by index (group teams into pots of 12)
                    List<Team> result = new ArrayList<>(finalSelection.size());
                    for (int i = 0; i < finalSelection.size(); i++) {
                        TeamEntity e = finalSelection.get(i);
                        int pot = (i / 12) + 1;
                        pot = Math.min(pot, 4);
                        result.add(new ConcreteTeam(e.getName(), e.getContinent(), pot, e.getCode(), e.getFlagUrl()));
                    }

                    return Flux.fromIterable(result);
                });
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
        Query q = Query.query(where("code").is(code).and("team_type").is("national"));
        return template.delete(q, TeamEntity.class).then();
    }
}

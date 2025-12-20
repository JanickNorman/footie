package com.example.footie.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Simulator;
import com.example.footie.newSimulator.Team;
import com.example.footie.newSimulator.TeamFactory;
import com.example.footie.newSimulator.constraint.AllDifferent;
import com.example.footie.newSimulator.constraint.AtMostTwoEuropeTeamsPerGroup;
import com.example.footie.newSimulator.constraint.ConstraintManager;
import com.example.footie.newSimulator.constraint.NoSameContinentInGroupForNonEurope;
import com.example.footie.newSimulator.constraint.SamePotCantBeInTheSameGroup;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class DrawService {
    private final TeamService teamRepository;

    public DrawService(TeamService teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Mono<Map<String, List<Team>>> runDraw() {
        // Flux<Team> teams = this.teamRepository.getRandomWorldCupTeams(48);
        Flux<Team> teams = getWorldCupTeams();
 
        return teams.collectList()
                .defaultIfEmpty(TeamFactory.createWorldCupTeams(4))
                .flatMap(list -> Mono.fromCallable(() -> doRun(list)).subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<Map<String, List<Team>>> runDrawRandomTeams(List<Team> teams) {
        Flux<Team> teamsFlux = teamRepository.getRandomWorldCupTeams(48);
        return teamsFlux.collectList()
                .defaultIfEmpty(TeamFactory.createWorldCupTeams(4))
                .flatMap(list -> Mono.fromCallable(() -> doRun(list)).subscribeOn(Schedulers.boundedElastic()));
    }

    private Map<String, List<Team>> doRun(List<Team> teams) {
        System.out.println("Running draw with teams: " + teams.stream().map(t -> t.getName() + " (" + t.pot() + ")").collect(Collectors.joining(", ")));
        System.out.println("Total teams: " + teams.size());
        List<GroupSlot> slots = buildWorldCupSlots();

        ConstraintManager cm = new ConstraintManager();
        cm.addConstraint(new AllDifferent());
        cm.addConstraint(new SamePotCantBeInTheSameGroup());
        cm.addConstraint(new AtMostTwoEuropeTeamsPerGroup());
        cm.addConstraint(new NoSameContinentInGroupForNonEurope());
        // cm.addConstraint(new TopSeedsBracketSeparation(Map.of(
        //         "Argentina", 1,
        //         "Spain", 2,
        //         "France", 3,
        //         "England", 4
        // )));

        Simulator simulator = new Simulator(slots, cm, teams);
        simulator.setOnlyCheckDomainAfter(28);

        boolean solved = false;
        try {
            solved = simulator.solveWorldCup2026Draw();
            
        } catch (RuntimeException e) {
            solved = simulator.solveWorldCup2026Draw();
            System.out.println("Failed to set max nodes: " + e.getMessage());
        }

        Map<String, List<Team>> grouped = new TreeMap<>();
        if (!solved) return grouped;

        AssignmentState state = simulator.getState();
        SortedMap<GroupSlot, Team> assignments = state.getAssignments();

        for (GroupSlot s : slots) grouped.computeIfAbsent(s.getGroupName(), k -> new ArrayList<>());

        Map<String, Integer> maxPos = slots.stream().collect(Collectors.groupingBy(GroupSlot::getGroupName,
                Collectors.collectingAndThen(Collectors.maxBy((a, b) -> Integer.compare(a.getPosition(), b.getPosition())),
                        opt -> opt.map(GroupSlot::getPosition).orElse(0))));

        for (Map.Entry<String, Integer> e : maxPos.entrySet()) {
            List<Team> list = new ArrayList<>(Collections.nCopies(e.getValue(), (Team) null));
            grouped.put(e.getKey(), list);
        }

        assignments.forEach((slot, team) -> {
            if (team == null) return;
            List<Team> list = grouped.get(slot.getGroupName());
            int idx = slot.getPosition() - 1;
            if (list != null && idx >= 0 && idx < list.size()) {
                list.set(idx, team);
            }
        });

        return grouped;
    }

    private Flux<Team> getWorldCupTeams() {
        List<Team> teams = TeamFactory.createWorldCupTeams(4);
        return teamRepository.findAll().collectList()
            .map(dbTeams -> {
                if (!dbTeams.isEmpty()) {
                    Map<String, Team> dbTeamMap = dbTeams.stream()
                        .collect(Collectors.toMap(Team::getCode, t -> t));
                    for (Team t : teams) {
                        Team dbTeam = dbTeamMap.get(t.getCode());
                        if (dbTeam != null) t.setFlag(dbTeam.getFlagUrl());
                    }
                }
                return teams;
            })
            .flatMapMany(Flux::fromIterable);
    }

    private List<GroupSlot> buildWorldCupSlots() {
        List<GroupSlot> slots = new ArrayList<>();
        for (char g = 'A'; g <= 'L'; g++) {
            String group = String.valueOf(g);
            for (int pos = 1; pos <= 4; pos++) {
                slots.add(new GroupSlot(group, pos));
            }
        }
        return slots;
    }
}

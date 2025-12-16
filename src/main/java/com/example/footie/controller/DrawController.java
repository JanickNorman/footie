package com.example.footie.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.example.footie.newSimulator.constraint.TopSeedsBracketSeparation;

import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class DrawController {

    @PostMapping("/draw")
    public Mono<Map<String, List<String>>> runDraw() {
        return Mono.fromSupplier(() -> {
            // Build teams and slots (world cup style: groups A..L, positions 1..4)
            List<Team> teams = TeamFactory.createWorldCupTeams(4);
            List<GroupSlot> slots = buildWorldCupSlots();

            // Setup constraints similar to existing simulation
            ConstraintManager cm = new ConstraintManager();
            cm.addConstraint(new AllDifferent());
            cm.addConstraint(new SamePotCantBeInTheSameGroup());
            cm.addConstraint(new AtMostTwoEuropeTeamsPerGroup());
            cm.addConstraint(new NoSameContinentInGroupForNonEurope());
            cm.addConstraint(new TopSeedsBracketSeparation(Map.of(
                    "Argentina", 1,
                    "Spain", 2,
                    "France", 3,
                    "England", 4
            )));   


            Simulator simulator = new Simulator(slots, cm, teams);

            // Shuffle teams to get varied solutions
            boolean solved = simulator.solveWorldCup2026Draw();
            Map<String, List<String>> grouped = new TreeMap<>();

            if (!solved) {
                // return empty mapping if no solution found
                return grouped;
            }

            AssignmentState state = simulator.getState();
            // Build a map groupName -> map(position -> teamName)
            SortedMap<GroupSlot, Team> assignments = state.getAssignments();

            // initialize groups
            for (GroupSlot s : slots) {
                grouped.computeIfAbsent(s.getGroupName(), k -> new ArrayList<>());
            }

            // For each group, build a list sized to max position and fill by position
            Map<String, Integer> maxPos = slots.stream().collect(Collectors.groupingBy(GroupSlot::getGroupName,
                    Collectors.collectingAndThen(Collectors.maxBy((a, b) -> Integer.compare(a.getPosition(), b.getPosition())),
                            opt -> opt.map(GroupSlot::getPosition).orElse(0))));

            // prepare lists with null placeholders
            for (Map.Entry<String, Integer> e : maxPos.entrySet()) {
                List<String> list = new ArrayList<>(Collections.nCopies(e.getValue(), null));
                grouped.put(e.getKey(), list);
            }

            assignments.forEach((slot, team) -> {
                if (team == null) return;
                List<String> list = grouped.get(slot.getGroupName());
                int idx = slot.getPosition() - 1;
                // defensive check
                if (list != null && idx >= 0 && idx < list.size()) {
                    list.set(idx, team.getName());
                }
            });

            return grouped;
        });
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

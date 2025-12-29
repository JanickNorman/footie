package com.example.footie.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
        Flux<Team> teamsFlux = teamRepository.getRandomWorldCupTeams(48, Math.random() < 0.5);
        return teamsFlux.collectList()
                .defaultIfEmpty(TeamFactory.createWorldCupTeams(4))
                .flatMap(list -> Mono.fromCallable(() -> doRun(list)).subscribeOn(Schedulers.boundedElastic()));
    }

    private Map<String, List<Team>> doRun(List<Team> teams) {
        System.out.println("Running draw with teams: "
                + teams.stream().map(t -> t.getName() + " (" + t.pot() + ")").collect(Collectors.joining(", ")));
        System.out.println("Total teams: " + teams.size());
        List<GroupSlot> slots = buildWorldCupSlots();

        ConstraintManager cm = new ConstraintManager();
        cm.addConstraint(new AllDifferent());
        cm.addConstraint(new SamePotCantBeInTheSameGroup());
        cm.addConstraint(new AtMostTwoEuropeTeamsPerGroup());
        cm.addConstraint(new NoSameContinentInGroupForNonEurope());
        // cm.addConstraint(new TopSeedsBracketSeparation(Map.of(
        // "Argentina", 1,
        // "Spain", 2,
        // "France", 3,
        // "England", 4
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
        if (!solved)
            return grouped;

        AssignmentState state = simulator.getState();
        SortedMap<GroupSlot, Team> assignments = state.getAssignments();

        for (GroupSlot s : slots)
            grouped.computeIfAbsent(s.getGroupName(), k -> new ArrayList<>());

        Map<String, Integer> maxPos = slots.stream().collect(Collectors.groupingBy(GroupSlot::getGroupName,
                Collectors.collectingAndThen(
                        Collectors.maxBy((a, b) -> Integer.compare(a.getPosition(), b.getPosition())),
                        opt -> opt.map(GroupSlot::getPosition).orElse(0))));

        for (Map.Entry<String, Integer> e : maxPos.entrySet()) {
            List<Team> list = new ArrayList<>(Collections.nCopies(e.getValue(), (Team) null));
            grouped.put(e.getKey(), list);
        }

        assignments.forEach((slot, team) -> {
            if (team == null)
                return;
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
                            if (dbTeam != null)
                                t.setFlag(dbTeam.getFlagUrl());
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

    public Mono<Map<String, Object>> getSampleRandomWorldCup() {
        return teamRepository.getRandomWorldCupTeams(48, Math.random() < 0.5)
                .collectMap(Team::getCode)
                .map(this::createSampleWorldCup);
    }

    public Mono<Map<String, Object>> getSampleWorldCup() {
        return teamRepository.getWorldCupTeams(List.of("QAT", "ECU", "SEN", "NED", 
                                        "ENG", "IRN", "USA", "WAL", 
                                        "ARG", "KSA", "MEX", "POL", 
                                        "FRA", "AUS", "DEN", "TUN", 
                                        "ESP", "CRC", "GER", "JPN", 
                                        "BEL", "CAN", "MAR", "IDN", 
                                        "BRA", "SRB", "SUI", "CMR", 
                                        "POR", "GHA", "URU", "KOR"), 32)
                .collectMap(Team::getCode)
                .map(this::createSampleWorldCup);
    }

    private Map<String, Object> createSampleWorldCup(Map<String, Team> teamsMap) {
        Map<String, Object> tournament = new HashMap<>();
        tournament.put("name", "Fictional World Cup");
        tournament.put("year", 2026);
        tournament.put("groups", createGroups(teamsMap));
        tournament.put("matchdays", createMatchdays(teamsMap));
        tournament.put("knockout", createKnockout(teamsMap));
        return tournament;
    }

    private List<Map<String, Object>> createGroups(Map<String, Team> teamsMap) {
        List<Map<String, Object>> groups = new ArrayList<>();
        
        // Group A
        Map<String, Object> groupA = new HashMap<>();
        groupA.put("name", "A");
        groupA.put("teams", List.of(
            createTeamMap("QAT", teamsMap),
            createTeamMap("ECU", teamsMap),
            createTeamMap("SEN", teamsMap),
            createTeamMap("NED", teamsMap)
        ));
        groupA.put("matches", List.of(
            createMatch(1, "15 Jun", "A", "NED", "ECU", 3, 0, "Stadium A", "20:00", teamsMap,
                List.of(
                    createScorer("home", "G. Wijnaldum", 23),
                    createScorer("home", "D. Bergwijn", 67),
                    createScorer("home", "B. Devrij", 81)
                )),
            createMatch(1, "15 Jun", "A", "QAT", "SEN", 1, 2, "Stadium B", "17:00", teamsMap,
                List.of(
                    createScorer("away", "S. Mane", 12),
                    createScorer("home", "A. Almoez", 55),
                    createScorer("away", "I. Gueye", 88)
                ))
        ));
        groups.add(groupA);

        // Group B
        Map<String, Object> groupB = new HashMap<>();
        groupB.put("name", "B");
        groupB.put("teams", List.of(
            createTeamMap("ENG", teamsMap),
            createTeamMap("IRN", teamsMap),
            createTeamMap("USA", teamsMap),
            createTeamMap("WAL", teamsMap)
        ));
        groups.add(groupB);

        // Group C
        Map<String, Object> groupC = new HashMap<>();
        groupC.put("name", "C");
        groupC.put("teams", List.of(
            createTeamMap("ARG", teamsMap),
            createTeamMap("KSA", teamsMap),
            createTeamMap("MEX", teamsMap),
            createTeamMap("POL", teamsMap)
        ));
        groups.add(groupC);

        // Group D
        Map<String, Object> groupD = new HashMap<>();
        groupD.put("name", "D");
        groupD.put("teams", List.of(
            createTeamMap("FRA", teamsMap),
            createTeamMap("AUS", teamsMap),
            createTeamMap("DEN", teamsMap),
            createTeamMap("TUN", teamsMap)
        ));
        groups.add(groupD);

        // Group E
        Map<String, Object> groupE = new HashMap<>();
        groupE.put("name", "E");
        groupE.put("teams", List.of(
            createTeamMap("ESP", teamsMap),
            createTeamMap("CRC", teamsMap),
            createTeamMap("GER", teamsMap),
            createTeamMap("JPN", teamsMap)
        ));
        groups.add(groupE);

        // Group F
        Map<String, Object> groupF = new HashMap<>();
        groupF.put("name", "F");
        groupF.put("teams", List.of(
            createTeamMap("BEL", teamsMap),
            createTeamMap("CAN", teamsMap),
            createTeamMap("MAR", teamsMap),
            createTeamMap("CRO", teamsMap)
        ));
        groups.add(groupF);

        // Group G
        Map<String, Object> groupG = new HashMap<>();
        groupG.put("name", "G");
        groupG.put("teams", List.of(
            createTeamMap("BRA", teamsMap),
            createTeamMap("SRB", teamsMap),
            createTeamMap("SUI", teamsMap),
            createTeamMap("CMR", teamsMap)
        ));
        groups.add(groupG);

        // Group H
        Map<String, Object> groupH = new HashMap<>();
        groupH.put("name", "H");
        groupH.put("teams", List.of(
            createTeamMap("POR", teamsMap),
            createTeamMap("GHA", teamsMap),
            createTeamMap("URU", teamsMap),
            createTeamMap("KOR", teamsMap)
        ));
        groups.add(groupH);

        return groups;
    }

    private List<Map<String, Object>> createMatchdays(Map<String, Team> teamsMap) {
        List<Map<String, Object>> matchdays = new ArrayList<>();
        
        Map<String, Object> day1 = new HashMap<>();
        day1.put("day", 1);
        day1.put("date", "15 Jun");
        day1.put("matches", List.of(
            createMatch(1, "15 Jun", "A", "NED", "ECU", 3, 0, "Stadium A", "20:00", teamsMap,
                List.of(
                    createScorer("home", "G. Wijnaldum", 23),
                    createScorer("home", "D. Bergwijn", 67),
                    createScorer("home", "B. Devrij", 81)
                )),
            createMatch(1, "15 Jun", "A", "QAT", "SEN", 1, 2, "Stadium B", "17:00", teamsMap,
                List.of(
                    createScorer("away", "S. Mane", 12),
                    createScorer("home", "A. Almoez", 55),
                    createScorer("away", "I. Gueye", 88)
                ))
        ));
        matchdays.add(day1);

        return matchdays;
    }

    private Map<String, Object> createKnockout(Map<String, Team> teamsMap) {
        Map<String, Object> knockout = new HashMap<>();
        
        // Round of 16
        knockout.put("roundOf16", List.of(
            createKnockoutMatch("NED", "IRN", 3, 1, "Khalifa Stadium", "16:00", null, teamsMap),
            createKnockoutMatch("ARG", "DEN", 2, 1, "Ahmad Bin Ali", "20:00", null, teamsMap),
            createKnockoutMatch("ESP", "CAN", 4, 0, "Al Thumama", "16:00", null, teamsMap),
            createKnockoutMatch("BRA", "GHA", 3, 0, "Stadium 974", "20:00", null, teamsMap),
            createKnockoutMatch("ENG", "ECU", 2, 0, "Al Bayt Stadium", "16:00", null, teamsMap),
            createKnockoutMatch("FRA", "KSA", 3, 1, "Education City", "20:00", null, teamsMap),
            createKnockoutMatch("BEL", "CRC", 1, 0, "Al Janoub", "16:00", null, teamsMap),
            createKnockoutMatch("POR", "SRB", 2, 1, "Lusail Stadium", "20:00", null, teamsMap)
        ));

        // Quarterfinals
        knockout.put("quarterfinals", List.of(
            createKnockoutMatch("NED", "ARG", 2, 3, "Lusail Stadium", "20:00", "ARG", teamsMap),
            createKnockoutMatch("ENG", "FRA", 1, 2, "Al Bayt Stadium", "20:00", null, teamsMap),
            createKnockoutMatch("BRA", "ESP", 1, 0, "Education City", "16:00", null, teamsMap),
            createKnockoutMatch("BEL", "POR", 0, 2, "Al Thumama", "16:00", null, teamsMap)
        ));

        // Semifinals
        knockout.put("semifinals", List.of(
            createKnockoutMatch("ARG", "BRA", 2, 0, "Lusail Stadium", "20:00", null, teamsMap),
            createKnockoutMatch("FRA", "POR", 2, 1, "Al Bayt Stadium", "20:00", null, teamsMap)
        ));

        // Final
        knockout.put("final", createKnockoutMatch("ARG", "FRA", 3, 3, "Lusail Stadium", "18:00", "ARG", teamsMap));

        // Third place
        knockout.put("thirdPlace", createKnockoutMatch("BRA", "POR", 2, 1, "Khalifa Stadium", "16:00", null, teamsMap));

        return knockout;
    }

    private Map<String, Object> createTeamMap(String code, Map<String, Team> teamsMap) {
        Map<String, Object> team = new HashMap<>();
        team.put("id", code);
        
        // Get team from database if available
        Team dbTeam = teamsMap.get(code);
        if (dbTeam != null) {
            team.put("name", dbTeam.getName());
            if (dbTeam.getFlagUrl() != null) {
                team.put("flagUrl", dbTeam.getFlagUrl());
            }
        } else {
            // Fallback to code as name if not in database
            team.put("name", code);
        }
        
        return team;
    }

    private Map<String, Object> createMatch(int day, String date, String group, 
                                            String homeId, String awayId,
                                            int homeScore, int awayScore,
                                            String venue, String time,
                                            Map<String, Team> teamsMap,
                                            List<Map<String, Object>> scorers) {
        Map<String, Object> match = new HashMap<>();
        match.put("day", day);
        match.put("date", date);
        match.put("group", group);
        match.put("home", createTeamMap(homeId, teamsMap));
        match.put("away", createTeamMap(awayId, teamsMap));
        match.put("homeScore", homeScore);
        match.put("awayScore", awayScore);
        match.put("venue", venue);
        match.put("time", time);
        match.put("scorers", scorers);
        return match;
    }

    private Map<String, Object> createKnockoutMatch(String homeId, String awayId,
                                                     int homeScore, int awayScore,
                                                     String venue, String time,
                                                     String winner,
                                                     Map<String, Team> teamsMap) {
        Map<String, Object> match = new HashMap<>();
        match.put("home", createTeamMap(homeId, teamsMap));
        match.put("away", createTeamMap(awayId, teamsMap));
        match.put("homeScore", homeScore);
        match.put("awayScore", awayScore);
        match.put("venue", venue);
        match.put("time", time);
        if (winner != null) {
            match.put("winner", winner);
        }
        return match;
    }

    private Map<String, Object> createScorer(String team, String player, int minute) {
        Map<String, Object> scorer = new HashMap<>();
        scorer.put("team", team);
        scorer.put("player", player);
        scorer.put("minute", minute);
        return scorer;
    }
}

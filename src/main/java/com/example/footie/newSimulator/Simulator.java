
package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import com.example.footie.newSimulator.constraint.ConstraintManager;

public class Simulator {
    private final List<GroupSlot> drawOrder;
    private final Map<String, GroupSlot> slotsByKey;
    final AssignmentState state;
    private final ConstraintManager constraintManager;
    private final BacktrackingSolver backtrackingSolver;
    private final Map<String, Team> assignedTeams;
    private final List<Team> registeredTeams = new ArrayList<>();

    public Simulator(List<GroupSlot> slots, ConstraintManager cm, List<Team> teams) {
        this.drawOrder = slots;
        this.slotsByKey = slots.stream()
                .collect(Collectors.toMap(
                        s -> s.getGroupName() + s.getPosition(),
                        s -> s));
        this.state = new AssignmentState(slots, teams);
        this.constraintManager = cm;
        this.assignedTeams = new HashMap<>(teams.stream()
                .collect(Collectors.toMap(Team::getName, t -> t)));
        this.backtrackingSolver = new BacktrackingSolver(cm);
    }

    /** Register a team to be placed later via team-first solver. */
    public void placeTeam(String teamName) {
        Team t = assignedTeams.get(teamName);
        if (t == null) {
            System.out.println("Unknown team: " + teamName);
            return;
        }
        if (state.isTeamUnassigned(t.getName())) {
            registeredTeams.add(t);
        } else {
            System.out.println("Team already assigned, skipping registration: " + teamName);
        }
    }

    /** Place all previously registered teams using a team-first backtracking solver. */
    public boolean makePlacements() {
        if (registeredTeams.isEmpty()) {
            System.out.println("No registered teams to place");
            return true;
        }
        // filter out teams that have been assigned since registration
        List<Team> toPlace = registeredTeams.stream()
                .filter(t -> state.isTeamUnassigned(t.getName()))
                .collect(Collectors.toList());
        if (toPlace.isEmpty()) {
            System.out.println("All registered teams are already assigned");
            registeredTeams.clear();
            return true;
        }

        boolean ok = backtrackingSolver.solveTeamFirst(this.state, new ArrayList<>(toPlace), 0);
        if (ok) {
            System.out.println("✅ Successfully placed registered teams");
            registeredTeams.clear();
        } else {
            System.out.println("❌ Failed to place all registered teams");
        }
        return ok;
    }

    public AssignmentState getState() {
        return state;
    }

    public boolean assignTeamToSlot(String slotKey, String teamName) {
        GroupSlot slot = slotsByKey.get(slotKey);
        Team team = assignedTeams.get(teamName);
        if (slot == null || team == null) {
            System.out.println("Invalid slot or team: " + slotKey + ", " + teamName);
            return false;
        }
        return assignTeamToSlot(slot, team);
    }

    public boolean assignTeamToSlot(GroupSlot slot, Team team) {
        StringBuilder reason = new StringBuilder();
        if (!constraintManager.isAssignmentValid(state, slot, team, reason)) {
            System.out.println("Assignment FAILED: " + slot + " -> " + team + "; reason="
                    + (reason.length() > 0 ? reason.toString() : "unknown"));
            return false;
        }
        // Delegate assignment + forward-check + snapshot handling to assignWithSnapshot
        AssignmentSnapshot snapshot = backtrackingSolver.assignWithSnapshot(this.state, slot, team, 0);
        if (snapshot == null) {
            System.out.println("Assignment FAILED (caused inconsistency): " + slot + " -> " + team);
            return false;
        }

        System.out.println("✅ Assignment SUCCEEDED: " + slot + " -> " + team);
        return true;
    }

    public boolean shuffleAndSolve() {
        List<Team> teams = assignedTeams.values().stream().collect(Collectors.toList());
        Collections.shuffle(teams);
        teams.stream().filter(t -> t.pot() == 1).forEach(t -> placeTeam(t.getName()));
        teams.stream().filter(t -> t.pot() == 2).forEach(t -> placeTeam(t.getName()));
        teams.stream().filter(t -> t.pot() == 3).forEach(t -> placeTeam(t.getName()));
        teams.stream().filter(t -> t.pot() == 4).forEach(t -> placeTeam(t.getName()));
        return makePlacements();
    }

    public boolean solveWorldCup2026Draw() {
        assignTeamToSlot("A1", "Mexico");
        assignTeamToSlot("B1", "Canada");
        assignTeamToSlot("D1", "USA");
        

        List<Team> teams = assignedTeams.values().stream().collect(Collectors.toList());
        // assignTeamToSlot("D1", "USA");
        Collections.shuffle(teams);
        
        // placeTeam("Mexico");
        
        teams.stream().filter(t -> t.pot() == 1).forEach(t -> placeTeam(t.getName()));
        teams.stream().filter(t -> t.pot() == 2).forEach(t -> placeTeam(t.getName()));
        teams.stream().filter(t -> t.pot() == 3).forEach(t -> placeTeam(t.getName()));
        teams.stream().filter(t -> t.pot() == 4).forEach(t -> placeTeam(t.getName()));
        return makePlacements();
    }

    /**
     * Assign a team to a slot, perform forward checking and return a snapshot of
     * domains
     * before the assignment. Returns null if the assignment immediately causes a
     * domain wipeout.
     */
    

    public void printAssignments() {
        System.out.println("Assignments:");
        state.getAssignments()
                .forEach((slot, team) -> System.out.println(slot + " -> " + (team != null ? team : "-")));
    }

    public boolean tryPlaceTeam(String teamName, int position) {
        // Find the team object
        Team teamToAssign = assignedTeams.get(teamName);

        int totalTeamsPerGroup = 4;
        for (int pos = position; pos <= totalTeamsPerGroup; pos++) {
            int startingPosition = (pos - 1) % totalTeamsPerGroup + 1;

            for (String g : state.getNextGroupsToAssign()) {
                GroupSlot slot = state.getUnassignedSlots()
                        .stream()
                        .filter(s -> s.getGroupName().equals(g) && s.getPosition() == startingPosition)
                        .findFirst()
                        .orElse(null);

                if (assignTeamToSlot(slot, teamToAssign))
                    return true;
            }

        }

        return false;
    }


       /**
     * Print groups vertically: one row per group, columns are positions.
     * Example header: "Group | 1 | 2 | 3 | 4"
     */
    public void prettyPrintGroupAssignmentsVertical() {
        PrettyPrinter.prettyPrintGroupAssignmentsVertical(drawOrder, state);
    }

    public void prettyPrintUnassignedDomains() {
        SortedMap<GroupSlot, Set<Team>> unassignedDomains = this.state.getUnassignedDomains();
        PrettyPrinter.prettyPrint(unassignedDomains);
    }

     // Summarize domain changes: print only slots whose domain changed (removed/added)
    @SuppressWarnings("unused")
    private void summarizeDomainChanges(Map<GroupSlot, Set<Team>> before, AssignmentState afterState, int depth) {
        for (GroupSlot s : before.keySet()) {
            Set<Team> beforeSet = before.getOrDefault(s, Set.of());
            Set<Team> afterSet = afterState.getDomains().getOrDefault(s, Set.of());
            // compute removed and added
            Set<Team> removed = new HashSet<>(beforeSet);
            removed.removeAll(afterSet);
            Set<Team> added = new HashSet<>(afterSet);
            added.removeAll(beforeSet);
            if (removed.isEmpty() && added.isEmpty())
                continue;
            StringBuilder sb = new StringBuilder();
            sb.append(s).append(": ");
            if (!removed.isEmpty()) {
                sb.append("removed=");
                sb.append(removed.stream().map(Object::toString).collect(Collectors.joining(", ")));
            }
            if (!added.isEmpty()) {
                if (!removed.isEmpty())
                    sb.append("; ");
                sb.append("added=");
                sb.append(added.stream().map(Object::toString).collect(Collectors.joining(", ")));
            }
            sb.append(" (").append(beforeSet.size()).append("->").append(afterSet.size()).append(")");
            log(depth, sb.toString());
        }
    }

    // helper for visual backtrack logging
    private void log(int depth, String msg) {
        String indent = "";
        if (depth > 0) {
            indent = "  ".repeat(Math.max(0, depth));
        }
        System.out.println(indent + msg);
    }


}

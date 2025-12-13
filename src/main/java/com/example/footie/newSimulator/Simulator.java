
package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.footie.newSimulator.constraint.ConstraintManager;

public class Simulator {
    private final List<GroupSlot> drawOrder;
    private final Map<String, GroupSlot> slotsByKey;
    final AssignmentState state;
    private final ConstraintManager constraintManager;
    private final Map<String, Team> assignedTeams;

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
        // Save original domains for backtracking
        Map<GroupSlot, Set<Team>> oldDomains = deepCopyDomains();

        state.assign(slot, team);
        constraintManager.forwardCheck(state, slot, team);
        System.out.println("Try assigning: " + slot + " -> " + team + "; running forward-check");

        // Check for dead ends (any domain empty)
        for (GroupSlot s : state.getUnassignedSlots()) {
            if (state.getDomains().get(s).isEmpty()) {
                // Report which slot hit an empty domain
                System.out.println("Assignment caused dead end: domain emptied for slot " + s + " after assigning "
                        + team + " to " + slot);
                // Restore domains and unassign
                for (GroupSlot restoreSlot : oldDomains.keySet()) {
                    state.getDomains().put(restoreSlot, oldDomains.get(restoreSlot));
                }
                // Restore the slot's original domain from oldDomains rather than
                // overwriting it with the singleton we just tried.
                List<Team> originalDomainForSlot = new ArrayList<>(oldDomains.get(slot));
                state.unassign(slot, originalDomainForSlot);
                System.out.println("❌ Unassigned (backtracked): " + slot + " <- " + team);
                return false;
            }
        }

        // Success
        System.out.println("✅ Assignment SUCCEEDED: " + slot + " -> " + team);

        return true;
    }

    private Map<GroupSlot, Set<Team>> deepCopyDomains() {
        Map<GroupSlot, Set<Team>> oldDomains = new HashMap<>();
        for (GroupSlot s : state.getUnassignedSlots()) {
            oldDomains.put(s, new HashSet<>(state.getDomains().get(s)));
        }
        return oldDomains;
    }

    public void shuffleAndAssignAll() {
        for (GroupSlot slot : drawOrder) {
            List<Team> candidates = new ArrayList<>(state.getDomains().get(slot));
            Collections.shuffle(candidates);
            boolean assigned = false;
            for (Team t : candidates) {
                if (assignTeamToSlot(slot, t)) {
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                System.out.println("Failed to assign a team to slot " + slot);
            }
        }
    }

    /**
     * Solve remaining assignments using recursive backtracking with forward
     * checking.
     * Returns true if a complete assignment was found.
     */
    public boolean solveWithBacktracking() {
        return backtrack(this.state);
    }

    private boolean backtrack(AssignmentState stateCopy) {
        // if no unassigned slots remain, solution found
        List<GroupSlot> unassigned = stateCopy.getUnassignedSlots();
        if (unassigned.isEmpty())
            return true;

        // choose next slot(s) to try using state's heuristic
        List<GroupSlot> slotsToTry = stateCopy.nextSlotsToTry();
        if (slotsToTry.isEmpty())
            return false;

        GroupSlot slot = slotsToTry.get(0);

        // iterate over candidates in domain (make a copy to avoid concurrent
        // modification)
        List<Team> candidates = new ArrayList<>(stateCopy.getDomains().get(slot));
        Collections.shuffle(candidates); // randomize order to get different solutions on different runs
        for (Team candidate : candidates) {
            StringBuilder reason = new StringBuilder();
            if (!constraintManager.isAssignmentValid(stateCopy, slot, candidate, reason)) {
                System.out.println("Skipping invalid candidate: " + candidate + " for slot " + slot + "; reason="
                        + (reason.length() > 0 ? reason.toString() : "unknown"));
                continue; // skip invalid candidate
            }

            Map<GroupSlot, Set<Team>> snapshot = assignWithSnapshot(stateCopy, slot, candidate);
            if (snapshot == null) {
                // immediate inconsistency, try next candidate
                continue;
            }

            // recurse
            if (backtrack(stateCopy))
                return true;

            // backtrack: restore state
            restoreFromSnapshot(slot, snapshot);
        }

        // no candidate led to a solution
        return false;
    }

    /**
     * Assign a team to a slot, perform forward checking and return a snapshot of
     * domains
     * before the assignment. Returns null if the assignment immediately causes a
     * domain wipeout.
     */
    private Map<GroupSlot, Set<Team>> assignWithSnapshot(AssignmentState stateCopy, GroupSlot slot, Team team) {
        Map<GroupSlot, Set<Team>> oldDomains = deepCopyDomains();

        stateCopy.assign(slot, team);
        constraintManager.forwardCheck(stateCopy, slot, team);

        // detect domain wipeout
        for (GroupSlot s : stateCopy.getUnassignedSlots()) {
            if (stateCopy.getDomains().get(s).isEmpty()) {
                // restore and unassign
                for (GroupSlot restoreSlot : oldDomains.keySet()) {
                    stateCopy.getDomains().put(restoreSlot, oldDomains.get(restoreSlot));
                }
                List<Team> originalDomainForSlot = new ArrayList<>(oldDomains.get(slot));
                stateCopy.unassign(slot, originalDomainForSlot);
                return null;
            }
        }

        return oldDomains;
    }

    private void restoreFromSnapshot(GroupSlot slot, Map<GroupSlot, Set<Team>> snapshot) {
        for (GroupSlot restoreSlot : snapshot.keySet()) {
            state.getDomains().put(restoreSlot, snapshot.get(restoreSlot));
        }
        List<Team> originalDomainForSlot = new ArrayList<>(snapshot.get(slot));
        state.unassign(slot, originalDomainForSlot);
    }

    public void printAssignments() {
        System.out.println("Assignments:");
        state.getAssignments()
                .forEach((slot, team) -> System.out.println(slot + " -> " + (team != null ? team : "-")));
    }

    public void prettyPrintGroupAssignments() {
        // Build a map: groupName -> map(position -> teamName)
        Map<String, Map<Integer, String>> groups = new HashMap<>();
        int maxPosition = 0;
        for (GroupSlot slot : drawOrder) {
            String g = slot.getGroupName();
            groups.putIfAbsent(g, new HashMap<>());
            Team team = state.getAssignments().get(slot);
            groups.get(g).put(slot.getPosition(), team != null ? team.getName() : "-");
            if (slot.getPosition() > maxPosition)
                maxPosition = slot.getPosition();
        }

        // Sort groups by name
        List<String> groupNames = new ArrayList<>(groups.keySet());
        Collections.sort(groupNames);

        // Compute column widths
        Map<String, Integer> colWidth = new HashMap<>();
        int posLabelWidth = Math.max(2, String.valueOf(maxPosition).length());
        for (String g : groupNames) {
            int w = g.length();
            Map<Integer, String> col = groups.get(g);
            for (int p = 1; p <= maxPosition; p++) {
                String cell = col.getOrDefault(p, "-");
                w = Math.max(w, cell.length());
            }
            // add padding
            colWidth.put(g, Math.max(w, 3) + 2);
        }

        // Print header
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%" + posLabelWidth + "s |", "Pos"));
        for (String g : groupNames) {
            int w = colWidth.get(g);
            sb.append(String.format(" %" + (-w) + "s|", "Group " + g));
        }
        System.out.println(sb.toString());

        // Print separator
        StringBuilder sep = new StringBuilder();
        sep.append("".repeat(Math.max(0, posLabelWidth))).append("-+-");
        for (String g : groupNames) {
            int w = colWidth.get(g);
            sep.append("".repeat(Math.max(0, w))).append("-+");
        }
        System.out.println(sep.toString());

        // Print rows by position
        for (int pos = 1; pos <= maxPosition; pos++) {
            StringBuilder row = new StringBuilder();
            row.append(String.format("%" + posLabelWidth + "d |", pos));
            for (String g : groupNames) {
                int w = colWidth.get(g);
                String cell = groups.get(g).getOrDefault(pos, "-");
                if (cell.length() > w - 2)
                    cell = cell.substring(0, w - 5) + "...";
                row.append(String.format(" %" + (-w) + "s|", cell));
            }
            System.out.println(row.toString());
        }
    }

    /**
     * Print groups vertically: one row per group, columns are positions.
     * Example header: "Group | 1 | 2 | 3 | 4"
     */
    public void prettyPrintGroupAssignmentsVertical() {
        // Build a map: groupName -> map(position -> teamName)
        Map<String, Map<Integer, String>> groups = new HashMap<>();
        int maxPosition = 0;
        for (GroupSlot slot : drawOrder) {
            String g = slot.getGroupName();
            groups.putIfAbsent(g, new HashMap<>());
            Team team = state.getAssignments().get(slot);
            groups.get(g).put(slot.getPosition(), team != null ? team.getName() : "-");
            if (slot.getPosition() > maxPosition)
                maxPosition = slot.getPosition();
        }

        // Sort groups by name
        List<String> groupNames = new ArrayList<>(groups.keySet());
        Collections.sort(groupNames);

        // Compute widths
        int groupLabelWidth = Math.max(5, groupNames.stream().mapToInt(String::length).max().orElse(5));
        int cellWidth = 15; // width per position column

        // Header
        StringBuilder header = new StringBuilder();
        header.append(String.format("%-" + (groupLabelWidth + 2) + "s", "Group"));
        for (int p = 1; p <= maxPosition; p++) {
            header.append(String.format(" %" + (-cellWidth) + "s", String.valueOf(p)));
        }
        System.out.println(header.toString());

        // Separator
        StringBuilder sep = new StringBuilder();
        sep.append("".repeat(Math.max(0, groupLabelWidth + 2))).append("-");
        for (int p = 1; p <= maxPosition; p++) {
            sep.append("".repeat(1)).append("".repeat(Math.max(0, cellWidth))).append("-");
        }
        System.out.println(sep.toString());

        // Rows
        for (String g : groupNames) {
            StringBuilder row = new StringBuilder();
            row.append(String.format("%-" + (groupLabelWidth + 2) + "s", "Group " + g));
            Map<Integer, String> col = groups.get(g);
            for (int p = 1; p <= maxPosition; p++) {
                String cell = col.getOrDefault(p, "-");
                if (cell.length() > cellWidth - 3)
                    cell = cell.substring(0, cellWidth - 6) + "...";
                row.append(String.format(" %" + (-cellWidth) + "s", cell));
            }
            System.out.println(row.toString());
        }
    }

    /**
     * Assign teams by iterating teams first, then picking an available slot for
     * each team.
     * For each team we try slots in the configured draw order where the team is
     * allowed
     * (present in the slot's current domain). This implements the "team-first"
     * strategy
     * requested by the user: try to place a team into the earliest possible slot.
     */
    public void assignTeamsFirst() {
        for (Team team : assignedTeams.values()) {
            boolean placed = false;
            System.out.println("Attempting to place team: " + team);
            for (GroupSlot slot : drawOrder) {
                if (state.isAssigned(slot))
                    continue;
                Set<Team> domain = state.getDomains().get(slot);
                if (domain == null)
                    continue;
                if (!domain.contains(team)) {
                    // Not allowed by domain/constraints — print small note and continue
                    StringBuilder reason = new StringBuilder();
                    if (!constraintManager.isAssignmentValid(state, slot, team, reason)) {
                        System.out.println("  Cannot place " + team + " into " + slot + " -> constraint: "
                                + (reason.length() > 0 ? reason.toString() : "unknown"));
                    } else {
                        System.out.println("  Not in domain for " + slot + " (may have been pruned)");
                    }
                    continue;
                }

                // Try assigning to this slot
                System.out.println("  Trying slot " + slot + " for team " + team);
                // instead assigning to slot, just assign it to group
                if (assignTeamToGroup(slot.getGroupName(), team)) {
                    placed = true;
                    break;
                } else {
                    System.out.println("  Failed to place " + team + " into " + slot + ", trying next slot");
                }
            }
            if (!placed) {
                System.out.println("Could not place team: " + team + " into any slot");
            }
        }
    }

    public boolean assignTeamToGroup(String groupName, Team team) {
        // iterate by group name first, then by position
        drawOrder.sort((a, b) -> {
            int groupComp = a.getGroupName().compareTo(b.getGroupName());
            if (groupComp != 0)
                return groupComp;
            return Integer.compare(a.getPosition(), b.getPosition());
        });

        for (GroupSlot slot : drawOrder) {
            if (slot.getGroupName().equals(groupName) && !state.isAssigned(slot)) {
                return assignTeamToSlot(slot, team);
            }
        }
        return false;
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

    public boolean tryPlaceTeam(String teamName) {
        // Find the team object
        Team teamToAssign = assignedTeams.get(teamName);

        List<GroupSlot> nextSlots = this.state.nextSlotsToTry();
        for (GroupSlot slot : nextSlots) {
            Map<GroupSlot, Set<Team>> snapshot = assignWithSnapshot(this.state, slot, teamToAssign);
            if (snapshot == null) {
                continue; // immediate inconsistency
            }

            // recurse
            if (backtrack(state)) {
                restoreFromSnapshot(slot, snapshot);
                // assignTeamToSlot(slot, teamToAssign);
                return true;
            }

            // backtrack: restore state
            restoreFromSnapshot(slot, snapshot);
        }

        return false;
    }

    /**
     * Place only the specified team into the first viable slot (does NOT fill
     * remaining slots, but verifies a complete solution exists via backtracking).
     * Runs AC-3 and then backtracking to ensure all possibilities are checked.
     */
    public boolean placeOnlyTeam(String teamName) {
        Team teamToAssign = assignedTeams.get(teamName);
        if (teamToAssign == null)
            return false;

        List<GroupSlot> candidates = this.state.nextSlotsToTry().stream()
                .filter(s -> !state.isAssigned(s)
                        && state.getDomains().getOrDefault(s, Set.of()).contains(teamToAssign))
                .collect(Collectors.toList());

        for (GroupSlot slot : candidates) {
            // snapshot domains and assignments before trying this placement
            Map<GroupSlot, Set<Team>> snapshot = deepCopyDomains();
            Map<GroupSlot, Team> assignmentSnapshot = new HashMap<>(state.getAssignments());

            // assign and run forward-check
            state.assign(slot, teamToAssign);
            constraintManager.forwardCheck(state, slot, teamToAssign);

            // check for immediate domain wipeout
            boolean immediateWipeout = false;
            for (GroupSlot s : state.getUnassignedSlots()) {
                if (state.getDomains().get(s).isEmpty()) {
                    immediateWipeout = true;
                    break;
                }
            }

            if (immediateWipeout) {
                // restore and try next candidate
                for (GroupSlot restoreSlot : snapshot.keySet()) {
                    state.getDomains().put(restoreSlot, snapshot.get(restoreSlot));
                }
                List<Team> originalDomainForSlot = new ArrayList<>(snapshot.get(slot));
                state.unassign(slot, originalDomainForSlot);
                System.out.println("Placement of " + teamToAssign + " into " + slot
                        + " caused immediate domain wipeout; trying next");
                continue;
            }

            // enforce full arc-consistency (AC-3)
            boolean ac3ok = constraintManager.enforceArcConsistency(state);
            if (!ac3ok) {
                // AC-3 found inconsistency, restore and try next candidate
                for (GroupSlot restoreSlot : snapshot.keySet()) {
                    state.getDomains().put(restoreSlot, snapshot.get(restoreSlot));
                }
                List<Team> originalDomainForSlot = new ArrayList<>(snapshot.get(slot));
                state.unassign(slot, originalDomainForSlot);
                System.out.println(
                        "Placement of " + teamToAssign + " into " + slot + " caused AC-3 domain wipeout; trying next");
                continue;
            }

            // AC-3 passed. Now verify that a complete solution exists by running
            // backtracking.
            // Take another snapshot of domains after AC-3 (domains may have been pruned)
            Map<GroupSlot, Set<Team>> snapshotAfterAC3 = deepCopyDomains();

            var deepCopyState = this.state;
            boolean solutionExists = backtrack(deepCopyState);

            if (solutionExists) {
                // Solution found! Restore to the state right after placing this team
                // (keep the team assigned, restore domains to post-AC3 state, undo the
                // backtracking assignments)
                for (GroupSlot s : snapshotAfterAC3.keySet()) {
                    state.getDomains().put(s, new HashSet<>(snapshotAfterAC3.get(s)));
                }
                // restore assignments to just the current team placed (undo backtracking)
                for (GroupSlot s : state.getAssignments().keySet()) {
                    if (s.equals(slot)) {
                        state.getAssignments().put(s, teamToAssign);
                    } else if (assignmentSnapshot.get(s) != null) {
                        state.getAssignments().put(s, assignmentSnapshot.get(s));
                    } else {
                        state.getAssignments().put(s, null);
                    }
                }

                System.out.println("✅ Placed " + teamToAssign + " into " + slot + " (verified solution exists)");
                return true;
            } else {
                // No solution exists with this placement, restore full snapshot and try next
                for (GroupSlot restoreSlot : snapshot.keySet()) {
                    state.getDomains().put(restoreSlot, snapshot.get(restoreSlot));
                }
                for (GroupSlot s : assignmentSnapshot.keySet()) {
                    state.getAssignments().put(s, assignmentSnapshot.get(s));
                }
                System.out.println(
                        "Placement of " + teamToAssign + " into " + slot + " has no complete solution; trying next");
            }
        }

        System.out.println("❌ Could not place " + teamToAssign + " into any viable slot");
        return false;
    }

}

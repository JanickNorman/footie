
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
        registeredTeams.add(t);
    }

    /** Place all previously registered teams using a team-first backtracking solver. */
    public boolean makePlacements() {
        if (registeredTeams.isEmpty()) {
            System.out.println("No registered teams to place");
            return true;
        }
        boolean ok = backtrackingSolver.solveTeamFirst(this.state, new ArrayList<>(registeredTeams));
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
                    state.restoreDomains(oldDomains);
                // Restore the slot's original domain from oldDomains rather than
                // overwriting it with the singleton we just tried.
                Set<Team> slotDomain = oldDomains.get(slot);
                List<Team> originalDomainForSlot = slotDomain != null ? new ArrayList<>(slotDomain) : new ArrayList<>();
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

    // deep copy domains from a specific AssignmentState (used for search-stack
    // operations so we don't mix global `state` with local copies)
    private Map<GroupSlot, Set<Team>> deepCopyDomains(AssignmentState st) {
        Map<GroupSlot, Set<Team>> oldDomains = new HashMap<>();
        for (GroupSlot s : st.getUnassignedSlots()) {
            Set<Team> d = st.getDomains().get(s);
            oldDomains.put(s, d != null ? new HashSet<>(d) : new HashSet<>());
        }
        return oldDomains;
    }

    /**
     * Solve remaining assignments using recursive backtracking with forward
     * checking.
     * Returns true if a complete assignment was found.
     */
    public boolean solveWithBacktracking() {
        return backtrackingSolver.solve(this.state);
    }
    // wrapper so older callers can still call backtrack(stateCopy)
    private boolean backtrack(AssignmentState stateCopy) {
        return backtrack(stateCopy, 0);
    }

    private boolean backtrack(AssignmentState stateCopy, int depth) {
        // if no unassigned slots remain, solution found
        List<GroupSlot> unassigned = stateCopy.getUnassignedSlots();
        log(depth, "ENTER backtrack; unassignedSlots=" + unassigned.size());
        if (unassigned.isEmpty()) {
            log(depth, "SOLUTION found (no unassigned slots)");
            return true;
        }

        // choose next slot(s) to try using state's heuristic
        List<GroupSlot> slotsToTry = stateCopy.nextSlotsToTry();
        if (slotsToTry.isEmpty())
            return false;

        GroupSlot slot = slotsToTry.get(0);
        log(depth, "Chose slot: " + slot + " (domainSize="
                + (stateCopy.getDomains().get(slot) != null ? stateCopy.getDomains().get(slot).size() : 0) + ")");

        // iterate over candidates in domain (make a copy to avoid concurrent
        // modification)
        Set<Team> domain = stateCopy.getDomains().get(slot);
        List<Team> candidates = domain != null ? new ArrayList<>(domain) : new ArrayList<>();
        // Collections.shuffle(candidates); // randomize order to get different solutions on different runs
        for (Team candidate : candidates) {
            StringBuilder reason = new StringBuilder();
            if (!constraintManager.isAssignmentValid(stateCopy, slot, candidate, reason)) {
                continue; // skip invalid candidate
            }

            log(depth, "Trying candidate: " + candidate);
            Map<GroupSlot, Set<Team>> snapshot = assignWithSnapshot(stateCopy, slot, candidate);
            if (snapshot == null) {
                // immediate inconsistency, try next candidate
                log(depth, "  -> immediate inconsistency with candidate: " + candidate + ", backtrack");
                continue;
            }

            // recurse
            if (backtrack(stateCopy, depth + 1))
                return true;

            // backtrack: restore state
            restoreFromSnapshot(stateCopy, slot, snapshot);
            log(depth, "  <- Backtracked from candidate: " + candidate + " for slot " + slot);
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
        Map<GroupSlot, Set<Team>> oldDomains = deepCopyDomains(stateCopy);

        // log candidate lists per slot (before forward-check)
        for (Map.Entry<GroupSlot, Set<Team>> e : oldDomains.entrySet()) {
            GroupSlot s = e.getKey();
            Set<Team> teams = e.getValue();
            String list = teams.stream().map(Object::toString).collect(Collectors.joining(", "));
            log(0, "Candidates for " + s + " before: " + (list.isEmpty() ? "(empty)" : list));
        }

        stateCopy.assign(slot, team);
        constraintManager.forwardCheck(stateCopy, slot, team);

        // summarize domain changes after forward-check (only show diffs)
        summarizeDomainChanges(oldDomains, stateCopy, 0);

        // detect domain wipeout
        for (GroupSlot s : stateCopy.getUnassignedSlots()) {
            Set<Team> dom = stateCopy.getDomains().get(s);
            if (dom == null || dom.isEmpty()) {
                // restore and unassign on the provided stateCopy
                    stateCopy.restoreDomains(oldDomains);
                Set<Team> slotDomain = oldDomains.get(slot);
                List<Team> originalDomainForSlot = slotDomain != null ? new ArrayList<>(slotDomain) : new ArrayList<>();
                stateCopy.unassign(slot, originalDomainForSlot);
                return null;
            }
        }

        return oldDomains;
    }

    // restore domains/assignment into the provided AssignmentState
    private void restoreFromSnapshot(AssignmentState target, GroupSlot slot, Map<GroupSlot, Set<Team>> snapshot) {
            target.restoreDomains(snapshot);
        Set<Team> slotDomain = snapshot.get(slot);
        List<Team> originalDomainForSlot = slotDomain != null ? new ArrayList<>(slotDomain) : new ArrayList<>();
        target.unassign(slot, originalDomainForSlot);
    }

    // Utility helpers to make backtracking/placeOnlyTeam logic clearer
    private List<Team> domainListFromSnapshot(Map<GroupSlot, Set<Team>> snapshot, GroupSlot slot) {
        Set<Team> slotDomain = snapshot.get(slot);
        return slotDomain != null ? new ArrayList<>(slotDomain) : new ArrayList<>();
    }

    private Map<GroupSlot, Team> snapshotAssignments() {
        return new HashMap<>(state.getAssignments());
    }

    private boolean hasImmediateWipeout() {
        for (GroupSlot s : state.getUnassignedSlots()) {
            Set<Team> domain = state.getDomains().get(s);
            if (domain == null || domain.isEmpty())
                return true;
        }
        return false;
    }

    private void restoreDomains(Map<GroupSlot, Set<Team>> snapshot) {
            state.restoreDomains(snapshot);
    }

    private void restoreAssignments(Map<GroupSlot, Team> assignmentSnapshot) {
        for (GroupSlot s : assignmentSnapshot.keySet()) {
            state.getAssignments().put(s, assignmentSnapshot.get(s));
        }
    }

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
     * Place only the specified team into the first viable slot (does NOT fill
     * remaining slots, but verifies a complete solution exists via backtracking).
     * Runs AC-3 and then backtracking to ensure all possibilities are checked.
     */
    public boolean placeOnlyTeam(String teamName) {
        return placeOnlyTeam(teamName, false);
    }

    /**
     * Place only the specified team into the first viable slot.
     * If {@code verifyCompleteSolution} is true, runs full backtracking to
     * ensure a complete solution exists; otherwise accepts the placement after
     * forward-check + AC-3 pruning (faster).
     */
    public boolean placeOnlyTeam(String teamName, boolean verifyCompleteSolution) {
        Team teamToAssign = assignedTeams.get(teamName);
        if (teamToAssign == null)
            return false;
        List<GroupSlot> candidates = this.state.nextSlots().stream()
                .filter(s -> s != null && !state.isAssigned(s)
                        && state.getDomains().getOrDefault(s, Set.of()).contains(teamToAssign))
                .collect(Collectors.toList());

        for (GroupSlot slot : candidates) {
            // quick check: if this assignment is invalid by constraints, skip it
            StringBuilder reason = new StringBuilder();
            if (!constraintManager.isAssignmentValid(state, slot, teamToAssign, reason)) {
                System.out.println("  Skipping " + slot + " for " + teamToAssign + " -> constraint: "
                        + (reason.length() > 0 ? reason.toString() : "unknown"));
                continue;
            }

            Map<GroupSlot, Set<Team>> snapshot = deepCopyDomains();
            Map<GroupSlot, Team> assignmentSnapshot = snapshotAssignments();

            // assign and run forward-check
            state.assign(slot, teamToAssign);
            constraintManager.forwardCheck(state, slot, teamToAssign);

            // immediate wipeout?
            if (hasImmediateWipeout()) {
                restoreDomains(snapshot);
                state.unassign(slot, domainListFromSnapshot(snapshot, slot));
                System.out.println("Placement of " + teamToAssign + " into " + slot
                        + " caused immediate domain wipeout; trying next");
                continue;
            }

            // enforce full arc-consistency (AC-3)
            boolean ac3ok = constraintManager.enforceArcConsistency(state);
            if (!ac3ok) {
                restoreDomains(snapshot);
                state.unassign(slot, domainListFromSnapshot(snapshot, slot));
                System.out.println("Placement of " + teamToAssign + " into " + slot
                        + " caused AC-3 domain wipeout; trying next");
                continue;
            }

            if (!verifyCompleteSolution) {
                // Accept the placement based on AC-3 pruning (fast path)
                Map<GroupSlot, Set<Team>> snapshotAfterAC3 = deepCopyDomains();
                // restore prior assignments, then ensure this slot remains assigned
                restoreAssignments(assignmentSnapshot);
                state.getAssignments().put(slot, teamToAssign);
                // apply post-AC3 domains defensively
                state.restoreDomains(snapshotAfterAC3);
                System.out.println("✅ Placed " + teamToAssign + " into " + slot + " (AC-3 accepted)");
                return true;
            }

            // AC-3 passed. Verify a complete solution exists via backtracking.
            Map<GroupSlot, Set<Team>> snapshotAfterAC3 = deepCopyDomains();
            var deepCopyState = this.state.copyForSearch();
            boolean solutionExists = backtrack(deepCopyState);

            if (solutionExists) {
                // Restore domains to post-AC3 pruning (use defensive copy)
                state.restoreDomains(snapshotAfterAC3);
                // restore prior assignments, then ensure this slot remains assigned
                restoreAssignments(assignmentSnapshot);
                state.getAssignments().put(slot, teamToAssign);

                System.out.println("✅ Placed " + teamToAssign + " into " + slot + " (verified solution exists)");
                return true;
            } else {
                // No solution exists with this placement, restore full snapshot and try next
                restoreDomains(snapshot);
                restoreAssignments(assignmentSnapshot);
                System.out.println("Placement of " + teamToAssign + " into " + slot
                        + " has no complete solution; trying next");
            }
        }

        System.out.println("❌ Could not place " + teamToAssign + " into any viable slot");
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

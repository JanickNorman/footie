package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.footie.newSimulator.constraint.ConstraintManager;

public class BacktrackingSolver {

    private final ConstraintManager constraintManager;

    public BacktrackingSolver(ConstraintManager constraintManager) {
        this.constraintManager = constraintManager;
    }

    /**
     * Solve remaining assignments using recursive backtracking with forward
     * checking.
     * Returns true if a complete assignment was found.
     */
    public boolean solve(AssignmentState state) {
        return backtrack(state, 0);
    }

    /**
     * Team-first backtracking: place the given list of teams (order will be
     * chosen with MRV-on-teams) into the state. Returns true if a complete
     * placement for those teams exists.
     */
    public boolean solveTeamFirst(AssignmentState state, List<Team> teamsToPlace, int depth) {
        // choose team with fewest candidate slots (MRV on teams)

        if (teamsToPlace.isEmpty())
            return true; // all placed
        // Team chosen = teamsToPlace.stream()
        //         .min(Comparator.comparingInt(t -> candidateSlots(state, t).size()))
        //         .orElse(null);
        // chose team from the order of register
        Team chosen = teamsToPlace.get(0);

        List<GroupSlot> candidates = candidateSlots(state, chosen);
        for (GroupSlot slot : candidates) {
            StringBuilder reason = new StringBuilder();
            if (!constraintManager.isAssignmentValid(state, slot, chosen, reason))
                continue;

            Map<GroupSlot, Set<Team>> snapshot = assignWithSnapshot(state, slot, chosen, depth);
            if (snapshot == null)
                continue;

            List<Team> remaining = new ArrayList<>(teamsToPlace);
            remaining.remove(chosen);
            if (solveTeamFirst(state, remaining, depth + 1))
                return true;

            // backtrack
            restoreFromSnapshot(state, slot, snapshot);
        }

        return false;
    }

    private List<GroupSlot> candidateSlots(AssignmentState state, Team team) {
        List<GroupSlot> results = state.nextSlots();
        
        return results.stream()
                .filter(s -> state.getDomains().getOrDefault(s, Set.of()).contains(team))
                .collect(Collectors.toList());
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
        List<GroupSlot> slotsToTry = stateCopy.nextSlotsByLeastDomainSize();
        log(depth, "Unassigned slots to try: "
                + slotsToTry.stream().map(Object::toString).collect(Collectors.joining(", ")));
        if (slotsToTry.isEmpty())
            return false;

        GroupSlot slot = slotsToTry.get(0);
        log(depth, "Chose slot: " + slot + " (domainSize="
                + (stateCopy.getDomains().get(slot) != null ? stateCopy.getDomains().get(slot).size() : 0) + ")");

        // iterate over candidates in domain (make a copy to avoid concurrent
        // modification)
        Set<Team> domain = stateCopy.getDomains().get(slot);
        List<Team> candidates = domain != null ? new ArrayList<>(domain) : new ArrayList<>();

        // LCV: score candidates by how constraining they are. We simulate the
        // assignment on a copy of the state and compute total remaining domain
        // sizes; prefer candidates that leave larger total domains (less
        // constraining).
        Map<Team, Integer> score = new HashMap<>();
        for (Team cand : candidates) {
            AssignmentState copy = stateCopy;
            Map<GroupSlot, Set<Team>> snap = assignWithSnapshot(copy, slot, cand);
            if (snap == null) {
                score.put(cand, Integer.MIN_VALUE); // immediate dead-end -> worst
                continue;
            }
            int total = 0;
            for (GroupSlot u : copy.getUnassignedSlots()) {
                total += copy.getDomains().getOrDefault(u, Set.of()).size();
            }
            score.put(cand, total);
        }

        candidates.sort(Comparator.comparingInt((Team t) -> score.getOrDefault(t, Integer.MIN_VALUE)).reversed());

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

    private Map<GroupSlot, Set<Team>> assignWithSnapshot(AssignmentState stateCopy, GroupSlot slot, Team team) {
        return assignWithSnapshot(stateCopy, slot, team, 0);
    }

    /**
     * Assign a team to a slot, perform forward checking and return a snapshot of
     * domains
     * before the assignment. Returns null if the assignment immediately causes a
     * domain wipeout.
     */
    private Map<GroupSlot, Set<Team>> assignWithSnapshot(AssignmentState stateCopy, GroupSlot slot, Team team, int depth) {
        Map<GroupSlot, Set<Team>> oldDomains = deepCopyDomains(stateCopy);

        stateCopy.assign(slot, team);
        constraintManager.forwardCheck(stateCopy, slot, team);

        // summarize domain changes after forward-check (only show diffs)
        summarizeDomainChanges(oldDomains, stateCopy, depth);

        // Run global consistency checks (AC-3, Hall/missing-team, domain wipeout)
        if (!constraintManager.checkGlobalConsistency(stateCopy)) {
            for (GroupSlot restoreSlot : oldDomains.keySet()) {
                stateCopy.getDomains().put(restoreSlot, oldDomains.get(restoreSlot));
            }
            Set<Team> slotDomain = oldDomains.get(slot);
            List<Team> originalDomainForSlot = slotDomain != null ? new ArrayList<>(slotDomain) : new ArrayList<>();
            stateCopy.unassign(slot, originalDomainForSlot);
            return null;
        }

        return oldDomains;
    }

    // restore domains/assignment into the provided AssignmentState
    private void restoreFromSnapshot(AssignmentState target, GroupSlot slot, Map<GroupSlot, Set<Team>> snapshot) {
        for (GroupSlot restoreSlot : snapshot.keySet()) {
            target.getDomains().put(restoreSlot, snapshot.get(restoreSlot));
        }
        Set<Team> slotDomain = snapshot.get(slot);
        List<Team> originalDomainForSlot = slotDomain != null ? new ArrayList<>(slotDomain) : new ArrayList<>();
        target.unassign(slot, originalDomainForSlot);
    }

    // deep copy domains from a specific AssignmentState (used for search-stack
    // operations so we don't mix global `state` with local copies)
    private Map<GroupSlot, Set<Team>> deepCopyDomains(AssignmentState st) {
        Map<GroupSlot, Set<Team>> oldDomains = new HashMap<>();
        for (GroupSlot s : st.getSlots()) {
            Set<Team> d = st.getDomains().get(s);
            oldDomains.put(s, d != null ? new HashSet<>(d) : new HashSet<>());
        }
        return oldDomains;
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

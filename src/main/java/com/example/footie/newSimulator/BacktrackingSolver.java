package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.example.footie.newSimulator.constraint.ConstraintManager;

public class BacktrackingSolver {

    private final ConstraintManager constraintManager;

    int onlyCheckDomainAfter = 30;

    // Node counting for search-limits
    private final AtomicLong nodesVisited = new AtomicLong(0);
    private volatile long maxNodes = 125L; // default: no limit

    public BacktrackingSolver(ConstraintManager constraintManager) {
        this.constraintManager = constraintManager;
    }

    /** Set a maximum number of search nodes to explore. Use <=0 to disable. */
    public void setMaxNodes(long max) {
        this.maxNodes = max > 0 ? max : Long.MAX_VALUE;
        this.nodesVisited.set(0);
    }

    public void setOnlyCheckDomainAfter(int onlyCheckDomainAfter) {
        this.onlyCheckDomainAfter = onlyCheckDomainAfter;
    }

    public long getNodesVisited() {
        return nodesVisited.get();
    }

    public boolean isNodeLimitReached() {
        return nodesVisited.get() >= maxNodes;
    }

    /**
     * Team-first backtracking: place the given list of teams (order will be
     * chosen with MRV-on-teams) into the state. Returns true if a complete
     * placement for those teams exists.
     */
    public boolean solveTeamFirst(AssignmentState state, List<Team> teamsToPlace, int depth) {
        long visited = nodesVisited.incrementAndGet();
        if (visited > maxNodes) throw new RuntimeException("Node limit reached");

        if (teamsToPlace.isEmpty())
            return true;

        Team chosen = teamsToPlace.get(0);

        List<GroupSlot> candidates = state.candidateSlots(chosen);
        for (GroupSlot slot : candidates) {
            if (!constraintManager.isAssignmentValid(state, slot, chosen)) {
                continue;
            }

            AssignmentSnapshot snapshot = assignWithSnapshot(state, slot, chosen, depth);
            if (snapshot == null)
                continue;

            List<Team> remaining = new ArrayList<>(teamsToPlace);
            remaining.remove(chosen);
            if (solveTeamFirst(state, remaining, depth + 1))
                return true;

            // backtrack
            state.restoreFromSnapshot(snapshot);
        }

        return false;
    }

    /**
     * Assign a team to a slot, perform forward checking and return a snapshot of
     * domains before the assignment. Returns null if the assignment immediately
     * causes a domain wipeout.
     */
    public AssignmentSnapshot assignWithSnapshot(AssignmentState state, GroupSlot slot, Team team, int depth) {
        // create a snapshot from the state (captures domains + assignments)
        AssignmentSnapshot oldSnapshot = state.createSnapshot();

        state.assign(slot, team);
        constraintManager.forwardCheck(state, slot, team);

        // summarize domain changes after forward-check (only show diffs)
        // summarizeDomainChanges(oldSnapshot.getDomainsSnapshot(), stateCopy, depth);

        if (this.dontCheckConsistencyBefore(depth))
            return oldSnapshot;

        // Run global consistency checks (AC-3, Hall/missing-team, domain wipeout)
        if (!constraintManager.checkGlobalConsistency(state)) {
            oldSnapshot.restore();
            return null;
        }

        return oldSnapshot;
    }

    private boolean dontCheckConsistencyBefore(int depth) {
        return depth < onlyCheckDomainAfter;
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

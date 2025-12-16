package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.List;

import com.example.footie.newSimulator.constraint.ConstraintManager;

public class BacktrackingSolver {

    private final ConstraintManager constraintManager;

    int onlyCheckDomainAfter = 30;

    public BacktrackingSolver(ConstraintManager constraintManager) {
        this.constraintManager = constraintManager;
    }

    /**
     * Team-first backtracking: place the given list of teams (order will be
     * chosen with MRV-on-teams) into the state. Returns true if a complete
     * placement for those teams exists.
     */
    public boolean solveTeamFirst(AssignmentState state, List<Team> teamsToPlace, int depth) {
        if (teamsToPlace.isEmpty())
            return true; // all placed

        Team chosen = teamsToPlace.getFirst();

        List<GroupSlot> candidates = state.candidateSlots(chosen);
        for (GroupSlot slot : candidates) {
            if (!constraintManager.isAssignmentValid(state, slot, chosen)) {
                continue;
            }

            AssignmentState.DomainsSnapshot snapshot = assignWithSnapshot(state, slot, chosen, depth);
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
    public AssignmentState.DomainsSnapshot assignWithSnapshot(AssignmentState state, GroupSlot slot, Team team,
            int depth) {
        AssignmentState.DomainsSnapshot oldDomains = state.snapshotDomains(slot);

        state.assign(slot, team);
        constraintManager.forwardCheck(state, slot, team);

        if (dontCheckConsistencyBefore(depth))
            return oldDomains;

        // Run global consistency checks (AC-3, Hall/missing-team, domain wipeout)
        if (!constraintManager.checkGlobalConsistency(state)) {
            state.restoreFromSnapshot(oldDomains);
            return null;
        }

        return oldDomains;
    }

    private boolean dontCheckConsistencyBefore(int depth) {
        return depth < onlyCheckDomainAfter;
    }

}

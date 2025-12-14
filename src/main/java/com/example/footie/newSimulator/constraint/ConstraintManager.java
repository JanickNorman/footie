package com.example.footie.newSimulator.constraint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;

public class ConstraintManager {
    private final List<Constraint> constraints = new ArrayList<>();

    public void addConstraint(Constraint c) {
        constraints.add(c);
    }

    public boolean isAssignmentValid(AssignmentState state, GroupSlot slot, Team team) {
        for (Constraint c : constraints) {
            if (!c.isAssignmentAllowed(state, slot, team))
                return false;
        }
        return true;
    }

    /**
     * Like {@link #isAssignmentValid(AssignmentState,GroupSlot,Team)} but fills
     * the provided StringBuilder with a short reason when the assignment is
     * rejected (the simple constraint class name).
     */
    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public boolean isAssignmentValid(AssignmentState state, GroupSlot slot, Team team, StringBuilder reason) {
        for (Constraint c : constraints) {
            if (!c.isAssignmentAllowed(state, slot, team)) {
                if (reason != null)
                    reason.append(c.getClass().getSimpleName());
                return false;
            }
        }

        // default constraint: team must be in slot's domain
        if (!state.getDomains(slot).contains(team)) {
            if (reason != null) {
                if (reason.length() > 0)
                    reason.append("; ");
                reason.append("Team not in slot domain");
            }
            return false;
        }

        if (state.isAssigned(slot)) {
            reason.append("Assignment FAILED: " + slot + " is already assigned to " + state.getAssignments().get(slot));
            return false;
        }

        return true;
    }

    public void forwardCheck(AssignmentState state, GroupSlot slot, Team team) {
        for (Constraint c : constraints) {
            c.forwardCheck(state, slot, team);
        }
        // Enforce that a team can only be assigned once: remove the assigned team
        // from the domains of all other unassigned slots.
        for (GroupSlot s : state.getUnassignedSlots()) {
            if (!s.equals(slot)) {
                state.removeTeamFromDomain(s, team.getName());
            }
        }
    }

    /**
     * Enforce arc-consistency (AC-3) across all unassigned variables.
     * Returns true if no domain was emptied, false if some domain became empty.
     */
    public boolean enforceArcConsistency(AssignmentState state) {
        List<GroupSlot> unassigned = state.getUnassignedSlots();
        Deque<GroupSlot[]> queue = new ArrayDeque<>();

        for (GroupSlot xi : unassigned) {
            for (GroupSlot xj : unassigned) {
                if (!xi.equals(xj))
                    queue.add(new GroupSlot[] { xi, xj });
            }
        }

        while (!queue.isEmpty()) {
            GroupSlot[] pair = queue.removeFirst();
            GroupSlot xi = pair[0];
            GroupSlot xj = pair[1];
            if (revise(state, xi, xj)) {
                if (state.getDomains().get(xi).isEmpty())
                    return false;
                for (GroupSlot xk : unassigned) {
                    if (!xk.equals(xi) && !xk.equals(xj)) {
                        queue.add(new GroupSlot[] { xk, xi });
                    }
                }
            }
        }
        // final sanity check: ensure matching covers all unassigned slots
        return hasPerfectMatching(state);
    }

    /**
     * Revise xi's domain with respect to xj. Return true if xi's domain changed.
     */
    private boolean revise(AssignmentState state, GroupSlot xi, GroupSlot xj) {
        boolean revised = false;
        Set<Team> domainXi = new HashSet<>(state.getDomains(xi));

        for (Team vx : new HashSet<>(domainXi)) {
            boolean hasSupport = false;
            List<Team> originalXiDomain = new ArrayList<>(state.getDomains(xi));

            // temporarily assign xi = vx and check if some value in xj's domain
            // is consistent with that assignment
            state.assign(xi, vx);
            for (Team vy : new HashSet<>(state.getDomains(xj))) {
                if (isAssignmentValid(state, xj, vy)) {
                    hasSupport = true;
                    break;
                }
            }
            state.unassign(xi, originalXiDomain);

            if (!hasSupport) {
                state.removeTeamFromDomain(xi, vx.getName());
                revised = true;
            }
        }

        return revised;
    }

    /**
     * Singleton Arc Consistency (SAC): for every unassigned variable xi and for
     * every value v in its domain, temporarily assign xi=v and run AC-3. If
     * a domain wipeout occurs, remove v from xi's domain. Returns false if a
     * domain becomes empty during pruning.
     *
     * This is stronger (and much more expensive) than plain AC-3.
     */
    public boolean enforceSingletonArcConsistency(AssignmentState state) {
        List<GroupSlot> unassigned = state.getUnassignedSlots();

        for (GroupSlot xi : new ArrayList<>(unassigned)) {
            Set<Team> domainCopy = new HashSet<>(state.getDomains(xi));

            for (Team v : new HashSet<>(domainCopy)) {
                // snapshot domains
                Map<GroupSlot, Set<Team>> snapshot = new HashMap<>();
                for (GroupSlot s : state.getUnassignedSlots()) {
                    snapshot.put(s, new HashSet<>(state.getDomains().get(s)));
                }

                // temporarily assign and propagate
                List<Team> originalDomainForXi = new ArrayList<>(snapshot.get(xi));
                state.assign(xi, v);
                forwardCheck(state, xi, v);
                boolean ok = enforceArcConsistency(state);

                // restore snapshot (do not keep pruning from the test)
                for (GroupSlot s : snapshot.keySet()) {
                    state.getDomains().put(s, new HashSet<>(snapshot.get(s)));
                }
                state.unassign(xi, originalDomainForXi);

                if (!ok) {
                    // prune v permanently
                    state.removeTeamFromDomain(xi, v.getName());
                    if (state.getDomains().get(xi).isEmpty())
                        return false;
                }
            }
        }
        // final sanity check

        return hasPerfectMatching(state);
    }

    /**
     * Delegates to AssignmentState to check for a perfect matching between
     * unassigned slots and unassigned teams (detects Hall violations).
     */
    public boolean hasPerfectMatching(AssignmentState state) {
        return state.hasPerfectMatchingForUnassignedSlots();
    }
}

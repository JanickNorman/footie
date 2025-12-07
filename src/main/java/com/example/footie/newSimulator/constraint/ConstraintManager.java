package com.example.footie.newSimulator.constraint;

import java.util.ArrayList;
import java.util.List;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;


public class ConstraintManager {
    private final List<Constraint> constraints = new ArrayList<>();

    public void addConstraint(Constraint c) { constraints.add(c); }

    public boolean isAssignmentValid(AssignmentState state, GroupSlot slot, Team team) {
        for (Constraint c : constraints) {
            if (!c.isAssignmentAllowed(state, slot, team)) return false;
        }
        return true;
    }

    /**
     * Like {@link #isAssignmentValid(AssignmentState,GroupSlot,Team)} but fills
     * the provided StringBuilder with a short reason when the assignment is
     * rejected (the simple constraint class name).
     */
    public boolean isAssignmentValid(AssignmentState state, GroupSlot slot, Team team, StringBuilder reason) {
        for (Constraint c : constraints) {
            if (!c.isAssignmentAllowed(state, slot, team)) {
                if (reason != null) reason.append(c.getClass().getSimpleName());
                return false;
            }
        }        

        // default constraint: team must be in slot's domain
        if (!state.getDomain(slot).contains(team)) {
            if (reason != null) {
                if (reason.length() > 0) reason.append("; ");
                reason.append("Team not in slot domain");
            }
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
                state.getDomains().get(s).removeIf(t -> t.getName().equals(team.getName()));
            }
        }
    }
}

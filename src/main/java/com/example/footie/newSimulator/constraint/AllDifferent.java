package com.example.footie.newSimulator.constraint;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;

/**
 * Enforces that each team may be assigned to at most one slot (global all-different).
 */
public class AllDifferent implements Constraint {

    @Override
    public boolean isAssignmentAllowed(AssignmentState state, GroupSlot slot, Team team) {
        // If the team is already assigned elsewhere, disallow
        return !state.isTeamAssigned(team);
    }

    @Override
    public void forwardCheck(AssignmentState state, GroupSlot slot, Team team) {
        // Remove the assigned team from domains of all other unassigned slots
        for (GroupSlot s : state.getUnassignedSlots()) {
            if (!s.equals(slot)) {
                state.removeTeamFromDomain(s, team.getName());
            }
        }
    }

}

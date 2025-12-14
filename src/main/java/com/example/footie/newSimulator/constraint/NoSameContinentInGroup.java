package com.example.footie.newSimulator.constraint;

import java.util.Map;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;


public class NoSameContinentInGroup implements Constraint {

    @Override
    public boolean isAssignmentAllowed(AssignmentState state, GroupSlot slot, Team team) {
        // For the target group, ensure no already-assigned team shares any continent
        for (Map.Entry<GroupSlot, Team> e : state.getAssignments().entrySet()) {
            GroupSlot otherSlot = e.getKey();
            Team assigned = e.getValue();
            if (assigned == null) continue;
            if (!otherSlot.getGroupName().equals(slot.getGroupName())) continue;

            // if any continent overlaps, assignment is not allowed
            for (String c : assigned.getContinents()) {
                if (team.getContinents().contains(c)) return false;
            }
        }
        return true;
    }

    @Override
    public void forwardCheck(AssignmentState state, GroupSlot slot, Team assignedTeam) {
        for (GroupSlot s : state.getUnassignedSlots()) {
            if (s.getGroupName().equals(slot.getGroupName())) {
                state.removeIfFromDomain(s, t -> t.getContinents().stream()
                        .anyMatch(continent -> assignedTeam.getContinents().contains(continent)));
            }
        }
    }
}

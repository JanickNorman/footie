package com.example.footie.newSimulator.constraint;

import java.util.Map;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;


public class NoSameContinentInGroup implements Constraint {

    @Override
    public boolean isAssignmentAllowed(AssignmentState state, GroupSlot slot, Team team) {
        for (Map.Entry<GroupSlot, Team> entry : state.getAssignments().entrySet()) {
            GroupSlot assignedSlot = entry.getKey();
            Team assignedTeam = entry.getValue();
            if (assignedSlot.getGroupName().equals(slot.getGroupName())) {
                for (String continent : team.getContinents()) {
                    if (assignedTeam.getContinents().contains(continent)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void forwardCheck(AssignmentState state, GroupSlot slot, Team assignedTeam) {
        for (GroupSlot s : state.getUnassignedSlots()) {
            if (s.getGroupName().equals(slot.getGroupName())) {
                state.getDomains().get(s).removeIf(t -> t.getContinents().stream()
                        .anyMatch(continent -> assignedTeam.getContinents().contains(continent)));
            }
        }
    }
}

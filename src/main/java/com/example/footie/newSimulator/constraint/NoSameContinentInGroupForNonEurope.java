package com.example.footie.newSimulator.constraint;

import java.util.Map;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;

/** Prevent two teams from the same non-European continent being in the same group. */
public class NoSameContinentInGroupForNonEurope implements Constraint {

    @Override
    public boolean isAssignmentAllowed(AssignmentState state, GroupSlot slot, Team team) {
        // For each non-European continent the candidate has, ensure it's not
        // already present in the group's assignments.
        for (String continent : team.getContinents()) {
            if ("Europe".equalsIgnoreCase(continent)) continue; // ignore Europe here

            for (Map.Entry<GroupSlot, Team> e : state.getAssignments().entrySet()) {
                GroupSlot gs = e.getKey();
                Team assigned = e.getValue();
                if (assigned == null) continue;
                if (!gs.getGroupName().equals(slot.getGroupName())) continue;
                if (assigned.getContinents().stream().anyMatch(c -> c.equalsIgnoreCase(continent))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void forwardCheck(AssignmentState state, GroupSlot slot, Team assignedTeam) {
        // For each continent present in the group (excluding Europe), remove
        // candidates from that continent from other unassigned slots in the group.
        java.util.Set<String> present = new java.util.HashSet<>();
        for (Map.Entry<GroupSlot, Team> e : state.getAssignments().entrySet()) {
            GroupSlot gs = e.getKey();
            Team assigned = e.getValue();
            if (assigned == null) continue;
            if (!gs.getGroupName().equals(slot.getGroupName())) continue;
            for (String c : assigned.getContinents()) {
                if (!"Europe".equalsIgnoreCase(c)) present.add(c.toLowerCase());
            }
        }

        for (GroupSlot s : state.getUnassignedSlots()) {
                if (!s.getGroupName().equals(slot.getGroupName())) continue;
                state.removeIfFromDomain(s, candidate -> candidate.getContinents().stream()
                    .anyMatch(c -> present.contains(c.toLowerCase())));
        }
    }
}

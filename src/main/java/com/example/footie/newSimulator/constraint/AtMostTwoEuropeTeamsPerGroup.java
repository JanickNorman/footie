package com.example.footie.newSimulator.constraint;

import java.util.Map;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;

/** Allow at most two European teams per group. */
public class AtMostTwoEuropeTeamsPerGroup implements Constraint {

    @Override
    public boolean isAssignmentAllowed(AssignmentState state, GroupSlot slot, Team team) {
        // If candidate isn't European, this constraint doesn't restrict it
        boolean candidateIsEurope = team.getContinents().stream().anyMatch(c -> "Europe".equalsIgnoreCase(c));
        if (!candidateIsEurope) return true;

        // Count existing Europeans in the target group
        int europeCount = 0;
        for (Map.Entry<GroupSlot, Team> e : state.getAssignments().entrySet()) {
            GroupSlot gs = e.getKey();
            Team assigned = e.getValue();
            if (assigned == null) continue;
            if (!gs.getGroupName().equals(slot.getGroupName())) continue;
            if (assigned.getContinents().stream().anyMatch(c -> "Europe".equalsIgnoreCase(c))) {
                europeCount++;
            }
        }

        return europeCount < 2; // allow if less than 2 currently
    }

    @Override
    public void forwardCheck(AssignmentState state, GroupSlot slot, Team assignedTeam) {
        // Recompute Europe count for the group after this assignment
        int europeCount = 0;
        for (Map.Entry<GroupSlot, Team> e : state.getAssignments().entrySet()) {
            GroupSlot gs = e.getKey();
            Team assigned = e.getValue();
            if (assigned == null) continue;
            if (!gs.getGroupName().equals(slot.getGroupName())) continue;
            if (assigned.getContinents().stream().anyMatch(c -> "Europe".equalsIgnoreCase(c))) {
                europeCount++;
            }
        }

        for (GroupSlot s : state.getUnassignedSlots()) {
            if (!s.getGroupName().equals(slot.getGroupName())) continue;
            // If group already has 2 Europeans, remove European candidates
            if (europeCount >= 2) {
                state.removeIfFromDomain(s, candidate -> candidate.getContinents().stream()
                        .anyMatch(c -> "Europe".equalsIgnoreCase(c)));
            }
        }
    }
}

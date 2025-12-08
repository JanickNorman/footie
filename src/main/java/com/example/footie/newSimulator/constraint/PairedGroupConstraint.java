package com.example.footie.newSimulator.constraint;

import java.util.Set;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;

/**
 * Enforces a paired-group relationship between two teams:
 * - if teamA is assigned to one of groupsA then teamB must be assigned to one
 * of groupsB
 * - if symmetric=true the reverse is also enforced (teamB in groupsB implies
 * teamA in groupsA)
 */
public class PairedGroupConstraint implements Constraint {
    private final String teamA;
    private final Set<String> groupsA;
    private final String teamB;
    private final Set<String> groupsB;
    private final boolean symmetric;

    public PairedGroupConstraint(String teamA, Set<String> groupsA, String teamB, Set<String> groupsB,
            boolean symmetric) {
        this.teamA = teamA;
        this.groupsA = groupsA;
        this.teamB = teamB;
        this.groupsB = groupsB;
        this.symmetric = symmetric;
    }

    @Override
    public boolean isAssignmentAllowed(AssignmentState state, GroupSlot slot, Team team) {
        String group = slot.getGroupName();

        // Helper to test one direction
        if (team.getName().equals(teamA)) {
            boolean inA = groupsA.contains(group);
            Team partner = findAssigned(state, teamB);
            if (partner != null) {
                boolean partnerInB = groupsB.contains(findGroupOf(state, teamB));
                if (inA && !partnerInB)
                    return false;
                if (symmetric && partnerInB && !inA)
                    return false;
            }
            return true;
        }

        if (team.getName().equals(teamB)) {
            boolean inB = groupsB.contains(group);
            Team partner = findAssigned(state, teamA);
            if (partner != null) {
                boolean partnerInA = groupsA.contains(findGroupOf(state, teamA));
                if (inB && !partnerInA)
                    return false;
                if (symmetric && partnerInA && !inB)
                    return false;
            }
            return true;
        }

        // Not relevant for other teams
        return true;
    }

    @Override
    public void forwardCheck(AssignmentState state, GroupSlot slot, Team team) {
        // If teamA was placed into groupsA, prune teamB to groupsB; and vice-versa when
        // symmetric
        if (team.getName().equals(teamA)) {
            if (groupsA.contains(slot.getGroupName())) {
                // allow teamB only in groupsB
                pruneTeamToGroups(state, teamB, groupsB);
            }
        }

        if (team.getName().equals(teamB)) {
            if (groupsB.contains(slot.getGroupName())) {
                pruneTeamToGroups(state, teamA, groupsA);
            }
        }
    }

    private Team findAssigned(AssignmentState state, String teamName) {
        for (var e : state.getAssignments().entrySet()) {
            Team t = e.getValue();
            if (t != null && t.getName().equals(teamName))
                return t;
        }
        return null;
    }

    private String findGroupOf(AssignmentState state, String teamName) {
        for (var e : state.getAssignments().entrySet()) {
            Team t = e.getValue();
            if (t != null && t.getName().equals(teamName))
                return e.getKey().getGroupName();
        }
        return null;
    }

    private void pruneTeamToGroups(AssignmentState state, String teamName, Set<String> allowedGroups) {
        for (GroupSlot s : state.getUnassignedSlots()) {
            state.getDomains().get(s)
                    .removeIf(t -> t.getName().equals(teamName) && !allowedGroups.contains(s.getGroupName()));
        }
    }
}

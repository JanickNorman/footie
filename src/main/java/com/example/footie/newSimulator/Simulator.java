
package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.example.footie.newSimulator.constraint.ConstraintManager;

public class Simulator {
    private final List<GroupSlot> drawOrder;
    private final AssignmentState state;
    private final ConstraintManager constraintManager;
    private final List<Team> teams;

    public Simulator(List<GroupSlot> slots, ConstraintManager cm, List<Team> teams) {
        this.drawOrder = slots;
        this.state = new AssignmentState(slots, teams);
        this.constraintManager = cm;
        this.teams = new ArrayList<>(teams);
    }

    public AssignmentState getState() {
        return state;
    }

    public boolean assignTeamToSlot(GroupSlot slot, Team team) {
        StringBuilder reason = new StringBuilder();
        if (!constraintManager.isAssignmentValid(state, slot, team, reason)) {
            System.out.println("Assignment FAILED: " + slot + " -> " + team + "; reason=" + (reason.length() > 0 ? reason.toString() : "unknown"));
            return false;
        }

        // Save original domains for backtracking
        Map<GroupSlot, Set<Team>> oldDomains = deepCopyDomains();
 
        state.assign(slot, team);
        constraintManager.forwardCheck(state, slot, team);
        System.out.println("Try assigning: " + slot + " -> " + team + "; running forward-check");

        // Check for dead ends (any domain empty)
        for (GroupSlot s : state.getUnassignedSlots()) {
            if (state.getDomains().get(s).isEmpty()) {
                // Report which slot hit an empty domain
                System.out.println("Assignment caused dead end: domain emptied for slot " + s + " after assigning " + team + " to " + slot);
                // Restore domains and unassign
                for (GroupSlot restoreSlot : oldDomains.keySet()) {
                    state.getDomains().put(restoreSlot, oldDomains.get(restoreSlot));
                }
                // Restore the slot's original domain from oldDomains rather than
                // overwriting it with the singleton we just tried.
                List<Team> originalDomainForSlot = new ArrayList<>(oldDomains.get(slot));
                state.unassign(slot, originalDomainForSlot);
                System.out.println("❌ Unassigned (backtracked): " + slot + " <- " + team);
                return false;
            }
        }

        // Success
        System.out.println("✅ Assignment SUCCEEDED: " + slot + " -> " + team);

        return true;
    }

    private Map<GroupSlot, Set<Team>> deepCopyDomains() {
        Map<GroupSlot, Set<Team>> oldDomains = new HashMap<>();
        for (GroupSlot s : state.getUnassignedSlots()) {
            oldDomains.put(s, new HashSet<>(state.getDomains().get(s)));
        }
        return oldDomains;
    }

    public void shuffleAndAssignAll() {
        Random rand = new Random();
        for (GroupSlot slot : drawOrder) {
            List<Team> candidates = new ArrayList<>(state.getDomains().get(slot));
            Collections.shuffle(candidates);
            boolean assigned = false;
            for (Team t : candidates) {
                if (assignTeamToSlot(slot, t)) {
                    assigned = true;
                    break;
                }
            }
            if (!assigned) {
                System.out.println("Failed to assign a team to slot " + slot);
            }
        }
    }

    public void printAssignments() {
        System.out.println("Assignments:");
        state.getAssignments()
        .forEach((slot, team) -> 
            System.out.println(slot + " -> " + (team != null ? team : "-"))
        );
    }

    /**
     * Assign teams by iterating teams first, then picking an available slot for each team.
     * For each team we try slots in the configured draw order where the team is allowed
     * (present in the slot's current domain). This implements the "team-first" strategy
     * requested by the user: try to place a team into the earliest possible slot.
     */
    public void assignTeamsFirst() {
        for (Team team : teams) {
            boolean placed = false;
            System.out.println("Attempting to place team: " + team);
            for (GroupSlot slot : drawOrder) {
                if (state.isAssigned(slot)) continue;
                Set<Team> domain = state.getDomains().get(slot);
                if (domain == null) continue;
                if (!domain.contains(team)) {
                    // Not allowed by domain/constraints — print small note and continue
                    StringBuilder reason = new StringBuilder();
                    if (!constraintManager.isAssignmentValid(state, slot, team, reason)) {
                        System.out.println("  Cannot place " + team + " into " + slot + " -> constraint: " + (reason.length() > 0 ? reason.toString() : "unknown"));
                    } else {
                        System.out.println("  Not in domain for " + slot + " (may have been pruned)");
                    }
                    continue;
                }

                // Try assigning to this slot
                System.out.println("  Trying slot " + slot + " for team " + team);
                // instead assigning to slot, just assign it to group
                if (assignTeamToGroup(slot.getGroupName(), team)) {
                    placed = true;  
                    break;
                } else {
                    System.out.println("  Failed to place " + team + " into " + slot + ", trying next slot");
                }
            }
            if (!placed) {
                System.out.println("Could not place team: " + team + " into any slot");
            }
        }
    }

    public boolean assignTeamToGroup(String groupName, Team team) {
        // iterate by group name first, then by position
        drawOrder.sort((a, b) -> {
            int groupComp = a.getGroupName().compareTo(b.getGroupName());
            if (groupComp != 0) return groupComp;
            return Integer.compare(a.getPosition(), b.getPosition());
        });

        for (GroupSlot slot : drawOrder) {
            if (slot.getGroupName().equals(groupName) && !state.isAssigned(slot)) {
                return assignTeamToSlot(slot, team);
            }
        }
        return false;
    }

    public boolean assignByGroupSequentially(String teamName, int position) {
        for (GroupSlot slot : drawOrder) {
            if (slot.getPosition() == position && !state.isAssigned(slot)) {
                for (Team t : teams) {
                    if (t.getName().equals(teamName)) {
                        assignTeamToSlot(slot, t);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}


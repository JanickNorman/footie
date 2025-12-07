/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AssignmentState {
    private final Map<GroupSlot, Team> assignments = new HashMap<>();
    private final Map<GroupSlot, Set<Team>> domains = new HashMap<>();

    public AssignmentState(List<GroupSlot> slots, List<Team> teams) {
        for (GroupSlot slot : slots) {
            assignments.put(slot, null);
            domains.put(slot, new HashSet<>(teams));
        }
    }

    public void assign(GroupSlot slot, Team team) {
        assignments.put(slot, team);
        domains.get(slot).clear(); // Slot is now assigned
        domains.put(slot, new HashSet<>(Collections.singleton(team)));
    }

    public void unassign(GroupSlot slot, List<Team> originalDomain) {
        assignments.put(slot, null);
        domains.put(slot, new HashSet<>(originalDomain));
    }

    public boolean isAssigned(GroupSlot slot) {
        return assignments.get(slot) != null;
    }

    public Team getAssigned(GroupSlot slot) {
        return assignments.get(slot);
    }

    public Map<GroupSlot, Team> getAssignments() { return assignments; }
    public Map<GroupSlot, Set<Team>> getDomains() { return domains; }

    public Set<Team> getDomain(GroupSlot slot) {
        return domains.get(slot);
    }

    public List<GroupSlot> getUnassignedSlots() {
        List<GroupSlot> unassigned = new ArrayList<>();
        for (Map.Entry<GroupSlot, Team> entry : assignments.entrySet()) {
            if (entry.getValue() == null) unassigned.add(entry.getKey());
        }
        return unassigned;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<GroupSlot, Team> entry : assignments.entrySet()) {
            sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}

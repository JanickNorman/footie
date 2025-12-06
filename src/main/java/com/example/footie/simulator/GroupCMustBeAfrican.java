package com.example.footie.simulator;

import java.util.Map;
import java.util.Set;

public class GroupCMustBeAfrican implements Constraint {

    @Override
    public boolean isConsistent(String slot, Team team, Map<String, Team> assignments, Map<String, Set<Team>> domains) {
        // If the slot is in group C, the team must be from Africa
        if (slot.charAt(0) == 'C') {
            return "Africa".equalsIgnoreCase(team.getContinent());
        }
        return true;
    }

    @Override
    public boolean isSatisfied(Map<String, Team> assignments) {
        for (Map.Entry<String, Team> e : assignments.entrySet()) {
            String slot = e.getKey();
            Team t = e.getValue();
            if (slot.charAt(0) == 'C' && t != null) {
                if (!"Africa".equalsIgnoreCase(t.getContinent())) return false;
            }
        }
        return true;
    }
}

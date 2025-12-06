package com.example.footie.simulator;

import java.util.Map;
import java.util.Set;

public class NoSameContinentConstraint implements Constraint {

    @Override
    public boolean isConsistent(String slot, Team team, Map<String, Team> assignments, Map<String, Set<Team>> domains) {
        // groups are identified by the first character of the slot (A1, B1, C1)
        char group = slot.charAt(0);
        for (Map.Entry<String, Team> e : assignments.entrySet()) {
            String otherSlot = e.getKey();
            Team otherTeam = e.getValue();
            if (otherSlot.charAt(0) == group) {
                if (otherTeam != null && otherTeam.getContinent().equalsIgnoreCase(team.getContinent())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean isSatisfied(Map<String, Team> assignments) {
        // ensure no two teams in same group share a continent
        for (Map.Entry<String, Team> e1 : assignments.entrySet()) {
            for (Map.Entry<String, Team> e2 : assignments.entrySet()) {
                String s1 = e1.getKey();
                String s2 = e2.getKey();
                if (s1.equals(s2)) continue;
                if (s1.charAt(0) == s2.charAt(0)) {
                    Team t1 = e1.getValue();
                    Team t2 = e2.getValue();
                    if (t1 != null && t2 != null && t1.getContinent().equalsIgnoreCase(t2.getContinent())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

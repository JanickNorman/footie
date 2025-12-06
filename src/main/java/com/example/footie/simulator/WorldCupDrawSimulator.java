package com.example.footie.simulator;

import java.util.*;

public class WorldCupDrawSimulator {

    private Map<String, Set<Team>> domains;
    private List<Constraint> constraints;
    private Map<String, Team> assignments = new HashMap<>();

    public WorldCupDrawSimulator(Map<String, Set<Team>> domains, List<Constraint> constraints) {
        this.domains = domains;
        this.constraints = constraints;
    }

    public boolean assignTeamSequentially(Team team) {

        // Try groups A, B, C in order:
        List<String> slots = Arrays.asList("A1", "B1", "C1");

        for (String slot : slots) {

            if (assignments.containsKey(slot)) continue; // slot occupied

            System.out.println("Trying to put " + team.getName() + " in " + slot + "...");

            // Save for rollback
            Map<String, Set<Team>> savedDomains = deepCopy(domains);

            // Tentatively assign
            assignments.put(slot, team);
            domains.get(slot).clear();
            domains.get(slot).add(team);

            // Run constraint propagation (AC-3)
            boolean consistent = AC3.run(domains, constraints, assignments);

            if (consistent) {
                System.out.println("✔ Assigned " + team.getName() + " to " + slot);
                return true;
            }

            // If inconsistent → rollback
            System.out.println("✘ Cannot place " + team.getName() + " in " + slot);
            assignments.remove(slot);
            domains = savedDomains;
        }

        return false; // No valid group found
    }

    private Map<String, Set<Team>> deepCopy(Map<String, Set<Team>> original) {
        Map<String, Set<Team>> copy = new HashMap<>();
        for (Map.Entry<String, Set<Team>> e : original.entrySet()) {
            copy.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        return copy;
    }

    public Map<String, Team> getAssignments() {
        return assignments;
    }
}

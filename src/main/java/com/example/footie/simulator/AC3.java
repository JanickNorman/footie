package com.example.footie.simulator;

import java.util.*;

public class AC3 {

    /**
     * Simple constraint propagation: remove domain values that are inconsistent
     * with the provided constraints given current partial assignments.
     * Returns true if domains remain non-empty and no constraint is violated.
     */
    public static boolean run(Map<String, Set<Team>> domains, List<Constraint> constraints, Map<String, Team> assignments) {
        boolean changed;
        do {
            changed = false;
            for (String var : new ArrayList<>(domains.keySet())) {
                Set<Team> domain = domains.get(var);
                Iterator<Team> it = domain.iterator();
                while (it.hasNext()) {
                    Team candidate = it.next();
                    // If there's already an assignment for var and it's different, candidate is invalid
                    if (assignments.containsKey(var) && assignments.get(var) != candidate) {
                        it.remove();
                        changed = true;
                        continue;
                    }

                    // Build a tentative assignment map including this candidate for var
                    Map<String, Team> tentative = new HashMap<>(assignments);
                    tentative.put(var, candidate);

                    // Check all constraints: the candidate must be consistent
                    boolean ok = true;
                    for (Constraint c : constraints) {
                        if (!c.isConsistent(var, candidate, assignments, domains)) {
                            ok = false;
                            break;
                        }
                        // Additionally, check tentative against already assigned slots via constraint
                        if (!c.isSatisfied(tentative)) {
                            ok = false;
                            break;
                        }
                    }

                    if (!ok) {
                        it.remove();
                        changed = true;
                    }
                }

                if (domains.get(var).isEmpty()) {
                    return false; // failure
                }
            }
        } while (changed);

        // Finally ensure current assignments do not violate any constraint
        for (Constraint c : constraints) {
            if (!c.isSatisfied(assignments)) return false;
        }

        return true;
    }
}

package com.example.footie.simulator;

import java.util.*;

public class ForwardChecking {

    /**
     * Forward Checking: After assigning a variable, prune values from domains
     * of unassigned variables that would violate constraints with the new assignment.
     * 
     * @param assignedVar The variable just assigned
     * @param assignedTeam The team assigned to the variable
     * @param domains Current domains for all variables
     * @param constraints List of constraints to check
     * @param assignments Current partial assignment
     * @return true if all domains remain non-empty after pruning, false otherwise
     */
    public static boolean check(String assignedVar, Team assignedTeam, 
                                 Map<String, Set<Team>> domains, 
                                 List<Constraint> constraints, 
                                 Map<String, Team> assignments) {
        
        // For each unassigned variable, prune its domain
        for (String var : domains.keySet()) {
            if (assignments.containsKey(var)) continue; // skip assigned variables
            
            Set<Team> domain = domains.get(var);
            Iterator<Team> it = domain.iterator();
            
            while (it.hasNext()) {
                Team candidate = it.next();
                
                // Create tentative assignment with this candidate
                Map<String, Team> tentative = new HashMap<>(assignments);
                tentative.put(var, candidate);
                
                // Check if this candidate violates any constraint with the new assignment
                boolean valid = true;
                for (Constraint c : constraints) {
                    // Check if the candidate is consistent with the newly assigned variable
                    if (!c.isConsistent(var, candidate, assignments, domains)) {
                        valid = false;
                        break;
                    }
                    // Check if adding this candidate would violate the constraint
                    if (!c.isSatisfied(tentative)) {
                        valid = false;
                        break;
                    }
                }
                
                if (!valid) {
                    it.remove(); // prune this value from domain
                }
            }
            
            // If any domain becomes empty, forward checking failed
            if (domain.isEmpty()) {
                return false;
            }
        }
        
        // Check that current assignments satisfy all constraints
        for (Constraint c : constraints) {
            if (!c.isSatisfied(assignments)) {
                return false;
            }
        }
        
        return true;
    }
}

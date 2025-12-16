package com.example.footie.newSimulator.constraint;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;

/**
 * Enforces bracket separation for top 4 seeds in World Cup 2026:
 * - Quarters: ABC (Q1), DEF (Q2), GHI (Q3), JKL (Q4)
 * - Halves: Q1+Q2 vs Q3+Q4
 * - Seeds 1-2 must be in opposite halves (can only meet in final)
 * - Seeds 3-4 must be in opposite halves (can only meet in final)
 * - All four seeds must be in different quarters (earliest meeting is semi-final)
 */
public class TopSeedsBracketSeparation implements Constraint {
    
    // Bracket structure
    private static final Set<String> QUARTER_1 = Set.of("E", "I", "F");
    private static final Set<String> QUARTER_2 = Set.of("H", "D", "G");
    private static final Set<String> QUARTER_3 = Set.of("C", "A", "L");
    private static final Set<String> QUARTER_4 = Set.of("J", "B", "K");
    
    private static final Set<String> HALF_1 = Set.of("E", "I", "F", "H", "D", "G");
    private static final Set<String> HALF_2 = Set.of("C", "A", "L", "J", "B", "K");

    // Pot 1 position rankings (assuming pot 1 teams are ranked 1-12, with top 4 being seeds 1-4)
    private final Map<String, Integer> teamRankings;

    public TopSeedsBracketSeparation(Map<String, Integer> teamRankings) {
        this.teamRankings = teamRankings;
    }

    @Override
    public boolean isAssignmentAllowed(AssignmentState state, GroupSlot slot, Team team) {
        Integer rank = teamRankings.get(team.getName());
        if (rank == null || rank > 4) {
            return true; // Only applies to top 4 seeds
        }

        String group = slot.getGroupName();
        int quarter = getQuarter(group);
        int half = getHalf(group);

        // Check constraints against already-assigned top seeds
        for (Map.Entry<GroupSlot, Team> entry : state.getAssignments().entrySet()) {
            if (entry.getValue() == null) continue;
            
            Integer otherRank = teamRankings.get(entry.getValue().getName());
            if (otherRank == null || otherRank > 4) continue;
            
            String otherGroup = entry.getKey().getGroupName();
            int otherQuarter = getQuarter(otherGroup);
            int otherHalf = getHalf(otherGroup);

            // All top 4 seeds must be in different quarters
            if (quarter == otherQuarter) {
                return false;
            }

            // Seeds 1-2 must be in opposite halves
            if ((rank <= 2 && otherRank <= 2) && half == otherHalf) {
                return false;
            }

            // Seeds 3-4 must be in opposite halves
            if ((rank >= 3 && rank <= 4) && (otherRank >= 3 && otherRank <= 4) && half == otherHalf) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void forwardCheck(AssignmentState state, GroupSlot slot, Team team) {
        Integer rank = teamRankings.get(team.getName());
        if (rank == null || rank > 4) {
            return; // Only applies to top 4 seeds
        }

        String assignedGroup = slot.getGroupName();
        int assignedQuarter = getQuarter(assignedGroup);
        int assignedHalf = getHalf(assignedGroup);

        // Prune domains of other top seeds based on this assignment
        for (String otherTeamName : teamRankings.keySet()) {
            Integer otherRank = teamRankings.get(otherTeamName);
            if (otherRank == null || otherRank > 4 || otherTeamName.equals(team.getName())) {
                continue;
            }

            if (state.isTeamAssigned(state.getAllTeams().get(otherTeamName))) {
                continue; // Already assigned
            }

            Set<String> forbiddenGroups = new HashSet<>();

            // Remove same quarter (all top 4 must be in different quarters)
            forbiddenGroups.addAll(getQuarterGroups(assignedQuarter));

            // If this is seed 1 or 2, and other is seed 1 or 2, remove same half
            if (rank <= 2 && otherRank <= 2) {
                forbiddenGroups.addAll(getHalfGroups(assignedHalf));
            }

            // If this is seed 3 or 4, and other is seed 3 or 4, remove same half
            if (rank >= 3 && rank <= 4 && otherRank >= 3 && otherRank <= 4) {
                forbiddenGroups.addAll(getHalfGroups(assignedHalf));
            }

            // Prune domains
            for (GroupSlot s : state.getUnassignedSlots()) {
                if (forbiddenGroups.contains(s.getGroupName())) {
                    state.removeTeamFromDomain(s, otherTeamName);
                }
            }
        }
    }

    private int getQuarter(String group) {
        if (QUARTER_1.contains(group)) return 1;
        if (QUARTER_2.contains(group)) return 2;
        if (QUARTER_3.contains(group)) return 3;
        if (QUARTER_4.contains(group)) return 4;
        return -1;
    }

    private int getHalf(String group) {
        if (HALF_1.contains(group)) return 1;
        if (HALF_2.contains(group)) return 2;
        return -1;
    }

    private Set<String> getQuarterGroups(int quarter) {
        return switch (quarter) {
            case 1 -> QUARTER_1;
            case 2 -> QUARTER_2;
            case 3 -> QUARTER_3;
            case 4 -> QUARTER_4;
            default -> Set.of();
        };
    }

    private Set<String> getHalfGroups(int half) {
        return half == 1 ? HALF_1 : HALF_2;
    }
}

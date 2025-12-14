package com.example.footie.newSimulator.constraint;

import java.util.Set;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;

/**
 * If a trigger team is placed in one of triggerGroups then at least one of
 * requiredTeams must be placed in requiredGroups. This class prunes domains
 * of required teams to requiredGroups when the trigger is placed.
 */
public class TriggerAnyTeamInGroupsConstraint implements Constraint {
    private final String triggerTeam;
    private final Set<String> triggerGroups;
    private final Set<String> requiredTeams;
    private final Set<String> requiredGroups;

    public TriggerAnyTeamInGroupsConstraint(String triggerTeam, Set<String> triggerGroups, Set<String> requiredTeams,
            Set<String> requiredGroups) {
        this.triggerTeam = triggerTeam;
        this.triggerGroups = triggerGroups;
        this.requiredTeams = requiredTeams;
        this.requiredGroups = requiredGroups;
    }

    @Override
    public boolean isAssignmentAllowed(AssignmentState state, GroupSlot slot, Team team) {
        String group = slot.getGroupName();

        // If assigning a required team while trigger already placed in triggerGroups,
        // that required team must be assigned into requiredGroups.
        if (requiredTeams.contains(team.getName())) {
            String triggerGroup = findGroupOf(state, triggerTeam);
            if (triggerGroup != null && triggerGroups.contains(triggerGroup)) {
                // trigger active -> required team must be in requiredGroups
                if (!requiredGroups.contains(group))
                    return false;
            }
        }

        // Assigning trigger team is allowed; forwardCheck will prune required teams'
        // domains
        return true;
    }

    @Override
    public void forwardCheck(AssignmentState state, GroupSlot slot, Team team) {
        // If trigger team assigned into triggerGroups, prune domains of requiredTeams
        if (!team.getName().equals(triggerTeam))
            return;
        if (!triggerGroups.contains(slot.getGroupName()))
            return;

        for (GroupSlot s : state.getUnassignedSlots()) {
            state.getDomains().get(s)
                    ;
                    state.removeIfFromDomain(s, t -> requiredTeams.contains(t.getName()) && !requiredGroups.contains(s.getGroupName()));
        }
    }

    private String findGroupOf(AssignmentState state, String teamName) {
        for (var e : state.getAssignments().entrySet()) {
            Team t = e.getValue();
            if (t != null && t.getName().equals(teamName))
                return e.getKey().getGroupName();
        }
        return null;
    }
}

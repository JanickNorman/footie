package com.example.footie.newSimulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AssignmentSnapshot {
    private final AssignmentState state;
    private final Map<GroupSlot, Set<Team>> domainsSnapshot;
    private final Map<GroupSlot, Team> assignmentsSnapshot;

    public AssignmentSnapshot(AssignmentState state, Map<GroupSlot, Set<Team>> domainsSnapshot,
            Map<GroupSlot, Team> assignmentsSnapshot) {
        this.state = state;
        // defensive copies
        this.domainsSnapshot = new HashMap<>();
        for (Map.Entry<GroupSlot, Set<Team>> e : domainsSnapshot.entrySet()) {
            this.domainsSnapshot.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        this.assignmentsSnapshot = new HashMap<>(assignmentsSnapshot);
    }

    /**
     * Construct a snapshot by copying data from the provided state.
     */
    public AssignmentSnapshot(AssignmentState state) {
        this.state = state;
        this.domainsSnapshot = new HashMap<>();
        for (Map.Entry<GroupSlot, Set<Team>> e : state.getDomains().entrySet()) {
            this.domainsSnapshot.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        this.assignmentsSnapshot = new HashMap<>();
        this.assignmentsSnapshot.putAll(state.getAssignments());
    }

    public Map<GroupSlot, Set<Team>> getDomainsSnapshot() {
        return domainsSnapshot;
    }

    public Map<GroupSlot, Team> getAssignmentsSnapshot() {
        return assignmentsSnapshot;
    }

    /**
     * Restore the saved assignments and domains into the original state.
     */
    public void restore() {
        // restore assignments first
        state.getAssignments().clear();
        state.getAssignments().putAll(assignmentsSnapshot);
        // restore domains and rebuild indexes
        state.restoreDomains(domainsSnapshot);
    }
}

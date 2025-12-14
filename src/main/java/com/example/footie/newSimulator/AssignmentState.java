/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AssignmentState {
    private final List<GroupSlot> slots;
    private final Map<String, Team> allTeams;
    private final SortedMap<GroupSlot, Team> assignments = new TreeMap<>();
    private final SortedMap<GroupSlot, Set<Team>> domains = new TreeMap<>();

    // fast lookup structures
    private final Set<String> unassignedTeamNames = new HashSet<>();
    private final Map<String, Set<GroupSlot>> teamToCandidateSlots = new HashMap<>();
    private final Map<String, Team> currentPotTeams = new HashMap<>();

    public AssignmentState(List<GroupSlot> slots, List<Team> teams) {
        this.slots = new ArrayList<>(slots);
        this.allTeams = teams.stream().collect(Collectors.toMap(Team::getName, t -> t));

        for (GroupSlot slot : slots) {
            assignments.put(slot, null);
            domains.put(slot, new HashSet<>(teams));
        }
        // initialize indexes
        for (Team t : teams) {
            unassignedTeamNames.add(t.getName());
            teamToCandidateSlots.put(t.getName(), new HashSet<>());
        }
        // each slot initially can accept all teams
        for (GroupSlot s : slots) {
            for (Team t : teams) {
                teamToCandidateSlots.get(t.getName()).add(s);
            }
        }
    }

    public AssignmentState copyForSearch() {
        return new AssignmentState(getSlots(), getAllTeams().values().stream().collect(Collectors.toList()));
    }

    public List<GroupSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    public Map<String, Team> getAllTeams() {
        return Collections.unmodifiableMap(allTeams);
    }

    public void assign(GroupSlot slot, Team team) {
        assignments.put(slot, team);
        // update domains/indexes: slot domain becomes the singleton team
        Set<Team> old = domains.get(slot);
        if (old != null) {
            for (Team t : new HashSet<>(old)) {
                if (!t.getName().equals(team.getName())) {
                    // remove slot from candidate list of t
                    Set<GroupSlot> sset = teamToCandidateSlots.get(t.getName());
                    if (sset != null) sset.remove(slot);
                }
            }
        }
        domains.put(slot, new HashSet<>(Collections.singleton(team)));
        // mark team as assigned
        unassignedTeamNames.remove(team.getName());
    }

    public void unassign(GroupSlot slot, List<Team> originalDomain) {
        assignments.put(slot, null);
        // restore domain
        Set<Team> newDom = new HashSet<>(originalDomain);
        domains.put(slot, newDom);
        // update candidate index
        for (Team t : newDom) {
            teamToCandidateSlots.computeIfAbsent(t.getName(), k -> new HashSet<>()).add(slot);
            unassignedTeamNames.add(t.getName());
        }
    }

    public boolean isAssigned(GroupSlot slot) {
        return assignments.get(slot) != null;
    }

    public Team getAssigned(GroupSlot slot) {
        return assignments.get(slot);
    }

    public SortedMap<GroupSlot, Team> getAssignments() {
        return assignments;
    }

    public Map<GroupSlot, Set<Team>> getDomains() {
        return domains;
    }

    public Set<Team> getDomains(String slotString) {
        String group = slotString.substring(0, 1);
        int position = Integer.parseInt(slotString.substring(1));
        GroupSlot targetSlot = new GroupSlot(group, position);
        return domains.get(targetSlot);
    }

    public Set<Team> getDomains(GroupSlot slot) {
        return domains.get(slot);
    }

    public List<GroupSlot> getUnassignedSlots() {
        List<GroupSlot> unassigned = new ArrayList<>();
        for (Map.Entry<GroupSlot, Team> entry : assignments.entrySet()) {
            if (entry.getValue() == null)
                unassigned.add(entry.getKey());
        }
        return unassigned;
    }

    public List<String> getNextGroupsToAssign() {
        // Group slots by group name and count how many assignments each group already
        // has.
        Map<String, Integer> counts = assignments.keySet().stream()
                .collect(Collectors.groupingBy(GroupSlot::getGroupName,
                        Collectors.summingInt(s -> assignments.get(s) != null ? 1 : 0)));

        // Find minimum assigned count (default to 0 when no groups present)
        int min = counts.values().stream().min((o1, o2) -> Integer.compare(o1, o2)).orElse(0);

        // Return alphabetically sorted group names that have the minimal count
        return counts.entrySet().stream()
                .filter(e -> e.getValue() == min)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<GroupSlot> nextSlotsToTry() {
        // use TreeMap next
        SortedMap<String, List<GroupSlot>> nextGroups = new TreeMap<>();
        for (GroupSlot slot : this.getUnassignedSlots()) {
            nextGroups.putIfAbsent(slot.getGroupName(), new ArrayList<>());
            nextGroups.get(slot.getGroupName()).add(slot);
        }

        // find minimal group size and collect groups with that size using streams
        int maxSize = nextGroups.values().stream().mapToInt(List::size).max().orElse(0);
        List<GroupSlot> slotsToTry = nextGroups.entrySet().stream()
                .filter(e -> e.getValue().size() == maxSize)
                // i want to flatMap the list of GroupSlot to a single list
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList());

        // slotsToTry.sort(Comparator
                // .comparing((GroupSlot s) -> (int) s.getGroupName().charAt(0) + ((s.getPosition() - 1) % maxSize) * 10));
        slotsToTry.sort(Comparator.comparing((GroupSlot s) ->
        s.getPosition()).thenComparing(s -> s.getGroupName()));

        return slotsToTry;
    }

    public List<GroupSlot> nextSlots() {
        Map<String, PriorityQueue<GroupSlot>> slotsByGroup = getUnassignedSlots().stream()
            .collect(Collectors.groupingBy(GroupSlot::getGroupName, 
                Collectors.toCollection(() -> new PriorityQueue<>(Comparator.comparingInt(GroupSlot::getPosition)))));

        List<GroupSlot> result = new ArrayList<>();
        int maxPosition = slotsByGroup.values().stream()
            .mapToInt(q -> q.size())
            .max().orElse(0);
        
        for (int pos = 0; pos < maxPosition; pos++) {
            for (PriorityQueue<GroupSlot> pq : slotsByGroup.values()) {
                if (!pq.isEmpty()) {
                    result.add(pq.poll());
                }
            }
        }

        return result;

    }

    public List<GroupSlot> nextSlotsByLeastDomainSize() {
        List<GroupSlot> unassigned = getUnassignedSlots();
        unassigned.sort(Comparator.comparingInt(s -> domains.get(s).size()));
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

    public SortedMap<GroupSlot, Set<Team>> getUnassignedDomains() {
        SortedMap<GroupSlot, Set<Team>> result = new TreeMap<>();
        for (Map.Entry<GroupSlot, Team> e : assignments.entrySet()) {
            if (e.getValue() == null) {
                GroupSlot s = e.getKey();
                Set<Team> dom = domains.get(s);
                result.put(s, dom != null ? new HashSet<>(dom) : new HashSet<>());
            }
        }
        return result;
    }

    // --- Index helpers ---
    public boolean isTeamUnassigned(String teamName) {
        return unassignedTeamNames.contains(teamName);
    }

    public Set<GroupSlot> getCandidateSlotsForTeam(String teamName) {
        return teamToCandidateSlots.getOrDefault(teamName, new HashSet<>());
    }

    /** Remove a single team (by name) from a slot's domain and update indexes. */
    public boolean removeTeamFromDomain(GroupSlot slot, String teamName) {
        Set<Team> dom = domains.get(slot);
        if (dom == null) return false;
        boolean removed = dom.removeIf(t -> t.getName().equals(teamName));
        if (removed) {
            Set<GroupSlot> sset = teamToCandidateSlots.get(teamName);
            if (sset != null) sset.remove(slot);
        }
        return removed;
    }

    /** Remove all teams matching a predicate from a slot domain; returns count removed. */
    public int removeIfFromDomain(GroupSlot slot, Predicate<Team> pred) {
        Set<Team> dom = domains.get(slot);
        if (dom == null) return 0;
        int removed = 0;
        for (Team t : new HashSet<>(dom)) {
            if (pred.test(t)) {
                dom.remove(t);
                Set<GroupSlot> sset = teamToCandidateSlots.get(t.getName());
                if (sset != null) sset.remove(slot);
                removed++;
            }
        }
        return removed;
    }

    /** Replace all domains from the provided snapshot and rebuild indexes. */
    public void restoreDomains(Map<GroupSlot, Set<Team>> snapshot) {
        domains.clear();
        for (Map.Entry<GroupSlot, Set<Team>> e : snapshot.entrySet()) {
            domains.put(e.getKey(), new HashSet<>(e.getValue()));
        }
        // rebuild candidate index
        teamToCandidateSlots.clear();
        unassignedTeamNames.clear();
        for (Team t : allTeams.values()) {
            unassignedTeamNames.add(t.getName());
            teamToCandidateSlots.put(t.getName(), new HashSet<>());
        }
        for (Map.Entry<GroupSlot, Set<Team>> e : domains.entrySet()) {
            GroupSlot s = e.getKey();
            for (Team t : e.getValue()) {
                teamToCandidateSlots.computeIfAbsent(t.getName(), k -> new HashSet<>()).add(s);
            }
        }
        // remove assigned teams from unassigned set
        for (Team assigned : assignments.values()) {
            if (assigned != null) unassignedTeamNames.remove(assigned.getName());
        }
    }

    /**
     * Return list of unassigned team names that do not appear in any
     * unassigned slot domain (i.e. cannot be placed anywhere).
     */
    public List<String> findUnassignedTeamsWithNoUnassignedDomain() {
        List<String> missing = new ArrayList<>();
        Set<String> teamsInUnassignedDomains = new HashSet<>();
        for (Map.Entry<GroupSlot, Set<Team>> e : getUnassignedDomains().entrySet()) {
            for (Team t : e.getValue()) {
                teamsInUnassignedDomains.add(t.getName());
            }
        }

        for (String teamName : getAllTeams().keySet()) {
            if (isTeamUnassigned(teamName) && !teamsInUnassignedDomains.contains(teamName)) {
                missing.add(teamName);
            }
        }
        return missing;
    }

    /**
     * More efficient check using candidate index: return unassigned teams
     * that have no candidate unassigned slot.
     */
    public List<String> findUnassignedTeamsWithNoUnassignedCandidateSlot() {
        List<String> missing = new ArrayList<>();
        Set<GroupSlot> unassignedSlots = new HashSet<>(getUnassignedSlots());

        for (String teamName : getAllTeams().keySet()) {
            if (!isTeamUnassigned(teamName)) continue;
            Set<GroupSlot> candidates = getCandidateSlotsForTeam(teamName);
            boolean hasUnassigned = candidates.stream().anyMatch(unassignedSlots::contains);
            if (!hasUnassigned) missing.add(teamName);
        }
        return missing;
    }


    public Map<String, Team> currentPotTeams() {
        return Collections.unmodifiableMap(currentPotTeams);
    }
}

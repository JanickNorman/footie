/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AssignmentState {
    private final List<GroupSlot> slots;
    private final Map<String, Team> allTeams;

    private final SortedMap<GroupSlot, Team> assignments = new TreeMap<>();
    private final SortedMap<GroupSlot, Set<Team>> domains = new TreeMap<>();

    public AssignmentState(List<GroupSlot> slots, List<Team> teams) {
        this.slots = new ArrayList<>(slots);
        this.allTeams = teams.stream().collect(Collectors.toMap(Team::getName, t -> t));

        for (GroupSlot slot : slots) {
            assignments.put(slot, null);
            domains.put(slot, new HashSet<>(teams));
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
}

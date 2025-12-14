package com.example.footie.newSimulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrettyPrinter {

    public static void prettyPrintGroupAssignments(List<GroupSlot> drawOrder, AssignmentState state) {
        // Build a map: groupName -> map(position -> teamName)
        Map<String, Map<Integer, String>> groups = new HashMap<>();
        int maxPosition = 0;
        for (GroupSlot slot : drawOrder) {
            String g = slot.getGroupName();
            groups.putIfAbsent(g, new HashMap<>());
            Team team = state.getAssignments().get(slot);
            groups.get(g).put(slot.getPosition(), team != null ? team.getName() : "-");
            if (slot.getPosition() > maxPosition)
                maxPosition = slot.getPosition();
        }

        // Sort groups by name
        List<String> groupNames = new ArrayList<>(groups.keySet());
        Collections.sort(groupNames);

        // Compute column widths
        Map<String, Integer> colWidth = new HashMap<>();
        int posLabelWidth = Math.max(2, String.valueOf(maxPosition).length());
        for (String g : groupNames) {
            int w = g.length();
            Map<Integer, String> col = groups.get(g);
            for (int p = 1; p <= maxPosition; p++) {
                String cell = col.getOrDefault(p, "-");
                w = Math.max(w, cell.length());
            }
            // add padding
            colWidth.put(g, Math.max(w, 3) + 2);
        }

        // Print header
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%" + posLabelWidth + "s |", "Pos"));
        for (String g : groupNames) {
            int w = colWidth.get(g);
            sb.append(String.format(" %" + (-w) + "s|", "Group " + g));
        }
        System.out.println(sb.toString());

        // Print separator
        StringBuilder sep = new StringBuilder();
        sep.append("".repeat(Math.max(0, posLabelWidth))).append("-+-");
        for (String g : groupNames) {
            int w = colWidth.get(g);
            sep.append("".repeat(Math.max(0, w))).append("-+");
        }
        System.out.println(sep.toString());

        // Print rows by position
        for (int pos = 1; pos <= maxPosition; pos++) {
            StringBuilder row = new StringBuilder();
            row.append(String.format("%" + posLabelWidth + "d |", pos));
            for (String g : groupNames) {
                int w = colWidth.get(g);
                String cell = groups.get(g).getOrDefault(pos, "-");
                if (cell.length() > w - 2)
                    cell = cell.substring(0, w - 5) + "...";
                row.append(String.format(" %" + (-w) + "s|", cell));
            }
            System.out.println(row.toString());
        }
    }

    /**
     * Print groups vertically: one row per group, columns are positions.
     * Example header: "Group | 1 | 2 | 3 | 4"
     */
    public static void prettyPrintGroupAssignmentsVertical(List<GroupSlot> drawOrder, AssignmentState state) {
        // Build a map: groupName -> map(position -> teamName)
        Map<String, Map<Integer, String>> groups = new HashMap<>();
        int maxPosition = 0;
        for (GroupSlot slot : drawOrder) {
            String g = slot.getGroupName();
            groups.putIfAbsent(g, new HashMap<>());
            Team team = state.getAssignments().get(slot);
            groups.get(g).put(slot.getPosition(), team != null ? team.getName() : "-");
            if (slot.getPosition() > maxPosition)
                maxPosition = slot.getPosition();
        }

        // Sort groups by name
        List<String> groupNames = new ArrayList<>(groups.keySet());
        Collections.sort(groupNames);

        // Compute widths
        int groupLabelWidth = Math.max(5, groupNames.stream().mapToInt(String::length).max().orElse(5));
        int cellWidth = 15; // width per position column

        // Header
        StringBuilder header = new StringBuilder();
        header.append(String.format("%-" + (groupLabelWidth + 2) + "s", "Group"));
        for (int p = 1; p <= maxPosition; p++) {
            header.append(String.format(" %" + (-cellWidth) + "s", String.valueOf(p)));
        }
        System.out.println(header.toString());

        // Separator
        StringBuilder sep = new StringBuilder();
        sep.append("".repeat(Math.max(0, groupLabelWidth + 2))).append("-");
        for (int p = 1; p <= maxPosition; p++) {
            sep.append("".repeat(1)).append("".repeat(Math.max(0, cellWidth))).append("-");
        }
        System.out.println(sep.toString());

        // Rows
        for (String g : groupNames) {
            StringBuilder row = new StringBuilder();
            row.append(String.format("%-" + (groupLabelWidth + 2) + "s", "Group " + g));
            Map<Integer, String> col = groups.get(g);
            for (int p = 1; p <= maxPosition; p++) {
                String cell = col.getOrDefault(p, "-");
                if (cell.length() > cellWidth - 3)
                    cell = cell.substring(0, cellWidth - 6) + "...";
                row.append(String.format(" %" + (-cellWidth) + "s", cell));
            }
            System.out.println(row.toString());
        }
    }
}

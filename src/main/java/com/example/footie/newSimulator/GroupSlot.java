package com.example.footie.newSimulator;


public class GroupSlot implements Comparable<GroupSlot> {
    private final String groupName;
    private final int position;

    public GroupSlot(String groupName, int position) { 
        this.groupName = groupName; 
        this.position = position;
    }

    public String getGroupName() { return groupName; }

    public int getPosition() { return position; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupSlot)) return false;
        GroupSlot other = (GroupSlot) o;
        return groupName.equals(other.groupName) && position == other.position;
    }

    @Override
    public String toString() { return groupName + position; }

    @Override
    public int compareTo(GroupSlot other) {
        int groupComp = toString().compareTo(other.toString());
        if (groupComp != 0) return groupComp;
        return Integer.compare(this.position, other.position);
    }
}


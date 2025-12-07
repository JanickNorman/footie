package com.example.footie.newSimulator;

import java.util.Objects;
import java.util.Set;

public class PlaceholderTeam implements Team {
    private final String name;
    private final String continent;
    private final String source; // e.g. "UEFA Playoff 1"

    public PlaceholderTeam(String name, String continent, String source) {
        this.name = name;
        this.continent = continent;
        this.source = source;
    }

    public String getSource() { return source; }

    public String getName() { return name; }

    @Override
    public Set<String> getContinents() { return Set.of(continent); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Team)) return false;
        Team other = (Team) o;
        return Objects.equals(name, other.getName());
    }

    @Override
    public int hashCode() { return Objects.hash(name, continent); }
}

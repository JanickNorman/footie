package com.example.footie.simulator;

import java.util.Objects;

public class Team {
    private final String name;
    private final String continent;

    public Team(String name, String continent) {
        this.name = Objects.requireNonNull(name, "name");
        this.continent = Objects.requireNonNull(continent, "continent");
    }

    public String getName() {
        return name;
    }

    public String getContinent() {
        return continent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return name.equals(team.name) && continent.equals(team.continent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, continent);
    }

    @Override
    public String toString() {
        return name + " (" + continent + ")";
    }
}

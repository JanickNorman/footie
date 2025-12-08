package com.example.footie.newSimulator;

import java.util.Objects;
import java.util.Set;

public class ConcreteTeam implements Team {
    private final String name;
    private final String continent;
    private final int pot;
    
    public ConcreteTeam(String name, String continent, int pot) {
        this.name = name;
        this.continent = continent;
        this.pot = pot;
    }

    @Override
    public int pot() {
        return pot;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getContinents() {
        if (continent == null || continent.isEmpty()) {
            return Set.of();
        }
        return Set.of(continent);
    }

    @Override
    public String toString() {
        return name + "(" + continent + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Team)) return false;
        Team other = (Team) o;
        return Objects.equals(name, other.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

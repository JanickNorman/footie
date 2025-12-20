package com.example.footie.newSimulator;

import java.util.Objects;
import java.util.Set;

public class PlaceholderTeam implements Team {
    private final String name;
    private final Set<String> continents;
    private final String source; // e.g. "UEFA Playoff 1"
    private final int pot;
    private String flagUrl;

    public PlaceholderTeam(String name, Set<String> continents, String source, int pot) {
        this.name = name;
        this.continents = continents;
        this.source = source;
        this.pot = pot;
        this.flagUrl = null;
    }

    public PlaceholderTeam(String name, Set<String> continents, String source, int pot, String flagUrl) {
        this(name, continents, source, pot);
        this.flagUrl = flagUrl;
    }

    public String getSource() { return source; }

    @Override
    public int pot() {
        return pot;
    }

    @Override
    public String getName() { return name; }

    @Override
    public Set<String> getContinents() { return continents; }

    @Override
    public String getFlagUrl() { return flagUrl; }

    @Override
    public void setFlag(String url) {
        this.flagUrl = url;
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
    public int hashCode() { return Objects.hash(name); }

    @Override
    public String toString() {
        return getName();
    }
}

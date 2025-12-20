package com.example.footie.newSimulator;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaceholderTeam implements Team {
    private final String name;
    private final Set<String> continents;
    private final String source; // e.g. "UEFA Playoff 1"
    private final int pot;
    private String flagUrl;
    private List<Team> possibleTeams;

    public PlaceholderTeam(String name, Set<String> continents, String source, int pot) {
        this.name = name;
        this.continents = continents;
        this.source = source;
        this.pot = pot;
        this.flagUrl = null;
        this.possibleTeams = null;
    }

    public PlaceholderTeam(String name, Set<String> continents, String source, int pot, String flagUrl) {
        this(name, continents, source, pot);
        this.flagUrl = flagUrl;
    }

    public PlaceholderTeam(String name, List<Team> teams, String source, int pot, String flagUrl) {
        this(name, teams.stream().map(Team::getContinents).flatMap(Set::stream).collect(Collectors.toSet()), source, pot);
        this.flagUrl = flagUrl;
        this.possibleTeams = teams;
    }

    public List<Team> getPossibleTeams() { return possibleTeams; }

    public String getSource() { return source; }

    public void setPossibleTeams(List<Team> teams) {
        this.possibleTeams = teams;
    }

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
        if (possibleTeams != null) {
            return name + " (" + possibleTeams.stream().map(Team::getName).collect(Collectors.joining(", ")) + ")";
        } else {
        return getName();
        }
    }
}

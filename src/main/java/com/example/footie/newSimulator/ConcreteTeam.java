package com.example.footie.newSimulator;

import java.util.Objects;
import java.util.Set;

public class ConcreteTeam implements Team {
    private final String name;
    private final String continent;
    private final int pot;
    private final String code;
    private String flagUrl;
    
    public ConcreteTeam(String name, String continent, int pot) {
        this(name, continent, pot, null, null);
    }

    public ConcreteTeam(String name, String continent, int pot, String code) {
        this(name, continent, pot, code, null);
    }

    public ConcreteTeam(String name, String continent, int pot, String code, String flagUrl) {
        this.name = name;
        this.continent = continent;
        this.pot = pot;
        this.code = code;
        this.flagUrl = flagUrl;
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

    @Override
    public String getCode() { return code; }

    @Override
    public String getFlagUrl() {
        if (flagUrl != null && !flagUrl.isEmpty()) return flagUrl;
        return code != null ? "/flags/" + code + ".svg" : null;
    }

    @Override
    public void setFlag(String url) {
        flagUrl = url;
    }
}

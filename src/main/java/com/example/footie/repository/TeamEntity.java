package com.example.footie.repository;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("teams")
public class TeamEntity {

    @Id
    private Long id;

    private String name;

    private String code;

    private String continent;

    private Integer pot;

    @Column("flag_url")
    private String flagUrl;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public TeamEntity() {}

    public TeamEntity(String name, String code, String continent, Integer pot, String flagUrl) {
        this.name = name;
        this.code = code;
        this.continent = continent;
        this.pot = pot;
        this.flagUrl = flagUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getContinent() { return continent; }
    public void setContinent(String continent) { this.continent = continent; }

    public Integer getPot() { return pot; }
    public void setPot(Integer pot) { this.pot = pot; }

    public String getFlagUrl() { return flagUrl; }
    public void setFlagUrl(String flagUrl) { this.flagUrl = flagUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

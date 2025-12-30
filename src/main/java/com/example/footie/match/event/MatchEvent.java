package com.example.footie.match.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchEvent {
    private String matchId;
    private String homeTeam;
    private String awayTeam;
    private Integer minute;
    private EventType eventType;
    private String player;
    private String team;
    private String description;
    private Integer homeScore;
    private Integer awayScore;
    private LocalDateTime timestamp;
    private String additionalInfo;
}

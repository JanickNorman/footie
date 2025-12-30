package com.example.footie.service;

import com.example.footie.match.event.EventType;
import com.example.footie.match.event.MatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MatchEventConsumer {

    // Store events by matchId for retrieval
    private final Map<String, List<MatchEvent>> matchEventsStore = new ConcurrentHashMap<>();

    @KafkaListener(topics = "${kafka.topic.match-events:match-events}", groupId = "${spring.kafka.consumer.group-id:match-events-group}")
    public void consumeMatchEvent(MatchEvent event) {
        log.info("Received match event: matchId={}, minute={}, type={}, description={}",
                event.getMatchId(), event.getMinute(), event.getEventType(), event.getDescription());

        // Store the event
        matchEventsStore.computeIfAbsent(event.getMatchId(), k -> new ArrayList<>()).add(event);

        // Handle specific event types
        handleMatchEvent(event);
    }

    private void handleMatchEvent(MatchEvent event) {
        switch (event.getEventType()) {
            case GOAL:
                log.info("⚽ GOAL! {} scored at minute {} for {}. Score: {} - {}",
                        event.getPlayer(), event.getMinute(), event.getTeam(),
                        event.getHomeScore(), event.getAwayScore());
                break;
            case YELLOW_CARD:
                log.info("🟨 Yellow card for {} ({}) at minute {}",
                        event.getPlayer(), event.getTeam(), event.getMinute());
                break;
            case RED_CARD:
                log.info("🟥 Red card for {} ({}) at minute {}",
                        event.getPlayer(), event.getTeam(), event.getMinute());
                break;
            case HALF_TIME:
                log.info("⏸️ Half-time: {} {} - {} {}",
                        event.getHomeTeam(), event.getHomeScore(),
                        event.getAwayScore(), event.getAwayTeam());
                break;
            case FULL_TIME:
                log.info("⏹️ Full-time: {} {} - {} {}",
                        event.getHomeTeam(), event.getHomeScore(),
                        event.getAwayScore(), event.getAwayTeam());
                break;
            case KICKOFF:
                log.info("⚡ Kickoff: {} vs {}", event.getHomeTeam(), event.getAwayTeam());
                break;
            default:
                // Other events already logged above
                break;
        }
    }

    /**
     * Retrieves all events for a specific match
     */
    public List<MatchEvent> getMatchEvents(String matchId) {
        return matchEventsStore.getOrDefault(matchId, new ArrayList<>());
    }

    /**
     * Retrieves all stored events
     */
    public Map<String, List<MatchEvent>> getAllMatchEvents() {
        return new ConcurrentHashMap<>(matchEventsStore);
    }

    /**
     * Clears events for a specific match
     */
    public void clearMatchEvents(String matchId) {
        matchEventsStore.remove(matchId);
        log.info("Cleared events for match: {}", matchId);
    }
}

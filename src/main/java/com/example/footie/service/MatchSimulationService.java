package com.example.footie.service;

import com.example.footie.match.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchSimulationService {

    private final MatchEventProducer matchEventProducer;
    private final Map<String, MatchState> activeMatches = new ConcurrentHashMap<>();
    private final Random random = new Random();

    /**
     * Starts a new match simulation
     */
    public Mono<MatchState> startMatch(String homeTeam, String awayTeam) {
        String matchId = UUID.randomUUID().toString();
        MatchState matchState = MatchState.builder()
                .matchId(matchId)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .homeScore(0)
                .awayScore(0)
                .currentMinute(0)
                .status(MatchStatus.FIRST_HALF)
                .build();

        activeMatches.put(matchId, matchState);

        // Send kickoff event
        MatchEvent kickoff = createEvent(matchState, 0, EventType.KICKOFF,
                null, null, "Match kicked off!");

        return matchEventProducer.sendMatchEvent(kickoff)
                .doOnSuccess(event -> log.info("Match started: {} vs {}", homeTeam, awayTeam))
                .thenReturn(matchState);
    }

    /**
     * Simulates a full match minute by minute
     * Emits events at configurable intervals (e.g., every second represents a
     * minute)
     */
    public Flux<MatchEvent> simulateMatchMinuteByMinute(String matchId, long intervalMillis) {
        MatchState matchState = activeMatches.get(matchId);
        if (matchState == null) {
            return Flux.error(new IllegalArgumentException("Match not found: " + matchId));
        }

        return Flux.interval(Duration.ofMillis(intervalMillis))
                .take(95) // 90 minutes + injury time
                .flatMap(tick -> {
                    int minute = tick.intValue() + 1;
                    matchState.setCurrentMinute(minute);

                    // Update match status based on minute
                    updateMatchStatus(matchState, minute);

                    // Generate random events for this minute
                    List<MatchEvent> events = generateMinuteEvents(matchState, minute);

                    return Flux.fromIterable(events)
                            .flatMap(event -> {
                                matchState.getEvents().add(event);
                                return matchEventProducer.sendMatchEvent(event);
                            });
                })
                .doOnComplete(() -> {
                    matchState.setStatus(MatchStatus.FINISHED);
                    log.info("Match simulation completed: {} - Final Score: {} - {}",
                            matchId, matchState.getHomeScore(), matchState.getAwayScore());
                })
                .doOnError(error -> log.error("Error during match simulation: {}", error.getMessage()));
    }

    /**
     * Simulates an accelerated match (all events generated immediately)
     */
    public Flux<MatchEvent> simulateFullMatch(String matchId) {
        MatchState matchState = activeMatches.get(matchId);
        if (matchState == null) {
            return Flux.error(new IllegalArgumentException("Match not found: " + matchId));
        }

        List<MatchEvent> allEvents = new ArrayList<>();

        for (int minute = 1; minute <= 90; minute++) {
            matchState.setCurrentMinute(minute);
            updateMatchStatus(matchState, minute);
            allEvents.addAll(generateMinuteEvents(matchState, minute));
        }

        matchState.setStatus(MatchStatus.FINISHED);

        return Flux.fromIterable(allEvents)
                .flatMap(event -> {
                    matchState.getEvents().add(event);
                    return matchEventProducer.sendMatchEvent(event);
                })
                .delayElements(Duration.ofMillis(100)); // Small delay between events
    }

    private void updateMatchStatus(MatchState matchState, int minute) {
        if (minute == 1) {
            matchState.setStatus(MatchStatus.FIRST_HALF);
        } else if (minute == 45) {
            matchState.setStatus(MatchStatus.HALF_TIME);
        } else if (minute == 46) {
            matchState.setStatus(MatchStatus.SECOND_HALF);
        } else if (minute >= 90) {
            matchState.setStatus(MatchStatus.FULL_TIME);
        }
    }

    private List<MatchEvent> generateMinuteEvents(MatchState matchState, int minute) {
        List<MatchEvent> events = new ArrayList<>();

        // Special events at fixed times
        if (minute == 45) {
            events.add(createEvent(matchState, minute, EventType.HALF_TIME,
                    null, null, "Half-time"));
        } else if (minute == 90) {
            events.add(createEvent(matchState, minute, EventType.FULL_TIME,
                    null, null, "Full-time"));
        } else {
            // Random event generation
            double eventChance = random.nextDouble();

            if (eventChance < 0.02) { // 2% chance of goal
                generateGoalEvent(matchState, minute, events);
            } else if (eventChance < 0.08) { // 6% chance of shot
                EventType shotType = random.nextBoolean() ? EventType.SHOT_ON_TARGET : EventType.SHOT_OFF_TARGET;
                String team = random.nextBoolean() ? matchState.getHomeTeam() : matchState.getAwayTeam();
                events.add(createEvent(matchState, minute, shotType,
                        generatePlayerName(), team, team + " attempts a shot"));
            } else if (eventChance < 0.12) { // 4% chance of foul
                String team = random.nextBoolean() ? matchState.getHomeTeam() : matchState.getAwayTeam();
                events.add(createEvent(matchState, minute, EventType.FOUL,
                        generatePlayerName(), team, "Foul by " + team));
            } else if (eventChance < 0.15) { // 3% chance of corner
                String team = random.nextBoolean() ? matchState.getHomeTeam() : matchState.getAwayTeam();
                events.add(createEvent(matchState, minute, EventType.CORNER,
                        null, team, "Corner for " + team));
            } else if (eventChance < 0.16) { // 1% chance of yellow card
                String team = random.nextBoolean() ? matchState.getHomeTeam() : matchState.getAwayTeam();
                events.add(createEvent(matchState, minute, EventType.YELLOW_CARD,
                        generatePlayerName(), team, "Yellow card for " + team));
            }

            // Possession updates every 5 minutes
            if (minute % 5 == 0) {
                int possession = 40 + random.nextInt(21); // 40-60%
                events.add(createEvent(matchState, minute, EventType.POSSESSION,
                        null, matchState.getHomeTeam(),
                        String.format("Possession: %s %d%% - %d%% %s",
                                matchState.getHomeTeam(), possession, 100 - possession, matchState.getAwayTeam())));
            }
        }

        return events;
    }

    private void generateGoalEvent(MatchState matchState, int minute, List<MatchEvent> events) {
        boolean isHomeGoal = random.nextBoolean();
        String scoringTeam = isHomeGoal ? matchState.getHomeTeam() : matchState.getAwayTeam();
        String player = generatePlayerName();

        if (isHomeGoal) {
            matchState.setHomeScore(matchState.getHomeScore() + 1);
        } else {
            matchState.setAwayScore(matchState.getAwayScore() + 1);
        }

        events.add(createEvent(matchState, minute, EventType.GOAL,
                player, scoringTeam,
                String.format("⚽ GOAL! %s scores for %s! (%d-%d)",
                        player, scoringTeam, matchState.getHomeScore(), matchState.getAwayScore())));
    }

    private MatchEvent createEvent(MatchState matchState, int minute, EventType eventType,
            String player, String team, String description) {
        return MatchEvent.builder()
                .matchId(matchState.getMatchId())
                .homeTeam(matchState.getHomeTeam())
                .awayTeam(matchState.getAwayTeam())
                .minute(minute)
                .eventType(eventType)
                .player(player)
                .team(team)
                .description(description)
                .homeScore(matchState.getHomeScore())
                .awayScore(matchState.getAwayScore())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String generatePlayerName() {
        String[] firstNames = { "John", "Michael", "David", "James", "Robert", "Luis", "Carlos", "Marco", "Pedro",
                "Andre" };
        String[] lastNames = { "Silva", "Martinez", "Johnson", "Smith", "Garcia", "Rodriguez", "Wilson", "Brown",
                "Davis", "Lopez" };
        return firstNames[random.nextInt(firstNames.length)] + " " +
                lastNames[random.nextInt(lastNames.length)];
    }

    public MatchState getMatchState(String matchId) {
        return activeMatches.get(matchId);
    }

    public Map<String, MatchState> getAllActiveMatches() {
        return new HashMap<>(activeMatches);
    }
}

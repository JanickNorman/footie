package com.example.footie.controller;

import com.example.footie.match.event.MatchEvent;
import com.example.footie.match.event.MatchState;
import com.example.footie.service.MatchEventConsumer;
import com.example.footie.service.MatchSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = {
        "http://localhost:3000",
        "http://127.0.0.1:3000",
        "https://*.ngrok-free.app",
        "http://*.ngrok.io"
})
@RequestMapping("/api/match")
@RequiredArgsConstructor
@Slf4j
public class MatchEventController {

    private final MatchSimulationService matchSimulationService;
    private final MatchEventConsumer matchEventConsumer;

    /**
     * Starts a new match and returns match state
     * POST /api/match/start
     */
    @PostMapping("/start")
    public Mono<MatchState> startMatch(@RequestBody Map<String, String> request) {
        String homeTeam = request.getOrDefault("homeTeam", "Home Team");
        String awayTeam = request.getOrDefault("awayTeam", "Away Team");

        log.info("Starting new match: {} vs {}", homeTeam, awayTeam);
        return matchSimulationService.startMatch(homeTeam, awayTeam);
    }

    /**
     * Simulates a full match minute by minute (real-time simulation)
     * Returns Server-Sent Events stream
     * GET /api/match/{matchId}/simulate-realtime?intervalMillis=1000
     */
    @GetMapping(value = "/{matchId}/simulate-realtime", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MatchEvent> simulateMatchRealtime(
            @PathVariable String matchId,
            @RequestParam(defaultValue = "1000") long intervalMillis) {

        log.info("Starting real-time simulation for match: {} with interval {}ms", matchId, intervalMillis);
        return matchSimulationService.simulateMatchMinuteByMinute(matchId, intervalMillis);
    }

    /**
     * Simulates a full match quickly (all events generated rapidly)
     * GET /api/match/{matchId}/simulate-fast
     */
    @GetMapping(value = "/{matchId}/simulate-fast", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MatchEvent> simulateMatchFast(@PathVariable String matchId) {
        log.info("Starting fast simulation for match: {}", matchId);
        return matchSimulationService.simulateFullMatch(matchId);
    }

    /**
     * Starts a match and immediately begins real-time simulation
     * POST /api/match/start-and-simulate
     */
    @PostMapping(value = "/start-and-simulate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MatchEvent> startAndSimulate(@RequestBody Map<String, String> request) {
        String homeTeam = request.getOrDefault("homeTeam", "Home Team");
        String awayTeam = request.getOrDefault("awayTeam", "Away Team");
        long intervalMillis = Long.parseLong(request.getOrDefault("intervalMillis", "1000"));

        log.info("Starting and simulating match: {} vs {} with interval {}ms",
                homeTeam, awayTeam, intervalMillis);

        return matchSimulationService.startMatch(homeTeam, awayTeam)
                .flatMapMany(matchState -> matchSimulationService.simulateMatchMinuteByMinute(
                        matchState.getMatchId(), intervalMillis));
    }

    /**
     * Gets current state of a match
     * GET /api/match/{matchId}/state
     */
    @GetMapping("/{matchId}/state")
    public Mono<MatchState> getMatchState(@PathVariable String matchId) {
        MatchState state = matchSimulationService.getMatchState(matchId);
        if (state == null) {
            return Mono.empty();
        }
        return Mono.just(state);
    }

    /**
     * Gets all active matches
     * GET /api/match/active
     */
    @GetMapping("/active")
    public Mono<Map<String, MatchState>> getActiveMatches() {
        return Mono.just(matchSimulationService.getAllActiveMatches());
    }

    /**
     * Gets all consumed events for a match
     * GET /api/match/{matchId}/events
     */
    @GetMapping("/{matchId}/events")
    public Mono<List<MatchEvent>> getMatchEvents(@PathVariable String matchId) {
        return Mono.just(matchEventConsumer.getMatchEvents(matchId));
    }

    /**
     * Gets all consumed events for all matches
     * GET /api/match/events/all
     */
    @GetMapping("/events/all")
    public Mono<Map<String, List<MatchEvent>>> getAllMatchEvents() {
        return Mono.just(matchEventConsumer.getAllMatchEvents());
    }

    /**
     * Clears stored events for a match
     * DELETE /api/match/{matchId}/events
     */
    @DeleteMapping("/{matchId}/events")
    public Mono<String> clearMatchEvents(@PathVariable String matchId) {
        matchEventConsumer.clearMatchEvents(matchId);
        return Mono.just("Events cleared for match: " + matchId);
    }
}

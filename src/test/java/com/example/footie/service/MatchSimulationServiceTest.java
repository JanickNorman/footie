package com.example.footie.service;

import com.example.footie.match.event.EventType;
import com.example.footie.match.event.MatchEvent;
import com.example.footie.match.event.MatchState;
import com.example.footie.match.event.MatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Match Simulation Service Tests")
class MatchSimulationServiceTest {

    @Mock
    private MatchEventProducer matchEventProducer;

    private MatchSimulationService matchSimulationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        matchSimulationService = new MatchSimulationService(matchEventProducer);

        // Mock producer to return the event passed to it
        when(matchEventProducer.sendMatchEvent(any(MatchEvent.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    @Test
    @DisplayName("Should start a new match with initial state")
    void testStartMatch() {
        // Given
        String homeTeam = "Barcelona";
        String awayTeam = "Real Madrid";

        // When
        Mono<MatchState> result = matchSimulationService.startMatch(homeTeam, awayTeam);

        // Then
        StepVerifier.create(result)
                .assertNext(matchState -> {
                    assertThat(matchState.getMatchId()).isNotNull();
                    assertThat(matchState.getHomeTeam()).isEqualTo(homeTeam);
                    assertThat(matchState.getAwayTeam()).isEqualTo(awayTeam);
                    assertThat(matchState.getHomeScore()).isEqualTo(0);
                    assertThat(matchState.getAwayScore()).isEqualTo(0);
                    assertThat(matchState.getCurrentMinute()).isEqualTo(0);
                    assertThat(matchState.getStatus()).isEqualTo(MatchStatus.FIRST_HALF);
                })
                .verifyComplete();

        // Verify kickoff event was sent
        ArgumentCaptor<MatchEvent> eventCaptor = ArgumentCaptor.forClass(MatchEvent.class);
        verify(matchEventProducer, times(1)).sendMatchEvent(eventCaptor.capture());

        MatchEvent kickoffEvent = eventCaptor.getValue();
        assertThat(kickoffEvent.getEventType()).isEqualTo(EventType.KICKOFF);
        assertThat(kickoffEvent.getMinute()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should generate events during match simulation")
    void testSimulateFullMatch() {
        // Given
        String homeTeam = "Barcelona";
        String awayTeam = "Real Madrid";

        // Start the match first
        MatchState matchState = matchSimulationService.startMatch(homeTeam, awayTeam).block();
        assertThat(matchState).isNotNull();

        reset(matchEventProducer); // Reset to not count kickoff event
        when(matchEventProducer.sendMatchEvent(any(MatchEvent.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When
        StepVerifier.create(matchSimulationService.simulateFullMatch(matchState.getMatchId()))
                // Then - verify that events are produced
                .expectNextCount(1) // At least one event should be generated
                .thenConsumeWhile(event -> {
                    // Verify event structure
                    assertThat(event.getMatchId()).isEqualTo(matchState.getMatchId());
                    assertThat(event.getHomeTeam()).isEqualTo(homeTeam);
                    assertThat(event.getAwayTeam()).isEqualTo(awayTeam);
                    assertThat(event.getMinute()).isGreaterThan(0);
                    return true;
                })
                .verifyComplete();

        // Verify match state was updated to FINISHED
        MatchState finalState = matchSimulationService.getMatchState(matchState.getMatchId());
        assertThat(finalState.getStatus()).isEqualTo(MatchStatus.FINISHED);
    }

    @Test
    @DisplayName("Should retrieve match state")
    void testGetMatchState() {
        // Given
        String homeTeam = "Barcelona";
        String awayTeam = "Real Madrid";
        MatchState matchState = matchSimulationService.startMatch(homeTeam, awayTeam).block();
        assertThat(matchState).isNotNull();

        // When
        MatchState retrievedState = matchSimulationService.getMatchState(matchState.getMatchId());

        // Then
        assertThat(retrievedState).isNotNull();
        assertThat(retrievedState.getMatchId()).isEqualTo(matchState.getMatchId());
        assertThat(retrievedState.getHomeTeam()).isEqualTo(homeTeam);
        assertThat(retrievedState.getAwayTeam()).isEqualTo(awayTeam);
    }

    @Test
    @DisplayName("Should return all active matches")
    void testGetAllActiveMatches() {
        // Given
        matchSimulationService.startMatch("Team A", "Team B").block();
        matchSimulationService.startMatch("Team C", "Team D").block();

        // When
        var activeMatches = matchSimulationService.getAllActiveMatches();

        // Then
        assertThat(activeMatches).hasSize(2);
    }

    @Test
    @DisplayName("Should return error when simulating non-existent match")
    void testSimulateNonExistentMatch() {
        // Given
        String nonExistentMatchId = "non-existent-id";

        // When & Then
        StepVerifier.create(matchSimulationService.simulateFullMatch(nonExistentMatchId))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Match not found"))
                .verify();
    }
}

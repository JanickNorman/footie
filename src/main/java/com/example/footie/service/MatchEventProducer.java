package com.example.footie.service;

import com.example.footie.match.event.MatchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchEventProducer {

    private final KafkaTemplate<String, MatchEvent> kafkaTemplate;

    @Value("${kafka.topic.match-events:match-events}")
    private String matchEventsTopic;

    /**
     * Sends a match event to Kafka topic
     * 
     * @param event The match event to send
     * @return Mono<MatchEvent> reactive response
     */
    public Mono<MatchEvent> sendMatchEvent(MatchEvent event) {
        return Mono.fromFuture(() -> {
            CompletableFuture<SendResult<String, MatchEvent>> future = kafkaTemplate.send(matchEventsTopic,
                    event.getMatchId(), event);

            return future.thenApply(result -> {
                log.info("Match event sent successfully: matchId={}, minute={}, type={}",
                        event.getMatchId(), event.getMinute(), event.getEventType());
                return event;
            }).exceptionally(ex -> {
                log.error("Failed to send match event: matchId={}, minute={}, error={}",
                        event.getMatchId(), event.getMinute(), ex.getMessage());
                throw new RuntimeException("Failed to send match event", ex);
            });
        });
    }

    /**
     * Sends multiple match events
     * 
     * @param events Array of match events
     */
    public void sendMatchEvents(MatchEvent... events) {
        for (MatchEvent event : events) {
            sendMatchEvent(event).subscribe();
        }
    }
}

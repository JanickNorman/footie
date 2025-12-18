package com.example.footie.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface TeamReactiveRepository extends ReactiveCrudRepository<TeamEntity, Long> {
    Mono<TeamEntity> findByCode(String code);
}

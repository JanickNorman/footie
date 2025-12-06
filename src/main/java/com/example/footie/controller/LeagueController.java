package com.example.footie.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    @GetMapping
    public Flux<League> getAllLeagues() {
        // TODO: Implement repository call
        return Flux.empty();
    }

    @GetMapping("/{id}")
    public Mono<League> getLeagueById(@PathVariable Long id) {
        // TODO: Implement repository call
        return Mono.empty();
    }

    @PostMapping
    public Mono<League> createLeague(@RequestBody League league) {
        // TODO: Implement repository call
        return Mono.empty();
    }

    @PutMapping("/{id}")
    public Mono<League> updateLeague(@PathVariable Long id, @RequestBody League league) {
        // TODO: Implement repository call
        return Mono.empty();
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteLeague(@PathVariable Long id) {
        // TODO: Implement repository call
        return Mono.empty();
    }


    // Inner class for League model (move to separate file later)
    public static class League {
        private Long id;
        private String name;
        private String country;
        private String season;

        // Constructors
        public League() {}

        public League(Long id, String name, String country, String season) {
            this.id = id;
            this.name = name;
            this.country = country;
            this.season = season;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getSeason() {
            return season;
        }

        public void setSeason(String season) {
            this.season = season;
        }
    }
}

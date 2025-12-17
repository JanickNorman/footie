package com.example.footie.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.footie.newSimulator.Team;
import com.example.footie.repository.ReactiveTeamRepository;
import com.example.footie.service.DrawService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class DrawController {

    private final DrawService drawService;
    private final ReactiveTeamRepository teamRepository;

    public DrawController(DrawService drawService, ReactiveTeamRepository teamRepository) {
        this.drawService = drawService;
        this.teamRepository = teamRepository;
    }
    
    @PostMapping("/draw")
    public Mono<Map<String, List<String>>> runDraw() {
        return drawService.runDraw();
    }

    @GetMapping("/teams")
    public Flux<Team> teams() {
        return teamRepository.findAll();
    }
    
}

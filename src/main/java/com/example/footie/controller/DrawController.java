package com.example.footie.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.footie.newSimulator.Team;
import com.example.footie.service.DrawService;
import com.example.footie.service.TeamService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@CrossOrigin(originPatterns = {"http://localhost:3000","http://127.0.0.1:3000","https://*.ngrok-free.app","http://*.ngrok.io"})
@RequestMapping("/api")
public class DrawController {
    private final DrawService drawService;
    private final TeamService teamService;

    public DrawController(DrawService drawService, TeamService teamService) {
        this.drawService = drawService;
        this.teamService = teamService;
    }
    
    @PostMapping("/draw")
    public Mono<Map<String, List<Team>>> runDraw(@RequestBody(required = false) Map<String, Object> body) {
        if (body != null && body.get("random") == Boolean.TRUE) {
            return drawService.runDrawRandomTeams(List.of());
        }
        return drawService.runDraw();
    }
    
    @GetMapping("/draw")
    public Mono<Map<String, List<Team>>> getRunDraw() {
        return drawService.runDraw();
    }

    @GetMapping("/teams")
    public Flux<Team> teams() {
        return teamService.findAll();
    }
    
}

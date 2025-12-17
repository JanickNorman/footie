package com.example.footie.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.footie.service.DrawService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class DrawController {

    private final DrawService drawService;

    public DrawController(DrawService drawService) {
        this.drawService = drawService;
    }
    
    @PostMapping("/draw")
    public Mono<Map<String, List<String>>> runDraw() {
        return Mono.fromCallable(() -> drawService.runDraw())
                .subscribeOn(Schedulers.boundedElastic());
    }
}

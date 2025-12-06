/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.footie.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.result.view.Rendering;

import com.example.footie.controller.LeagueController.League;

import reactor.core.publisher.Mono;

@Controller
public class HomeController {

        // Thymeleaf page showing national leagues table
    @GetMapping(path = "/leagues", produces = "text/html")
    @RequestMapping(value = "/leagues", method = RequestMethod.GET)
    public Mono<Rendering> leaguesPage() {
        List<League> sample = List.of(
                new League(1L, "Premier League", "England", "2025/26"),
                new League(2L, "La Liga", "Spain", "2025/26"),
                new League(3L, "Bundesliga", "Germany", "2025/26"),
                new League(4L, "Serie A", "Italy", "2025/26"),
                new League(5L, "Ligue 1", "France", "2025/26")
        );

        return Mono.just(Rendering.view("leagues")
                .modelAttribute("leagues", sample)
                .build());
    }

}

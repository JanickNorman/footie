package com.example.footie.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.footie.service.DrawService;

import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class DrawController {
    private final DrawService drawService;
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "draw-solver-thread");
        // t.setDaemon(true);
        return t;
    });

    // hold current running draw task so we can cancel it
    private static volatile Future<Map<String, List<String>>> currentRun = null;

    public DrawController(DrawService drawService) {
        this.drawService = drawService;
    }

    @PostMapping("/draw")
    public Mono<Map<String, List<String>>> runDraw() {
        // submit the simulation to the executor and keep a reference so it can be
        // aborted
        CompletableFuture<Map<String, List<String>>> cf = CompletableFuture.supplyAsync(() -> {
            return drawService.runDraw();
        }, EXECUTOR);

        // store reference (volatile) so abort endpoint can cancel
        currentRun = cf;
        System.out.println("HAI! Draw started, currentRun=" + currentRun);
        return Mono.fromFuture(cf);
    }

    @GetMapping("/draw/abort")
    @ResponseBody
    public Mono<Boolean> abortDraw() {
        Future<Map<String, List<String>>> f = currentRun;
        System.out.println("Abort requested, currentRun=" + f);
        if (f == null)
            return Mono.just(false);
        boolean cancelled = f.cancel(true);
        // clear reference if cancelled or already done
        if (cancelled || f.isDone() || f.isCancelled()) {
            currentRun = null;
        }
        return Mono.just(cancelled);
    }
}

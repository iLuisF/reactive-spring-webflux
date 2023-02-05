package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1")
public class MoviesInfoController {

    private final MovieInfoService movieInfoService;

    public MoviesInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @GetMapping("/movie-info")
    public Flux<MovieInfo> movies() {
        return movieInfoService.getAllMovies().log();
    }

    @GetMapping("/movie-info/{id}")
    public Mono<MovieInfo> movie(@PathVariable String id) {
        return movieInfoService.getMovieById(id);
    }

    @PostMapping("/movie-info")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovie(@RequestBody MovieInfo movie) {
        System.out.println("addMovie");
        return this.movieInfoService.addMovie(movie).log();
    }

    @PutMapping("/movie-info/{id}")
    public Mono<MovieInfo> update(@RequestBody MovieInfo movie, @PathVariable String id) {
        return this.movieInfoService.update(movie, id);
    }
}
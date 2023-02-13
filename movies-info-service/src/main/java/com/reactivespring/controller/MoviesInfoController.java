package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class MoviesInfoController {

    private final MovieInfoService movieInfoService;
    Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().latest();
    public MoviesInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @GetMapping("/movie-info")
    public Flux<MovieInfo> movies() {
        return movieInfoService.getAllMovies().log();
    }

    @GetMapping("/movie-info/{id}")
    public Mono<ResponseEntity<MovieInfo>> movie(@PathVariable("id") String id) {
        return movieInfoService.getMovieById(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @GetMapping(value = "/movie-info/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> movie() {
        return movieInfoSink.asFlux().log();
    }

    @GetMapping("/movie-info-by-id")
    public Flux<MovieInfo> moviesByYear(@RequestParam(value = "year") Integer year){
        return this.movieInfoService.moviesByYear(year);
    }

    @GetMapping("/movie-info-by-name")
    public Mono<MovieInfo> moviesByName(@RequestParam(value = "name") String name){
        return this.movieInfoService.moviesByName(name);
    }

    @PostMapping("/movie-info")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovie(@RequestBody @Valid MovieInfo movie) {
        System.out.println("addMovie");
        return this.movieInfoService.addMovie(movie)
                .doOnNext(savedMovieInfo -> movieInfoSink.tryEmitNext(savedMovieInfo))
                .log();
    }

    @PutMapping("/movie-info/{id}")
    public Mono<ResponseEntity<MovieInfo>> update(@RequestBody MovieInfo movie, @PathVariable String id) {
        return this.movieInfoService.update(movie, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/movie-info/{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return this.movieInfoService.delete(id);
    }
}
package com.reactivespring.service;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovieInfoService {

    private MovieInfoRepository repository;

    public MovieInfoService(MovieInfoRepository repository) {
        this.repository = repository;
    }

    public Mono<MovieInfo> addMovie(MovieInfo movie) {
        return this.repository.save(movie);
    }

    public Flux<MovieInfo> getAllMovies() {
        return this.repository.findAll();
    }
}

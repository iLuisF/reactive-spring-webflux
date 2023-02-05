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

    public Mono<MovieInfo> getMovieById(String id) {
        return this.repository.findById(id);
    }

    public Mono<MovieInfo> update(MovieInfo movie, String id) {
        return repository.findById(id)
                .flatMap(updatedMovie -> {
                    updatedMovie.setCast(movie.getCast());
                    updatedMovie.setName(movie.getName());
                    updatedMovie.setYear(movie.getYear());
                    return repository.save(updatedMovie);
                });
    }
}

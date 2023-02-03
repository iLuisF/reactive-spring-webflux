package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryTest {

    @Autowired
    MovieInfoRepository repository;

    @BeforeEach
    void beforeEach() {
        List<MovieInfo> movies = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                         2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        this.repository.saveAll(movies).blockLast();
    }

    @AfterEach
    void tearDown() {
        this.repository.deleteAll().block();
    }

    @Test
    void findAll() {
        Flux<MovieInfo> movies = repository.findAll().log();
        StepVerifier.create(movies).expectNextCount(3).verifyComplete();
    }

    @Test
    void findById() {
        Mono<MovieInfo> movie = repository.findById("abc").log();
        StepVerifier.create(movie)
                .assertNext(info -> assertEquals("Dark Knight Rises", info.getName()))
                .verifyComplete();
    }

    @Test
    void save() {
        MovieInfo movie = new MovieInfo(null, "Superman",
                2005, List.of("henry"), LocalDate.parse("2005-06-15"));
        Mono<MovieInfo> movieMono = this.repository.save(movie).log();
        StepVerifier.create(movieMono)
                .assertNext(movieInfo -> {
                    assertNotNull(movieInfo.getMovieInfoId());
                    assertEquals("Superman", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void update() {
        MovieInfo movie = repository.findById("abc").block();
        movie.setYear(2020);
        Mono<MovieInfo> movieMono = this.repository.save(movie).log();
        StepVerifier.create(movieMono)
                .assertNext(movieInfo -> assertEquals(2020, movieInfo.getName()))
                .verifyComplete();
    }

    @Test
    void delete() {
        this.repository.deleteById("abc").block();
        Flux<MovieInfo> movies = this.repository.findAll().log();
        StepVerifier.create(movies)
                .expectNextCount(2)
                .verifyComplete();
    }
}
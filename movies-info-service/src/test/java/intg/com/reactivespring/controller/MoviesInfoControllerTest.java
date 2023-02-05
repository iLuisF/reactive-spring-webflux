package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MoviesInfoControllerTest {

    @Autowired
    private MovieInfoRepository repository;
    @Autowired
    private WebTestClient client;
    static String MOVIES_INFO_URL = "/v1/movie-info";

    @BeforeEach
    void setUp() {
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
    void  addMovie() {
        MovieInfo movie = new MovieInfo(null, "Superman",
                2005, List.of("henry"), LocalDate.parse("2005-06-15"));
        client
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieEntity -> {
                    MovieInfo savedMovie = movieEntity.getResponseBody();
                    assert savedMovie != null;
                    assert savedMovie.getMovieInfoId() != null;
                });
    }

}
package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@AutoConfigureWebTestClient
@WebFluxTest(controllers = MoviesInfoController.class)
public class MovieInfoControllerTest {

    @Autowired
    private WebTestClient client;
    @MockBean
    private MovieInfoService service;
    static String MOVIES_INFO_URL = "/v1/movie-info";

    @Test
    void allMovies() {
        List<MovieInfo> movies = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        when(service.getAllMovies()).thenReturn(Flux.fromIterable(movies));
        client
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);

    }

    @Test
    void getMovieInfoById() {
        String id = "abc";
        when(this.service.getMovieById(isA(String.class))).thenReturn(Mono.just(
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))));
        client
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void addNewMovieInfo() {
        MovieInfo movie = new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        when(this.service.addMovie(isA(MovieInfo.class))).thenReturn(Mono.just(
                new MovieInfo("mockId", "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));
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
                    assert Objects.requireNonNull(savedMovie).getMovieInfoId() != null;
                });
    }

    @Test
    void addNewMovieValidation() {
        MovieInfo movie = new MovieInfo(null, "", -2005, List.of(""), LocalDate.parse("2005-06-15"));
        client
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movie)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class);
    }

    @Test
    void updateMovieInfo() {
        String id = "abc";
        MovieInfo updatedMovie = new MovieInfo("abc", "Dark Knight Rises 1",
                2013, List.of("Christian Bale1", "Tom Hardy1"), LocalDate.parse("2012-07-20"));
        when(this.service.update(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(updatedMovie));
        client
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .bodyValue(updatedMovie)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieEntity -> {
                    MovieInfo movieInfo = movieEntity.getResponseBody();
                    assert movieInfo != null;
                    assertEquals("Dark Knight Rises 1", movieInfo.getName());
                });
    }

    @Test
    void deleteMovieInfoById() {
        String id = "abc";
        when(this.service.delete(isA(String.class))).thenReturn(Mono.empty());
        client
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful();
    }
}
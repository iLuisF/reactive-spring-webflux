package com.reactivespring.controller;

import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@AutoConfigureWireMock(port = 8084)
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
                "restClient.movieInfoUrl: http://localhost:8084/v1/movie-info",
                "restClient.reviewsUrl: http://localhost:8084/v1/reviews"
        }
)
public class MoviesControllerIntgTest {

    @Autowired
    private WebTestClient client;

    @Test
    void retrieveMovieById() {
        String movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-info/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));
        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));
        client
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntity -> {
                    Movie movie = movieEntity.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });
    }

    @Test
    void retrieveMovieByIdError404() {
        String movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movie-info/" + movieId))
                .willReturn(aResponse().withStatus(404)));
        stubFor(get(urlPathEqualTo("/v1/reviews"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));
        client
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }
}
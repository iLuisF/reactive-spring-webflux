package com.reactivespring.controller;

import com.reactivespring.client.MovieInfoRestClient;
import com.reactivespring.client.ReviewRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.Review;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/v1/movies")
public class MovieController {


    private final MovieInfoRestClient movieInfoRestClient;
    private final ReviewRestClient reviewRestClient;

    public MovieController(MovieInfoRestClient movieInfoRestClient, ReviewRestClient reviewRestClient) {
        this.movieInfoRestClient = movieInfoRestClient;
        this.reviewRestClient = reviewRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> movieById(@PathVariable("id") String id) {
        return movieInfoRestClient.movies(id)
                .flatMap(info -> {
                    Mono<List<Review>> reviewList = reviewRestClient.reviews(id).collectList();
                    return reviewList.map(reviews -> new Movie(info, reviews));
                });
    }

}

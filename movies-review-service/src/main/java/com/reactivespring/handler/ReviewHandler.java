package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class ReviewHandler {

    private final ReviewReactiveRepository repository;

    public ReviewHandler(ReviewReactiveRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .flatMap(repository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        Optional<String> movieInfoId = request.queryParam("movieInfoId");
        Flux<Review> flux;
        if (movieInfoId.isPresent()) {
            flux = repository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
        } else {
            flux = repository.findAll();
        }
        return buildReviewsResponse(flux);
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Review> actual = repository.findById(id);
        return actual.flatMap(review -> request.bodyToMono(Review.class)
                .map(requestReview -> {
                    review.setComment(requestReview.getComment());
                    review.setRating(requestReview.getRating());
                    return review;
                })
                .flatMap(repository::save)
                .flatMap(ServerResponse.ok()::bodyValue)
        );
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Review> actual = repository.findById(id);
        return actual.flatMap(review -> repository.deleteById(id))
                .then(ServerResponse.noContent().build());
    }

    private static Mono<ServerResponse> buildReviewsResponse(Flux<Review> flux) {
        return ServerResponse.ok().body(flux, Review.class);
    }
}
package com.reactivespring.handler;

import com.reactivespring.domain.Review;
    import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReviewHandler {

    private final ReviewReactiveRepository repository;
    private final Validator validator;
    Sinks.Many<Review> reviewSink = Sinks.many().replay().latest();

    public ReviewHandler(ReviewReactiveRepository repository, Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(repository::save)
                .doOnNext(review -> reviewSink.tryEmitNext(review))
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        Set<ConstraintViolation<Review>> violations = this.validator.validate(review);
        log.info("constraintViolations : {}", violations);
        if (!violations.isEmpty()) {
            String errors = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errors);
        }
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
        Mono<Review> actual = repository.findById(id)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id " + id)));
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

    public Mono<ServerResponse> reviewsStream(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(reviewSink.asFlux(), Review.class)
                .log();
    }
}
package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ReviewRestClient {

    private WebClient client;
    @Value("${restClient.reviewsUrl}")
    private String url;

    public ReviewRestClient(WebClient client) {
        this.client = client;
    }

    public Flux<Review> reviews(String movieId) {
        var url = UriComponentsBuilder.fromHttpUrl(this.url)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand().toUriString();
        return this.client
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ReviewsClientException(responseMessage)));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(responseMessage -> Mono.error(
                                new ReviewsServerException("Server Exception in ReviewService " + responseMessage))))
                .bodyToFlux(Review.class);
    }
}
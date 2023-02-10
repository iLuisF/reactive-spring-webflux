package com.reactivespring.client;

import com.reactivespring.domain.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

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
                .bodyToFlux(Review.class);
    }
}

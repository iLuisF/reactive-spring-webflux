package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MovieInfoRestClient {

    private WebClient client;
    @Value("${restClient.movieInfoUrl}")
    private String movieInfoUrl;

    public MovieInfoRestClient(WebClient client) {
        this.client = client;
    }

    public Mono<MovieInfo> movies(String id) {
        String url = this.movieInfoUrl.concat("/{id}");
        return client
                .get()
                .uri(url, id)
                .retrieve()
                .bodyToMono(MovieInfo.class)
                .log();
    }

}

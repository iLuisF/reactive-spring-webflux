package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {

    private static final String LUIS = "luis";
    private static final String FLORES = "flores";
    private static final String GONZALEZ = "gonzalez";
    private final FluxAndMonoGeneratorService service = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {
        Flux<String> namesFlux = service.namesFlux();
        StepVerifier.create(namesFlux).expectNext(LUIS).expectNextCount(2).verifyComplete();
    }

    @Test
    void namesFluxMap() {
        Flux<String> names = service.namesFluxMap();
        StepVerifier.create(names).expectNext(LUIS.toUpperCase()).expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void namesFluxImmutability() {
        Flux<String> names = service.namesFluxImmutability();
        StepVerifier.create(names).expectNext(LUIS.toUpperCase(), FLORES.toUpperCase(), GONZALEZ.toUpperCase())
                .verifyComplete();
    }

    @Test
    void namesFluxFilter() {
        Flux<String> names = service.namesFluxFilter(4);
        StepVerifier.create(names).expectNext(FLORES.toUpperCase(), GONZALEZ.toUpperCase()).verifyComplete();
    }

    @Test
    void namesFluxFlatmap() {
        Flux<String> names = service.namesFluxFlatmap(4);
        StepVerifier.create(names).expectNext("F", "L", "O", "R", "E", "S", "G", "O", "N", "Z", "A", "L", "E", "Z")
                .verifyComplete();
    }

    @Test
    void namesFluxFlatmapAsync() {
        Flux<String> names = service.namesFluxFlatmapAsync(4);
        int length = FLORES.length() + GONZALEZ.length();
        StepVerifier.create(names).expectNextCount(length).verifyComplete();
    }

    @Test
    void namesFluxConcatmap() {
        Flux<String> names = service.namesFluxConcatmap(4);
        StepVerifier.create(names).expectNext("F", "L", "O", "R", "E", "S", "G", "O", "N", "Z", "A", "L", "E", "Z")
                .verifyComplete();
    }

    @Test
    void nameMonoFlatmap() {
        Mono<List<String>> characters = service.nameMonoFlatmap(3);
        StepVerifier.create(characters).expectNext(List.of("L", "U", "I", "S"))
                .verifyComplete();
    }

    @Test
    void nameMonoFlatmapMany() {
        Flux<String> characters = service.nameMonoFlatmapMany(3);
        StepVerifier.create(characters).expectNext("L", "U", "I", "S").verifyComplete();
    }

    @Test
    void namesFluxTransform() {
        Flux<String> characters = service.namesFluxTransform(4);
        StepVerifier.create(characters)
                .expectNext("F", "L", "O", "R", "E", "S", "G", "O", "N", "Z", "A", "L", "E", "Z")
                .verifyComplete();
    }

    @Test
    void namesFluxTransformDefaultIfEmpty() {
        Flux<String> characters = service.namesFluxTransform(10);
        StepVerifier.create(characters)
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    void namesFluxSwitchIfEmpty() {
        Flux<String> characters = service.namesFluxSwitchIfEmpty(6);
        StepVerifier.create(characters)
                .expectNext("D", "E", "F", "A", "U", "L", "T")
                .verifyComplete();
    }

    @Test
    void exploreConcat() {
        Flux<String> concat = service.exploreConcat();
        StepVerifier.create(concat).expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void exploreConcatWith() {
        Flux<String> concat = service.exploreConcatWith();
        StepVerifier.create(concat).expectNext("A", "B").verifyComplete();
    }

    @Test
    void exploreMerge() {
        Flux<String> merge = service.exploreMerge();
        StepVerifier.create(merge).expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }

    @Test
    void exploreZip() {
        Flux<String> zip = service.exploreZip();
        StepVerifier.create(zip).expectNext("AD", "BE", "CF")
                .verifyComplete();
    }
}
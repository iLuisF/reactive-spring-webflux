package com.learnreactiveprogramming.service;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class FluxAndMonoGeneratorService {

    public static final String LUIS = "luis";
    public static final String FLORES = "flores";
    public static final String GONZALEZ = "gonzalez";

    public static void main(String[] args) {
        FluxAndMonoGeneratorService service = new FluxAndMonoGeneratorService();
        service.namesFlux().subscribe(name -> System.out.println("name is: " + name));
        service.namesFluxMap().subscribe(name -> System.out.println("name is: " + name));
        service.namesFluxFlatmap(4).subscribe(character -> System.out.println("Character is: " + character));
        service.namesFluxFlatmapAsync(4).subscribe(character -> System.out.println("Character is: " + character));
        service.namesFluxImmutability().subscribe(name -> System.out.println("name is: " + name));
        service.namesFluxFilter(4).subscribe(name -> System.out.println("name is: " + name));
        service.nameMono().subscribe(name -> System.out.println("mono name is: " + name));
    }

    public Mono<String> nameMono() {
        return Mono.just(LUIS).log();
    }

    public Mono<List<String>> nameMonoFlatmap(int stringLength) {
        return Mono.just(LUIS)
                .map(String::toUpperCase)
                .filter(name -> (name.length() > stringLength))
                .flatMap(this::splitStringMono).log();
    }

    public Flux<String> nameMonoFlatmapMany(int stringLength) {
        return Mono.just(LUIS)
                .map(String::toUpperCase)
                .filter(name -> (name.length() > stringLength))
                .flatMapMany(this::splitString).log();
    }

    private Mono<List<String>> splitStringMono(String name) {
        List<String> characters = List.of(name.split(""));
        return Mono.just(characters);
    }

    public Flux<String> namesFlux() {
        //db o remote call
        return Flux.fromIterable(List.of(LUIS, FLORES, GONZALEZ)).log();
    }

    public Flux<String> namesFluxMap() {
        return Flux.fromIterable(List.of(LUIS, FLORES, GONZALEZ)).map(String::toUpperCase).log();
    }

    /**
     * @param length for filter
     * @return flux of characters
     * @see <a href="https://www.baeldung.com/java-reactor-map-flatmap">Flatmap vs map</a>
     */
    public Flux<String> namesFluxFlatmap(int length) {
        Function<String, Publisher<String>> splitString = this::splitString;
        return Flux.fromIterable(List.of(LUIS, FLORES, GONZALEZ))
                .map(String::toUpperCase)
                .filter(name -> (name.length() > length))
                .flatMap(splitString)
                .log();
    }

    public Flux<String> namesFluxTransform(int length) {
        UnaryOperator<Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(word -> word.length() > length);
        return Flux.fromIterable(List.of(LUIS, FLORES, GONZALEZ))
                .transform(filterMap)
                .flatMap(this::splitString)
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> namesFluxSwitchIfEmpty(int length) {
        UnaryOperator<Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(word -> word.length() > length)
                .flatMap(this::splitString);
        Flux<String> defaultFlux = Flux.just("default").transform(filterMap);
        return Flux.fromIterable(List.of(LUIS, FLORES))
                .transform(filterMap)
                .switchIfEmpty(defaultFlux)
                .log();
    }

    public Flux<String> namesFluxFlatmapAsync(int length) {
        return Flux.fromIterable(List.of(LUIS, FLORES, GONZALEZ))
                .map(String::toUpperCase)
                .filter(name -> (name.length() > length))
                .flatMap(this::splitStringWithDelay)
                .log();
    }

    public Flux<String> namesFluxConcatmap(int length) {
        return Flux.fromIterable(List.of(LUIS, FLORES, GONZALEZ))
                .map(String::toUpperCase)
                .filter(name -> (name.length() > length))
                .concatMap(this::splitStringWithDelay)
                .log();
    }

    /**
     * Convert from word to flux.
     * For example: LUIS to FLUX(L, U, I, S)
     *
     * @param word which will be split
     * @return flux of characters
     */
    private Flux<String> splitString(String word) {
        String[] charArray = word.split("");
        return Flux.fromArray(charArray);
    }

    private Flux<String> splitStringWithDelay(String word) {
        int delay = new Random().nextInt(1000);
        return this.splitString(word).delayElements(Duration.ofMillis(delay));
    }

    public Flux<String> namesFluxFilter(int length) {
        return Flux.fromIterable(List.of(LUIS, FLORES, GONZALEZ))
                .filter(name -> name.length() > length)
                .map(String::toUpperCase).log();
    }

    /**
     * Map returns new data structure, instead of modify var of names.
     * E.I. is immutable.
     *
     * @return names
     */
    public Flux<String> namesFluxImmutability() {
        Flux<String> names = Flux.fromIterable(List.of(LUIS, FLORES, GONZALEZ)).map(String::toUpperCase).log();
        names.map(String::toUpperCase);
        return names;
    }

    public Flux<String> exploreConcat(){
        Flux<String> abc = Flux.just("A", "B", "C");
        Flux<String> def = Flux.just("D", "E", "F");
        return Flux.concat(abc, def);
    }

    public Flux<String> exploreConcatWith(){
        Mono<String> a = Mono.just("A");
        Mono<String> b = Mono.just("B");
        return Flux.concat(a, b);
    }

    /**
     * With merge() the publishers are subscribed at the same time
     * With concat() the publishers are subscribed in a sequence.
     * With merge() is for Flux
     * With mergeWith() is for Flux and Mono
     * With mergeSequential() the publishers (Flux) are subscriber at the same time
     * but happens in a sequence.
     */
    public Flux<String> exploreMerge(){
        Flux<String> abc = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));
        Flux<String> def = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));
        return Flux.merge(abc, def).log();
    }

    /**
     * zip() only for flux
     * zipWith() for flux and mono
     */
    public Flux<String> exploreZip(){
        Flux<String> abc = Flux.just("A", "B", "C");
        Flux<String> def = Flux.just("D", "E", "F");
        return Flux.zip(abc, def, (first, second) -> first + second);
    }
}
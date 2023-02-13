package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    void sink() {
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        Flux<Integer> flux = replaySink.asFlux();
        flux.subscribe((i) -> {
            System.out.println("Subscriber 1" + i);
        });
    }
}

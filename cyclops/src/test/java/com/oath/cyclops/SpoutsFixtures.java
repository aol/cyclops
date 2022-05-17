package com.oath.cyclops;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executor;
import java.util.stream.Stream;

public interface SpoutsFixtures {

    static <T> ReactiveSeq<T> reactive(Stream<T> seq, Executor exec){
        return Spouts.from(Flux.fromStream(seq).publishOn(Schedulers.fromExecutor(exec)));
    }

}

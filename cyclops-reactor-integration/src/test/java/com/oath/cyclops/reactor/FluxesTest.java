package com.oath.cyclops.reactor;

import com.oath.cyclops.reactor.adapter.FluxReactiveSeq;
import cyclops.companion.reactor.Fluxs;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.monads.AnyM;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.monads.Witness.optional;
import cyclops.monads.transformers.StreamT;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.collections.mutable.ListX;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FluxesTest {

    @Test
    public void fluxTest(){
        Flux.just(2).single().block();
    }
    @Test
    public void fluxifyTest(){

        StreamT<optional,Integer> streamT = AnyMs.liftM(ReactiveSeq.of(1,2,3),optional.INSTANCE);
        StreamT<optional,Integer> fluxes = Fluxs.fluxify(streamT);
        AnyM<optional, Stream<Integer>> anyM = fluxes.unwrap();
        Optional<Stream<Integer>> opt = Witness.optional(anyM);
        Stream<Integer> stream = opt.get();
        assertTrue(stream instanceof FluxReactiveSeq);
        FluxReactiveSeq<Integer> f = (FluxReactiveSeq)stream;
        assertTrue(f.getFlux() instanceof Flux);
    }
    @Test
    public void fluxComp() {


        Flux<Tuple2<Integer, Integer>> stream = Fluxs.forEach(Flux.range(1, 10), i -> Flux.range(i, 10), Tuple::tuple);
        Flux<Integer> result = Fluxs.forEach(Flux.just(10, 20), a -> Flux.<Integer> just(a + 10), (a, b) -> a + b);
        assertThat(result.collectList()
                         .block(),
                   equalTo(ListX.of(30, 50)));
    }
    @Test
    public void tupleGen(){
        Fluxs.forEach(Flux.range(1, 10), i -> Flux.range(i, 10), Tuple::tuple)
              .subscribe(System.out::println);
    }
    @Test
    public void tupleGenFilter(){
        Fluxs.forEach(Flux.range(1, 10), i -> Flux.range(i, 10),(a, b) -> a>2 && b<10,Tuple::tuple)
                          .subscribe(System.out::println);
    }


}

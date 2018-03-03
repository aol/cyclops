package com.oath.cyclops.reactor.hkt.typeclasses.instances;
import static com.oath.cyclops.reactor.hkt.FluxKind.widen;
import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;

import cyclops.companion.reactor.Fluxs;
import com.oath.cyclops.reactor.hkt.FluxKind;
import com.oath.cyclops.hkt.Higher;
import cyclops.collections.mutable.ListX;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import cyclops.monads.ReactorWitness.flux;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;


import reactor.core.publisher.Flux;

public class FluxTest {

    @Test
    public void unit(){

        FluxKind<String> list = Fluxs.Instances.unit()
                                     .unit("hello")
                                     .convert(FluxKind::narrowK);

        assertThat(list.collect(Collectors.toList()).block(),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void functor(){

        FluxKind<Integer> list = Fluxs.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Fluxs.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(FluxKind::narrowK);

        assertThat(list.collect(Collectors.toList()).block(),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void apSimple(){
        Fluxs.Instances.zippingApplicative()
            .ap(widen(Flux.just(l1(this::multiplyByTwo))),widen(Flux.just(1,2,3)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        FluxKind<Function1<Integer, Integer>> listFn = Fluxs.Instances.unit()
                                                              .unit(Lambda.l1((Integer i) -> i * 2))
                                                              .convert(FluxKind::narrowK);

        FluxKind<Integer> list = Fluxs.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Fluxs.Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> Fluxs.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(FluxKind::narrowK);

        assertThat(list.collect(Collectors.toList()).block(),equalTo(Arrays.asList("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       FluxKind<Integer> list  = Fluxs.Instances.monad()
                                      .flatMap(i->widen(ReactiveSeq.range(0,i)), widen(Flux.just(1,2,3)))
                                      .convert(FluxKind::narrowK);
    }
    @Test
    public void monad(){

        FluxKind<Integer> list = Fluxs.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Fluxs.Instances.monad().flatMap((String v) -> Fluxs.Instances.unit().unit(v.length()), h))
                                     .convert(FluxKind::narrowK);

        assertThat(list.collect(Collectors.toList()).block(),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void monadZeroFilter(){

        FluxKind<String> list = Fluxs.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Fluxs.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(FluxKind::narrowK);

        assertThat(list.collect(Collectors.toList()).block(),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void monadZeroFilterOut(){

        FluxKind<String> list = Fluxs.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Fluxs.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(FluxKind::narrowK);

        assertThat(list.collect(Collectors.toList()).block(),equalTo(Arrays.asList()));
    }

    @Test
    public void monadPlus(){
        FluxKind<Integer> list = Fluxs.Instances.<Integer>monadPlus()
                                      .plus(FluxKind.widen(Flux.empty()), FluxKind.widen(Flux.just(10)))
                                      .convert(FluxKind::narrowK);
        assertThat(list.collect(Collectors.toList()).block(),equalTo(Arrays.asList(10)));
    }
/**
    @Test
    public void monadPlusNonEmpty(){

        Monoid<FluxKind<Integer>> m = Monoid.of(FluxKind.widen(Flux.empty()), (a,b)->a.isEmpty() ? b : a);
        FluxKind<Integer> list = Fluxs.Instances.<Integer>monadPlus(m)
                                      .plus(FluxKind.widen(Flux.of(5)), FluxKind.widen(Flux.of(10)))
                                      .convert(FluxKind::narrowK);
        assertThat(list,equalTo(Arrays.asList(5)));
    }
**/
    @Test
    public void  foldLeft(){
        int sum  = Fluxs.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, FluxKind.widen(Flux.just(1,2,3,4)));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = Fluxs.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, FluxKind.widen(Flux.just(1,2,3,4)));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void traverse(){

       Maybe<Higher<flux, Integer>> res = Fluxs.Instances.traverse()
                                                         .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), FluxKind.just(1,2,3))
                                                         .convert(Maybe::narrowK);


       assertThat(res.map(i->i.convert(FluxKind::narrowK).collect(Collectors.toList()).block()),
                  equalTo(Maybe.just(ListX.of(2,4,6))));
    }

}

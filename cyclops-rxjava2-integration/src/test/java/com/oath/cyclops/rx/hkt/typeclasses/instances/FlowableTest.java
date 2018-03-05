package com.oath.cyclops.rx.hkt.typeclasses.instances;

import com.oath.cyclops.hkt.Higher;
import cyclops.collections.mutable.ListX;
import cyclops.companion.rx2.Flowables;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import cyclops.monads.Rx2Witness.flowable;
import cyclops.reactive.ReactiveSeq;
import io.reactivex.Flowable;
import org.junit.Test;

import java.util.Arrays;

import static com.oath.cyclops.rx2.hkt.FlowableKind.widen;
import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class FlowableTest {

    @Test
    public void unit(){

        FlowableKind<String> list = Flowables.Instances.unit()
                                     .unit("hello")
                                     .convert(FlowableKind::narrowK);

        assertThat(Flowables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void functor(){

        FlowableKind<Integer> list = Flowables.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Flowables.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(FlowableKind::narrowK);

        assertThat(Flowables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void apSimple(){
        Flowables.Instances.zippingApplicative()
            .ap(widen(Flowable.just(l1(this::multiplyByTwo))),widen(Flowable.just(1,2,3)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        FlowableKind<Function1<Integer,Integer>> listFn =Flowables.Instances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(FlowableKind::narrowK);

        FlowableKind<Integer> list = Flowables.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Flowables.Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h->Flowables.Instances.zippingApplicative().ap(listFn, h))
                                     .convert(FlowableKind::narrowK);

        assertThat(Flowables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       FlowableKind<Integer> list  = Flowables.Instances.monad()
                                      .flatMap(i->widen(ReactiveSeq.range(0,i)), widen(Flowable.just(1,2,3)))
                                      .convert(FlowableKind::narrowK);
    }
    @Test
    public void monad(){

        FlowableKind<Integer> list = Flowables.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Flowables.Instances.monad().flatMap((String v) ->Flowables.Instances.unit().unit(v.length()), h))
                                     .convert(FlowableKind::narrowK);

        assertThat(Flowables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList("hello".length())));
    }
    @Test
    public void monadZeroFilter(){

        FlowableKind<String> list = Flowables.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Flowables.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(FlowableKind::narrowK);

        assertThat(Flowables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList("hello")));
    }
    @Test
    public void monadZeroFilterOut(){

        FlowableKind<String> list = Flowables.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Flowables.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(FlowableKind::narrowK);

        assertThat(Flowables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList()));
    }

    @Test
    public void monadPlus(){
        FlowableKind<Integer> list = Flowables.Instances.<Integer>monadPlus()
                                      .plus(FlowableKind.widen(Flowable.empty()), FlowableKind.widen(Flowable.just(10)))
                                      .convert(FlowableKind::narrowK);
        assertThat(Flowables.reactiveSeq(list.narrow()).toList(),equalTo(Arrays.asList(10)));
    }

    @Test
    public void  foldLeft(){
        int sum  = Flowables.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, FlowableKind.widen(Flowable.just(1,2,3,4)));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = Flowables.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, FlowableKind.widen(Flowable.just(1,2,3,4)));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void traverse(){


        Maybe<Higher<flowable, Integer>> res = Flowables.Instances.traverse()
                                                                 .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), FlowableKind.just(1,2,3))
                                                                .convert(Maybe::narrowK);


       assertThat(res.map(i->Flowables.reactiveSeq(FlowableKind.narrow(i)).toList()),
                  equalTo(Maybe.just(ListX.of(2,4,6))));
    }


}

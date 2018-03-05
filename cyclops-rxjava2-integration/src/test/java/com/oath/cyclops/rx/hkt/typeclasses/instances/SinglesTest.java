package com.oath.cyclops.rx.hkt.typeclasses.instances;

import com.oath.cyclops.hkt.Higher;
import cyclops.async.Future;

import cyclops.companion.rx2.Singles.Instances;
import cyclops.control.Maybe;
import cyclops.function.Function1;

import cyclops.function.Monoid;
import cyclops.monads.Rx2Witness.single;
import cyclops.monads.Witness.maybe;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Traverse;
import io.reactivex.Single;
import org.junit.Test;


import java.util.concurrent.atomic.AtomicBoolean;

import static com.oath.cyclops.rx2.hkt.SingleKind.widen;
import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SinglesTest {

    @Test
    public void unit(){

        SingleKind<String> opt = Instances.unit()
                                            .unit("hello")
                                            .convert(SingleKind::narrowK);

      assertThat(opt.toFuture().orElse(""),equalTo(Future.ofResult("hello").orElse("1")));
    }
    @Test
    public void functor(){

        SingleKind<Integer> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.functor().map((String v) ->v.length(), h))
                                     .convert(SingleKind::narrowK);

      assertThat(opt.toFuture().orElse(-1),equalTo(Future.ofResult("hello".length()).orElse(10000)));
    }
    @Test
    public void apSimple(){
        Instances.applicative()
            .ap(widen(Future.ofResult(l1(this::multiplyByTwo))),widen(Future.ofResult(1)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        SingleKind<Function1<Integer,Integer>> optFn = Instances.unit().unit(l1((Integer i) ->i*2)).convert(SingleKind::narrowK);

        SingleKind<Integer> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> Instances.applicative().ap(optFn, h))
                                     .convert(SingleKind::narrowK);

        assertThat(opt.toFuture().orElse(-1),equalTo(Future.ofResult("hello".length()*2).orElse(10000)));
    }
    @Test
    public void monadSimple(){
       SingleKind<Integer> opt  = Instances.monad()
                                            .<Integer,Integer>flatMap(i->widen(Future.ofResult(i*2)), widen(Future.ofResult(3)))
                                            .convert(SingleKind::narrowK);
    }
    @Test
    public void monad(){

        SingleKind<Integer> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.monad().flatMap((String v) -> Instances.unit().unit(v.length()), h))
                                     .convert(SingleKind::narrowK);

        assertThat(opt.toFuture().orElse(-1),equalTo(Future.ofResult("hello".length()).orElse(10000)));
    }
    @Test
    public void monadZeroFilter(){

        SingleKind<String> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(SingleKind::narrowK);

        assertThat(opt.toFuture().orElse("boo!"),equalTo(Future.ofResult("hello").orElse("no")));
    }
    @Test
    public void monadZeroFilterOut(){

        SingleKind<String> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(SingleKind::narrowK);

        ReactiveSeq<String> f = Spouts.from(opt.narrow().toFlowable());

        AtomicBoolean complete = new AtomicBoolean(false);
        f.forEach(System.out::println,System.err::println,()->complete.set(true));
        assertFalse(complete.get());
    }

    @Test
    public void monadPlus(){
        SingleKind<Integer> opt = Instances.<Integer>monadPlus()
                                      .plus(widen(Single.never()), widen(Single.just(10)))
                                      .convert(SingleKind::narrowK);

        assertTrue(opt.blockingGet()==10);
    }
    @Test
    public void monadPlusNonEmpty(){

        Monoid<SingleKind<Integer>> m = Monoid.of(widen(Single.never()), (a, b)->a.toFuture().isDone() ? b : a);
        SingleKind<Integer> opt = Instances.<Integer>monadPlusK(m)
                                      .plus(widen(Single.just(5)), widen(Single.just(10)))
                                      .convert(SingleKind::narrowK);
        assertThat(opt.blockingGet(),equalTo(10));
    }
    @Test
    public void  foldLeft(){
        int sum  = Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, widen(Future.ofResult(4)));

        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = Instances.foldable()
                        .foldRight(0, (a,b)->a+b, widen(Future.ofResult(1)));

        assertThat(sum,equalTo(1));
    }
    @Test
    public void traverse(){
        Traverse<single> traverse = Instances.traverse();
        Applicative<maybe> applicative = Maybe.Instances.applicative();

        SingleKind<Integer> mono = widen(Single.just(1));

        Higher<maybe, Higher<single, Integer>> t = traverse.traverseA(applicative, (Integer a) -> Maybe.just(a * 2), mono);

        Maybe<Higher<single, Integer>> res = traverse.traverseA(applicative, (Integer a)-> Maybe.just(a*2),mono)
                                                        .convert(Maybe::narrowK);



       assertThat(res.map(h->h.convert(SingleKind::narrowK).blockingGet()),
                  equalTo(Maybe.just(Single.just(2).blockingGet())));
    }
    @Test
    public void sequence(){
        Traverse<single> traverse = Instances.traverse();
        Applicative<maybe> applicative = Maybe.Instances.applicative();

        Higher<maybe, Higher<single, Integer>> res = traverse.sequenceA(applicative, widen(Single.just(Maybe.just(1))));
        Maybe<Single<Integer>> nk = res.convert(Maybe::narrowK)
                                     .map(h -> h.convert(SingleKind::narrow));

    }

}

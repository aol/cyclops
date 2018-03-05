package com.oath.cyclops.rx.hkt.typeclasses.instances;

import com.oath.cyclops.hkt.Higher;
import cyclops.async.Future;
import cyclops.companion.rx2.Maybes.Instances;

import cyclops.function.Function1;
import cyclops.function.Monoid;
import cyclops.monads.Rx2Witness.maybe;
import cyclops.monads.Witness;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Traverse;
import io.reactivex.Maybe;
import org.junit.Test;

import static com.oath.cyclops.rx2.hkt.MaybeKind.widen;
import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MaybesTest {

    @Test
    public void unit(){

        MaybeKind<String> opt = Instances.unit()
                                            .unit("hello")
                                            .convert(MaybeKind::narrowK);

        assertThat(opt.toFuture().orElse(""),equalTo(Future.ofResult("hello").orElse("1")));
    }
    @Test
    public void functor(){

        MaybeKind<Integer> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.functor().map((String v) ->v.length(), h))
                                     .convert(MaybeKind::narrowK);

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

        MaybeKind<Function1<Integer,Integer>> optFn = Instances.unit().unit(l1((Integer i) ->i*2)).convert(MaybeKind::narrowK);

        MaybeKind<Integer> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> Instances.applicative().ap(optFn, h))
                                     .convert(MaybeKind::narrowK);

        assertThat(opt.toFuture().orElse(-1),equalTo(Future.ofResult("hello".length()*2).orElse(50000)));
    }
    @Test
    public void monadSimple(){
       MaybeKind<Integer> opt  = Instances.monad()
                                            .<Integer,Integer>flatMap(i->widen(Future.ofResult(i*2)), widen(Future.ofResult(3)))
                                            .convert(MaybeKind::narrowK);
    }
    @Test
    public void monad(){

        MaybeKind<Integer> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.monad().flatMap((String v) -> Instances.unit().unit(v.length()), h))
                                     .convert(MaybeKind::narrowK);

        assertThat(opt.toFuture().orElse(-1),equalTo(Future.ofResult("hello".length()).orElse(500)));
    }
    @Test
    public void monadZeroFilter(){

        MaybeKind<String> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(MaybeKind::narrowK);

        assertThat(opt.toFuture().orElse("boo!"),equalTo(Future.ofResult("hello").orElse("no!")));
    }
    @Test
    public void monadZeroFilterOut(){

        MaybeKind<String> opt = Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(MaybeKind::narrowK);

        assertTrue(opt.blockingGet()==null);
    }

    @Test
    public void monadPlus(){
        MaybeKind<Integer> opt = Instances.<Integer>monadPlus()
                                      .plus(widen(Maybe.empty()), widen(Maybe.just(10)))
                                      .convert(MaybeKind::narrowK);
        assertTrue(opt.blockingGet()==null);
    }
    @Test
    public void monadPlusNonEmpty(){

        Monoid<MaybeKind<Integer>> m = Monoid.of(widen(Maybe.never()), (a, b)->a.toFuture().isDone() ? b : a);
        MaybeKind<Integer> opt = Instances.<Integer>monadPlusK(m)
                                      .plus(widen(Maybe.just(5)), widen(Maybe.just(10)))
                                      .convert(MaybeKind::narrowK);
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
        Traverse<maybe> traverse = Instances.traverse();
        Applicative<Witness.maybe> applicative = cyclops.control.Maybe.Instances.applicative();

        MaybeKind<Integer> mono = widen(Maybe.just(1));

        Higher<Witness.maybe, Higher<maybe, Integer>> t = traverse.traverseA(applicative, (Integer a) -> cyclops.control.Maybe.just(a * 2), mono);

        cyclops.control.Maybe<Higher<maybe, Integer>> res = traverse.traverseA(applicative, (Integer a)-> cyclops.control.Maybe.just(a*2),mono)
                                                        .convert(cyclops.control.Maybe::narrowK);




       assertThat(res.map(h->h.convert(MaybeKind::narrowK).blockingGet()),
                  equalTo( cyclops.control.Maybe.just(Maybe.just(2).blockingGet())));
    }
    @Test
    public void sequence(){
        Traverse<maybe> traverse = Instances.traverse();
        Applicative<Witness.maybe> applicative = cyclops.control.Maybe.Instances.applicative();

        Higher<Witness.maybe, Higher<maybe, Integer>> res = traverse.sequenceA(applicative, widen(cyclops.control.Maybe.just(cyclops.control.Maybe.just(1))));
        cyclops.control.Maybe<Maybe<Integer>> nk = res.convert(cyclops.control.Maybe::narrowK)
                                     .map(h -> h.convert(MaybeKind::narrow));

    }

}

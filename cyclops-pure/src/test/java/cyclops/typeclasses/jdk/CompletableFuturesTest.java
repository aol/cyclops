package cyclops.typeclasses.jdk;

import static cyclops.companion.CompletableFutures.CompletableFutureKind.widen;
import static cyclops.function.Lambda.l1;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;

import com.oath.cyclops.hkt.Higher;
import cyclops.companion.CompletableFutures;
import cyclops.companion.CompletableFutures.CompletableFutureKind;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;

import com.oath.cyclops.hkt.DataWitness.completableFuture;
import cyclops.typeclasses.functions.MonoidKs;
import org.junit.Test;



public class CompletableFuturesTest {

    @Test
    public void unit(){

        CompletableFuture<String> opt = CompletableFutures.CompletableFutureInstances.unit()
                                            .unit("hello")
                                            .convert(CompletableFutureKind::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(CompletableFuture.completedFuture("hello").join()));
    }
    @Test
    public void functor(){

        CompletableFuture<Integer> opt = CompletableFutures.CompletableFutureInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> CompletableFutures.CompletableFutureInstances.functor().map((String v) ->v.length(), h))
                                     .convert(CompletableFutureKind::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(CompletableFuture.completedFuture("hello".length()).join()));
    }
    @Test
    public void apSimple(){
        CompletableFutures.CompletableFutureInstances.applicative()
            .ap(widen(CompletableFuture.completedFuture(l1(this::multiplyByTwo))),widen(CompletableFuture.completedFuture(1)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        CompletableFutureKind<Function1<Integer,Integer>> optFn = CompletableFutures.CompletableFutureInstances.unit()
                                                                                .unit(Lambda.l1((Integer i) ->i*2))
                                                                                .convert(CompletableFutureKind::narrow);

        CompletableFuture<Integer> opt = CompletableFutures.CompletableFutureInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> CompletableFutures.CompletableFutureInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> CompletableFutures.CompletableFutureInstances.applicative().ap(optFn, h))
                                     .convert(CompletableFutureKind::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(CompletableFuture.completedFuture("hello".length()*2).join()));
    }
    @Test
    public void monadSimple(){
       CompletableFuture<Integer> opt  = CompletableFutures.CompletableFutureInstances.monad()
                                            .<Integer,Integer>flatMap(i->widen(CompletableFuture.completedFuture(i*2)), widen(CompletableFuture.completedFuture(3)))
                                            .convert(CompletableFutureKind::narrowK);
    }
    @Test
    public void monad(){

        CompletableFuture<Integer> opt = CompletableFutures.CompletableFutureInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> CompletableFutures.CompletableFutureInstances.monad().flatMap((String v) -> CompletableFutures.CompletableFutureInstances.unit().unit(v.length()), h))
                                     .convert(CompletableFutureKind::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(CompletableFuture.completedFuture("hello".length()).join()));
    }
    @Test
    public void monadZeroFilter(){

        CompletableFuture<String> opt = CompletableFutures.CompletableFutureInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> CompletableFutures.CompletableFutureInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(CompletableFutureKind::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(CompletableFuture.completedFuture("hello").join()));
    }
    @Test
    public void monadZeroFilterOut(){

        CompletableFuture<String> opt = CompletableFutures.CompletableFutureInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> CompletableFutures.CompletableFutureInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(CompletableFutureKind::narrowK);

        assertFalse(opt.toCompletableFuture().isDone());
    }

    @Test
    public void monadPlus(){
        CompletableFuture<Integer> opt = CompletableFutures.CompletableFutureInstances.<Integer>monadPlus()
                                      .plus(widen(new CompletableFuture<>()), widen(CompletableFuture.completedFuture(10)))
                                      .convert(CompletableFutureKind::narrowK);
        assertThat(opt.toCompletableFuture().join(),equalTo(CompletableFuture.completedFuture(10).join()));
    }
    @Test
    public void monadPlusNonEmpty(){

        CompletableFuture<Integer> opt = CompletableFutures.CompletableFutureInstances.<Integer>monadPlus(MonoidKs.firstCompleteCompletableFuture())
                                      .plus(widen(CompletableFuture.completedFuture(5)), widen(CompletableFuture.completedFuture(10)))
                                      .convert(CompletableFutureKind::narrowK);
        assertThat(opt.toCompletableFuture().join(),equalTo(CompletableFuture.completedFuture(5).join()));
    }
    @Test
    public void  foldLeft(){
        int sum  = CompletableFutures.CompletableFutureInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, widen(CompletableFuture.completedFuture(4)));

        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = CompletableFutures.CompletableFutureInstances.foldable()
                        .foldRight(0, (a,b)->a+b, widen(CompletableFuture.completedFuture(1)));

        assertThat(sum,equalTo(1));
    }
    @Test
    public void traverse(){
       Maybe<Higher<completableFuture, Integer>> res = CompletableFutures.CompletableFutureInstances.traverse()
                                                                          .traverseA(Maybe.MaybeInstances.applicative(), (Integer a)->Maybe.just(a*2), CompletableFutureKind.completedFuture(1))
                                                                         .convert(Maybe::narrowK);


       assertThat(res.toOptional().get().convert(CompletableFutureKind::narrowK).join(),equalTo(2));
    }

}

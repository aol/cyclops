package cyclops.typeclasses.cyclops;


import com.oath.cyclops.hkt.Higher;
import cyclops.control.Future;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import com.oath.cyclops.hkt.DataWitness.future;
import cyclops.typeclasses.functions.MonoidKs;
import org.junit.Test;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class FuturesTest {

    @Test
    public void unit(){

        Future<String> opt = FutureInstances.unit()
                                            .unit("hello")
                                            .convert(Future::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult("hello").toCompletableFuture().join()));
    }
    @Test
    public void functor(){

        Future<Integer> opt = FutureInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> FutureInstances.functor().map((String v) ->v.length(), h))
                                     .convert(Future::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult("hello".length()).toCompletableFuture().join()));
    }
    @Test
    public void apSimple(){
        FutureInstances.applicative()
            .ap(Future.ofResult(l1(this::multiplyByTwo)),Future.ofResult(1));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        Future<Function1<Integer,Integer>> optFn = FutureInstances.unit()
                                                        .unit(Lambda.l1((Integer i) ->i*2))
                                                        .convert(Future::narrowK);

        Future<Integer> opt = FutureInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> FutureInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> FutureInstances.applicative().ap(optFn, h))
                                     .convert(Future::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult("hello".length()*2).toCompletableFuture().join()));
    }
    @Test
    public void monadSimple(){
       Future<Integer> opt  = FutureInstances.monad()
                                            .<Integer,Integer>flatMap(i->Future.ofResult(i*2), Future.ofResult(3))
                                            .convert(Future::narrowK);
    }
    @Test
    public void monad(){

        Future<Integer> opt = FutureInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> FutureInstances.monad().flatMap((String v) -> FutureInstances.unit().unit(v.length()), h))
                                     .convert(Future::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult("hello".length()).toCompletableFuture().join()));
    }
    @Test
    public void monadZeroFilter(){

        Future<String> opt = FutureInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> FutureInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(Future::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult("hello").toCompletableFuture().join()));
    }
    @Test
    public void monadZeroFilterOut(){

        Future<String> opt = FutureInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> FutureInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(Future::narrowK);

        assertFalse(opt.toCompletableFuture().isDone());
    }

    @Test
    public void monadPlus(){
        Future<Integer> opt = FutureInstances.<Integer>monadPlus()
                                      .plus(Future.future(), Future.ofResult(10))
                                      .convert(Future::narrowK);
        assertThat(opt.get(),equalTo(Future.ofResult(10).get()));
    }
    @Test
    public void monadPlusNonEmpty(){

        Future<Integer> opt = FutureInstances.<Integer>monadPlus(MonoidKs.firstCompleteFuture())
                                      .plus(Future.ofResult(5), Future.ofResult(10))
                                      .convert(Future::narrowK);
        System.out.println(opt.getFuture().join().getClass());
        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult(5).toCompletableFuture().join()));
    }
    @Test
    public void  foldLeft(){
        int sum  = FutureInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, Future.ofResult(4));

        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = FutureInstances.foldable()
                        .foldRight(0, (a,b)->a+b, Future.ofResult(1));

        assertThat(sum,equalTo(1));
    }

    @Test
    public void traverse(){
       Maybe<Higher<future, Integer>> res = FutureInstances.traverse()
                                                               .traverseA(Maybe.MaybeInstances.applicative(), (Integer a)->Maybe.just(a*2), Future.ofResult(1))
                                                              .convert(Maybe::narrowK);


       assertThat(res.map(h->h.convert(Future::narrowK).get()),
                  equalTo(Maybe.just(Future.ofResult(2).get())));
    }

}

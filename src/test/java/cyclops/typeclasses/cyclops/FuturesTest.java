package cyclops.typeclasses.cyclops;


import com.oath.cyclops.hkt.Higher;
import cyclops.async.Future;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import cyclops.function.Monoid;
import cyclops.monads.Witness.future;
import org.junit.Test;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class FuturesTest {

    @Test
    public void unit(){

        Future<String> opt = Future.Instances.unit()
                                            .unit("hello")
                                            .convert(Future::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult("hello").toCompletableFuture().join()));
    }
    @Test
    public void functor(){

        Future<Integer> opt = Future.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Future.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(Future::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult("hello".length()).toCompletableFuture().join()));
    }
    @Test
    public void apSimple(){
        Future.Instances.applicative()
            .ap(Future.ofResult(l1(this::multiplyByTwo)),Future.ofResult(1));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        Future<Function1<Integer,Integer>> optFn =Future.Instances.unit()
                                                        .unit(Lambda.l1((Integer i) ->i*2))
                                                        .convert(Future::narrowK);

        Future<Integer> opt = Future.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Future.Instances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h->Future.Instances.applicative().ap(optFn, h))
                                     .convert(Future::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult("hello".length()*2).toCompletableFuture().join()));
    }
    @Test
    public void monadSimple(){
       Future<Integer> opt  = Future.Instances.monad()
                                            .<Integer,Integer>flatMap(i->Future.ofResult(i*2), Future.ofResult(3))
                                            .convert(Future::narrowK);
    }
    @Test
    public void monad(){

        Future<Integer> opt = Future.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Future.Instances.monad().flatMap((String v) ->Future.Instances.unit().unit(v.length()), h))
                                     .convert(Future::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult("hello".length()).toCompletableFuture().join()));
    }
    @Test
    public void monadZeroFilter(){

        Future<String> opt = Future.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Future.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(Future::narrowK);

        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult("hello").toCompletableFuture().join()));
    }
    @Test
    public void monadZeroFilterOut(){

        Future<String> opt = Future.Instances.unit()
                                     .unit("hello")
                                     .applyHKT(h->Future.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(Future::narrowK);

        assertFalse(opt.toCompletableFuture().isDone());
    }

    @Test
    public void monadPlus(){
        Future<Integer> opt = Future.Instances.<Integer>monadPlus()
                                      .plus(Future.future(), Future.ofResult(10))
                                      .convert(Future::narrowK);
        assertThat(opt.get(),equalTo(Future.ofResult(10).get()));
    }
    @Test
    public void monadPlusNonEmpty(){

        Monoid<Future<Integer>> m = Monoid.of(Future.future(), (a, b)->a.toCompletableFuture().isDone() ? b : a);
        Future<Integer> opt = Future.Instances.<Integer>monadPlus(m)
                                      .plus(Future.ofResult(5), Future.ofResult(10))
                                      .convert(Future::narrowK);
        assertThat(opt.toCompletableFuture().join(),equalTo(Future.ofResult(10).toCompletableFuture().join()));
    }
    @Test
    public void  foldLeft(){
        int sum  = Future.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, Future.ofResult(4));

        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = Future.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, Future.ofResult(1));

        assertThat(sum,equalTo(1));
    }

    @Test
    public void traverse(){
       Maybe<Higher<future, Integer>> res = Future.Instances.traverse()
                                                               .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), Future.ofResult(1))
                                                              .convert(Maybe::narrowK);


       assertThat(res.map(h->h.convert(Future::narrowK).get()),
                  equalTo(Maybe.just(Future.ofResult(2).get())));
    }

}

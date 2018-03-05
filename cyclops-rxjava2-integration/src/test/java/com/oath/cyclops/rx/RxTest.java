package com.oath.cyclops.rx;

import static cyclops.collections.mutable.ListX.listX;
import static cyclops.companion.rx2.Observables.reactiveSeq;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.types.anyM.AnyMSeq;
import cyclops.monads.Rx2Witness.observable;

import cyclops.collections.mutable.ListX;
import cyclops.companion.rx2.Observables;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import io.reactivex.Observable;
import org.junit.Test;




import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class RxTest {
    @Test
    public void observableTest(){
        Observable.just(1)
                  .singleOrError()
                  .blockingGet();
    }
    @Test
    public void asyncList(){

        AtomicBoolean complete = new AtomicBoolean(false);


        Observable<Integer> async =  Observables.fromStream(Spouts.async(Stream.of(100,200,300), Executors.newFixedThreadPool(1)))
                                                .doOnComplete(()->complete.set(true));

        ListX<Integer> asyncList = listX(reactiveSeq(async))
                                        .map(i->i+1);

        System.out.println("Blocked? " + complete.get());

        System.out.println("First value is "  + asyncList.get(0));

        System.out.println("Blocked? " + complete.get());




    }

    @Test
    public void anyMAsync(){



        AtomicBoolean complete = new AtomicBoolean(false);

        ReactiveSeq<Integer> asyncSeq = Spouts.async(Stream.of(1, 2, 3), Executors.newFixedThreadPool(1));
        Observable<Integer> observableAsync = Observables.observableFrom(asyncSeq);
        AnyMSeq<observable,Integer> monad = Observables.anyM(observableAsync);

        monad.map(i->i*2)
                .forEach(System.out::println,System.err::println,()->complete.set(true));

        System.out.println("Blocked? " + complete.get());
        while(!complete.get()){
            Thread.yield();
        }

        Observable<Integer> converted = Observables.raw(monad);
    }
    @Test
    public void observable() {
        assertThat(Observables.anyM(Observable.just(1, 2, 3))
                            .toListX(),
                   equalTo(ListX.of(1, 2, 3)));
    }

    @Test
    public void observableFlatMap() {
        assertThat(Observables.anyM(Observable.just(1, 2, 3))
                            .flatMap(a -> Observables.anyM(Observable.just(a + 10)))
                            .toListX(),
                   equalTo(ListX.of(11, 12, 13)));
    }


    @Test
    public void observableComp() {
        Observable<Integer> result = Observables.forEach(Observable.just(10, 20),
                                                                   a -> Observable.<Integer> just(a + 10),
                                                                   (a, b) -> a + b);

        assertThat(result.toList()
                         .blockingGet() ,
                   equalTo(ListX.of(30, 50)));

    }



}

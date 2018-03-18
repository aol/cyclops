package cyclops.streams.flowables.asyncreactivestreams;


import cyclops.companion.rx2.Flowables;

import cyclops.reactive.ReactiveSeq;
import cyclops.streams.flowables.CollectableTest;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ForkJoinPool;

public class AsyncRSCollectableTest extends CollectableTest {


    public <T> ReactiveSeq<T> of(T... values){

        return Flowables.reactiveSeq(Flux.just(values)
                .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));
    }

}

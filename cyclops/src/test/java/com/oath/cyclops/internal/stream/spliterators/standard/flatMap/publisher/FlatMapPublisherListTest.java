package com.oath.cyclops.internal.stream.spliterators.standard.flatMap.publisher;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static cyclops.reactive.ReactiveSeq.of;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Created by johnmcclean on 28/06/2017.
 */
public class FlatMapPublisherListTest {
    Executor exec = Executors.newFixedThreadPool(1);

    protected <U> ReactiveSeq<U> flux(U... array){

        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(exec)));


    }
    @Test
    public void flatMapList(){
        for(int i=0;i<100_000;i++){
            System.out.println("Iteration " + i);
            List<Integer> list = flux(1)
                    .mergeMap(1, in -> of(1, 2, 3))
                    .toList();
          //  System.out.println("List is " + list);
            Assert.assertThat(list,
                    Matchers.hasItems(1, 2, 3));
            Assert.assertThat(list.size(),
                    equalTo(3));
        }

    }
}

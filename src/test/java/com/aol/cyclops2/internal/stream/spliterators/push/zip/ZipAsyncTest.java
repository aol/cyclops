package com.aol.cyclops2.internal.stream.spliterators.push.zip;

import com.aol.cyclops2.types.stream.reactive.AsyncSubscriber;
import cyclops.collections.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by johnmcclean on 20/01/2017.
 */
public class ZipAsyncTest {
    @Test
    public void asyncZipSimple(){
        /**
        nextAsync().printOut();
        Spouts.of(1,2,3,4,5)
                .zipS(nextAsync()).printOut();
        Spouts.of(1,2,3,4,5)
                .zipS(Spouts.of(1,2)).printOut();
**/
        /**
        Spouts.of(1,2,3,4,5)
                .zipS(nextAsync()).forEach(System.out::println,
                System.err::println);
**/

        /**
        Spouts.of(1,2,3,4,5)
                .zipS(nextAsync())
                .toListX()
                .printOut();
**/



       ListX<Tuple2<Integer,Integer>> list = Spouts.of(1,2,3,4,5)
                                                    .peek(System.out::println)
                                                    .zipS(nextAsync())
                                                    .toListX();

       System.out.println("List creation is non-blocking");

       list.printOut();
       System.out.println("Print out the list asynchronously");





    }
    @Test
    public void asyncCollect(){


       Spouts.of(1, 2, 3, 4, 5)
               // .peek(System.out::println)
                .zipS(nextAsync())
                .collectAll(Collectors.toList())
                .forEach(System.out::println);

       System.out.println(Spouts.of(1, 2, 3, 4, 5)
                                .peek(System.out::println)
                                .zipS(nextAsync())
                                .collectAll(Collectors.toList())
                                .single());

        System.out.println(Spouts.of(1, 2, 3, 4, 5)
                .peek(System.out::println)
                .zipS(nextAsync())
                .collectAll(Collectors.toList())
                .findFirst().get());


    }
    @Test
    public void asyncZip(){
        System.out.println(Thread.currentThread().getId());
        Spouts.of(1,2,3,4,5)
                .zipS(nextAsync())
                .grouped(2)
                .flatMap(i->i.stream())
                .toListX()
                .materialize()
                .printOut();






        assertThat(Spouts.of(1,2,3,4,5)
                .zipS(nextAsync())
                .grouped(2)
                .flatMap(i->i.stream())
                .toListX(),equalTo(ListX.of(Tuple.tuple(1,1),Tuple.tuple(2,2))));

    }
    private ReactiveSeq<Integer> nextAsync() {
        AsyncSubscriber<Integer> sub = Spouts.asyncSubscriber();
        new Thread(()->{

            sub.awaitInitialization();
            try {
                //not a reactive-stream so we don't know with certainty when demand signalled
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sub.onNext(1);
            sub.onNext(2);
            sub.onComplete();
        }).start();
        return sub.stream();
    }
}

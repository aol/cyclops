package com.aol.cyclops2.internal.stream.spliterators.push.zip;

import com.aol.cyclops2.types.stream.reactive.AsyncSubscriber;
import cyclops.collections.ListX;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * Created by johnmcclean on 20/01/2017.
 */
public class ZipAsyncTest {

    protected <U> ReactiveSeq<U> flux(U... array){
        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

    }
    @Test
    public void zip2of(){

        for(int i=0;i<50000;i++) {
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i+"***********************");
            List<Tuple2<Integer, Integer>> list = flux(1, 2, 3, 4, 5, 6)
                    .zip(flux(100, 200, 300, 400).stream()).collect(Collectors.toList());
                    //.toListX();

            System.out.println(list);

            List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
            Assert.assertThat(right, hasItem(100));
            Assert.assertThat(right, hasItem(200));
            Assert.assertThat(right, hasItem(300));
            Assert.assertThat(right, hasItem(400));

            List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
            System.out.println(left);
            Assert.assertThat(Arrays.asList(1, 2, 3, 4, 5, 6), hasItem(left.get(0)));
        }

    }
    @Test
    public void zip2InvertedOf(){
        Spouts.of(1,2,3)
                    .toListX();
        for(int i=0;i<50000;i++) {
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i+"***********************");
            List<Tuple2<Integer, Integer>> list = flux(100, 200, 300, 400)
                    .zip(flux(1, 2, 3, 4, 5, 6).stream()).collect(Collectors.toList());
            //.toListX();

            System.out.println(list);

            List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
            Assert.assertThat(right, hasItem(1));
            Assert.assertThat(right, hasItem(2));
            Assert.assertThat(right, hasItem(3));
            Assert.assertThat(right, hasItem(4));

            List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
            System.out.println(left);
            Assert.assertThat(Arrays.asList(100, 200, 300, 400), hasItem(left.get(0)));
        }

    }
    @Test
    public void zip2ofLazyListX(){

        for(int i=0;i<50000;i++) {
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i+"***********************");
            List<Tuple2<Integer, Integer>> list = flux(1, 2, 3, 4, 5, 6)
                    .zip(flux(100, 200, 300, 400).stream())
                    .toListX();

            System.out.println(list);

            List<Integer> right = list.stream().map(t -> t.v2).collect(Collectors.toList());
            Assert.assertThat(right, hasItem(100));
            Assert.assertThat(right, hasItem(200));
            Assert.assertThat(right, hasItem(300));
            Assert.assertThat(right, hasItem(400));

            List<Integer> left = list.stream().map(t -> t.v1).collect(Collectors.toList());
            System.out.println(left);
            Assert.assertThat(Arrays.asList(1, 2, 3, 4, 5, 6), hasItem(left.get(0)));
        }

    }
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
    public void asyncReduce(){
        assertThat(Spouts.of(1, 2, 3, 4, 5)
                .peek(System.out::println)

                .reduceAll(0,(a,b)->a+b)
                .single(),equalTo(15));
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

package com.oath.cyclops.internal.stream.spliterators.push.zip;

import com.oath.cyclops.internal.stream.spliterators.push.Fixtures;
import com.oath.cyclops.types.reactive.AsyncSubscriber;
import cyclops.control.Future;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
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
    public void exactElements1(){
        assertThat(Spouts.iterate(0l, i->i+1l).zip(Spouts.iterate(0l,i->i+1l))
                .limit(1l).map(t->t._1()).count(),equalTo(1l));
        assertThat(Spouts.of(1).zip(Spouts.of(1))
                        .map(t->t._1()).count(),equalTo(1l));
    }
    @Test
    public void exactElements500(){
        assertThat(Spouts.iterate(0l, i->i+1l).zip(Spouts.iterate(0l,i->i+1l))
                .limit(500l).map(t->t._1()).count(),equalTo(500l));

    }
    @Test
    public void exactElements1000(){
        AtomicLong count = new AtomicLong(0);
        Future future = Future.future();
        Spouts.iterate(0l, i->i+1l).zip(Spouts.iterate(0l,i->i+1l))
                                                    .limit(500l)
                                                    .map(t->t._1())
                                                    .forEach(1000,n->count.incrementAndGet(),
                e->{},()->future.complete(1));
        future.get();
        assertThat(count.get(),equalTo(500l));

    }
    @Test
    public void zipErrors(){
        Future future = Future.future();
        Spouts.of(1,2,3).zipWithPublisher(Spouts.reactiveStream(Fixtures.threeErrorsSource))
                .forEach(System.out::println,System.out::println,()->future.complete(1));
        future.get();

    }
    @Test
    public void zip2of(){

        for(int i=0;i<500;i++) {
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i+"***********************");
            List<Tuple2<Integer, Integer>> list = flux(1, 2, 3, 4, 5, 6)
                    .zip(flux(100, 200, 300, 400).stream()).collect(Collectors.toList());
                    //.listX();

            System.out.println(list);

            List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
            Assert.assertThat(right, hasItem(100));
            Assert.assertThat(right, hasItem(200));
            Assert.assertThat(right, hasItem(300));
            Assert.assertThat(right, hasItem(400));

            List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
            System.out.println(left);
            Assert.assertThat(Arrays.asList(1, 2, 3, 4, 5, 6), hasItem(left.get(0)));
        }

    }
    @Test
    public void zip2InvertedOf(){
        Spouts.of(1,2,3)
                    .toList();
        for(int i=0;i<500;i++) {
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i+"***********************");
            List<Tuple2<Integer, Integer>> list = flux(100, 200, 300, 400)
                    .zip(flux(1, 2, 3, 4, 5, 6).stream()).collect(Collectors.toList());
            //.listX();

            System.out.println(list);

            List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
            Assert.assertThat(right, hasItem(1));
            Assert.assertThat(right, hasItem(2));
            Assert.assertThat(right, hasItem(3));
            Assert.assertThat(right, hasItem(4));

            List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
            System.out.println(left);
            Assert.assertThat(Arrays.asList(100, 200, 300, 400), hasItem(left.get(0)));
        }

    }
    @Test
    public void zip2ofStreamX(){

        for(int i=0;i<500;i++) {
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i+"***********************");
            List<Tuple2<Integer, Integer>> list = flux(1, 2, 3, 4, 5, 6)
                    .zip(flux(100, 200, 300, 400).stream())
                    .toList();

            System.out.println(list);

            List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
            Assert.assertThat(right, hasItem(100));
            Assert.assertThat(right, hasItem(200));
            Assert.assertThat(right, hasItem(300));
            Assert.assertThat(right, hasItem(400));

            List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
            System.out.println(left);
            Assert.assertThat(Arrays.asList(1, 2, 3, 4, 5, 6), hasItem(left.get(0)));
        }

    }
    @Test
    public void zip2ofIterator(){

        for(int i=0;i<5000;i++) {
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i);
            System.out.println("*************Iteration " + i+"***********************");
            List<Tuple2<Integer, Integer>> list = new ArrayList<>();
            Iterator<Tuple2<Integer, Integer>> it = flux(1, 2, 3, 4, 5, 6)
                    .zip(flux(100, 200, 300, 400).stream()).iterator();

            while(it.hasNext()){
                list.add(it.next());
            }

            System.out.println(list);

            List<Integer> right = list.stream().map(t -> t._2()).collect(Collectors.toList());
            Assert.assertThat(right, hasItem(100));
            Assert.assertThat(right, hasItem(200));
            Assert.assertThat(right, hasItem(300));
            Assert.assertThat(right, hasItem(400));

            List<Integer> left = list.stream().map(t -> t._1()).collect(Collectors.toList());
            System.out.println(left);
            Assert.assertThat(Arrays.asList(1, 2, 3, 4, 5, 6), hasItem(left.get(0)));
        }

    }

    @Test
    public void asyncReduce(){
        assertThat(Spouts.of(1, 2, 3, 4, 5)
                .peek(System.out::println)

                .reduceAll(0,(a,b)->a+b)
                .singleOrElse(null),equalTo(15));
    }
    @Test
    public void asyncCollect(){


       Spouts.of(1, 2, 3, 4, 5)
               // .peek(System.out::println)
                .zipWithStream(nextAsync())
                .collectAll(Collectors.toList())
                .forEach(System.out::println);

       System.out.println(Spouts.of(1, 2, 3, 4, 5)
                                .peek(System.out::println)
                                .zipWithStream(nextAsync())
                                .collectAll(Collectors.toList())
                                .singleOrElse(null));

        System.out.println(Spouts.of(1, 2, 3, 4, 5)
                .peek(System.out::println)
                .zipWithStream(nextAsync())
                .collectAll(Collectors.toList())
                .findFirst().get());


    }
    @Test
    public void asyncZip(){
        System.out.println(Thread.currentThread().getId());







        assertThat(Spouts.of(1,2,3,4,5)
                .zipWithStream(nextAsync())
                .grouped(2)
                .flatMap(i->i.stream())
                .toList(),equalTo(Arrays.asList(Tuple.tuple(1,1),Tuple.tuple(2,2))));

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

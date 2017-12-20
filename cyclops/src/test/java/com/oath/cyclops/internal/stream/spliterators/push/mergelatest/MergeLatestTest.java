package com.oath.cyclops.internal.stream.spliterators.push.mergelatest;

import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

/**
 * Created by johnmcclean on 27/01/2017.
 */
public class MergeLatestTest {
    Executor ex= Executors.newFixedThreadPool(20);
    final  static int ITERATIONS = 10;
    protected <U> ReactiveSeq<U> flux(U... array){

        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ex))).peek(s->System.out.println("next value " + s));


    }
    Flux<Integer> range(int size){
        return Flux.range(0,size).subscribeOn(Schedulers.fromExecutor(ex));
    }

    @Test
    public void mergeLongStreamSimple(){
        int size1 = 1500;
     //   long count = range(size1).count().block();
        long count = Spouts.mergeLatest(range(size1)).count();
        assertThat(count,equalTo((long)size1));
    }
    @Test
    public void mergeLongStreamSimple2(){
        int size1 = 1000;
        int size2 = 500;
        //   long count = range(size1).count().block();
        long count = Spouts.mergeLatest(range(size1),range(size2)).count();
        assertThat(count,equalTo((long)(size1+size2)));
    }
    @Test
    public void mergeStreamSimple3(){
        for(int i=0;i<10;i++) {
            int size1 = 100;
            int size2 = 500;
            int size3 = 600;
            //   long count = range(size1).count().block();
            long count = Spouts.mergeLatest(range(size1), range(size2), range(size3)).count();
            assertThat(count, equalTo((long) (size1 + size2 + size3)));
        }
    }

    @Test
    public void mergeLong(){
        int size1 = 10000;
        int size2 = 4000;
        int size3= 1000;
        long size = Spouts.mergeLatest(range(size1),range(size2),range(size3)).count();

       assertThat(size,equalTo((long)size1+size2+size3));
    }

    @Test
    public void mapGroupMerge3(){
        for(int k=0;k<ITERATIONS;k++) {
            List<Integer> res = Spouts.of(1,2,3)
                                      .map(i -> nextAsync()).grouped(3)
                                      .map(l -> Spouts.mergeLatest(l))
                                      .flatMap(i -> i)
                                      .toListX();

            System.out.println("Result is " + res);
            assertThat(res.size(), equalTo(ListX.of(1, 2, 1, 2, 1, 2).size()));
            assertThat(res, hasItems(1,2));
            int one = 0;
            int two = 0;
            for(Integer next : res){
                if(next==1){
                    one++;
                }
                if(next==2){
                    two++;
                }
            }
            assertThat(one,equalTo(3));
            assertThat(two,equalTo(3));
        }
    }
    @Test
    public void mapGroupMerge3FlatMapPForEach(){
        for(int k=0;k<ITERATIONS;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k+ "**************************");
           Spouts.of(1, 2, 3)
                    .map(i -> nextAsync())
                    .grouped(3)
                    .map(l -> Spouts.mergeLatest(l))
                    .mergeMap(i -> i)
                    .forEach(System.out::println);
        }
    }
    @Test
    public void mapGroupMerge3FlatMapP(){
        for(int k=0;k<ITERATIONS;k++) {
            List<Integer> res = Spouts.of(1,2,3)
                                      .map(i -> nextAsync())
                                      .grouped(3)
                                      .map(l -> Spouts.mergeLatest(l))
                                      .mergeMap(i -> i)
                                        .toListX();

            System.out.println("Result is " + res);
            assertThat(res.size(), equalTo(ListX.of(1, 2, 1, 2, 1, 2).size()));
            assertThat(res, hasItems(1,2));
            int one = 0;
            int two = 0;
            for(Integer next : res){
                if(next==1){
                    one++;
                }
                if(next==2){
                    two++;
                }
            }
            assertThat(one,equalTo(3));
            assertThat(two,equalTo(3));
        }
    }
    @Test
    public void mapGroupMerge(){
        for(int k=0;k<ITERATIONS;k++) {
            assertThat(Spouts.of(1)
                    .map(i -> nextAsync()).grouped(1)
                    .map(l -> Spouts.mergeLatest(l))
                    .flatMap(i -> i).toListX(), equalTo(ListX.of(1, 2)));
        }
    }
    @Test
    public void mergeSimple(){
        for(int i=0;i<ITERATIONS;i++) {
            assertThat(Spouts.mergeLatest(nextAsync()).toList(), equalTo(ListX.of(1, 2)));
        }
    }
    @Test
    public void mergeSimpleList(){
        for(int i=0;i<ITERATIONS;i++) {
            assertThat(Spouts.mergeLatest(ListX.of(nextAsync())).toList(), equalTo(ListX.of(1, 2)));
        }
    }
    @Test
    public void mergePAsync2(){
        for(int k=0;k<ITERATIONS;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k+ "**************************");
            List<Integer> res =  Spouts.mergeLatest(nextAsync(),nextAsync(),nextAsync())
                    .toList();
            System.out.println("Result is " + res);
            assertThat(res.size(), equalTo(ListX.of(1, 2, 1, 2, 1, 2).size()));
            assertThat(res, hasItems(1,2));
            int one = 0;
            int two = 0;
            for(Integer next : res){
                if(next==1){
                    one++;
                }
                if(next==2){
                    two++;
                }
            }
            assertThat(one,equalTo(3));
            assertThat(two,equalTo(3));
        }
    }
    @Test
    public void mergePAsyncSynchronous2(){
        for(int k=0;k<ITERATIONS;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k+ "**************************");
            List<Integer> res =  ReactiveSeq.fromPublisher(Spouts.mergeLatest(nextAsync(),nextAsync(),nextAsync()))
                    .toList();
            System.out.println("Result is " + res);
            assertThat(res.size(), equalTo(ListX.of(1, 2, 1, 2, 1, 2).size()));
            assertThat(res, hasItems(1,2));
            int one = 0;
            int two = 0;
            for(Integer next : res){
                if(next==1){
                    one++;
                }
                if(next==2){
                    two++;
                }
            }
            assertThat(one,equalTo(3));
            assertThat(two,equalTo(3));
        }
    }
    private Publisher<Integer> nextAsync() {
        return flux(1,2);
        /**
         PushSubscriber<Integer> sub = Spouts.asyncSubscriber();
         new Thread(()->{

         sub.awaitInitialization();
         try {
         //not a reactiveBuffer-stream so we don't know with certainty when demand signalled
         Thread.sleep(10);
         } catch (InterruptedException e) {
         e.printStackTrace();
         }
         sub.onNext(1);
         sub.onNext(2);
         sub.onComplete();
         }).start();
         return sub.stream();
         **/
    }
}

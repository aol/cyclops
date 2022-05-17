package com.oath.cyclops.internal.stream.spliterators.push.mergelatest;

import cyclops.control.Future;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static com.oath.cyclops.Iterations.SHORT_CYCLE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

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
        for(int i=0;i<SHORT_CYCLE;i++) {
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
                                      .toList();

            System.out.println("Result is " + res);
            assertThat(res.size(), equalTo(Arrays.asList(1, 2, 1, 2, 1, 2).size()));
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
                                        .toList();

            System.out.println("Result is " + res);
            assertThat(res.size(), equalTo(Arrays.asList(1, 2, 1, 2, 1, 2).size()));
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
                    .flatMap(i -> i).toList(), equalTo(Arrays.asList(1, 2)));
        }
    }
    @Test
    public void mergeSimple(){
        for(int i=0;i<ITERATIONS;i++) {
            assertThat(Spouts.mergeLatest(nextAsync()).toList(), equalTo(Arrays.asList(1, 2)));
        }
    }
    @Test
    public void mergeSimpleList(){
        for(int i=0;i<ITERATIONS;i++) {
            assertThat(Spouts.mergeLatest(ReactiveSeq.of(nextAsync())).toList(), equalTo(Arrays.asList(1, 2)));
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
            assertThat(res.size(), equalTo(Arrays.asList(1, 2, 1, 2, 1, 2).size()));
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
            assertThat(res.size(), equalTo(Arrays.asList(1, 2, 1, 2, 1, 2).size()));
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
         **/
    }
    @Test
    public void fluxMerge() {
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.mergeLatest(Flux.just(1),
            Flux.range(2, 2),
            Flux.just(4, 5, 6)
                .hide()) .forEach(n->{
            assertFalse(complete.get());
            data.updateAndGet(s->s.plus(n));
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertThat("Values " +  data,data.get(), hasItems(1,2,3,4,5,6));
        Assert.assertThat(complete.get(), IsEqual.equalTo(true));
        assertNull(error.get());

    }
    @Test
    public void fluxStepverifier() {
        StepVerifier.create(Spouts.mergeLatest(Future.ofResult(1),Flux.just(2, 3)))
            .expectNext(1, 2, 3)
            .verifyComplete();
    }

    @Test
    public void mergeLatestEmpty(){



        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);
        Spouts.<Integer>mergeLatest(Spouts.<ReactiveSeq<Integer>>of()) .forEach(z->{
            assertFalse(complete.get());
            data.updateAndGet(s->s.plus(z));
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });
        Assert.assertThat(complete.get(), equalTo(true));
    }


    @Test
    public void mergeOne(){
        StepVerifier.create(Spouts.mergeLatest(Spouts.of(Spouts.of(1))))
            .expectNext(1)
            .verifyComplete();
        StepVerifier.create(Spouts.mergeLatestList(Seq.of(Spouts.of(1))))
            .expectNext(1)
            .verifyComplete();
        StepVerifier.create(Spouts.mergeLatest(Spouts.of(Spouts.of(1))))
            .expectNext(1)
            .verifyComplete();
    }

    @Test
    public void mergePublisherPublisher(){
        AtomicReference<Vector<Integer>> data = new AtomicReference(Vector.empty());
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.mergeLatest(Flux.just(Flux.just(1, 2), Flux.just(3, 4))).forEach(z->{
            assertFalse(complete.get());
            data.updateAndGet(s->s.plus(z));
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });
        assertThat("Values " +  data,data.get(), hasItems(1,2,3,4));
        Assert.assertThat(complete.get(), IsEqual.equalTo(true));
        assertNull(error.get());

    }

}

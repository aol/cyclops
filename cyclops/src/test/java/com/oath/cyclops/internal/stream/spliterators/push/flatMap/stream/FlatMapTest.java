package com.oath.cyclops.internal.stream.spliterators.push.flatMap.stream;

import com.oath.cyclops.types.reactive.AsyncSubscriber;
import com.oath.cyclops.types.reactive.ReactiveSubscriber;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.control.Maybe;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.reactive.Spouts.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

/**
 * Created by johnmcclean on 25/01/2017.
 */
public class FlatMapTest {

    protected <U> ReactiveSeq<U> flux(U... array){

        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));


    }
    @Test
    public void flatMapFlux(){
        for(int i=0;i<10000;i++){
            System.out.println("************Iteration " + i);
            Assert.assertThat(flux(1)
                            .flatMap(in -> flux(1, 2, 3))
                            .toList(),
                    Matchers.equalTo(Arrays.asList(1, 2, 3)));
        }

    }

    @Test
    public void flatMapList(){
        for(int i=0;i<1000;i++){
            System.out.println("************Iteration " + i);
            Assert.assertThat(flux(1)
                            .flatMap(in -> of(1, 2, 3))
                            .toList(),
                    Matchers.equalTo(Arrays.asList(1, 2, 3)));
        }

    }
    @Test
    public void flatMapS() throws InterruptedException{
        //of(1,2,3)
        //		.flatMapP(i->Maybe.of(i)).printOut();

        Assert.assertThat(of(1,2,3)
                .flatMap(i-> Maybe.of(i).stream())
                .toListX(), Matchers.equalTo(Arrays.asList(1,2,3)));


    }
    @Test
    public void flatMap(){
        assertThat(Spouts.of(1,2,3)
                .flatMap(i->Spouts.of(i))
                .toList(),equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void flatMap2(){
        assertThat(Spouts.of(1,2,3)
                .flatMap(i->Spouts.of(1,i))
                .toList(),equalTo(ListX.of(1,1,1,2,1,3)));
    }
    @Test
    public void flatMapAsync2(){
        for(int k=0;k<100;k++) {
            List<Integer> res = Spouts.of(1, 2, 3)
                    .flatMap(i -> nextAsync())
                    .toList();
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
    public void flatMapPAsync3(){
        for(int k=0;k<10;k++) {
            List<Integer> res = Spouts.of(1, 2, 3)
                    .flatMap(i -> nextAsyncRS())
                    .toList();
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
    Subscription subs;
    AtomicInteger count;
    AtomicBoolean complete;
    @Test
    public void flatMapAsyncRS(){
        for(int k=0;k<1000;k++) {
            complete = new AtomicBoolean(false);
            count = new AtomicInteger(0);
            ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
            Spouts.of(1, 2, 3).peek(System.out::println)
                    .flatMap(i -> nextAsyncRS())
                    .subscribe(new Subscriber<Integer>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            subs = s;

                        }

                        @Override
                        public void onNext(Integer integer) {
                            System.out.println("RECIEVED " + integer);
                            assertThat(integer, Matchers.isOneOf(1, 2));
                            System.out.println("count " + count.incrementAndGet());
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onComplete() {
                            complete.set(true);
                        }
                    });
            subs.request(Long.MAX_VALUE);
            while (!complete.get()) {

            }
            assertThat(count.get(), equalTo(6));
        }

    }

    @Test
    public void flatMapAsyncRS2(){
        for(int k=0;k<1000;k++) {
            System.out.println("********0---------------------K " + k);
            ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
            Spouts.of(1, 2, 3).peek(System.out::println)
                    .flatMap(i -> nextAsyncRS())
                    //  .flatMapP(i->Spouts.of(1,2))
                    .subscribe(sub);

            List<Integer> res = sub.reactiveStream().collect(Collectors.toList());
            System.out.println(res);
            assertThat(res.size(), equalTo(ListX.of(1, 2, 1, 2, 1, 2).size()));

            assertThat(res, hasItems(1, 2));
            int one = 0;
            int two = 0;
            for (Integer next : res) {
                if (next == 1) {
                    one++;
                }
                if (next == 2) {
                    two++;
                }
            }
            assertThat(one, equalTo(3));
            assertThat(two, equalTo(3));

        }

    }
    @Test
    public void flatMapAsyncRS3(){
        for(int k=0;k<100;k++) {


            List<Integer> res = Spouts.of(1, 2, 3).peek(System.out::println)
                    .flatMap(i -> nextAsyncRS()).collect(Collectors.toList());
            System.out.println(res);
            assertThat(res.size(), equalTo(ListX.of(1, 2, 1, 2, 1, 2).size()));
            assertThat(res, hasItems(1, 2));
            int one = 0;
            int two = 0;
            for (Integer next : res) {
                if (next == 1) {
                    one++;
                }
                if (next == 2) {
                    two++;
                }
            }
            assertThat(one, equalTo(3));
            assertThat(two, equalTo(3));
        }

    }
    AtomicInteger start= new AtomicInteger(0);
    private Stream<Integer> nextAsyncRS() {
        ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
        AtomicLong req = new AtomicLong(0);
        int id = start.incrementAndGet();
        sub.onSubscribe(new Subscription() {

            @Override
            public void request(long n) {

                req.addAndGet(n);

            }

            @Override
            public void cancel() {

            }
            public String toString(){
                return "subscription " + id;
            }
        });
        new Thread(()->{
            int sent=0;
            while(sent<2){
                if(req.get()>0){
                    sub.onNext( ++sent);

                    req.decrementAndGet();
                }
            }
            sub.onComplete();


            // Flux.just(1,2).forEachAsync(sub);


        }).start();

        return sub.reactiveStream();
    }
    private Stream<Integer> nextAsync() {
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

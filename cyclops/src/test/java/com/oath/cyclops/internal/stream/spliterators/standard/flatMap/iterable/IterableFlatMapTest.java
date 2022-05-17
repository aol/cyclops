package com.oath.cyclops.internal.stream.spliterators.standard.flatMap.iterable;

import com.oath.cyclops.types.reactive.ReactiveSubscriber;
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

import static com.oath.cyclops.Iterations.SHORT_CYCLE;
import static com.oath.cyclops.Iterations.VERY_SHORT_CYCLE;
import static cyclops.reactive.Spouts.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

/**
 * Created by johnmcclean on 25/01/2017.
 */
public class IterableFlatMapTest {

    protected <U> ReactiveSeq<U> flux(U... array){

        return ReactiveSeq.fromPublisher(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));


    }
    @Test
    public void flatMapFlux(){
        for(int i=0;i<SHORT_CYCLE;i++){
            System.out.println("************Iteration " + i);
            Assert.assertThat(flux(1)
                            .concatMap(in -> flux(1, 2, 3))
                            .toList(),
                    Matchers.equalTo(Arrays.asList(1, 2, 3)));
        }

    }

    @Test
    public void flatMapList(){
        for(int i=0;i<SHORT_CYCLE;i++){
            System.out.println("************Iteration " + i);
            Assert.assertThat(flux(1)
                            .concatMap(in -> of(1, 2, 3))
                            .toList(),
                    Matchers.equalTo(Arrays.asList(1, 2, 3)));
        }

    }
    @Test
    public void flatMapS() throws InterruptedException{


        Assert.assertThat(of(1,2,3)
                .concatMap(i-> Maybe.of(i))
                .toList(), Matchers.equalTo(Arrays.asList(1,2,3)));


    }
    @Test
    public void flatMap(){
        assertThat(Spouts.of(1,2,3)
                .concatMap(i->Spouts.of(i))
                .toList(),equalTo(Arrays.asList(1,2,3)));
    }
    @Test
    public void flatMap2(){
        assertThat(Spouts.of(1,2,3)
                .concatMap(i->Spouts.of(1,i))
                .toList(),equalTo(Arrays.asList(1,1,1,2,1,3)));
    }
    @Test
    public void flatMapAsync2(){
        for(int k=0;k<VERY_SHORT_CYCLE;k++) {
            List<Integer> res = Spouts.of(1, 2, 3)
                    .concatMap(i -> Spouts.from(Flux.just(1,2).subscribeOn(Schedulers.elastic())))
                    .toList();
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
    public void flatMapPAsync3(){
        for(int k=0;k<VERY_SHORT_CYCLE;k++) {
            List<Integer> res = Spouts.of(1, 2, 3)
                    .concatMap(i -> nextAsyncRS())
                    .toList();
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
    Subscription subs;
    AtomicInteger count;
    AtomicBoolean complete;
    @Test
    public void flatMapAsyncRS(){
        for(int k=0;k<SHORT_CYCLE;k++) {
            complete = new AtomicBoolean(false);
            count = new AtomicInteger(0);
            ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
            Spouts.of(1, 2, 3).peek(System.out::println)
                    .concatMap(i -> nextAsyncRS())
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
        for(int k=0;k<SHORT_CYCLE;k++) {
            System.out.println("********0---------------------K " + k);
            ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
            Spouts.of(1, 2, 3).peek(System.out::println)
                    .concatMap(i -> nextAsyncRS())
                    //  .flatMapP(i->Spouts.of(1,2))
                    .subscribe(sub);

            List<Integer> res = sub.reactiveStream().collect(Collectors.toList());
            System.out.println(res);
            assertThat(res.size(), equalTo(Arrays.asList(1, 2, 1, 2, 1, 2).size()));

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
        for(int k=0;k<VERY_SHORT_CYCLE;k++) {



            List<Integer> res =  Spouts.from(Spouts.of(1, 2, 3).peek(System.out::println)
                    .concatMap(i -> nextAsyncRS())).collect(Collectors.toList());
            System.out.println(res);
            assertThat(res.size(), equalTo(Arrays.asList(1, 2, 1, 2, 1, 2).size()));
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
    private ReactiveSeq<Integer> nextAsyncRS() {
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

}

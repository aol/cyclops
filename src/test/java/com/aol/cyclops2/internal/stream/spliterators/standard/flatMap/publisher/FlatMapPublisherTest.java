package com.aol.cyclops2.internal.stream.spliterators.standard.flatMap.publisher;

import cyclops.collections.mutable.ListX;
import cyclops.control.lazy.Maybe;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cyclops.stream.ReactiveSeq.of;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

/**
 * Created by johnmcclean on 19/01/2017.
 */
public class FlatMapPublisherTest {
    Executor exec = Executors.newFixedThreadPool(1);

    protected <U> ReactiveSeq<U> flux(U... array){

        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(exec)));


    }

    @Test
    public void flatMapPublisher() throws InterruptedException{
        //of(1,2,3)
        //		.flatMapP(i->Maybe.of(i)).printOut();

        Assert.assertThat(of(1,2,3)
                .flatMapP(i-> Maybe.of(i))
                .toListX(), Matchers.equalTo(Arrays.asList(1,2,3)));


    }
    @Test
    public void flatMapP(){
        assertThat(of(1,2,3)
                .flatMapP(i->Spouts.of(i))
                .toList(),Matchers.hasItems(1,2,3));
    }
    @Test
    public void flatMapP2(){
        assertThat(of(1,2,3)
                .flatMapP(i->Spouts.of(1,i))
                .toList(),Matchers.hasItems(1,1,1,2,1,3));
    }
    @Test
    public void flatMapPAsync2(){
        for(int k=0;k<100;k++) {
            List<Integer> res = of(1, 2, 3)
                    .flatMapP(i -> nextAsync())
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
                    .flatMapP(i -> nextAsync())
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
    public void flatMapPSanity(){
        complete = new AtomicBoolean(false);
        count = new AtomicInteger(0);
        of(1, 2, 3).peek(System.out::println)
                .flatMapP(i -> nextAsync())
                .forEach(r->System.out.println("Next res " + r + " count " + count.incrementAndGet()),
                        e->{},()->complete.set(true));//.request(Long.MAX_VALUE);

        while(!complete.get()){

        }
        System.out.println("Count " + count);
        System.out.println("Size " + Spouts.of(1, 2, 3).peek(System.out::println)
                .flatMapP(i -> nextAsync())
                .toList().size());
    }
    @Test
    public void flatMapPAsyncRS(){
        for(int k=0;k<1;k++) {
            complete = new AtomicBoolean(false);
            count = new AtomicInteger(0);
            //SeqSubscriber<Integer> sub = SeqSubscriber.subscriber();
            of(1, 2, 3).peek(System.out::println)
                    .flatMapP(i -> nextAsync())
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
/**
    @Test
    public void flatMapPAsyncRS2() throws InterruptedException {
        for(int k=0;k<1000;k++) {
            System.out.println("********0---------------------K " + k);
            System.out.println("********0---------------------K " + k);
            System.out.println("********0---------------------K " + k);
            System.out.println("********0---------------------K " + k);
            System.out.println("********0---------------------K " + k);
            System.out.println("********0---------------------K " + k);



            List<Integer> res = of(1, 2, 3)
                                            .peek(System.out::println)
                                            .flatMapP(2,i -> nextAsync())
                                            .peek(e->System.out.println("e " + e))
                                            .collect(Collectors.toList());
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
    public void flatMapPAsyncRS2Conc(){
        for(int k=0;k<1000;k++) {
            System.out.println("********0---------------------K " + k);




            List<Integer> res = of(1, 2, 3)
                    .peek(System.out::println)
                    .flatMapP(2,i -> nextAsync())
                    .collect(Collectors.toList());
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
**/
    @Test
    public void flatMapPAsyncRS3(){
        for(int k=0;k<100;k++) {

            List<Integer> res = Spouts.from(of(1, 2, 3).peek(System.out::println)
                                      .flatMapP(i -> nextAsync()))
                                        .collect(Collectors.toList());
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

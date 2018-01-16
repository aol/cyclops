package com.oath.cyclops.internal.stream.spliterators.push.flatMap.publisher;

import com.oath.cyclops.types.reactive.ReactiveSubscriber;
import cyclops.control.Maybe;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static cyclops.reactive.Spouts.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

/**
 * Created by johnmcclean on 19/01/2017.
 */
public class FlatMapPublisherTest {

    static Executor ex[]= {Executors.newFixedThreadPool(1),Executors.newFixedThreadPool(1),Executors.newFixedThreadPool(1)};
    static int index =0;
    protected <U> ReactiveSeq<U> flux(U... array){

        int indexToUse = index%3;
        index++;
        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ex[indexToUse])));


    }

    @Test
    public void flatMapFlux(){

        System.out.println("Print out");
        flux(1)
                .mergeMap(in -> flux(1, 2, 3))
                .forEach(System.out::println);
        System.out.println("Print out finished");

       for(int i=0;i<100;i++){
            System.out.println("************Iteration " + i);
            System.out.println("************Iteration " + i);
            System.out.println("************Iteration " + i);
            Assert.assertThat(flux(1)
                            .mergeMap(in -> flux(1, 2, 3))
                            .toList(),
                    Matchers.equalTo(Arrays.asList(1, 2, 3)));
        }

    }

    @Test
    public void flatMapFluxMaxCon(){

        for(int i=0;i<1000;i++){
            System.out.println("************Iteration " + i);
            System.out.println("************Iteration " + i);
            System.out.println("************Iteration " + i);


            Assert.assertThat(flux(1)
                            .mergeMap(2, in -> flux(10, 20, 30))
                            .toList(),
                    Matchers.equalTo(Arrays.asList(10, 20, 30)));
        }

    }


    @Test
    public void flatMapList(){
        for(int i=0;i<100_000;i++){
            System.out.println("Iteration " + i);
            List<Integer> list = flux(1)
                    .mergeMap(in -> of(1, 2, 3))
                    .toList();
            System.out.println("List is " + list);
            Assert.assertThat(list,
                    Matchers.hasItems(1, 2, 3));
            Assert.assertThat(list.size(),
                    equalTo(3));
        }

    }
    @Test
    public void flatMapPublisher() throws InterruptedException{
        //of(1,2,3)
        //		.flatMapP(i->Maybe.of(i)).printOut();

        Assert.assertThat(of(1,2,3)
                .mergeMap(i-> Maybe.of(i))
                .toList(), Matchers.equalTo(Arrays.asList(1,2,3)));


    }
    @Test
    public void flatMapP(){
        assertThat(Spouts.of(1,2,3)
                .mergeMap(i->Spouts.of(i))
                .toList(),equalTo(Arrays.asList(1,2,3)));
    }
    @Test
    public void flatMapP2(){
        MatcherAssert.assertThat(Spouts.of(1,2,3)
                .mergeMap(i->Spouts.of(1,i))
                .toList(),Matchers.hasItems(1,1,1,2,1,3));
    }
    @Test
    public void asyncFlatMap(){
        List<Integer> res = Spouts.of(1,2,3)
                                    .map(i->nextAsync())
                                    .grouped(3)
                                    .mergeMap(l->Spouts.mergeLatest(l))
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

    @Test
    public void flatMapPSanity(){
       Spouts.of(1,2,3).mergeMap(i->Spouts.of(10,20,30,40,50,60,70,80,90)).printOut();
    }


    @Test
    public void concurrentFlatMapP1(){
        for(int k=0;k<500;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k + "*************************!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            List<Integer> res =  Spouts.of(1,2,3)
                    .mergeMap(2, i -> nextAsync())
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
    public void concurrentFlatMapP2(){
        for(int k=0;k<500;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k + "*************************!!!!!!!!!!!!!!!!!!!!!!!!!!!");
             System.out.println(Spouts.of(1, 2, 3)
                    .mergeMap(3, i -> nextAsync()).toList());

        }
    }
    @Test
    public void concurrentFlatMapP(){
        for(int k=0;k<500;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k + "*************************!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            List<Integer> res =  Spouts.of(1, 2, 3)
                    .mergeMap(3, i -> nextAsync())
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
    public void concurrentFlatMapPIteration(){
        for(int k=0;k<500;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k + "*************************!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            List<Integer> res =  new ArrayList<>();
            Iterator<Integer> it = Spouts.of(1, 2, 3)
                    .mergeMap(3, i -> nextAsync())
                    .iterator();
            while(it.hasNext()){
                res.add(it.next());
            }
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
    public void range(){


            List<Integer> res =  Spouts.range(1,500)
                                       .mergeMap(i -> nextAsync())
                                        .toList();
            System.out.println("Result is " + res);


    }

    @Test
    public void flatMapPAsync2(){
        for(int k=0;k<500;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k + "*************************!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            List<Integer> res =  Spouts.of(1, 2, 3)
                                       .mergeMap(i -> nextAsync())
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
    public void flatMapPAsync2Synchronous(){
        for(int k=0;k<5000;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k + "*************************!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            List<Integer> res =   Spouts.from(Flux.just(1, 2, 3)
                    .flatMap(i -> nextAsync())).toList();
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
    public void flatMapPAsync2Iterator(){
        for(int k=0;k<500;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k + "*************************!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            List<Integer> res = new ArrayList<>();
            Iterator<Integer> it = Spouts.of(1, 2, 3)
                                         .mergeMap(i -> nextAsync()).iterator();
            while(it.hasNext()){
                res.add(it.next());
            }


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
    public void fluxSanityCheck() {
        Flux.just(1, 2, 3).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()))
                .flatMap(i -> flux(i, 1, 2, 3))
                .subscribe(System.out::println);
        // .forEachAsync(System.out::println);
/**
 Spouts.of(1,2,3)
 .flatMap(i->flux(i,1,2,3))
 .forEach(System.out::println);
 **/

    }

    @Test
    public void flatMapPAsyncFlux(){
        for(int k=0;k<1000;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            List<Integer> res =  Spouts.from(Flux.just(1, 2, 3)
                    .flatMap(i -> nextAsync())
            ).toList();
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
        for(int k=0;k<10;k++) {
            List<Integer> res = Spouts.of(1, 2, 3)
                    .mergeMap(i -> nextAsync())
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
    volatile Subscription subs;
    volatile AtomicInteger count;
    volatile AtomicBoolean complete;
    @Test
    public void flatMapPAsyncRS(){
        for(int k=0;k<1000;k++) {
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k);
            System.out.println("****************************NEXT ITERATION "+ k + "*************************!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            complete = new AtomicBoolean(false);
            count = new AtomicInteger(0);
            ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
            Spouts.of(1, 2, 3).peek(System.out::println)
                    .mergeMap(i -> nextAsync())
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
    public void and(){
        System.out.println(2& (1L << 1));
        System.out.println("Not set " + (1& (1L << 1)));
        System.out.println("3 test " + (3& (1L << 1)));
        System.out.println("3 test " + ((3& (1L << 1))==0));
        System.out.println("Not set " + (5& (1L << 1)));
        System.out.println("Not set " + (0& (1L << 1)));
    }

    @Test
    public void flatMapPAsyncRS2(){
        for(int k=0;k<1000;k++) {
            System.out.println("********0---------------------K " + k);


            List<Integer> res = Spouts.from(Spouts.of(1, 2, 3).peek(System.out::println)
                    .mergeMap(i -> nextAsync())).collect(Collectors.toList());
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
    /**
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
    public void arrayConcat(){
        for(int k=0;k<1000;k++) {
            System.out.println("********0---------------------K " + k);




            List<Integer> res = Spouts.concat(nextAsync(),nextAsync(),nextAsync())
                    .collect(Collectors.toList());
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
    public void arrayConcatIt(){
        for(int k=0;k<1000;k++) {
            System.out.println("********0---------------------K " + k);


            List<Integer> res = new ArrayList();
            Iterator<Integer> it = Spouts.concat(nextAsync(),nextAsync(),nextAsync()).iterator();
            while(it.hasNext()){
                res.add(it.next());
            }
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
    public void flatMapPAsyncRS3(){
        for(int k=0;k<100;k++) {

            Flux<Integer> flux = Flux.from(Spouts.of(1, 2, 3).peek(System.out::println)
                    .mergeMap(i -> nextAsync()));

            List<Integer> res = flux.collect(Collectors.toList())
                                    .block();
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

    private ReactiveSeq<Integer> nextAsync() {
        return flux(1,2);

    }
}

package cyclops.streams.push.asyncreactivestreams;


import cyclops.data.Seq;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class AsyncRSFlatMapPTest {
    protected <U> ReactiveSeq<U> of(U... array){
        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

    }
    protected ReactiveSeq<Integer> range(int start, int end){
        return Spouts.from(Flux.range(start,end).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

    }
    protected ReactiveSeq<Integer> empty(){
        return Spouts.from(Flux.from(Spouts.<Integer>empty()).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

    }
    Executor ex = Executors.newFixedThreadPool(4);
    protected <U> ReactiveSeq<U> of2(U... array){
        return Spouts.from(Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

    }
    protected <U> Flux<U> flux(U... array){
        return Flux.just(array).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()));

    }
    @Test
    public void simple(){
        of(1,2,3).mergeMap(1, i->flux(5,6,7)).peek(e->System.out.println("e " + e)).printOut();
    }

    @Test
    public void fluxTest(){
       List<String> list = flux(1,2,3,4)
                                .flatMap(i->flux(10,20,30).flatMap(j->flux("hello"+j,"world"),4),3)
                                .collect(Collectors.toList()).block();

       System.out.println(list);
    }
    @Test
    public void fluxList(){
        for(int l=0;l<1000;l++) {
            System.out.println("Starting!");
            List<Integer> it = this.flux(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .flatMap(i -> flux(i, i * 2, i * 4, 5, 6, 7, 8, 9)
                            .flatMap(x -> of2(5, 6, 7, 7, 8, 9),4),4)
                            .collect(Collectors.toList()).block();
            System.out.println("Size =  " + it.size());
        }
    }
    @Test
    public void nestedSync(){
        System.out.println(of(1, 2, 3)
                .mergeMap(2, i -> Spouts.of(5,6).mergeMap(2, k->Spouts.of(8,9))).toList());
    }
    @Test
    public void failing(){
        System.out.println(Spouts.of(1, 2, 3)
                .mergeMap(2, i -> Spouts.of(5,6)).toList());
        System.out.println(of(1, 2, 3)
                .mergeMap(2, i -> of(5,6)).toList());
    }
    @Test
    public void flatMapPList(){
        for(int l=0;l<1_000;l++) {
            System.out.println("Starting!");
            List<Integer> it = this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(4, i -> of2(i, i * 2, i * 4, 5, 6, 7, 8, 9)
                            .mergeMap(4, x -> of2(5, 6, 7, 7, 8, 9)))
                    .toList();
            System.out.println("Size =  " + it.size());
        }
    }

    @Test
    public void flatMapPIt(){
        for(int l=0;l<1_000;l++) {
            Iterator<Integer> it = this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap( 4, i -> of2(i, i * 2, i * 4, 5, 6, 7, 8, 9)
                            .mergeMap( 4, x -> of2(5, 6, 7, 7, 8, 9)))
                    .iterator();
            assertThat("Iteration " + l,ReactiveSeq.fromIterator(it).size(),equalTo(480));
        }
    }
    @Test
    public void flatMapPIterate(){
        for(int l=0;l<10;l++) {
            System.out.println("Iteration " + l);
            Iterator<Integer> it = this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> of2(i, i * 2, i * 4, 5, 6, 7, 8, 9)
                            .mergeMap(x -> of2(5, 6, 7, 7, 8, 9)))
                    .iterator();
            List<Integer> result = new ArrayList<>(480);
            while(it.hasNext()){
                result.add(it.next());
            }
            assertThat("Iteration " + l,result.size(),equalTo(480));
        }
    }
    @Test
    public void flatMapPIterateNoConc(){
        // int l = 0;
        for(int l=0;l<1_000;l++)
        {
            System.out.println("Iteration " + l);
            Iterator<Integer> it = this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> of2(i, i * 2, i * 4, 5, 6, 7, 8, 9)
                            .mergeMap(x -> of2(5, 6, 7, 7, 8, 9)))
                    .iterator();
            List<Integer> result = new ArrayList<>(480);
            while(it.hasNext()){
                result.add(it.next());
            }
            assertThat("Iteration " + l,result.size(),equalTo(480));
        }
    }
    @Test
    public void flatMapPubIteration(){
        Iterator<Integer> it = of(1,2,3,4)
                .mergeMap(i->of(5,6)
                        .mergeMap(k->of(k)))
                .iterator();

        assertThat(ReactiveSeq.fromIterator(it).size(),equalTo(8));
    }
    @Test
    public void flatMapPub1(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);

            assertThat(Arrays.asList(5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7, 5, 6, 7).size(),
                    equalTo(this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                            .mergeMap(i -> of(i, i * 2, i * 4)
                                    .mergeMap(x -> of(5, 6, 7)))
                            .toList().size()));

        }
    }
    @Test
    public void flatMapPub(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);

            System.out.println(this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> of(i, i * 2, i * 4)
                            .mergeMap(x -> of(5, 6, 7)))
                    .toList());

        }
    }
    @Test
    public void flatMapP(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> of(i, i * 2, i * 4)
                            .mergeMap(x -> of(5, 6, 7)))
                    .toList());
        }
    }
    @Test
    public void flatMapPConc(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .mergeMap(i -> of2(i, i * 2, i * 4,5,6,7,8,9)
                            .mergeMap(x -> of2(5, 6, 7,7,8,9)))
                    .toList());
        }
    }
    @Test
    public void flatMapP3(){
        System.out.println(this.of(1,2)
                .mergeMap(i->of(i,i*2,i*4)
                        .mergeMap(x->of(5,6,7)
                                .mergeMap(y->of(2,3,4))))
                .toList());
    }
    @Test
    public void flatMapP2(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.of("1", "2")
                    .mergeMap(i -> of(1, 2,3))
                    .mergeMap(x -> of('a','b'))
                    .toList());
        }
    }
    @Test
    public void flatMapP2a(){
        for(int l=0;l<1_000;l++) {
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println("************Iteration " + l);
            System.out.println(this.of("1", "2","3")
                    .mergeMap(i -> of(1, 2,3,4,5))
                    .mergeMap(x -> of('a','b'))
                    .toList());
        }
    }
    @Test
    public void rangeMaxConcurrencyM32() {
        List<Integer> list =range(0, 1_000_000).mergeMap(32,Flux::just).toList();

        assertThat(list.size(), CoreMatchers.equalTo(1_000_000));
    }
    @Test
    public void rangeMaxConcurrencyM64() {
        List<Integer> list = range(0, 1_000_000).mergeMap(64,Flux::just).toList();

        assertThat(list.size(), CoreMatchers.equalTo(1_000_000));
    }
    @Test
    public void nullReactiveStreamsPublisher() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        range(0, 1000).mergeMap(v -> Flux.just((Integer)null))
            .forEach(n->{
                data.set(true);
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertFalse(data.get());
        assertFalse(complete.get());
        while(error.get()==null){
            LockSupport.parkNanos(10l);
        }
        assertThat(error.get(),instanceOf(NullPointerException.class));
    }
    @Test
    public void nullReactiveSeq() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        range(0, 1000).mergeMap(v -> (v%2==0 ? Spouts.of((Integer)null): Spouts.of(v)))
            .forEach(n->{
                data.set(true);
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertTrue(data.get());
        assertTrue(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));
    }

    @Test
    public void emptyTest() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        this.<Integer>empty().mergeMap(v -> Flux.just(v)).forEach(n->{
            data.set(true);
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertFalse(data.get());
        assertTrue(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));
    }

    @Test
    public void innerEmpty() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        range(0,  1000).mergeMap(v -> Flux.<Integer>empty()).forEach(n->{
            data.set(true);
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }
        assertFalse(data.get());
        assertTrue(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));
    }

    @Test
    public void simpleMergeMap() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        range(0, 1000).mergeMap(Flux::just).forEach(n->{
            data.incrementAndGet();
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }
        assertThat(data.get(), CoreMatchers.equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));

    }

    @Test
    public void mixedMergeMap() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        range(0, 1000).mergeMap(
            v -> v % 2 == 0 ? Flux.just(v) : Flux.fromIterable(Arrays.asList(v))).forEach(n->{
            data.incrementAndGet();
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(), CoreMatchers.equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));
    }

    @Test
    public void mergeMapMixedIncremental() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription c = range(0, 1000).mergeMap(v -> v % 2 == 0 ? Spouts.of(v) : ReactiveSeq.of(v)).forEach(0, n -> {
            data.incrementAndGet();
        }, e -> {
            error.set(e);
        }, () -> {
            complete.set(true);
        });
        assertThat(data.get(), CoreMatchers.equalTo(0));
        assertFalse(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));



        c.request(500);
        while(data.get()!=500){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(), CoreMatchers.equalTo(500));
        assertFalse(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));



        c.request(500);
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(), CoreMatchers.equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));
    }

    @Test
    public void mergeMapMixedOversubscribed() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription c = range(0, 1000).mergeMap(v -> v % 2 == 0 ? Spouts.of(v) : Seq.of(v)).forEach(0, n -> {
            data.incrementAndGet();
        }, e -> {
            error.set(e);
        }, () -> {
            complete.set(true);
        });
        assertThat(data.get(), CoreMatchers.equalTo(0));
        assertFalse(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));



        c.request(500);
        while(data.get()!=500){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(), CoreMatchers.equalTo(500));
        assertFalse(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));



        c.request(1500);
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(), CoreMatchers.equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));
    }

    @Test
    public void mergeMapIncremental() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription c =  range(0, 1000).mergeMap(Flux::just).forEach(0, n -> {
            data.incrementAndGet();
        }, e -> {
            error.set(e);
        }, () -> {
            complete.set(true);
        });

        assertThat(data.get(), CoreMatchers.equalTo(0));
        assertFalse(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));


        c.request(500);

        while(data.get()!=500){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(), CoreMatchers.equalTo(500));
        assertFalse(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));



        c.request(500);
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(), CoreMatchers.equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));
    }

    @Test
    public void mergeMapIncrementalOversubscribed() throws InterruptedException {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription c =  range(0, 1000).mergeMap(Flux::just).forEach(0, n -> {
            data.incrementAndGet();
        }, e -> {
            error.set(e);
        }, () -> {
            complete.set(true);
        });

        assertThat(data.get(), CoreMatchers.equalTo(0));
        assertFalse(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));


        c.request(500);

        while(data.get()!=500){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(), CoreMatchers.equalTo(500));
        assertFalse(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));



        c.request(1500);
        while(!complete.get()){
            LockSupport.parkNanos(10l);
        }

        assertThat(data.get(), CoreMatchers.equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(), CoreMatchers.equalTo(null));
    }

}

package cyclops.reactive;

import com.oath.cyclops.types.reactive.AsyncSubscriber;
import com.oath.cyclops.types.reactive.ReactiveSubscriber;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.companion.Monoids;
import cyclops.companion.Semigroups;
import com.oath.cyclops.async.QueueFactories;
import com.oath.cyclops.async.adapters.Topic;


import cyclops.data.Seq;
import cyclops.function.Effect;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import cyclops.data.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.oath.cyclops.types.foldable.Evaluation.LAZY;
import static cyclops.reactive.ReactiveSeq.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 20/01/2017.
 */
public class SpoutsTest {

    int i=0;

    Executor ex = Executors.newFixedThreadPool(5);

    int count = 0;
    @Before
    public void setup(){
        i=0;
        count=0;
        err = null;
    }

    @Test
    public void plusAllSize(){

        ReactiveSeq<Integer> vec = Spouts.empty();
        vec = vec.insertAt(Math.max(0,vec.size()),Spouts.of(1));
        System.out.println("Vec1 " + vec.vector());
        vec = vec.insertAt(Math.max(0,vec.size()),Spouts.of(2));

       // System.out.println("Vec " + vec.vector());
        /**Iterator<Integer> it = vec.iterator();
        while(it.hasNext()){
            System.out.println(it.next());
        }**/
        assertThat(vec,equalTo(Spouts.of(1,2)));
    }
    @Test
    public void rangeMaxConcurrencyM32() {
        List<Integer> list =Spouts.range(0, 1_000_000).mergeMap(32,Flux::just).toList();

       assertThat(list.size(),equalTo(1_000_000));
    }
    @Test
    public void rangeMaxConcurrencyM64() {
        List<Integer> list =Spouts.range(0, 1_000_000).mergeMap(64,Flux::just).toList();

        assertThat(list.size(),equalTo(1_000_000));
    }
    @Test
    public void nullReactiveStreamsPublisher() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.range(0, 1000).mergeMap(v -> Flux.just((Integer)null))
            .forEach(n->{
                data.set(true);
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

          assertFalse(data.get());
          assertFalse(complete.get());
          assertThat(error.get(),instanceOf(NullPointerException.class));
    }
    @Test
    public void nullReactiveSeq() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.range(0, 1000).mergeMap(v -> (v%2==0 ? Spouts.of((Integer)null): Spouts.of(v)))
            .forEach(n->{
                data.set(true);
            },e->{
                error.set(e);
            },()->{
                complete.set(true);
            });

        assertTrue(data.get());
        assertTrue(complete.get());
        assertThat(error.get(),equalTo(null));
    }

    @Test
    public void empty() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.<Integer>empty().mergeMap(v -> Flux.just(v)).forEach(n->{
            data.set(true);
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });

        assertFalse(data.get());
        assertTrue(complete.get());
        assertThat(error.get(),equalTo(null));
    }

    @Test
    public void innerEmpty() {
        AtomicBoolean data = new AtomicBoolean(false);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.range(0,  1000).mergeMap(v -> Flux.<Integer>empty()).forEach(n->{
            data.set(true);
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });
        assertFalse(data.get());
        assertTrue(complete.get());
        assertThat(error.get(),equalTo(null));
    }

    @Test
    public void simpleMergeMap() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.range(0, 1000).mergeMap(Flux::just).forEach(n->{
            data.incrementAndGet();
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });
        assertThat(data.get(),equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(),equalTo(null));

    }

    @Test
    public void mixedMergeMap() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Spouts.range(0, 1000).mergeMap(
            v -> v % 2 == 0 ? Flux.just(v) : Flux.fromIterable(Arrays.asList(v))).forEach(n->{
            data.incrementAndGet();
        },e->{
            error.set(e);
        },()->{
            complete.set(true);
        });

        assertThat(data.get(),equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(),equalTo(null));
    }

    @Test
    public void mergeMapMixedIncremental() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription c = Spouts.range(0, 1000)
                                .mergeMap(v -> v % 2 == 0 ? Spouts.of(v) : ReactiveSeq.of(v))
                                .forEach(0, n -> {
                                                data.incrementAndGet();
                                            }, e -> {
                                                error.set(e);
                                            }, () -> {
                                                complete.set(true);
                                            });
        assertThat(data.get(),equalTo(0));
        assertFalse(complete.get());
        assertThat(error.get(),equalTo(null));



        c.request(500);

        assertThat(data.get(),equalTo(500));
        assertFalse(complete.get());
        assertThat(error.get(),equalTo(null));



        c.request(500);

        assertThat(data.get(),equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(),equalTo(null));
    }

    @Test
    public void mergeMapMixedOversubscribed() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription c = Spouts.range(0, 1000).mergeMap(v -> v % 2 == 0 ? Spouts.of(v) : Seq.of(v)).forEach(0, n -> {
            data.incrementAndGet();
        }, e -> {
            error.set(e);
        }, () -> {
            complete.set(true);
        });
        assertThat(data.get(),equalTo(0));
        assertFalse(complete.get());
        assertThat(error.get(),equalTo(null));



        c.request(500);

        assertThat(data.get(),equalTo(500));
        assertFalse(complete.get());
        assertThat(error.get(),equalTo(null));



        c.request(1500);

        assertThat(data.get(),equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(),equalTo(null));
    }

    @Test
    public void mergeMapIncremental() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription c =  Spouts.range(0, 1000).mergeMap(Flux::just).forEach(0, n -> {
            data.incrementAndGet();
        }, e -> {
            error.set(e);
        }, () -> {
            complete.set(true);
        });

        assertThat(data.get(),equalTo(0));
        assertFalse(complete.get());
        assertThat(error.get(),equalTo(null));


        c.request(500);

        assertThat(data.get(),equalTo(500));
        assertFalse(complete.get());
        assertThat(error.get(),equalTo(null));



        c.request(500);

        assertThat(data.get(),equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(),equalTo(null));
    }

    @Test
    public void mergeMapIncrementalOversubscribed() {
        AtomicInteger data = new AtomicInteger(0);
        AtomicBoolean complete = new AtomicBoolean(false);
        AtomicReference<Throwable> error = new AtomicReference<Throwable>(null);

        Subscription c =  Spouts.range(0, 1000).mergeMap(Flux::just).forEach(0, n -> {
            data.incrementAndGet();
        }, e -> {
            error.set(e);
        }, () -> {
            complete.set(true);
        });

        assertThat(data.get(),equalTo(0));
        assertFalse(complete.get());
        assertThat(error.get(),equalTo(null));


        c.request(500);

        assertThat(data.get(),equalTo(500));
        assertFalse(complete.get());
        assertThat(error.get(),equalTo(null));



        c.request(1500);

        assertThat(data.get(),equalTo(1000));
        assertTrue(complete.get());
        assertThat(error.get(),equalTo(null));
    }

    Throwable err;
    @Test
    public void dropRightValues(){
        assertThat(of(1,2,3).dropRight(-1).seq(), Matchers.equalTo(of(1,2,3).seq()));
        assertThat(of(1,2,3).dropRight(0).seq(), Matchers.equalTo(of(1,2,3).seq()));
        assertThat(of(1,2,3).dropRight(1).seq(), Matchers.equalTo(of(1,2).seq()));
        assertThat(of(1,2,3).dropRight(2).seq(), Matchers.equalTo(of(1).seq()));
        assertThat(of(1,2,3).dropRight(3).seq(), Matchers.equalTo(of().seq()));
        assertThat(of(1,2,3).dropRight(4).seq(), Matchers.equalTo(of().seq()));
    }
    @Test
    public void takeRightValues(){
        assertThat(of(1,2,3).takeRight(-1).seq(), Matchers.equalTo(of().seq()));
        assertThat(of(1,2,3).takeRight(0).seq(), Matchers.equalTo(of().seq()));
        assertThat(of(1,2,3).takeRight(1).seq(), Matchers.equalTo(of(3).seq()));
        assertThat(of(1,2,3).takeRight(2).seq(), Matchers.equalTo(of(2,3).seq()));
        assertThat(of(1,2,3).takeRight(3).seq(), Matchers.equalTo(of(1,2,3).seq()));
        assertThat(of(1,2,3).takeRight(4).seq(), Matchers.equalTo(of(1,2,3).seq()));
    }
    @Test
    public void generateErrorTest(){
        Spouts.generate(()->{throw new RuntimeException();}).forEach(System.out::println,t->err=t);
        assertNotNull(err);

    }

    @Test
    public void deferErrorTest(){
        Spouts.defer(()->{throw new RuntimeException();}).forEach(System.out::println,t->err=t);
        assertNotNull(err);
    }

    @Test
    public void reactiveBuffer() throws InterruptedException {

        Subscription sub = Spouts.reactiveBuffer(16, s -> {

            s.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    if(i==0) {

                        Effect e = () -> {


                            s.onNext("hello " + i++);
                        };
                        e.cycle(30).runAsync();
                    }
                }

                @Override
                public void cancel() {

                }
            });

        }).forEach(2, in->count++);

        //count will be 2, buffer will be 16
        Thread.sleep(500);

        sub.request(30);

        assertThat(i,equalTo(30));
        assertThat(count,equalTo(18));
    }
    AtomicInteger counter;
    @Test
    public void reactiveBufferCycle() throws InterruptedException {
        counter = new AtomicInteger(0);

        Subscription sub = Spouts.reactiveBuffer(10, s -> {

            s.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    if(counter.get()==0) {
                        Effect e = () -> {

                            s.onNext("hello " +counter.incrementAndGet());
                        };
                        e.cycleAsync(30,ex).run();
                    }
                }

                @Override
                public void cancel() {

                }
            });

        }).forEach(2, in->count++);

        Thread.sleep(510);
        sub.request(30);


        assertThat(counter.get(),equalTo(30));
        assertThat(count,equalTo(18));
    }
    @Test
    public void reactiveBufferBlock() throws InterruptedException {

        Subscription sub = Spouts.reactiveBufferBlock(10, s -> {

            s.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    if(i==0) {
                        Effect e = () -> {


                            s.onNext("hello " + i++);
                        };
                        e.cycle(30).runAsync();
                    }
                }

                @Override
                public void cancel() {

                }
            });

        }).forEach(2, in->count++);

        Thread.sleep(500);
        sub.request(30);
        Thread.sleep(500);
        assertThat(i,equalTo(30));
        assertThat(count,equalTo(30));
    }

    @Test
    public void asyncBufferBlock() throws InterruptedException {

        Subscription sub = Spouts.asyncBufferBlock(10, s -> {
                    if (i == 0) {
                        Effect e = () -> {
                            s.onNext("hello " + i++);
                        };
                        e.cycle(30).runAsync();
                    }
                }
            ).forEach(2, in->count++);



        Thread.sleep(500);
        sub.request(30);
        Thread.sleep(500);
        assertThat(i,equalTo(30));
        assertThat(count,equalTo(30));

    }
    @Test
    public void asyncStream(){
        assertThat(Arrays.asList(1,2,3),equalTo(Spouts.async(ReactiveSeq.of(1,2,3),Executors.newFixedThreadPool(1)).toList()));
    }
    @Test
    public void asyncStreamAsync(){
        assertThat(Arrays.asList(1,2,3),equalTo(Spouts.async(ReactiveSeq.of(1,2,3),Executors.newFixedThreadPool(1)).toList()));
    }
    @Test
    public void combineLatest(){
       for(int i=0;i<10_000;i++) {

           assertThat("Iteration " + i,Spouts.of(100, 200, 300)
                            .zipLatest(nextAsyncRS(), (a, b) -> b)
                           .toList(),
                   hasItems(1,2));
           assertThat("Iteration " + i,Spouts.of(100, 200, 300)
                           .zipLatest(nextAsyncRS(), (a, b) -> a)
                           .toList(),
                   hasItems(300));
       }
   }




    @Test
    public void iteratePredicate(){
        Iterator<Integer> it = Spouts.iterate(1,i->i<4,i->i+1).iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(Arrays.asList(1,2,3)));
    }
    @Test
    public void iterate(){
        Spouts.iterate(1,i->i+1).limit(3).printOut();
        Iterator<Integer> it = Spouts.iterate(1,i->i+1).limit(3).iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(Arrays.asList(1,2,3)));
    }
    @Test
    public void array(){
        Iterator<Integer> it = Spouts.of(1, 2, 3).iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(Arrays.asList(1,2,3)));
    }
    @Test
    public void iterable(){
        Iterator<Integer> it = Spouts.fromIterable(Arrays.asList(1, 2, 3)).iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(Arrays.asList(1,2,3)));
    }
    @Test
    public void range(){
        Iterator<Integer> it = Spouts.range(1,4).iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(Arrays.asList(1,2,3)));
    }
    @Test
    public void rangeLong(){
        Iterator<Long> it = Spouts.rangeLong(1,4).iterator();
        List<Long> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(Arrays.asList(1l,2l,3l)));
    }

    @Test
    public void fluxConcat(){
        Iterator<Integer> it = Spouts.from(Flux.concat(Flux.just(1, 2, 3), Flux.just(10, 20, 30))).iterator();
        while(it.hasNext()){
            System.out.println("next " + it.next());
        }
    }
    @Test
    public void fluxMerge(){
        Iterator<Integer> it = Spouts.from(Flux.merge(Flux.just(1, 2, 3), Flux.just(10, 20, 30))).iterator();
        while(it.hasNext()){
            System.out.println("next " + it.next());
        }
    }
    @Test
    public void publishOn(){
        assertThat(Spouts.reactive(Stream.of(1,2),Executors.newFixedThreadPool(1)).toList(), CoreMatchers.equalTo(Arrays.asList(1,2)));
    }
    @Test
    public void iteratorTest(){
        for(int x=100;x<10000;x=x+1000) {
            Set<Integer> result = new HashSet<>(x);
            int max = x;
            Iterator<Integer> it = Spouts.<Integer>async(sub -> {

                for (int i = 0; i < max; i++) {
                    sub.onNext(i);
                }
                sub.onComplete();

            }).iterator();


            while (it.hasNext()) {
                result.add(it.next());
            }
            assertThat(result.size(),equalTo(x));
        }
    }

    @Test
    public void parallelFanOut2(){

        assertThat(Spouts.of(1,2,3,4)
                .parallelFanOut(ForkJoinPool.commonPool(), s1->s1.filter(i->i%2==0).map(i->i*2),
                        s2->s2.filter(i->i%2!=0).map(i->i*100))
                .toList(), Matchers.equalTo(Arrays.asList(4,100,8,300)));
        assertThat(Spouts.of(1,2,3,4,5,6,7,8,9)
                .parallelFanOut(ForkJoinPool.commonPool(),s1->s1.filter(i->i%3==0).map(i->i*2),
                        s2->s2.filter(i->i%3==1).map(i->i*100),
                        s3->s3.filter(i->i%3==2).map(i->i*1000))
                .toList(), Matchers.equalTo(Arrays.asList(6, 100, 2000, 12, 400, 5000, 18, 700, 8000)));
        assertThat(Spouts.of(1,2,3,4,5,6,7,8,9,10,11,12)
                .parallelFanOut(ForkJoinPool.commonPool(),s1->s1.filter(i->i%4==0).map(i->i*2),
                        s2->s2.filter(i->i%4==1).map(i->i*100),
                        s3->s3.filter(i->i%4==2).map(i->i*1000),
                        s4->s4.filter(i->i%4==3).map(i->i*10000))
                .toList(), Matchers.equalTo(Arrays.asList(8, 100, 2000, 30000, 16, 500, 6000, 70000, 24, 900, 10000, 110000)));
    }
    @Test
    public void async(){
       assertThat(Spouts.async(sub->{
            sub.onNext(1);
            sub.onNext(2);
            sub.onComplete();
           }).toList(),equalTo(Arrays.asList(1,2)));
    }
    @Test
    public void generate() throws Exception {
        assertThat(Spouts.generate(()->1)
                         .limit(5)
                         .toList(),equalTo(Arrays.asList(1,1,1,1,1)));

        assertThat(Spouts.generate(()->Spouts.of(1))
                .limit(1)
                .flatMap(i->i)
                .toList(),equalTo(Arrays.asList(1)));

        assertThat(Spouts.generate(()->Spouts.of(1,2))
                .limit(1)
                .flatMap(i->i)
                .collect(Collectors.toList()),equalTo(Arrays.asList(1,2)));
    }

    @Test
    public void interval() throws Exception {

        assertThat(Spouts.interval(10, Executors.newScheduledThreadPool(1))
                .limit(4)
                .collect(Collectors.toList()).size(),greaterThan(0));
    }
    @Test
    public void intervalCron() throws Exception {
        assertThat(Spouts.interval("* * * * * ?", Executors.newScheduledThreadPool(1))
                        .limit(2)
            .peek(System.out::println)
                        .collect(Collectors.toList()).size(),greaterThan(0));
    }
    @Test
    public void scheduleCron() throws Exception {

        assertThat(Spouts.schedule(Stream.of(1,2,3),"* * * * * ?", Executors.newScheduledThreadPool(1))
            .limit(5)
            .peek(System.out::println)
            .collect(Collectors.toList()).size(),equalTo(3));

    }

    @Test
    public void collect(){
        assertThat(Spouts.of(1,2,3).collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
    }
    @Test
    public void defer() throws Exception {

        Flux.just(1,2,3).publish(f->f);
        assertThat(Spouts.of(1,2,3).flatMap(i->Spouts.of(i)).collect(Collectors.toList())
                        ,equalTo(Arrays.asList(1,2,3)));
        assertThat(Spouts.defer(()-> Flux.just(1,2,3))
                .collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
        assertThat(Spouts.deferFromStream(()-> ReactiveSeq.of(1,2,3))
                .collect(Collectors.toList()),equalTo(Arrays.asList(1,2,3)));
    }

    @Test
    public void ambMonoid(){

        for(int i=0;i<1000;i++) {
            assertThat(Monoids.<Integer>ambReactiveSeq().foldLeft(Stream.of((Spouts.of(1, 2, 3)), Spouts.of(100, 200, 300))).toList(), isOneOf(Arrays.asList(100, 200, 300), Arrays.asList(1, 2, 3)));

        }

        //    assertThat(Monoids.<Integer>amb()
       //         .applyHKT(nextAsync(),Spouts.of(100,200,300)).listX(),equalTo(ListX.of(100,200,300)));
     //   assertThat(Monoids.<Integer>amb().reduce(Stream.of((nextAsync()),Spouts.of(100,200,300))).listX(),equalTo(ListX.of(100,200,300)));
       // assertThat(Spouts.amb(ListX.of(nextAsync(),Spouts.of(100,200,300))).listX(),equalTo(ListX.of(100,200,300)));
    }

    @Test
    public void nextAsyncToListX(){
        System.out.println(nextAsync().toList());

    }
    @Test
    public void publishToAndMerge(){
        com.oath.cyclops.async.adapters.Queue<Integer> queue = QueueFactories.<Integer>boundedNonBlockingQueue(10)
                .build();

        Thread t=  new Thread( ()-> {

            while(true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Closing!");
                queue.close();

            }
        });
        t.start();
        assertThat(Spouts.of(1,2,3)
                .publishTo(queue)
                .peek(System.out::println)
                .merge(queue)
                .toList(), Matchers.equalTo(Arrays.asList(1,1,2,2,3,3)));
    }
    @Test
    public void duplicateTest(){
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> tp = Spouts.of(1, 2, 3, 4).duplicate();

     //   tp._1.printOut();
     //  tp._2.printOut();
        System.out.println("Merge!");
     //   tp._1.mergeP(tp._2).printOut();

        Spouts.of("a","b","c").mergeP(ReactiveSeq.of("bb","cc")).printOut();
    }
    @Test
    public void fanOut2(){
        assertThat(Spouts.of(1,2,3,4)
                .fanOut(s1->s1.filter(i->i%2==0).map(i->i*2),
                        s2->s2.filter(i->i%2!=0).map(i->i*100))
                .toList(), Matchers.equalTo(Arrays.asList(4, 100, 8, 300)));
    }

    @Test
    public void fanOut(){

        assertThat(Spouts.of(1,2,3,4)
                .fanOut(s1->s1.filter(i->i%2==0).map(i->i*2),
                        s2->s2.filter(i->i%2!=0).map(i->i*100))
                .toList(), Matchers.equalTo(Arrays.asList(4, 100, 8, 300)));
        assertThat(Spouts.of(1,2,3,4,5,6,7,8,9)
                .fanOut(s1->s1.filter(i->i%3==0).map(i->i*2),
                        s2->s2.filter(i->i%3==1).map(i->i*100),
                        s3->s3.filter(i->i%3==2).map(i->i*1000))
                .toList(), Matchers.equalTo(Arrays.asList(6, 100, 2000, 12, 400, 5000, 18, 700, 8000)));
        assertThat(Spouts.of(1,2,3,4,5,6,7,8,9,10,11,12)
                .fanOut(s1->s1.filter(i->i%4==0).map(i->i*2),
                        s2->s2.filter(i->i%4==1).map(i->i*100),
                        s3->s3.filter(i->i%4==2).map(i->i*1000),
                        s4->s4.filter(i->i%4==3).map(i->i*10000))
                .toList(), Matchers.equalTo(Arrays.asList(8, 100, 2000, 30000, 16, 500, 6000, 70000, 24, 900, 10000, 110000)));
    }
    @Test
    public void iteratePred(){
        System.out.println(Spouts.iterate(0,i->i<10,i->i+1).toList());
        assertThat(Spouts.iterate(0,i->i<10,i->i+1)
                .toList().size(), Matchers.equalTo(10));
    }
    @Test
    public void broadcastTest(){
        Topic<Integer> topic = Spouts.of(1,2,3)
                .broadcast();


        ReactiveSeq<Integer> stream1 = topic.stream();
        ReactiveSeq<Integer> stream2 = topic.stream();
        assertThat(stream1.toList(), Matchers.equalTo(Arrays.asList(1,2,3)));
        assertThat(stream2.toList(), Matchers.equalTo(Arrays.asList(1,2,3)));

    }
    @Test
    public void broadcastThreads() throws InterruptedException {

        Topic<Integer> topic = Spouts.range(0,100_000)
                .broadcast();

        Thread t=  new Thread( ()-> {
            ReactiveSeq<Integer> stream2 = topic.stream();
            assertThat(stream2.takeRight(1).singleOrElse(null), Matchers.equalTo(99_999));
        });


        Thread t2 = new Thread( ()->{
        ReactiveSeq<Integer> stream1 = topic.stream();

        assertThat(stream1.takeRight(1).singleOrElse(null), Matchers.equalTo(99_999));
});
        t.start();
        t2.start();
        t.join();
        t2.join();

    }



    @Test
    public void ambTest(){
        assertThat(Spouts.of(1,2,3).ambWith(Flux.just(10,20,30)).toList(), Matchers.isOneOf(Arrays.asList(1,2,3),Arrays.asList(10,20,30)));
    }



    @Test
    public void merge(){

        Spouts.mergeLatest(Spouts.of(1,2,3),Spouts.of(5,6,7)).printOut();
        Spouts.mergeLatest(Spouts.of(10,20,30),nextAsyncRS()).printOut();



    }
    @Test
    public void mergeLatest(){

       Spouts.mergeLatest(Spouts.of(1,2,3),Spouts.of(5,6,7)).printOut();
       Spouts.mergeLatest(Spouts.of(10,20,30),nextAsyncRS()).printOut();

    }
    @Test
    public void testCollector(){
        Collector<Integer, ?, List<Integer>> list = Collectors.toList();
        List<Integer> res= null;
        res =Stream.of(1,2,3)
                    .map(i->i+2)
                    .collect(list);
        System.out.println("res " + res);
     //   Stream.of(1,2,3).collect((Supplier)list.supplier(),list.accumulator(),list.combiner());
    }
    @Test
    public void ambSemigroupTest(){

        System.out.println(ReactiveSeq.fromPublisher(
    Semigroups.<Integer>amb()
        .apply(Spouts.of(100,200,300),nextAsyncRS())).collect(Collectors.toList()));
/**
        ReactiveSeq.fromPublisher(Flux.amb(nextAsync(),nextAsyncRS()))
                .forEach(System.out::println);
**/
 /**
        assertThat(SemigroupK.<Integer>amb()
                .applyHKT(Spouts.of(100,200,300),nextAsyncRS()).listX(),equalTo(ListX.of(100,200,300)));
        assertThat(SemigroupK.<Integer>amb()
                .applyHKT(nextAsyncRS(),Spouts.of(100,200,300)).listX(),equalTo(ListX.of(100,200,300)));
**/
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

    @Test
    public void takeWhile(){
        assertThat(Spouts.of(1,2,3).takeWhile(i->i<3).toList(),equalTo(Arrays.asList(1,2)));
        assertThat(Spouts.<Integer>of().takeWhile(i->i<3).toList(), equalTo(Arrays.asList()));
        assertThat(Spouts.range(1,1_000_000).takeWhile(i->i<300_000).toList(),
            Matchers.equalTo(ReactiveSeq.range(1,300_000).toList()));

        AtomicInteger count = new AtomicInteger(0);
        int size = Spouts.range(1,1_000_000)
            .peek(i->count.incrementAndGet())
            .takeWhile(i->i<300_000).toList().size();
        assertThat(count.get(), Matchers.equalTo(300_000));
        assertThat(size, Matchers.equalTo(299999));
    }
    @Test
    public void takeWhileInclusive(){
        assertThat(Spouts.of(1,2,3).takeWhileInclusive(i->i<3).toList(), equalTo(Arrays.asList(1,2,3)));
        assertThat(Spouts.<Integer>of().takeWhileInclusive(i->i<3).toList(), equalTo(Arrays.asList()));
        assertThat(Spouts.range(1,1_000_000).takeWhileInclusive(i->i<300_000).toList(),
                            equalTo(ReactiveSeq.range(1,300_001).toList()));

        AtomicInteger count = new AtomicInteger(0);
        int size = Spouts.range(1,1_000_000)
            .peek(i->count.incrementAndGet())
            .takeWhileInclusive(i->i<300_000).toList().size();
        assertThat(count.get(), equalTo(300_000));
        assertThat(size, equalTo(300000));
    }

}

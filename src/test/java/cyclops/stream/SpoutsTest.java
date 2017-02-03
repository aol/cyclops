package cyclops.stream;

import com.aol.cyclops2.types.stream.reactive.AsyncSubscriber;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import cyclops.Monoids;
import cyclops.Semigroups;
import cyclops.async.Future;
import cyclops.async.QueueFactories;
import cyclops.async.Topic;
import cyclops.collections.ListX;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 20/01/2017.
 */
public class SpoutsTest {

    @Test
    public void iteratePredicate(){
        Iterator<Integer> it = Spouts.iterate(1,i->i<4,i->i+1).iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void iterate(){
        Iterator<Integer> it = Spouts.iterate(1,i->i+1).limit(3).iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void array(){
        Iterator<Integer> it = Spouts.of(1, 2, 3).iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void iterable(){
        Iterator<Integer> it = Spouts.fromIterable(Arrays.asList(1, 2, 3)).iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void range(){
        Iterator<Integer> it = Spouts.range(1,4).iterator();
        List<Integer> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void rangeLong(){
        Iterator<Long> it = Spouts.rangeLong(1,4).iterator();
        List<Long> list = new ArrayList<>();
        while(it.hasNext()){
            list.add(it.next());

        }
        assertThat(list,equalTo(ListX.of(1l,2l,3l)));
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
        assertThat(Spouts.publishOn(Stream.of(1,2),Executors.newFixedThreadPool(1)).toList(), CoreMatchers.equalTo(ListX.of(1,2)));
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
                .toListX(), Matchers.equalTo(ListX.of(4,100,8,300)));
        assertThat(Spouts.of(1,2,3,4,5,6,7,8,9)
                .parallelFanOut(ForkJoinPool.commonPool(),s1->s1.filter(i->i%3==0).map(i->i*2),
                        s2->s2.filter(i->i%3==1).map(i->i*100),
                        s3->s3.filter(i->i%3==2).map(i->i*1000))
                .toListX(), Matchers.equalTo(ListX.of(6, 100, 2000, 12, 400, 5000, 18, 700, 8000)));
        assertThat(Spouts.of(1,2,3,4,5,6,7,8,9,10,11,12)
                .parallelFanOut(ForkJoinPool.commonPool(),s1->s1.filter(i->i%4==0).map(i->i*2),
                        s2->s2.filter(i->i%4==1).map(i->i*100),
                        s3->s3.filter(i->i%4==2).map(i->i*1000),
                        s4->s4.filter(i->i%4==3).map(i->i*10000))
                .toListX(), Matchers.equalTo(ListX.of(8, 100, 2000, 30000, 16, 500, 6000, 70000, 24, 900, 10000, 110000)));
    }
    @Test
    public void async(){
       assertThat(Spouts.async(sub->{
            sub.onNext(1);
            sub.onNext(2);
            sub.onComplete();
           }).toList(),equalTo(ListX.of(1,2)));
    }
    @Test
    public void generate() throws Exception {
        assertThat(Spouts.generate(()->1)
                         .limit(5)
                         .toListX(),equalTo(ListX.of(1,1,1,1,1)));

        assertThat(Spouts.generate(()->Spouts.of(1))
                .limit(1)
                .flatMap(i->i)
                .toListX(),equalTo(ListX.of(1)));

        assertThat(Spouts.generate(()->Spouts.of(1,2))
                .limit(1)
                .flatMap(i->i)
                .collect(Collectors.toList()),equalTo(ListX.of(1,2)));
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
                        .collect(Collectors.toList()).size(),greaterThan(0));
    }

    @Test
    public void collect(){
        assertThat(Spouts.of(1,2,3).collect(Collectors.toList()),equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void defer() throws Exception {

        assertThat(Spouts.of(1,2,3).flatMap(i->Spouts.of(i)).collect(Collectors.toList())
                        ,equalTo(ListX.of(1,2,3)));
        assertThat(Spouts.deferred(()-> Flux.just(1,2,3))
                .collect(Collectors.toList()),equalTo(ListX.of(1,2,3)));
        assertThat(Spouts.deferredS(()-> ReactiveSeq.of(1,2,3))
                .collect(Collectors.toList()),equalTo(ListX.of(1,2,3)));
    }

    @Test
    public void ambMonoid(){

        for(int i=0;i<1000;i++) {
            assertThat(Monoids.<Integer>ambReactiveSeq().reduce(Stream.of((Spouts.of(1, 2, 3)), Spouts.of(100, 200, 300))).toListX(), isOneOf(ListX.of(100, 200, 300), ListX.of(1, 2, 3)));

        }

        //    assertThat(Monoids.<Integer>amb()
       //         .apply(nextAsync(),Spouts.of(100,200,300)).toListX(),equalTo(ListX.of(100,200,300)));
     //   assertThat(Monoids.<Integer>amb().reduce(Stream.of((nextAsync()),Spouts.of(100,200,300))).toListX(),equalTo(ListX.of(100,200,300)));
       // assertThat(Spouts.amb(ListX.of(nextAsync(),Spouts.of(100,200,300))).toListX(),equalTo(ListX.of(100,200,300)));
    }

    @Test
    public void nextAsyncToListX(){
        nextAsync().toListX().printOut();

    }
    @Test
    public void publishToAndMerge(){
        cyclops.async.Queue<Integer> queue = QueueFactories.<Integer>boundedNonBlockingQueue(10)
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
                .toListX(), Matchers.equalTo(ListX.of(1,1,2,2,3,3)));
    }
    @Test
    public void duplicateTest(){
        Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> tp = Spouts.of(1, 2, 3, 4).duplicate();

     //   tp.v1.printOut();
     //  tp.v2.printOut();
        System.out.println("Merge!");
     //   tp.v1.mergeP(tp.v2).printOut();

        Spouts.of("a","b","c").mergeP(ReactiveSeq.of("bb","cc")).printOut();
    }
    @Test
    public void fanOut2(){
        assertThat(Spouts.of(1,2,3,4)
                .fanOut(s1->s1.filter(i->i%2==0).map(i->i*2),
                        s2->s2.filter(i->i%2!=0).map(i->i*100))
                .toListX(), Matchers.equalTo(ListX.of(4, 100, 8, 300)));
    }

    @Test
    public void fanOut(){

        assertThat(Spouts.of(1,2,3,4)
                .fanOut(s1->s1.filter(i->i%2==0).map(i->i*2),
                        s2->s2.filter(i->i%2!=0).map(i->i*100))
                .toListX(), Matchers.equalTo(ListX.of(4, 100, 8, 300)));
        assertThat(Spouts.of(1,2,3,4,5,6,7,8,9)
                .fanOut(s1->s1.filter(i->i%3==0).map(i->i*2),
                        s2->s2.filter(i->i%3==1).map(i->i*100),
                        s3->s3.filter(i->i%3==2).map(i->i*1000))
                .toListX(), Matchers.equalTo(ListX.of(6, 100, 2000, 12, 400, 5000, 18, 700, 8000)));
        assertThat(Spouts.of(1,2,3,4,5,6,7,8,9,10,11,12)
                .fanOut(s1->s1.filter(i->i%4==0).map(i->i*2),
                        s2->s2.filter(i->i%4==1).map(i->i*100),
                        s3->s3.filter(i->i%4==2).map(i->i*1000),
                        s4->s4.filter(i->i%4==3).map(i->i*10000))
                .toListX(), Matchers.equalTo(ListX.of(8, 100, 2000, 30000, 16, 500, 6000, 70000, 24, 900, 10000, 110000)));
    }
    @Test
    public void iteratePred(){
        System.out.println(Spouts.iterate(0,i->i<10,i->i+1).toListX());
        assertThat(Spouts.iterate(0,i->i<10,i->i+1)
                .toListX().size(), Matchers.equalTo(10));
    }
    @Test
    public void broadcastTest(){
        Topic<Integer> topic = Spouts.of(1,2,3)
                .broadcast();


        ReactiveSeq<Integer> stream1 = topic.stream();
        ReactiveSeq<Integer> stream2 = topic.stream();
        assertThat(stream1.toListX(), Matchers.equalTo(ListX.of(1,2,3)));
        assertThat(stream2.toListX(), Matchers.equalTo(ListX.of(1,2,3)));

    }
    @Test
    public void broadcastThreads() throws InterruptedException {

        Topic<Integer> topic = Spouts.range(0,100_000)
                .broadcast();

        Thread t=  new Thread( ()-> {
            ReactiveSeq<Integer> stream2 = topic.stream();
            assertThat(stream2.takeRight(1).single(), Matchers.equalTo(99_999));
        });
        t.start();

        ReactiveSeq<Integer> stream1 = topic.stream();

        assertThat(stream1.takeRight(1).single(), Matchers.equalTo(99_999));

        t.join();

    }



    @Test
    public void ambTest(){
        assertThat(Spouts.of(1,2,3).ambWith(Flux.just(10,20,30)).toListX(), Matchers.isOneOf(ListX.of(1,2,3),ListX.of(10,20,30)));
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
        assertThat(Semigroups.<Integer>amb()
                .apply(Spouts.of(100,200,300),nextAsyncRS()).toListX(),equalTo(ListX.of(100,200,300)));
        assertThat(Semigroups.<Integer>amb()
                .apply(nextAsyncRS(),Spouts.of(100,200,300)).toListX(),equalTo(ListX.of(100,200,300)));
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


            // Flux.just(1,2).subscribeAll(sub);


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

}
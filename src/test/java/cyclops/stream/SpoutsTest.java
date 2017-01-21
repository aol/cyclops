package cyclops.stream;

import com.aol.cyclops2.types.stream.reactive.AsyncSubscriber;
import com.aol.cyclops2.types.stream.reactive.ReactiveSubscriber;
import cyclops.Monoids;
import cyclops.Semigroups;
import cyclops.collections.ListX;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 20/01/2017.
 */
public class SpoutsTest {
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
        assertThat(Monoids.<Integer>amb().reduce(Stream.of((Spouts.of(1,2,3)),Spouts.of(100,200,300))).toListX(),equalTo(ListX.of(1,2,3)));

        //    assertThat(Monoids.<Integer>amb()
       //         .apply(nextAsync(),Spouts.of(100,200,300)).toListX(),equalTo(ListX.of(100,200,300)));
     //   assertThat(Monoids.<Integer>amb().reduce(Stream.of((nextAsync()),Spouts.of(100,200,300))).toListX(),equalTo(ListX.of(100,200,300)));
       // assertThat(Spouts.amb(ListX.of(nextAsync(),Spouts.of(100,200,300))).toListX(),equalTo(ListX.of(100,200,300)));
    }

    @Test
    public void nextAsyncToListX(){
        nextAsync().toListX().printOut();
        while(true){

        }
    }
    @Test
    public void ambSemigroupTest(){

        System.out.println(
    Semigroups.<Integer>amb()
        .apply(Spouts.of(100,200,300),nextAsyncRS()).collect(Collectors.toList()));
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


            // Flux.just(1,2).subscribe(sub);


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
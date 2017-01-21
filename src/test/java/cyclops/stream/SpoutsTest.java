package cyclops.stream;

import com.aol.cyclops2.types.stream.reactive.AsyncSubscriber;
import cyclops.Monoids;
import cyclops.Semigroups;
import cyclops.collections.ListX;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.concurrent.Executors;
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
     //    assertThat(Monoids.<Integer>ambReactiveSeq()
       //         .apply(nextAsync(),Spouts.of(100,200,300)).toListX(),equalTo(ListX.of(100,200,300)));
        assertThat(Monoids.<Integer>ambReactiveSeq().reduce(Stream.of((nextAsync()),Spouts.of(100,200,300))).toListX(),equalTo(ListX.of(100,200,300)));
       // assertThat(Spouts.amb(ListX.of(nextAsync(),Spouts.of(100,200,300))).toListX(),equalTo(ListX.of(100,200,300)));
    }

    @Test
    public void ambSemigroupTest(){


        assertThat(Semigroups.<Integer>ambReactiveSeq()
                .apply(Spouts.of(100,200,300),nextAsync()).toListX(),equalTo(ListX.of(100,200,300)));
        assertThat(Semigroups.<Integer>ambReactiveSeq()
                .apply(nextAsync(),Spouts.of(100,200,300)).toListX(),equalTo(ListX.of(100,200,300)));

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
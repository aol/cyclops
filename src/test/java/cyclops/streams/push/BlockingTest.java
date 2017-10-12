package cyclops.streams.push;

import com.aol.cyclops2.types.reactive.AsyncSubscriber;
import com.aol.cyclops2.types.reactive.ReactiveSubscriber;
import cyclops.collectionx.mutable.ListX;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by johnmcclean on 14/01/2017.
 */
public class BlockingTest {
    @Test
    public void blockingOrNot(){

        for(int k=0;k<10;k++) {
            AsyncSubscriber<Integer> sub = Spouts.asyncSubscriber();

            System.out.println("Starting!");
            Thread t = new Thread(() -> {
                //  System.out.println("Initiailizing..");
                while (!sub.isInitialized()) {
                    // System.out.println(sub.isInitialized());
                    LockSupport.parkNanos(0l);
                }
                //  System.out.println("Initialized!");
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("On Next!");
                for (int i = 0; i < 100; i++) {
                    sub.onNext(1);
                }
                System.out.println("On Complete!");
                sub.onComplete();
            });

            t.start();
            System.out.println("Setting up Stream!");
            //sub.onComplete();
            assertThat(sub.stream().peek(System.out::println).collect(Collectors.toList()).size(), equalTo(100));
            //   sub.reactiveStream().forEach(System.out::println);
            sub.onNext(1);
            System.out.println("End!");
        }

    }

    @Test
    public void reactiveStreams(){
        ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();

        Flux.just(1,2,3).subscribe(sub);
        sub.reactiveStream().forEach(System.out::println);
    }

    @Test
    public void spoutCollect(){
        assertThat(Spouts.of(1,2,3).collect(Collectors.toList()),equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void findFirst(){
        assertThat(Spouts.of(1,2,3).findFirst().get(),equalTo(1));
    }

    @Test
    public void testIterator(){
        assertThat(Spouts.of(1).iterator().next(),equalTo(1));
        assertThat(Spouts.of(1).iterator().hasNext(),equalTo(true));
        Iterator<Integer> it = Spouts.of(1,2).iterator();
        assertThat(it.hasNext(),equalTo(true));
        assertThat(it.next(),equalTo(1));
        assertThat(it.hasNext(),equalTo(true));
        assertThat(it.next(),equalTo(2));
        assertThat(it.hasNext(),equalTo(false));

    }

    Integer result = null;
    @Test
    public void simple(){

        Spouts.of(1).forEach(i->result = i);
        assertThat(result,equalTo(1));
        Spouts.of(1).map(i->i*2).forEach(i->result = i);
        assertThat(result,equalTo(2));
    }
    @Test
    public void simpleList(){
        List<Integer> list = new ArrayList<>();
        Spouts.of(1,2).forEach(list::add);
        assertThat(list,equalTo(ListX.of(1,2)));
        list = new ArrayList<>();
        Spouts.of(1,2).map(i->i*2).forEach(list::add);
        assertThat(list,equalTo(ListX.of(2,4)));
    }


    @Test
    public void collect(){
        ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
        ReactiveSeq.of(1,2,3).peek(System.out::println).subscribe(sub);
        System.out.println(sub.reactiveStream().peek(System.out::println).collect(Collectors.toList()));
    }
    @Test
    public void forEach(){
        ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
        ReactiveSeq.of(1,2,3).subscribe(sub);
        sub.reactiveStream().forEach(System.out::println);
    }

    @Test
    public void init(){
        ReactiveSubscriber<Integer> sub = Spouts.reactiveSubscriber();
        Flux.just(1,2,3).subscribe(sub);
        sub.reactiveStream().forEach(System.out::println);
        assertTrue(sub.isInitialized());
    }
}

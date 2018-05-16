package cyclops.reactive;

import cyclops.reactive.collections.immutable.BagX;
import cyclops.reactive.collections.immutable.LinkedListX;
import cyclops.reactive.collections.immutable.OrderedSetX;
import cyclops.reactive.collections.immutable.PersistentQueueX;
import cyclops.reactive.collections.immutable.PersistentSetX;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.reactive.collections.mutable.DequeX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.collections.mutable.QueueX;
import cyclops.reactive.collections.mutable.SetX;
import cyclops.reactive.collections.mutable.SortedSetX;

import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class FluxCollectionsTest {

    Executor ex = Executors.newFixedThreadPool(1);
    AtomicBoolean complete;
    Flux<Integer> async ;

    @Before
    public void setup(){
        complete = new AtomicBoolean(false);
        async =  Flux.from(Spouts.reactive(Stream.of(100,100,100), ex))
            .map(i->{
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return i;
            })
            .doOnComplete(()->complete.set(true));
    }
    @Test
    public void listX(){


        System.out.println("Initializing!");
        ListX<Integer> asyncList = FluxCollections.listX(async)
            .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.get(0);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void listX2(){

        FluxCollections.listX(Flux.interval(Duration.ofMillis(5), Schedulers.single()).doOnNext(System.out::println).take(2)
            .doOnComplete(()->complete.set(true)));

        System.out.println("Blocked? " + complete.get());
        assertFalse(complete.get());
        while(!complete.get()){

        }
        assertTrue(complete.get());
    }
    @Test
    public void queueX(){


        System.out.println("Initializing!");
        QueueX<Integer> asyncList = FluxCollections.queueX(async)
            .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.firstValue(-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void setX(){


        System.out.println("Initializing!");
        SetX<Integer> asyncList = FluxCollections.setX(async)
                                         .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.firstValue(-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void sortedSetX(){


        System.out.println("Initializing!");
        SortedSetX<Integer> asyncList = FluxCollections.sortedSetX(async)
                                                           .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.firstValue(-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void dequeX(){


        System.out.println("Initializing!");
        DequeX<Integer> asyncList = FluxCollections.dequeX(async)
            .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.firstValue(-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void linkedListX(){


        System.out.println("Initializing!");
        LinkedListX<Integer> asyncList = FluxCollections.linkedListX(async)
                                                            .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.getOrElse(0,-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void vectorX(){


        System.out.println("Initializing!");
        VectorX<Integer> asyncList = FluxCollections.vectorX(async)
            .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.getOrElse(0,-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void persistentQueueX(){


        System.out.println("Initializing!");
        PersistentQueueX<Integer> asyncList = FluxCollections.persistentQueueX(async)
            .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.firstValue(-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void persistentSetX(){


        System.out.println("Initializing!");
        PersistentSetX<Integer> asyncList = FluxCollections.persistentSetX(async)
            .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.firstValue(-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void orderedSetX(){


        System.out.println("Initializing!");
        OrderedSetX<Integer> asyncList = FluxCollections.orderedSetX(async)
            .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.firstValue(-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void bagX(){


        System.out.println("Initializing!");
        BagX<Integer> asyncList = FluxCollections.bagX(async)
            .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.firstValue(-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
}

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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;



public class SpoutsAsyncCollectionsTest {

    Executor ex = Executors.newFixedThreadPool(1);
    AtomicBoolean complete;
    ReactiveSeq<Integer> async ;

    @Before
    public void setup(){
        complete = new AtomicBoolean(false);

        async =  Spouts.async(Stream.of(100,100,100), ex)
                        .map(i->{
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return i;
                        })
                        .onComplete(()->complete.set(true));
    }


    @Test
    public void listX(){


        System.out.println("Initializing!");
        ListX<Integer> asyncList = ReactiveCollections.listX(async)
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
    public void queueX(){


        System.out.println("Initializing!");
        QueueX<Integer> asyncList = ReactiveCollections.queueX(async)
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
        SetX<Integer> asyncList = ReactiveCollections.setX(async)
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
        SortedSetX<Integer> asyncList = ReactiveCollections.sortedSetX(async)
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
    public void dequeX() throws InterruptedException {


        System.out.println("Initializing!");
        DequeX<Integer> asyncList = ReactiveCollections.dequeX(async)
            .map(i->i+1);


        boolean blocked = complete.get();
        System.out.println("Blocked? " + blocked);
        assertFalse(complete.get());


        int value = asyncList.firstValue(-1);

        System.out.println("First value is "  + value);
        assertThat(value,equalTo(101));

        Thread.sleep(1000);
        System.out.println("Blocked? " + complete.get());
        assertTrue(complete.get());
    }
    @Test
    public void linkedListX(){


        System.out.println("Initializing!");
        LinkedListX<Integer> asyncList = ReactiveCollections.linkedListX(async)
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
        VectorX<Integer> asyncList = ReactiveCollections.vectorX(async)
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
        PersistentQueueX<Integer> asyncList = ReactiveCollections.persistentQueueX(async)
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
        PersistentSetX<Integer> asyncList = ReactiveCollections.persistentSetX(async)
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
        OrderedSetX<Integer> asyncList = ReactiveCollections.orderedSetX(async)
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
        BagX<Integer> asyncList = ReactiveCollections.bagX(async)
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

package cyclops.futurestream;

import com.oath.cyclops.internal.react.FutureStreamImpl;
import com.oath.cyclops.async.adapters.Topic;
import com.oath.cyclops.types.reactive.ReactiveSubscriber;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by wyang14 on 23/07/2017.
 */
public class FutureStreamImplTest {

    public static class BooleanProxy {
        public boolean value;

        public BooleanProxy(boolean b) {
            value = b;
        }
    }

    @Test
    public void testOnComplete() {
        BooleanProxy completed = new BooleanProxy(false);
        try {
            final Topic<Integer> topic = new Topic<>();

            Thread t2 = new Thread(() -> {
                ((FutureStreamImpl) new LazyReact(10, 10).fromAdapter(topic)).onComplete(() -> {
                    completed.value = true;
                }).forEach(x -> {
                    assertFalse(completed.value);
                });
            });

            Thread t = new Thread(() -> {
                topic.offer(100);
                topic.offer(200);
                topic.close();
            });
            t2.start();
            t.start();
            t2.join();
            t.join();

            assertTrue(completed.value);

        } catch (Exception ex) {
            assertTrue(false);
        }

    }

    public void subcribeOnce(){
        AtomicBoolean called1=  new AtomicBoolean(false);
        AtomicBoolean called2=  new AtomicBoolean(false);
        ReactiveSeq<Integer> initialStream = ReactiveSeq.of(1, 2, 3, 4, 5, 6);

        ReactiveSubscriber<Integer> sub1 = Spouts.reactiveSubscriber();
        ReactiveSubscriber<Integer> sub2 = Spouts.reactiveSubscriber();

        FutureStream<Integer> futureStream = FutureStream.builder().fromStream(initialStream)
            .map(v -> v -1);


        futureStream.subscribe(sub1);
        futureStream.subscribe(sub2);

        CompletableFuture future1 = CompletableFuture.runAsync(() -> sub1.reactiveStream().peek(a->called1.set(true)).forEach(v -> System.out.println("1 -> " + v)));
        CompletableFuture future2 = CompletableFuture.runAsync(() -> sub2.reactiveStream().peek(a->called2.set(true)).forEach(v -> System.out.println("2 -> " + v)));

        try {
            future1.get();
            future2.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        assertThat(called1.get(),equalTo(true));
        assertThat(called1.get(),equalTo(false));
    }
}

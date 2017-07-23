package com.aol.cyclops2.internal.react;

import cyclops.async.LazyReact;
import cyclops.async.adapters.Topic;
import org.junit.Test;

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
                ((FutureStreamImpl) new LazyReact(10, 10).fromAdapter(topic)).complete(() -> {
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
}
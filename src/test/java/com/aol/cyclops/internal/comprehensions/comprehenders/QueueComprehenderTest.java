package com.aol.cyclops.internal.comprehensions.comprehenders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;

public class QueueComprehenderTest {
    QueueComprehender comprehender = new QueueComprehender();

    @Test
    public void map() {

        Queue<Integer> d = new LinkedList<>();
        d.add(1);
        d.add(2);
        d.add(3);
        Queue r = (Queue) comprehender.map(d, i -> (Integer) i * 2);

        assertEquals(2, r.poll());
        assertEquals(4, r.poll());
        assertEquals(6, r.poll());
    }

    @Test
    public void flatMap() {

        Queue<Integer> d = new LinkedList<>();
        d.add(1);
        d.add(2);
        d.add(3);
        Queue r = (Queue) comprehender.flatMap(d, i -> QueueX.of((Integer) i * 2));

        assertEquals(2, r.poll());
        assertEquals(4, r.poll());
        assertEquals(6, r.poll());
    }

    @Test
    public void of() {
        Queue d = comprehender.of(1);
        assertEquals(1, d.poll());
    }

    @Test
    public void fromIterator() {
        Queue d = comprehender.fromIterator(ListX.of(1, 2, 3).iterator());
        assertEquals(1, d.poll());
        assertEquals(2, d.poll());
        assertEquals(3, d.poll());
    }

    @Test
    public void empty() {
        assertTrue(comprehender.empty().isEmpty());
    }

    @Test
    public void getTargetClass() {
        assertEquals(Queue.class, comprehender.getTargetClass());
    }

    @Test
    public void unwrap() {
        Queue d = comprehender.unwrap(Stream.of(1, 2, 3));
        assertEquals(1, d.poll());
        assertEquals(2, d.poll());
        assertEquals(3, d.poll());
    }
}

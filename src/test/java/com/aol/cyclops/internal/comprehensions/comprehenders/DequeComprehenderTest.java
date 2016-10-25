package com.aol.cyclops.internal.comprehensions.comprehenders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public class DequeComprehenderTest {

    DequeComprehender comprehender = new DequeComprehender();

    @Test
    public void map() {

        Deque<Integer> d = new LinkedList<>();
        d.add(1);
        d.add(2);
        d.add(3);
        Deque r = (Deque) comprehender.map(d, i -> (Integer) i * 2);

        assertEquals(2, r.pop());
        assertEquals(4, r.pop());
        assertEquals(6, r.pop());
    }

    @Test
    public void flatMap() {

        Deque<Integer> d = new LinkedList<>();
        d.add(1);
        d.add(2);
        d.add(3);
        Deque r = (Deque) comprehender.flatMap(d, i -> DequeX.of((Integer) i * 2));

        assertEquals(2, r.pop());
        assertEquals(4, r.pop());
        assertEquals(6, r.pop());
    }

    @Test
    public void of() {
        Deque d = comprehender.of(1);
        assertEquals(1, d.pop());
    }

    @Test
    public void fromIterator() {
        Deque d = comprehender.fromIterator(ListX.of(1, 2, 3).iterator());
        assertEquals(1, d.pop());
        assertEquals(2, d.pop());
        assertEquals(3, d.pop());
    }

    @Test
    public void empty() {
        assertTrue(comprehender.empty().isEmpty());
    }

    @Test
    public void getTargetClass() {
        assertEquals(Deque.class, comprehender.getTargetClass());
    }

    @Test
    public void unwrap() {
        Deque d = comprehender.unwrap(Stream.of(1, 2, 3));
        assertEquals(1, d.pop());
        assertEquals(2, d.pop());
        assertEquals(3, d.pop());
    }
}

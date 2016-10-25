package com.aol.cyclops.internal.comprehensions.comprehenders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;

public class SortedSetComprehenderTest {
    SortedSetComprehender comprehender = new SortedSetComprehender();

    @Test
    public void map() {

        SortedSet<Integer> d = new TreeSet<>();
        d.add(1);
        d.add(2);
        d.add(3);
        SortedSet r = (SortedSet) comprehender.map(d, i -> (Integer) i * 2);

        Iterator it = r.iterator();

        assertEquals(2, it.next());
        assertEquals(4, it.next());
        assertEquals(6, it.next());
    }

    @Test
    public void flatMap() {

        SortedSet<Integer> d = new TreeSet<>();
        d.add(1);
        d.add(2);
        d.add(3);
        SortedSet r = (SortedSet) comprehender.flatMap(d, i -> SortedSetX.of((Integer) i * 2));

        Iterator it = r.iterator();

        assertEquals(2, it.next());
        assertEquals(4, it.next());
        assertEquals(6, it.next());
    }

    @Test
    public void of() {
        SortedSet d = comprehender.of(1);
        assertEquals(1, d.iterator().next());
    }

    @Test
    public void fromIterator() {
        SortedSet d = comprehender.fromIterator(ListX.of(1, 2, 3).iterator());
        Iterator it = d.iterator();

        assertEquals(1, it.next());
        assertEquals(2, it.next());
        assertEquals(3, it.next());
    }

    @Test
    public void empty() {
        assertTrue(comprehender.empty().isEmpty());
    }

    @Test
    public void getTargetClass() {
        assertEquals(SortedSet.class, comprehender.getTargetClass());
    }

    @Test
    public void unwrap() {
        SortedSet d = comprehender.unwrap(Stream.of(1, 2, 3));
        Iterator it = d.iterator();

        assertEquals(1, it.next());
        assertEquals(2, it.next());
        assertEquals(3, it.next());
    }
}

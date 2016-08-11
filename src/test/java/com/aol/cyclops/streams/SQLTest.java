package com.aol.cyclops.streams;

import static com.aol.cyclops.control.ReactiveSeq.of;
import static java.util.Arrays.asList;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;

import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;

public class SQLTest {

    BiPredicate TRUE = (t, u) -> true;

    @Test
    public void testCrossJoin() {

        // {A} x {B}
        // ---------------------------------------------------------------------
        assertEquals(asList(), of().crossJoin(of()).toList());
        assertEquals(asList(), of().crossJoin(of(1)).toList());
        assertEquals(asList(), of().crossJoin(of(1, 2)).toList());

        assertEquals(asList(), of("A").crossJoin(of()).toList());
        assertEquals(asList(tuple("A", 1)), of("A").crossJoin(of(1)).toList());
        assertEquals(asList(tuple("A", 1), tuple("A", 2)), of("A").crossJoin(of(1, 2)).toList());

        assertEquals(asList(), of("A", "B").crossJoin(of()).toList());
        assertEquals(asList(tuple("A", 1), tuple("B", 1)), of("A", "B").crossJoin(of(1)).toList());
        assertEquals(asList(tuple("A", 1), tuple("A", 2), tuple("B", 1), tuple("B", 2)),
                of("A", "B").crossJoin(of(1, 2)).toList());

        assertEquals(asList(), of("A", "B", "C").crossJoin(of()).toList());
        assertEquals(asList(tuple("A", 1), tuple("B", 1), tuple("C", 1)), of("A", "B", "C").crossJoin(of(1)).toList());
        assertEquals(asList(tuple("A", 1), tuple("A", 2), tuple("B", 1), tuple("B", 2), tuple("C", 1), tuple("C", 2)),
                of("A", "B", "C").crossJoin(of(1, 2)).toList());

    }
    @Test
    public void testCrossJoinStream() {

        // {A} x {B}
        // ---------------------------------------------------------------------
        assertEquals(asList(), of().crossJoin(Stream.of()).toList());
        assertEquals(asList(), of().crossJoin(Stream.of(1)).toList());
        assertEquals(asList(), of().crossJoin(Stream.of(1, 2)).toList());

        assertEquals(asList(), of("A").crossJoin(Stream.of()).toList());
        assertEquals(asList(tuple("A", 1)), of("A").crossJoin(Stream.of(1)).toList());
        assertEquals(asList(tuple("A", 1), tuple("A", 2)), of("A").crossJoin(Stream.of(1, 2)).toList());

        assertEquals(asList(), of("A", "B").crossJoin(Stream.of()).toList());
        assertEquals(asList(tuple("A", 1), tuple("B", 1)), of("A", "B").crossJoin(Stream.of(1)).toList());
        assertEquals(asList(tuple("A", 1), tuple("A", 2), tuple("B", 1), tuple("B", 2)),
                of("A", "B").crossJoin(Stream.of(1, 2)).toList());

        assertEquals(asList(), of("A", "B", "C").crossJoin(of()).toList());
        assertEquals(asList(tuple("A", 1), tuple("B", 1), tuple("C", 1)), of("A", "B", "C").crossJoin(Stream.of(1)).toList());
        assertEquals(asList(tuple("A", 1), tuple("A", 2), tuple("B", 1), tuple("B", 2), tuple("C", 1), tuple("C", 2)),
                of("A", "B", "C").crossJoin(Stream.of(1, 2)).toList());

    }
    @Test
    public void testCrossJoinIterable() {

        // {A} x {B}
        // ---------------------------------------------------------------------
        assertEquals(asList(), of().crossJoin(ListX.of()).toList());
        assertEquals(asList(), of().crossJoin(ListX.of(1)).toList());
        assertEquals(asList(), of().crossJoin(ListX.of(1, 2)).toList());

        assertEquals(asList(), of("A").crossJoin(ListX.of()).toList());
        assertEquals(asList(tuple("A", 1)), of("A").crossJoin(ListX.of(1)).toList());
        assertEquals(asList(tuple("A", 1), tuple("A", 2)), of("A").crossJoin(ListX.of(1, 2)).toList());

        assertEquals(asList(), of("A", "B").crossJoin(ListX.of()).toList());
        assertEquals(asList(tuple("A", 1), tuple("B", 1)), of("A", "B").crossJoin(ListX.of(1)).toList());
        assertEquals(asList(tuple("A", 1), tuple("A", 2), tuple("B", 1), tuple("B", 2)),
                of("A", "B").crossJoin(ListX.of(1, 2)).toList());

        assertEquals(asList(), of("A", "B", "C").crossJoin(ListX.of()).toList());
        assertEquals(asList(tuple("A", 1), tuple("B", 1), tuple("C", 1)), of("A", "B", "C").crossJoin(ListX.of(1)).toList());
        assertEquals(asList(tuple("A", 1), tuple("A", 2), tuple("B", 1), tuple("B", 2), tuple("C", 1), tuple("C", 2)),
                of("A", "B", "C").crossJoin(ListX.of(1, 2)).toList());

    }

    @Test
    public void testInnerJoin() {

        assertEquals(asList(), of().innerJoin(of(), TRUE).toList());
        assertEquals(asList(), of().innerJoin(of(1), TRUE).toList());
        assertEquals(asList(), of().innerJoin(of(1, 2), TRUE).toList());

        assertEquals(asList(), of(1).innerJoin(of(), TRUE).toList());
        assertEquals(asList(), of(1).innerJoin(of(2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).innerJoin(of(2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1)), of(1).innerJoin(of(1, 2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).innerJoin(of(1, 2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(1, 2)), of(1).innerJoin(of(1, 2), TRUE).toList());
    }
    @Test
    public void testInnerJoinStream() {

        assertEquals(asList(), of().innerJoin(Stream.of(), TRUE).toList());
        assertEquals(asList(), of().innerJoin(Stream.of(1), TRUE).toList());
        assertEquals(asList(), of().innerJoin(Stream.of(1, 2), TRUE).toList());

        assertEquals(asList(), of(1).innerJoin(Stream.of(), TRUE).toList());
        assertEquals(asList(), of(1).innerJoin(Stream.of(2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).innerJoin(Stream.of(2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1)), of(1).innerJoin(Stream.of(1, 2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).innerJoin(Stream.of(1, 2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(1, 2)), of(1).innerJoin(Stream.of(1, 2), TRUE).toList());
    }
    @Test
    public void testInnerJoinIterable() {

        assertEquals(asList(), of().innerJoin(ListX.of(), TRUE).toList());
        assertEquals(asList(), of().innerJoin(ListX.of(1), TRUE).toList());
        assertEquals(asList(), of().innerJoin(ListX.of(1, 2), TRUE).toList());

        assertEquals(asList(), of(1).innerJoin(ListX.of(), TRUE).toList());
        assertEquals(asList(), of(1).innerJoin(ListX.of(2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).innerJoin(ListX.of(2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1)), of(1).innerJoin(ListX.of(1, 2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).innerJoin(ListX.of(1, 2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(1, 2)), of(1).innerJoin(ListX.of(1, 2), TRUE).toList());
    }

    @Test
    public void testLeftOuterJoin() {

        assertEquals(asList(), of().leftOuterJoin(of(), TRUE).toList());
        assertEquals(asList(), of().leftOuterJoin(of(1), TRUE).toList());
        assertEquals(asList(), of().leftOuterJoin(of(1, 2), TRUE).toList());

        assertEquals(asList(tuple(1, null)), of(1).leftOuterJoin(of(), TRUE).toList());
        assertEquals(asList(tuple(1, null)), of(1).leftOuterJoin(of(2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).leftOuterJoin(of(2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1)), of(1).leftOuterJoin(of(1, 2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).leftOuterJoin(of(1, 2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(1, 2)), of(1).leftOuterJoin(of(1, 2), TRUE).toList());
    }
    @Test
    public void testLeftOuterJoinStream() {

        assertEquals(asList(), of().leftOuterJoin(Stream.of(), TRUE).toList());
        assertEquals(asList(), of().leftOuterJoin(Stream.of(1), TRUE).toList());
        assertEquals(asList(), of().leftOuterJoin(Stream.of(1, 2), TRUE).toList());

        assertEquals(asList(tuple(1, null)), of(1).leftOuterJoin(Stream.of(), TRUE).toList());
        assertEquals(asList(tuple(1, null)), of(1).leftOuterJoin(Stream.of(2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).leftOuterJoin(Stream.of(2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1)), of(1).leftOuterJoin(Stream.of(1, 2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).leftOuterJoin(Stream.of(1, 2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(1, 2)), of(1).leftOuterJoin(Stream.of(1, 2), TRUE).toList());
    }
    @Test
    public void testLeftOuterJoinIterable() {

        assertEquals(asList(), of().leftOuterJoin(ListX.of(), TRUE).toList());
        assertEquals(asList(), of().leftOuterJoin(ListX.of(1), TRUE).toList());
        assertEquals(asList(), of().leftOuterJoin(ListX.of(1, 2), TRUE).toList());

        assertEquals(asList(tuple(1, null)), of(1).leftOuterJoin(ListX.of(), TRUE).toList());
        assertEquals(asList(tuple(1, null)), of(1).leftOuterJoin(ListX.of(2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).leftOuterJoin(ListX.of(2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1)), of(1).leftOuterJoin(ListX.of(1, 2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).leftOuterJoin(ListX.of(1, 2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(1, 2)), of(1).leftOuterJoin(ListX.of(1, 2), TRUE).toList());
    }

    @Test
    public void testRightOuterJoin() {

        assertEquals(asList(), of().rightOuterJoin(of(), TRUE).toList());
        assertEquals(asList(tuple(null, 1)), of().rightOuterJoin(of(1), TRUE).toList());
        assertEquals(asList(tuple(null, 1), tuple(null, 2)), of().rightOuterJoin(of(1, 2), TRUE).toList());

        assertEquals(asList(), of(1).rightOuterJoin(of(), TRUE).toList());
        assertEquals(asList(tuple(null, 2)), of(1).rightOuterJoin(of(2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).rightOuterJoin(of(2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(null, 2)), of(1).rightOuterJoin(of(1, 2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(null, 1), tuple(1, 2)),
                of(1).rightOuterJoin(of(1, 2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(1, 2)), of(1).rightOuterJoin(of(1, 2), TRUE).toList());
    }
    @Test
    public void testRightOuterJoinStream() {

        assertEquals(asList(), of().rightOuterJoin(Stream.of(), TRUE).toList());
        assertEquals(asList(tuple(null, 1)), of().rightOuterJoin(Stream.of(1), TRUE).toList());
        assertEquals(asList(tuple(null, 1), tuple(null, 2)), of().rightOuterJoin(Stream.of(1, 2), TRUE).toList());

        assertEquals(asList(), of(1).rightOuterJoin(Stream.of(), TRUE).toList());
        assertEquals(asList(tuple(null, 2)), of(1).rightOuterJoin(Stream.of(2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).rightOuterJoin(Stream.of(2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(null, 2)), of(1).rightOuterJoin(Stream.of(1, 2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(null, 1), tuple(1, 2)),
                of(1).rightOuterJoin(Stream.of(1, 2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(1, 2)), of(1).rightOuterJoin(Stream.of(1, 2), TRUE).toList());
    }
    @Test
    public void testRightOuterJoinList() {

        assertEquals(asList(), of().rightOuterJoin(ListX.of(), TRUE).toList());
        assertEquals(asList(tuple(null, 1)), of().rightOuterJoin(ListX.of(1), TRUE).toList());
        assertEquals(asList(tuple(null, 1), tuple(null, 2)), of().rightOuterJoin(ListX.of(1, 2), TRUE).toList());

        assertEquals(asList(), of(1).rightOuterJoin(ListX.of(), TRUE).toList());
        assertEquals(asList(tuple(null, 2)), of(1).rightOuterJoin(ListX.of(2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(1, 2)), of(1).rightOuterJoin(ListX.of(2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(null, 2)), of(1).rightOuterJoin(ListX.of(1, 2), (t, u) -> t == u).toList());
        assertEquals(asList(tuple(null, 1), tuple(1, 2)),
                of(1).rightOuterJoin(ListX.of(1, 2), (t, u) -> t * 2 == u).toList());
        assertEquals(asList(tuple(1, 1), tuple(1, 2)), of(1).rightOuterJoin(ListX.of(1, 2), TRUE).toList());
    }

    @Test
    public void testOnEmpty() throws X {
        assertEquals(asList(1), of().onEmpty(1).toList());
        assertEquals(asList(1), of().onEmptyGet(() -> 1).toList());

        assertEquals(asList(2), of(2).onEmpty(1).toList());
        assertEquals(asList(2), of(2).onEmptyGet(() -> 1).toList());
        assertEquals(asList(2), of(2).onEmptyThrow(() -> new X()).toList());

        assertEquals(asList(2, 3), of(2, 3).onEmpty(1).toList());
        assertEquals(asList(2, 3), of(2, 3).onEmptyGet(() -> 1).toList());
        assertEquals(asList(2, 3), of(2, 3).onEmptyThrow(() -> new X()).toList());
    }

    @SuppressWarnings("serial")
    public class X extends Exception {
    }

    @Test
    public void testConcat() {
        assertEquals(asList(1, 2, 3, 4), of(1).concat(of(2, 3, 4)).toList());
        assertEquals(asList(1, 2, 3, 4), of(1, 2).concat(of(3, 4)).toList());
        assertEquals(asList(1, 2, 3, 4), of(1).concat(2, 3, 4).toList());
        assertEquals(asList(1, 2, 3, 4), of(1, 2).concat(3, 4).toList());
    }

}
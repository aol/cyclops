package com.aol.cyclops.javaslang.reactivestreams.reactivestream;
/*     / \____  _    _  ____   ______  / \ ____  __    _ _____
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  / /  _  \   Javaslang
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/  \__/  /   Copyright 2014-now Daniel Dietrich
 * /___/\_/  \_/\____/\_/  \_/\__\/__/___\_/  \_//  \__/_____/    Licensed under the Apache License, Version 2.0
 */


//import javaslang.Serializables;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.stream.Collector;

import javaslang.collection.List;
import javaslang.collection.LazyStream;
import javaslang.collection.Stream;
import javaslang.control.Try;

import org.junit.Test;

import com.aol.cyclops.javaslang.reactivestreams.ReactiveStream;

public class JavaslangStreamTest extends AbstractSeqTest {

	  // -- construction

	
    @Override
    protected <T> Collector<T, ArrayList<T>, LazyStream<T>> collector() {
        return LazyStream.collector();
    }

    @Override
    protected <T> LazyStream<T> empty() {
        return ReactiveStream.empty();
    }

    @Override
    protected <T> LazyStream<T> of(T element) {
        return ReactiveStream.of(element);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <T> LazyStream<T> of(T... elements) {
        return  ReactiveStream.fromStream(LazyStream.of(elements));
    }

    @Override
    protected <T> LazyStream<T> ofAll(java.lang.Iterable<? extends T> elements) {
        return  ReactiveStream.fromStream(LazyStream.ofAll(elements));
    }

    @Override
    protected LazyStream<Boolean> ofAll(boolean[] array) {
        return  ReactiveStream.fromStream(LazyStream.ofAll(array));
    }

    @Override
    protected LazyStream<Byte> ofAll(byte[] array) {
        return  ReactiveStream.fromStream(LazyStream.ofAll(array));
    }

    @Override
    protected LazyStream<Character> ofAll(char[] array) {
        return  ReactiveStream.fromStream(LazyStream.ofAll(array));
    }

    @Override
    protected LazyStream<Double> ofAll(double[] array) {
        return  ReactiveStream.fromStream(LazyStream.ofAll(array));
    }

    @Override
    protected LazyStream<Float> ofAll(float[] array) {
        return  ReactiveStream.fromStream(LazyStream.ofAll(array));
    }

    @Override
    protected LazyStream<Integer> ofAll(int[] array) {
        return  ReactiveStream.fromStream(LazyStream.ofAll(array));
    }

    @Override
    protected LazyStream<Long> ofAll(long[] array) {
        return  ReactiveStream.fromStream(LazyStream.ofAll(array));
    }

    @Override
    protected LazyStream<Short> ofAll(short[] array) {
        return  ReactiveStream.fromStream(LazyStream.ofAll(array));
    }

    @Override
    protected LazyStream<Character> range(char from, char toExclusive) {
        return  ReactiveStream.fromStream(LazyStream.range(from, toExclusive));
    }

    @Override
    protected LazyStream<Character> rangeBy(char from, char toExclusive, int step) {
        return  ReactiveStream.fromStream(LazyStream.rangeBy(from, toExclusive, step));
    }

    @Override
    protected LazyStream<Double> rangeBy(double from, double toExclusive, double step) {
        return  ReactiveStream.fromStream(LazyStream.rangeBy(from, toExclusive, step));
    }

    @Override
    protected LazyStream<Integer> range(int from, int toExclusive) {
        return ReactiveStream.range(from, toExclusive);
    }

    @Override
    protected LazyStream<Integer> rangeBy(int from, int toExclusive, int step) {
        return  ReactiveStream.fromStream(LazyStream.rangeBy(from, toExclusive, step));
    }

    @Override
    protected LazyStream<Long> range(long from, long toExclusive) {
        return ReactiveStream.range(from, toExclusive);
    }

    @Override
    protected LazyStream<Long> rangeBy(long from, long toExclusive, long step) {
        return  ReactiveStream.fromStream(LazyStream.rangeBy(from, toExclusive, step));
    }

    @Override
    protected LazyStream<Character> rangeClosed(char from, char toInclusive) {
        return  ReactiveStream.fromStream(LazyStream.rangeClosed(from, toInclusive));
    }

    @Override
    protected LazyStream<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return  ReactiveStream.fromStream(LazyStream.rangeClosedBy(from, toInclusive, step));
    }

    @Override
    protected LazyStream<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return  ReactiveStream.fromStream(LazyStream.rangeClosedBy(from, toInclusive, step));
    }

    @Override
    protected LazyStream<Integer> rangeClosed(int from, int toInclusive) {
        return  ReactiveStream.fromStream(LazyStream.rangeClosed(from, toInclusive));
    }

    @Override
    protected LazyStream<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return  ReactiveStream.fromStream(LazyStream.rangeClosedBy(from, toInclusive, step));
    }

    @Override
    protected LazyStream<Long> rangeClosed(long from, long toInclusive) {
        return  ReactiveStream.fromStream(LazyStream.rangeClosed(from, toInclusive));
    }

    @Override
    protected LazyStream<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return ReactiveStream.fromStream(LazyStream.rangeClosedBy(from, toInclusive, step));
    }

    // -- static from(int)

    @Test
    public void shouldGenerateIntStream() {
        assertThat(ReactiveStream.from(-1).take(3)).isEqualTo(ReactiveStream.of(-1, 0, 1));
    }

    @Test
    public void shouldGenerateTerminatingIntStream() {
        //noinspection NumericOverflow
        assertThat(ReactiveStream.from(Integer.MAX_VALUE).take(2))
                .isEqualTo(ReactiveStream.of(Integer.MAX_VALUE, Integer.MAX_VALUE + 1));
    }

    // -- static from(long)

    @Test
    public void shouldGenerateLongStream() {
        assertThat(ReactiveStream.from(-1L).take(3)).isEqualTo(ReactiveStream.of(-1L, 0L, 1L));
    }

    @Test
    public void shouldGenerateTerminatingLongStream() {
        //noinspection NumericOverflow
        assertThat(ReactiveStream.from(Long.MAX_VALUE).take(2)).isEqualTo(ReactiveStream.of(Long.MAX_VALUE, Long.MAX_VALUE + 1));
    }

    // -- static gen(Supplier)

    @Test
    public void shouldGenerateInfiniteStreamBasedOnSupplier() {
        assertThat(ReactiveStream.generate(() -> 1).take(13).reduce((i, j) -> i + j)).isEqualTo(13);
    }

    // -- static gen(T, Function)

    @Test
    public void shouldGenerateInfiniteStreamBasedOnSupplierWithAccessToPreviousValue() {
        assertThat(ReactiveStream.iterate(2, (i) -> i + 2).take(3).reduce((i, j) -> i + j)).isEqualTo(12);
    }

    // -- static cons(T, Supplier)

    @Test
    public void shouldBuildStreamBasedOnHeadAndTailSupplierWithAccessToHead() {
        assertThat(ReactiveStream.cons(()->1, () -> ReactiveStream.cons(()->2, LazyStream::empty))).isEqualTo(ReactiveStream.of(1, 2));
    }

    // -- combinations

    @Test
    public void shouldComputeCombinationsOfEmptyStream() {
        assertThat(ReactiveStream.of(ReactiveStream.empty()).single()).isEqualTo(ReactiveStream.empty().combinations().single());
    }

    @Test
    public void shouldComputeCombinationsOfNonEmptyStream() {
        assertThat(ReactiveStream.of(1, 2, 3).combinations().map(r->r.toList())).isEqualTo(ReactiveStream.of(List.empty(), List.of(1), List.of(2),
                List.of(3), List.of(1, 2), List.of(1, 3), List.of(2, 3), List.of(1, 2, 3)));
    }

    // -- combinations(k)

    @Test
    public void shouldComputeKCombinationsOfEmptyStream() {
        assertThat(ReactiveStream.empty().combinations(1)).isEqualTo(ReactiveStream.empty());
    }

    @Test
    public void shouldComputeKCombinationsOfNonEmptyStream() {
        assertThat(ReactiveStream.of(1, 2, 3).combinations(2))
                .isEqualTo(ReactiveStream.of(ReactiveStream.of(1, 2), ReactiveStream.of(1, 3), ReactiveStream.of(2, 3)));
    }

    // -- flatMap

    @Test
    public void shouldFlatMapInfiniteTraversable() {
        assertThat(ReactiveStream.iterate(1, i -> i + 1).flatMap(i -> List.of(i, 2 * i)).take(7))
                .isEqualTo(ReactiveStream.of(1, 2, 2, 4, 3, 6, 4));
    }

    // -- peek

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 3;
    }

    // -- permutations

    @Test
    public void shouldComputePermutationsOfEmptyStream() {
        assertThat(ReactiveStream.empty().permutations()).isEqualTo(ReactiveStream.empty());
    }

    @Test
    public void shouldComputePermutationsOfNonEmptyStream() {
    	System.out.println(Stream.of(1, 2, 3).permutations());
        assertThat(Stream.of(1, 2, 3).permutations().map(r->r.toList()))
        				.isEqualTo(ReactiveStream.of(List.of(1, 2, 3),
                List.of(1, 3, 2), List.of(2, 1, 3), List.of(2, 3, 1), List.of(3, 1, 2), List.of(3, 2, 1)));
    }

    // -- addSelf

    @Test
    public void shouldRecurrentlyCalculateFibonacci() {
        assertThat(ReactiveStream.of(1, 1).appendSelf(self -> self.zip(self.tail()).map(t -> t._1 + t._2)).take(10))
                .isEqualTo(ReactiveStream.of(1, 1, 2, 3, 5, 8, 13, 21, 34, 55));
    }

    @Test
    public void shouldRecurrentlyCalculatePrimes() {
        assertThat(LazyStream
                .of(2)
                .appendSelf(self -> LazyStream
                        .gen(3, i -> i + 2)
                        .filter(i -> self.takeWhile(j -> j * j <= i).forAll(k -> i % k > 0)))
                .take(10)).isEqualTo(ReactiveStream.of(2, 3, 5, 7, 11, 13, 17, 19, 23, 29));
    }

    @Test
    public void shouldDoNothingOnNil() {
        assertThat(ReactiveStream.empty().appendSelf(self -> self)).isEqualTo(ReactiveStream.empty());
    }

    @Test
    public void shouldRecurrentlyCalculateArithmeticProgression() {
        assertThat(ReactiveStream.of(1).appendSelf(self -> self.map(t -> t + 1)).take(4)).isEqualTo(ReactiveStream.of(1, 2, 3, 4));
    }

    @Test
    public void shouldRecurrentlyCalculateGeometricProgression() {
        assertThat(ReactiveStream.of(1).appendSelf(self -> self.map(t -> t * 2)).take(4)).isEqualTo(ReactiveStream.of(1, 2, 4, 8));
    }

    // -- containsSlice

    @Test
    public void shouldRecognizeInfiniteDoesContainSlice() {
        final boolean actual = ReactiveStream.iterate(1, i -> i + 1).containsSlice(of(12, 13, 14));
        assertThat(actual).isTrue();
    }

    // -- toString

    @Test
    public void shouldStringifyNil() {
        assertThat(empty().toString()).isEqualTo("Stream()");
    }

    @Test
    public void shouldStringifyNonNil() {
        assertThat(of(1, 2, 3).toString()).isEqualTo("Stream(1, ?)");
    }

    @Test
    public void shouldStringifyNonNilEvaluatingFirstTail() {
        final ReactiveStream<Integer> stream = ReactiveStream.of(1, 2, 3);
        stream.tail(); // evaluates second head element
        assertThat(stream.toString()).isEqualTo("Stream(1, 2, ?)");
    }

    // -- Serializable
/**
    @Test(expected = InvalidObjectException.class)
    public void shouldNotSerializeEnclosingClassOfCons() throws Throwable {
        Serializables.callReadObject(ReactiveStream.cons(1, Stream::empty));
    }

    @Test(expected = InvalidObjectException.class)
    public void shouldNotDeserializeStreamWithSizeLessThanOne() throws Throwable {
        try {
            /*
             * This implementation is stable regarding jvm impl changes of object serialization. The index of the number
             * of Stream elements is gathered dynamically.
             */
    /**
            final byte[] listWithOneElement = Serializables.serialize(ReactiveStream.of(0));
            final byte[] listWithTwoElements = Serializables.serialize(ReactiveStream.of(0, 0));
            int index = -1;
            for (int i = 0; i < listWithOneElement.length && index == -1; i++) {
                final byte b1 = listWithOneElement[i];
                final byte b2 = listWithTwoElements[i];
                if (b1 != b2) {
                    if (b1 != 1 || b2 != 2) {
                        throw new IllegalStateException("Difference does not indicate number of elements.");
                    } else {
                        index = i;
                    }
                }
            }
            if (index == -1) {
                throw new IllegalStateException("Hack incomplete - index not found");
            }
            /*
			 * Hack the serialized data and fake zero elements.
			 */
    /**
            listWithOneElement[index] = 0;
            Serializables.deserialize(listWithOneElement);
        } catch (IllegalStateException x) {
            throw (x.getCause() != null) ? x.getCause() : x;
        }
    }
**/
    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Test
    public void shouldEvaluateTailAtMostOnce() {
        final int[] counter = { 0 };
        final LazyStream<Integer> stream = ReactiveStream.generate(() -> counter[0]++);
        // this test ensures that the `tail.append(100)` does not modify the tail elements
        final LazyStream<Integer> tail = stream.tail().append(100);
        final String expected = stream.drop(1).take(3).mkString(",");
        final String actual = tail.take(3).mkString(",");
        assertThat(expected).isEqualTo("1,2,3");
        assertThat(actual).isEqualTo(expected);
    }

    

    @Test // See #327, #594
    public void shouldNotEvaluateHeadOfTailWhenCallingIteratorHasNext() {

        final Integer[] vals = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        final StringBuilder actual = new StringBuilder();
        flatTryWithJavaslangStream(vals, i -> doStuff(i, actual));

        final StringBuilder expected = new StringBuilder();
        flatTryWithJavaStream(vals, i -> doStuff(i, expected));

        assertThat(actual.toString()).isEqualTo(expected.toString());
    }

    private Try<Void> flatTryWithJavaslangStream(Integer[] vals, Try.CheckedConsumer<Integer> func) {
        return LazyStream.of(vals)
                .map(v -> Try.run(() -> func.accept(v)))
                .find(Try::isFailure)
                .getOrElse(() -> Try.success(null));
    }

    private Try<Void> flatTryWithJavaStream(Integer[] vals, Try.CheckedConsumer<Integer> func) {
        return java.util.stream.Stream.of(vals)
                .map(v -> Try.run(() -> func.accept(v)))
                .filter(Try::isFailure)
                .findFirst()
                .orElseGet(() -> Try.success(null));
    }


    private String doStuff(int i, StringBuilder builder) throws Exception {
        builder.append(i);
        if (i == 5) {
            throw new Exception("Some error !!!");
        }
        return i + " Value";
    }

}
package com.aol.cyclops.types.stream;

import static com.aol.cyclops.control.ReactiveSeq.fromStream;

import java.util.Comparator;
import java.util.function.Function;

import javax.annotation.Generated;

import org.jooq.lambda.Seq;
import org.jooq.lambda.Window;
import org.jooq.lambda.WindowSpecification;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple10;
import org.jooq.lambda.tuple.Tuple11;
import org.jooq.lambda.tuple.Tuple12;
import org.jooq.lambda.tuple.Tuple13;
import org.jooq.lambda.tuple.Tuple14;
import org.jooq.lambda.tuple.Tuple15;
import org.jooq.lambda.tuple.Tuple16;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;
import org.jooq.lambda.tuple.Tuple6;
import org.jooq.lambda.tuple.Tuple7;
import org.jooq.lambda.tuple.Tuple8;
import org.jooq.lambda.tuple.Tuple9;

import com.aol.cyclops.control.ReactiveSeq;

public interface JoolWindowing<T> extends Seq<T> {

    /**
     * Map this stream to a windowed stream using the default partition and order.
     * <p>
     * <code><pre>
     * // (0, 1, 2, 3, 4)
     * ReactiveSeq.of(1, 2, 4, 2, 3).window().map(Window::rowNumber)
     * </pre></code>
     */
    default ReactiveSeq<Window<T>> window() {
        return fromStream(Seq.super.window());
    }

    /**
     * Map this stream to a windowed stream using the default partition and order with frame.
     * <p>
     * <code><pre>
     * // (2, 4, 4, 4, 3)
     * ReactiveSeq.of(1, 2, 4, 2, 3).window(-1, 1).map(Window::max)
     * </pre></code>
     */
    default ReactiveSeq<Window<T>> window(long lower, long upper) {
        return fromStream(Seq.super.window(lower, upper));

    }

    /**
     * Map this stream to a windowed stream using the default partition and a specific order.
     * <p>
     * <code><pre>
     * // (0, 1, 4, 2, 3)
     * ReactiveSeq.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::rowNumber)
     * </pre></code>
     */
    default ReactiveSeq<Window<T>> window(Comparator<? super T> orderBy) {
        return fromStream(Seq.super.window(orderBy));
    }

    /**
     * Map this stream to a windowed stream using the default partition and a specific order with frame.
     * <p>
     * <code><pre>
     * // (1, 1, 3, 2, 2)
     * ReactiveSeq.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(Window::min)
     * </pre></code>
     */
    default ReactiveSeq<Window<T>> window(Comparator<? super T> orderBy, long lower, long upper) {
        return window(Window.of(orderBy, lower, upper)).map(t -> t.v1);
    }

    /**
     * Map this stream to a windowed stream using a specific partition and the default order.
     * <p>
     * <code><pre>
     * // (1, 2, 2, 2, 1)
     * ReactiveSeq.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::min)
     * </pre></code>
     */
    default <U> ReactiveSeq<Window<T>> window(Function<? super T, ? extends U> partitionBy) {
        return window(Window.of(partitionBy)).map(t -> t.v1);
    }

    /**
     * Map this stream to a windowed stream using a specific partition and the default order.
     * <p>
     * <code><pre>
     * // (3, 4, 4, 2, 3)
     * ReactiveSeq.of(1, 4, 2, 2, 3).window(i -> i % 2, -1, 1).map(Window::max)
     * </pre></code>
     */
    default <U> ReactiveSeq<Window<T>> window(Function<? super T, ? extends U> partitionBy, long lower, long upper) {
        return window(Window.of(partitionBy, lower, upper)).map(t -> t.v1);
    }

    /**
     * Map this stream to a windowed stream using a specific partition and order with frame.
     * <p>
     * <code><pre>
     * // (3, 2, 4, 4, 3)
     * ReactiveSeq.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(Window::max)
     * </pre></code>
     */
    default <U> ReactiveSeq<Window<T>> window(Function<? super T, ? extends U> partitionBy, Comparator<? super T> orderBy, long lower, long upper) {
        return window(Window.of(partitionBy, orderBy, lower, upper)).map(t -> t.v1);
    }

    // [jooq-tools] START [windows]

    /**
     * Map this stream to a windowed stream with 1 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple1<Window<T>>> window(WindowSpecification<T> specification1) {
        return fromStream(Seq.super.window(specification1));
    }

    /**
     * Map this stream to a windowed stream with 2 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple2<Window<T>, Window<T>>> window(WindowSpecification<T> specification1, WindowSpecification<T> specification2) {
        return fromStream(Seq.super.window(specification1, specification2));
    }

    /**
     * Map this stream to a windowed stream with 3 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple3<Window<T>, Window<T>, Window<T>>> window(WindowSpecification<T> specification1, WindowSpecification<T> specification2,
            WindowSpecification<T> specification3) {
        return fromStream(Seq.super.window(specification1, specification2, specification3));
    }

    /**
     * Map this stream to a windowed stream with 4 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple4<Window<T>, Window<T>, Window<T>, Window<T>>> window(WindowSpecification<T> specification1,
            WindowSpecification<T> specification2, WindowSpecification<T> specification3, WindowSpecification<T> specification4) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4));
    }

    /**
     * Map this stream to a windowed stream with 5 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple5<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(WindowSpecification<T> specification1,
            WindowSpecification<T> specification2, WindowSpecification<T> specification3, WindowSpecification<T> specification4,
            WindowSpecification<T> specification5) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5));
    }

    /**
     * Map this stream to a windowed stream with 6 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple6<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(WindowSpecification<T> specification1,
            WindowSpecification<T> specification2, WindowSpecification<T> specification3, WindowSpecification<T> specification4,
            WindowSpecification<T> specification5, WindowSpecification<T> specification6) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6));
    }

    /**
     * Map this stream to a windowed stream with 7 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple7<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
            WindowSpecification<T> specification1, WindowSpecification<T> specification2, WindowSpecification<T> specification3,
            WindowSpecification<T> specification4, WindowSpecification<T> specification5, WindowSpecification<T> specification6,
            WindowSpecification<T> specification7) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6,
                                           specification7));
    }

    /**
     * Map this stream to a windowed stream with 8 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple8<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
            WindowSpecification<T> specification1, WindowSpecification<T> specification2, WindowSpecification<T> specification3,
            WindowSpecification<T> specification4, WindowSpecification<T> specification5, WindowSpecification<T> specification6,
            WindowSpecification<T> specification7, WindowSpecification<T> specification8) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6,
                                           specification7, specification8));
    }

    /**
     * Map this stream to a windowed stream with 9 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple9<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
            WindowSpecification<T> specification1, WindowSpecification<T> specification2, WindowSpecification<T> specification3,
            WindowSpecification<T> specification4, WindowSpecification<T> specification5, WindowSpecification<T> specification6,
            WindowSpecification<T> specification7, WindowSpecification<T> specification8, WindowSpecification<T> specification9) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6,
                                           specification7, specification8, specification9));
    }

    /**
     * Map this stream to a windowed stream with 10 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple10<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
            WindowSpecification<T> specification1, WindowSpecification<T> specification2, WindowSpecification<T> specification3,
            WindowSpecification<T> specification4, WindowSpecification<T> specification5, WindowSpecification<T> specification6,
            WindowSpecification<T> specification7, WindowSpecification<T> specification8, WindowSpecification<T> specification9,
            WindowSpecification<T> specification10) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6,
                                           specification7, specification8, specification9, specification10));
    }

    /**
     * Map this stream to a windowed stream with 11 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple11<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
            WindowSpecification<T> specification1, WindowSpecification<T> specification2, WindowSpecification<T> specification3,
            WindowSpecification<T> specification4, WindowSpecification<T> specification5, WindowSpecification<T> specification6,
            WindowSpecification<T> specification7, WindowSpecification<T> specification8, WindowSpecification<T> specification9,
            WindowSpecification<T> specification10, WindowSpecification<T> specification11) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6,
                                           specification7, specification8, specification9, specification10, specification11));
    }

    /**
     * Map this stream to a windowed stream with 12 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple12<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
            WindowSpecification<T> specification1, WindowSpecification<T> specification2, WindowSpecification<T> specification3,
            WindowSpecification<T> specification4, WindowSpecification<T> specification5, WindowSpecification<T> specification6,
            WindowSpecification<T> specification7, WindowSpecification<T> specification8, WindowSpecification<T> specification9,
            WindowSpecification<T> specification10, WindowSpecification<T> specification11, WindowSpecification<T> specification12) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6,
                                           specification7, specification8, specification9, specification10, specification11, specification12));
    }

    /**
     * Map this stream to a windowed stream with 13 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple13<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
            WindowSpecification<T> specification1, WindowSpecification<T> specification2, WindowSpecification<T> specification3,
            WindowSpecification<T> specification4, WindowSpecification<T> specification5, WindowSpecification<T> specification6,
            WindowSpecification<T> specification7, WindowSpecification<T> specification8, WindowSpecification<T> specification9,
            WindowSpecification<T> specification10, WindowSpecification<T> specification11, WindowSpecification<T> specification12,
            WindowSpecification<T> specification13) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6,
                                           specification7, specification8, specification9, specification10, specification11, specification12,
                                           specification13));
    }

    /**
     * Map this stream to a windowed stream with 14 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple14<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
            WindowSpecification<T> specification1, WindowSpecification<T> specification2, WindowSpecification<T> specification3,
            WindowSpecification<T> specification4, WindowSpecification<T> specification5, WindowSpecification<T> specification6,
            WindowSpecification<T> specification7, WindowSpecification<T> specification8, WindowSpecification<T> specification9,
            WindowSpecification<T> specification10, WindowSpecification<T> specification11, WindowSpecification<T> specification12,
            WindowSpecification<T> specification13, WindowSpecification<T> specification14) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6,
                                           specification7, specification8, specification9, specification10, specification11, specification12,
                                           specification13, specification14));
    }

    /**
     * Map this stream to a windowed stream with 15 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple15<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
            WindowSpecification<T> specification1, WindowSpecification<T> specification2, WindowSpecification<T> specification3,
            WindowSpecification<T> specification4, WindowSpecification<T> specification5, WindowSpecification<T> specification6,
            WindowSpecification<T> specification7, WindowSpecification<T> specification8, WindowSpecification<T> specification9,
            WindowSpecification<T> specification10, WindowSpecification<T> specification11, WindowSpecification<T> specification12,
            WindowSpecification<T> specification13, WindowSpecification<T> specification14, WindowSpecification<T> specification15) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6,
                                           specification7, specification8, specification9, specification10, specification11, specification12,
                                           specification13, specification14, specification15));
    }

    /**
     * Map this stream to a windowed stream with 16 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default ReactiveSeq<Tuple16<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
            WindowSpecification<T> specification1, WindowSpecification<T> specification2, WindowSpecification<T> specification3,
            WindowSpecification<T> specification4, WindowSpecification<T> specification5, WindowSpecification<T> specification6,
            WindowSpecification<T> specification7, WindowSpecification<T> specification8, WindowSpecification<T> specification9,
            WindowSpecification<T> specification10, WindowSpecification<T> specification11, WindowSpecification<T> specification12,
            WindowSpecification<T> specification13, WindowSpecification<T> specification14, WindowSpecification<T> specification15,
            WindowSpecification<T> specification16) {
        return fromStream(Seq.super.window(specification1, specification2, specification3, specification4, specification5, specification6,
                                           specification7, specification8, specification9, specification10, specification11, specification12,
                                           specification13, specification14, specification15, specification16));
    }

}

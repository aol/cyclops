package com.aol.cyclops.SequenceMuence;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

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



public interface JoolWindowing<T> extends Seq<T>{
	public <T> SequenceM<T> fromStream(Stream<T> stream);
	
	/**
     * Map this stream to a windowed stream using the default partition and order.
     * <p>
     * <code><pre>
     * // (0, 1, 2, 3, 4)
     * SequenceM.of(1, 2, 4, 2, 3).window().map(Window::rowNumber)
     * </pre></code>
     */ 
    default SequenceM<Window<T>> window() {
        return fromStream(Seq.super.window());
    }
   
    /**
     * Map this stream to a windowed stream using the default partition and order with frame.
     * <p>
     * <code><pre>
     * // (2, 4, 4, 4, 3)
     * SequenceM.of(1, 2, 4, 2, 3).window(-1, 1).map(Window::max)
     * </pre></code>
     */ 
    default SequenceM<Window<T>> window(long lower, long upper) {
    	 return fromStream(Seq.super.window(lower,upper));
       
    }
   
    /**
     * Map this stream to a windowed stream using the default partition and a specific order.
     * <p>
     * <code><pre>
     * // (0, 1, 4, 2, 3)
     * SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder()).map(Window::rowNumber)
     * </pre></code>
     */ 
    default SequenceM<Window<T>> window(Comparator<? super T> orderBy) {
    	return fromStream(Seq.super.window(orderBy));
    }
    
    /**
     * Map this stream to a windowed stream using the default partition and a specific order with frame.
     * <p>
     * <code><pre>
     * // (1, 1, 3, 2, 2)
     * SequenceM.of(1, 2, 4, 2, 3).window(naturalOrder(), -1, 1).map(Window::min)
     * </pre></code>
     */ 
    default SequenceM<Window<T>> window(Comparator<? super T> orderBy, long lower, long upper) {
        return window(Window.of(orderBy, lower, upper)).map(t -> t.v1);
    }
    
    /**
     * Map this stream to a windowed stream using a specific partition and the default order.
     * <p>
     * <code><pre>
     * // (1, 2, 2, 2, 1)
     * SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2).map(Window::min)
     * </pre></code>
     */ 
    default <U> SequenceM<Window<T>> window(Function<? super T, ? extends U> partitionBy) {
        return window(Window.of(partitionBy)).map(t -> t.v1);
    }
    
    /**
     * Map this stream to a windowed stream using a specific partition and the default order.
     * <p>
     * <code><pre>
     * // (3, 4, 4, 2, 3)
     * SequenceM.of(1, 4, 2, 2, 3).window(i -> i % 2, -1, 1).map(Window::max)
     * </pre></code>
     */ 
    default <U> SequenceM<Window<T>> window(Function<? super T, ? extends U> partitionBy, long lower, long upper) {
        return window(Window.of(partitionBy, lower, upper)).map(t -> t.v1);
    }
    
    /**
     * Map this stream to a windowed stream using a specific partition and order.
     * <p>
     * <code><pre>
     * // (1, 2, 4, 4, 3)
     * SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder()).map(Window::max)
     * </pre></code>
     */ 
    default <U> SequenceM<Window<T>> window(Function<? super T, ? extends U> partitionBy, Comparator<? super T> orderBy) {
        return window(Window.of(partitionBy, orderBy)).map(t -> t.v1);
    }
    
    /**
     * Map this stream to a windowed stream using a specific partition and order with frame.
     * <p>
     * <code><pre>
     * // (3, 2, 4, 4, 3)
     * SequenceM.of(1, 2, 4, 2, 3).window(i -> i % 2, naturalOrder(), -1, 1).map(Window::max)
     * </pre></code>
     */ 
    default <U> SequenceM<Window<T>> window(Function<? super T, ? extends U> partitionBy, Comparator<? super T> orderBy, long lower, long upper) {
        return window(Window.of(partitionBy, orderBy, lower, upper)).map(t -> t.v1);
    }

    // [jooq-tools] START [windows]

    /**
     * Map this stream to a windowed stream with 1 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple1<Window<T>>> window(
        WindowSpecification<T> specification1
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1)
              ));
    }

    /**
     * Map this stream to a windowed stream with 2 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple2<Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2)
              ));
    }

    /**
     * Map this stream to a windowed stream with 3 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple3<Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3)
              ));
    }

    /**
     * Map this stream to a windowed stream with 4 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple4<Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4)
              ));
    }

    /**
     * Map this stream to a windowed stream with 5 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple5<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5)
              ));
    }

    /**
     * Map this stream to a windowed stream with 6 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple6<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6)
              ));
    }

    /**
     * Map this stream to a windowed stream with 7 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple7<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6,
        WindowSpecification<T> specification7
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);
        Map<?, Partition<T>> partitions7 = SequenceMUtils.partitions(specification7, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6),
                   new WindowImpl<>(t, partitions7.get(specification7.partition().apply(t.v1)), specification7)
              ));
    }

    /**
     * Map this stream to a windowed stream with 8 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple8<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6,
        WindowSpecification<T> specification7,
        WindowSpecification<T> specification8
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);
        Map<?, Partition<T>> partitions7 = SequenceMUtils.partitions(specification7, buffer);
        Map<?, Partition<T>> partitions8 = SequenceMUtils.partitions(specification8, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6),
                   new WindowImpl<>(t, partitions7.get(specification7.partition().apply(t.v1)), specification7),
                   new WindowImpl<>(t, partitions8.get(specification8.partition().apply(t.v1)), specification8)
              ));
    }

    /**
     * Map this stream to a windowed stream with 9 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple9<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6,
        WindowSpecification<T> specification7,
        WindowSpecification<T> specification8,
        WindowSpecification<T> specification9
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);
        Map<?, Partition<T>> partitions7 = SequenceMUtils.partitions(specification7, buffer);
        Map<?, Partition<T>> partitions8 = SequenceMUtils.partitions(specification8, buffer);
        Map<?, Partition<T>> partitions9 = SequenceMUtils.partitions(specification9, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6),
                   new WindowImpl<>(t, partitions7.get(specification7.partition().apply(t.v1)), specification7),
                   new WindowImpl<>(t, partitions8.get(specification8.partition().apply(t.v1)), specification8),
                   new WindowImpl<>(t, partitions9.get(specification9.partition().apply(t.v1)), specification9)
              ));
    }

    /**
     * Map this stream to a windowed stream with 10 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple10<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6,
        WindowSpecification<T> specification7,
        WindowSpecification<T> specification8,
        WindowSpecification<T> specification9,
        WindowSpecification<T> specification10
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);
        Map<?, Partition<T>> partitions7 = SequenceMUtils.partitions(specification7, buffer);
        Map<?, Partition<T>> partitions8 = SequenceMUtils.partitions(specification8, buffer);
        Map<?, Partition<T>> partitions9 = SequenceMUtils.partitions(specification9, buffer);
        Map<?, Partition<T>> partitions10 = SequenceMUtils.partitions(specification10, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6),
                   new WindowImpl<>(t, partitions7.get(specification7.partition().apply(t.v1)), specification7),
                   new WindowImpl<>(t, partitions8.get(specification8.partition().apply(t.v1)), specification8),
                   new WindowImpl<>(t, partitions9.get(specification9.partition().apply(t.v1)), specification9),
                   new WindowImpl<>(t, partitions10.get(specification10.partition().apply(t.v1)), specification10)
              ));
    }

    /**
     * Map this stream to a windowed stream with 11 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple11<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6,
        WindowSpecification<T> specification7,
        WindowSpecification<T> specification8,
        WindowSpecification<T> specification9,
        WindowSpecification<T> specification10,
        WindowSpecification<T> specification11
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);
        Map<?, Partition<T>> partitions7 = SequenceMUtils.partitions(specification7, buffer);
        Map<?, Partition<T>> partitions8 = SequenceMUtils.partitions(specification8, buffer);
        Map<?, Partition<T>> partitions9 = SequenceMUtils.partitions(specification9, buffer);
        Map<?, Partition<T>> partitions10 = SequenceMUtils.partitions(specification10, buffer);
        Map<?, Partition<T>> partitions11 = SequenceMUtils.partitions(specification11, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6),
                   new WindowImpl<>(t, partitions7.get(specification7.partition().apply(t.v1)), specification7),
                   new WindowImpl<>(t, partitions8.get(specification8.partition().apply(t.v1)), specification8),
                   new WindowImpl<>(t, partitions9.get(specification9.partition().apply(t.v1)), specification9),
                   new WindowImpl<>(t, partitions10.get(specification10.partition().apply(t.v1)), specification10),
                   new WindowImpl<>(t, partitions11.get(specification11.partition().apply(t.v1)), specification11)
              ));
    }

    /**
     * Map this stream to a windowed stream with 12 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple12<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6,
        WindowSpecification<T> specification7,
        WindowSpecification<T> specification8,
        WindowSpecification<T> specification9,
        WindowSpecification<T> specification10,
        WindowSpecification<T> specification11,
        WindowSpecification<T> specification12
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);
        Map<?, Partition<T>> partitions7 = SequenceMUtils.partitions(specification7, buffer);
        Map<?, Partition<T>> partitions8 = SequenceMUtils.partitions(specification8, buffer);
        Map<?, Partition<T>> partitions9 = SequenceMUtils.partitions(specification9, buffer);
        Map<?, Partition<T>> partitions10 = SequenceMUtils.partitions(specification10, buffer);
        Map<?, Partition<T>> partitions11 = SequenceMUtils.partitions(specification11, buffer);
        Map<?, Partition<T>> partitions12 = SequenceMUtils.partitions(specification12, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6),
                   new WindowImpl<>(t, partitions7.get(specification7.partition().apply(t.v1)), specification7),
                   new WindowImpl<>(t, partitions8.get(specification8.partition().apply(t.v1)), specification8),
                   new WindowImpl<>(t, partitions9.get(specification9.partition().apply(t.v1)), specification9),
                   new WindowImpl<>(t, partitions10.get(specification10.partition().apply(t.v1)), specification10),
                   new WindowImpl<>(t, partitions11.get(specification11.partition().apply(t.v1)), specification11),
                   new WindowImpl<>(t, partitions12.get(specification12.partition().apply(t.v1)), specification12)
              ));
    }

    /**
     * Map this stream to a windowed stream with 13 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple13<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6,
        WindowSpecification<T> specification7,
        WindowSpecification<T> specification8,
        WindowSpecification<T> specification9,
        WindowSpecification<T> specification10,
        WindowSpecification<T> specification11,
        WindowSpecification<T> specification12,
        WindowSpecification<T> specification13
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);
        Map<?, Partition<T>> partitions7 = SequenceMUtils.partitions(specification7, buffer);
        Map<?, Partition<T>> partitions8 = SequenceMUtils.partitions(specification8, buffer);
        Map<?, Partition<T>> partitions9 = SequenceMUtils.partitions(specification9, buffer);
        Map<?, Partition<T>> partitions10 = SequenceMUtils.partitions(specification10, buffer);
        Map<?, Partition<T>> partitions11 = SequenceMUtils.partitions(specification11, buffer);
        Map<?, Partition<T>> partitions12 = SequenceMUtils.partitions(specification12, buffer);
        Map<?, Partition<T>> partitions13 = SequenceMUtils.partitions(specification13, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6),
                   new WindowImpl<>(t, partitions7.get(specification7.partition().apply(t.v1)), specification7),
                   new WindowImpl<>(t, partitions8.get(specification8.partition().apply(t.v1)), specification8),
                   new WindowImpl<>(t, partitions9.get(specification9.partition().apply(t.v1)), specification9),
                   new WindowImpl<>(t, partitions10.get(specification10.partition().apply(t.v1)), specification10),
                   new WindowImpl<>(t, partitions11.get(specification11.partition().apply(t.v1)), specification11),
                   new WindowImpl<>(t, partitions12.get(specification12.partition().apply(t.v1)), specification12),
                   new WindowImpl<>(t, partitions13.get(specification13.partition().apply(t.v1)), specification13)
              ));
    }

    /**
     * Map this stream to a windowed stream with 14 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple14<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6,
        WindowSpecification<T> specification7,
        WindowSpecification<T> specification8,
        WindowSpecification<T> specification9,
        WindowSpecification<T> specification10,
        WindowSpecification<T> specification11,
        WindowSpecification<T> specification12,
        WindowSpecification<T> specification13,
        WindowSpecification<T> specification14
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);
        Map<?, Partition<T>> partitions7 = SequenceMUtils.partitions(specification7, buffer);
        Map<?, Partition<T>> partitions8 = SequenceMUtils.partitions(specification8, buffer);
        Map<?, Partition<T>> partitions9 = SequenceMUtils.partitions(specification9, buffer);
        Map<?, Partition<T>> partitions10 = SequenceMUtils.partitions(specification10, buffer);
        Map<?, Partition<T>> partitions11 = SequenceMUtils.partitions(specification11, buffer);
        Map<?, Partition<T>> partitions12 = SequenceMUtils.partitions(specification12, buffer);
        Map<?, Partition<T>> partitions13 = SequenceMUtils.partitions(specification13, buffer);
        Map<?, Partition<T>> partitions14 = SequenceMUtils.partitions(specification14, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6),
                   new WindowImpl<>(t, partitions7.get(specification7.partition().apply(t.v1)), specification7),
                   new WindowImpl<>(t, partitions8.get(specification8.partition().apply(t.v1)), specification8),
                   new WindowImpl<>(t, partitions9.get(specification9.partition().apply(t.v1)), specification9),
                   new WindowImpl<>(t, partitions10.get(specification10.partition().apply(t.v1)), specification10),
                   new WindowImpl<>(t, partitions11.get(specification11.partition().apply(t.v1)), specification11),
                   new WindowImpl<>(t, partitions12.get(specification12.partition().apply(t.v1)), specification12),
                   new WindowImpl<>(t, partitions13.get(specification13.partition().apply(t.v1)), specification13),
                   new WindowImpl<>(t, partitions14.get(specification14.partition().apply(t.v1)), specification14)
              ));
    }

    /**
     * Map this stream to a windowed stream with 15 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple15<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6,
        WindowSpecification<T> specification7,
        WindowSpecification<T> specification8,
        WindowSpecification<T> specification9,
        WindowSpecification<T> specification10,
        WindowSpecification<T> specification11,
        WindowSpecification<T> specification12,
        WindowSpecification<T> specification13,
        WindowSpecification<T> specification14,
        WindowSpecification<T> specification15
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);
        Map<?, Partition<T>> partitions7 = SequenceMUtils.partitions(specification7, buffer);
        Map<?, Partition<T>> partitions8 = SequenceMUtils.partitions(specification8, buffer);
        Map<?, Partition<T>> partitions9 = SequenceMUtils.partitions(specification9, buffer);
        Map<?, Partition<T>> partitions10 = SequenceMUtils.partitions(specification10, buffer);
        Map<?, Partition<T>> partitions11 = SequenceMUtils.partitions(specification11, buffer);
        Map<?, Partition<T>> partitions12 = SequenceMUtils.partitions(specification12, buffer);
        Map<?, Partition<T>> partitions13 = SequenceMUtils.partitions(specification13, buffer);
        Map<?, Partition<T>> partitions14 = SequenceMUtils.partitions(specification14, buffer);
        Map<?, Partition<T>> partitions15 = SequenceMUtils.partitions(specification15, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6),
                   new WindowImpl<>(t, partitions7.get(specification7.partition().apply(t.v1)), specification7),
                   new WindowImpl<>(t, partitions8.get(specification8.partition().apply(t.v1)), specification8),
                   new WindowImpl<>(t, partitions9.get(specification9.partition().apply(t.v1)), specification9),
                   new WindowImpl<>(t, partitions10.get(specification10.partition().apply(t.v1)), specification10),
                   new WindowImpl<>(t, partitions11.get(specification11.partition().apply(t.v1)), specification11),
                   new WindowImpl<>(t, partitions12.get(specification12.partition().apply(t.v1)), specification12),
                   new WindowImpl<>(t, partitions13.get(specification13.partition().apply(t.v1)), specification13),
                   new WindowImpl<>(t, partitions14.get(specification14.partition().apply(t.v1)), specification14),
                   new WindowImpl<>(t, partitions15.get(specification15.partition().apply(t.v1)), specification15)
              ));
    }

    /**
     * Map this stream to a windowed stream with 16 distinct windows.
     */
    @Generated("This method was generated using jOOQ-tools")
    default SequenceM<Tuple16<Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>, Window<T>>> window(
        WindowSpecification<T> specification1,
        WindowSpecification<T> specification2,
        WindowSpecification<T> specification3,
        WindowSpecification<T> specification4,
        WindowSpecification<T> specification5,
        WindowSpecification<T> specification6,
        WindowSpecification<T> specification7,
        WindowSpecification<T> specification8,
        WindowSpecification<T> specification9,
        WindowSpecification<T> specification10,
        WindowSpecification<T> specification11,
        WindowSpecification<T> specification12,
        WindowSpecification<T> specification13,
        WindowSpecification<T> specification14,
        WindowSpecification<T> specification15,
        WindowSpecification<T> specification16
    ) {
        List<Tuple2<T, Long>> buffer = zipWithIndex().toList();

        Map<?, Partition<T>> partitions1 = SequenceMUtils.partitions(specification1, buffer);
        Map<?, Partition<T>> partitions2 = SequenceMUtils.partitions(specification2, buffer);
        Map<?, Partition<T>> partitions3 = SequenceMUtils.partitions(specification3, buffer);
        Map<?, Partition<T>> partitions4 = SequenceMUtils.partitions(specification4, buffer);
        Map<?, Partition<T>> partitions5 = SequenceMUtils.partitions(specification5, buffer);
        Map<?, Partition<T>> partitions6 = SequenceMUtils.partitions(specification6, buffer);
        Map<?, Partition<T>> partitions7 = SequenceMUtils.partitions(specification7, buffer);
        Map<?, Partition<T>> partitions8 = SequenceMUtils.partitions(specification8, buffer);
        Map<?, Partition<T>> partitions9 = SequenceMUtils.partitions(specification9, buffer);
        Map<?, Partition<T>> partitions10 = SequenceMUtils.partitions(specification10, buffer);
        Map<?, Partition<T>> partitions11 = SequenceMUtils.partitions(specification11, buffer);
        Map<?, Partition<T>> partitions12 = SequenceMUtils.partitions(specification12, buffer);
        Map<?, Partition<T>> partitions13 = SequenceMUtils.partitions(specification13, buffer);
        Map<?, Partition<T>> partitions14 = SequenceMUtils.partitions(specification14, buffer);
        Map<?, Partition<T>> partitions15 = SequenceMUtils.partitions(specification15, buffer);
        Map<?, Partition<T>> partitions16 = SequenceMUtils.partitions(specification16, buffer);

        return SequenceM(buffer)
              .map(t -> tuple(
                   new WindowImpl<>(t, partitions1.get(specification1.partition().apply(t.v1)), specification1),
                   new WindowImpl<>(t, partitions2.get(specification2.partition().apply(t.v1)), specification2),
                   new WindowImpl<>(t, partitions3.get(specification3.partition().apply(t.v1)), specification3),
                   new WindowImpl<>(t, partitions4.get(specification4.partition().apply(t.v1)), specification4),
                   new WindowImpl<>(t, partitions5.get(specification5.partition().apply(t.v1)), specification5),
                   new WindowImpl<>(t, partitions6.get(specification6.partition().apply(t.v1)), specification6),
                   new WindowImpl<>(t, partitions7.get(specification7.partition().apply(t.v1)), specification7),
                   new WindowImpl<>(t, partitions8.get(specification8.partition().apply(t.v1)), specification8),
                   new WindowImpl<>(t, partitions9.get(specification9.partition().apply(t.v1)), specification9),
                   new WindowImpl<>(t, partitions10.get(specification10.partition().apply(t.v1)), specification10),
                   new WindowImpl<>(t, partitions11.get(specification11.partition().apply(t.v1)), specification11),
                   new WindowImpl<>(t, partitions12.get(specification12.partition().apply(t.v1)), specification12),
                   new WindowImpl<>(t, partitions13.get(specification13.partition().apply(t.v1)), specification13),
                   new WindowImpl<>(t, partitions14.get(specification14.partition().apply(t.v1)), specification14),
                   new WindowImpl<>(t, partitions15.get(specification15.partition().apply(t.v1)), specification15),
                   new WindowImpl<>(t, partitions16.get(specification16.partition().apply(t.v1)), specification16)
              ));
    }

}

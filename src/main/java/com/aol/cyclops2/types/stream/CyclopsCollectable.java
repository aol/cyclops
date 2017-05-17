package com.aol.cyclops2.types.stream;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import com.aol.cyclops2.types.stream.reactive.ReactiveStreamsTerminalOperations;
import org.jooq.lambda.Collectable;

import com.aol.cyclops2.types.Folds;
import org.jooq.lambda.Seq;

/**
 * 
 * Wrapper around jool.Collectable type and Iterable
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements in this Collectable
 */
public interface CyclopsCollectable<T> extends  Iterable<T>, Folds<T>,ReactiveStreamsTerminalOperations<T> {



   /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#mode()
     */

    default Optional<T> mode() {
        return statisticalOperations().mode();
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#commonPrefix()
     */
    
    default String commonPrefix() {

        return statisticalOperations().commonPrefix();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#commonSuffix()
     */
    
    default String commonSuffix() {

        return statisticalOperations().commonSuffix();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.reactiveStream.Collector)
     */
    
    default <R, A> R collect(final Collector<? super T, A, R> collector) {
        return statisticalOperations().collect(collector);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#count()
     */
    
    default long count() {
        return statisticalOperations().count();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#count(java.util.function.Predicate)
     */
    
    default long count(final Predicate<? super T> predicate) {
        return statisticalOperations().count(predicate);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinct(java.util.function.Predicate)
     */
    
    default long countDistinct(final Predicate<? super T> predicate) {
        return statisticalOperations().countDistinct(predicate);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinctBy(java.util.function.Function, java.util.function.Predicate)
     */
    
    default <U> long countDistinctBy(final Function<? super T, ? extends U> function, final Predicate<? super U> predicate) {
        return statisticalOperations().countDistinctBy(function, predicate);
    }

    /**
     * Narrow this class to a Collectable
     * 
     * @return Collectable
     */
    default Collectable<T> statisticalOperations(){
        return Seq.seq(this);
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinct()
     */
    
    default long countDistinct() {
        return statisticalOperations().countDistinct();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinctBy(java.util.function.Function)
     */
    
    default <U> long countDistinctBy(final Function<? super T, ? extends U> function) {
        return statisticalOperations().countDistinctBy(function);
    }






    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumInt(java.util.function.ToIntFunction)
     */
    
    default int sumInt(final ToIntFunction<? super T> function) {

        return statisticalOperations().sumInt(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumLong(java.util.function.ToLongFunction)
     */
    
    default long sumLong(final ToLongFunction<? super T> function) {
        return statisticalOperations().sumLong(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumDouble(java.util.function.ToDoubleFunction)
     */
    
    default double sumDouble(final ToDoubleFunction<? super T> function) {
        return statisticalOperations().sumDouble(function);
    }


    /**
     * True if predicate matches all elements when Monad converted to a Stream
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
     * }
     * </pre>
     * 
     * @param c
     *            Predicate to check if all match
     */
    
    default boolean allMatch(final Predicate<? super T> c) {
        return statisticalOperations().allMatch(c);
    }

    /**
     * True if a single element matches when Monad converted to a Stream
     * 
     * <pre>
     * {@code 
     *     ReactiveSeq.of(1,2,3,4,5).anyMatch(it-> it.equals(3))
     *     //true
     * }
     * </pre>
     * 
     * @param c
     *            Predicate to check if any match
     */
    
    default boolean anyMatch(final Predicate<? super T> c) {
        return statisticalOperations().anyMatch(c);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#noneMatch(java.util.function.Predicate)
     */
    
    default boolean noneMatch(final Predicate<? super T> c) {
        return statisticalOperations().noneMatch(c);
    }

        /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toCollection(java.util.function.Supplier)
     */
    
    default <C extends Collection<T>> C toCollection(final Supplier<C> factory) {
        return statisticalOperations().toCollection(factory);
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toString(java.lang.CharSequence)
     */
    
    default String toString(final CharSequence delimiter) {
        return statisticalOperations().toString(delimiter);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toString(java.lang.CharSequence, java.lang.CharSequence, java.lang.CharSequence)
     */
    
    default String toString(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        return statisticalOperations().toString(delimiter, prefix, suffix);
    }

}

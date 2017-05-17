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
    /**
     * Collect the collectable into an {@link ArrayList}.
     */
    default List<T> toList(){
        return  collectionOperations().toList();
    }
    /**
     * Collect the collectable into a {@link LinkedHashSet}.
     */
    default Set<T> toSet(){
        return collectionOperations().toSet();
    }
    /**
     * Get the minimum value by a function.
     */
    default Optional<T> min(Comparator<? super T> comparator){
        return collectionOperations().min(comparator);
    }
    /**
     * Get the minimum value by a function.
     */
    default <U extends Comparable<? super U>> Optional<T> minBy(Function<? super T, ? extends U> function){
        return collectionOperations().minBy(function);
    }
    /**
     * Get the maximum value by a function.
     */
    default Optional<T> max(Comparator<? super T> comparator){
        return collectionOperations().max(comparator);
    }
    /**
     * Get the maximum value by a function.
     */
    default <U extends Comparable<? super U>> Optional<T> maxBy(Function<? super T, ? extends U> function){
        return collectionOperations().maxBy(function);
    }

   /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#mode()
     */

    default Optional<T> mode() {
        return collectionOperations().mode();
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#commonPrefix()
     */
    
    default String commonPrefix() {

        return collectionOperations().commonPrefix();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#commonSuffix()
     */
    
    default String commonSuffix() {

        return collectionOperations().commonSuffix();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.reactiveStream.Collector)
     */
    
    default <R, A> R collect(final Collector<? super T, A, R> collector) {
        return collectionOperations().collect(collector);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#count()
     */
    
    default long count() {
        return collectionOperations().count();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#count(java.util.function.Predicate)
     */
    
    default long count(final Predicate<? super T> predicate) {
        return collectionOperations().count(predicate);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinct(java.util.function.Predicate)
     */
    
    default long countDistinct(final Predicate<? super T> predicate) {
        return collectionOperations().countDistinct(predicate);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinctBy(java.util.function.Function, java.util.function.Predicate)
     */
    
    default <U> long countDistinctBy(final Function<? super T, ? extends U> function, final Predicate<? super U> predicate) {
        return collectionOperations().countDistinctBy(function, predicate);
    }

    /**
     * Narrow this class to a Collectable
     * 
     * @return Collectable
     */
    default Collectable<T> collectionOperations(){
        return Seq.seq(this);
    }

    /**
     * Collect the collectable into a {@link Map}.
     */
    default <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper){
        return collectionOperations().toMap(keyMapper,valueMapper);
    }

    /**
     * Collect the collectable into a {@link Map} with the given keys and the self element as value.
     */
    default <K> Map<K, T> toMap(Function<? super T, ? extends K> keyMapper){
        return collectionOperations().toMap(keyMapper);
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinct()
     */
    
    default long countDistinct() {
        return collectionOperations().countDistinct();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinctBy(java.util.function.Function)
     */
    
    default <U> long countDistinctBy(final Function<? super T, ? extends U> function) {
        return collectionOperations().countDistinctBy(function);
    }




    /**
     * Get the sum of the elements in this collectable.
     */
    default <U> Optional<U> sum(Function<? super T, ? extends U> function){
        return collectionOperations().sum(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumInt(java.util.function.ToIntFunction)
     */
    
    default int sumInt(final ToIntFunction<? super T> function) {

        return collectionOperations().sumInt(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumLong(java.util.function.ToLongFunction)
     */
    
    default long sumLong(final ToLongFunction<? super T> function) {
        return collectionOperations().sumLong(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumDouble(java.util.function.ToDoubleFunction)
     */
    
    default double sumDouble(final ToDoubleFunction<? super T> function) {
        return collectionOperations().sumDouble(function);
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
        return collectionOperations().allMatch(c);
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
        return collectionOperations().anyMatch(c);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#noneMatch(java.util.function.Predicate)
     */
    
    default boolean noneMatch(final Predicate<? super T> c) {
        return collectionOperations().noneMatch(c);
    }

        /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toCollection(java.util.function.Supplier)
     */
    
    default <C extends Collection<T>> C toCollection(final Supplier<C> factory) {
        return collectionOperations().toCollection(factory);
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toString(java.lang.CharSequence)
     */
    
    default String toString(final CharSequence delimiter) {
        return collectionOperations().toString(delimiter);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toString(java.lang.CharSequence, java.lang.CharSequence, java.lang.CharSequence)
     */
    
    default String toString(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        return collectionOperations().toString(delimiter, prefix, suffix);
    }

}

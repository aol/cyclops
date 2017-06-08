package com.aol.cyclops2.types.foldable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import com.aol.cyclops2.types.reactive.ReactiveStreamsTerminalOperations;
import org.jooq.lambda.Collectable;

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
    default ConvertableSequence<T> to(){
        return new ConvertableSequence<>(this);
    }
    /**
     * Collect the collectable into an {@link ArrayList}.
     */
    default List<T> toList(){
        return  collectors().toList();
    }
    /**
     * Collect the collectable into a {@link LinkedHashSet}.
     */
    default Set<T> toSet(){
        return collectors().toSet();
    }
    /**
     * Get the minimum value by a function.
     */
    default Optional<T> min(Comparator<? super T> comparator){
        return collectors().min(comparator);
    }
    /**
     * Get the minimum value by a function.
     */
    default <U extends Comparable<? super U>> Optional<T> minBy(Function<? super T, ? extends U> function){
        return collectors().minBy(function);
    }
    /**
     * Get the maximum value by a function.
     */
    default Optional<T> max(Comparator<? super T> comparator){
        return collectors().max(comparator);
    }
    /**
     * Get the maximum value by a function.
     */
    default <U extends Comparable<? super U>> Optional<T> maxBy(Function<? super T, ? extends U> function){
        return collectors().maxBy(function);
    }

   /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#mode()
     */

    default Optional<T> mode() {
        return collectors().mode();
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#commonPrefix()
     */
    
    default String commonPrefix() {

        return collectors().commonPrefix();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#commonSuffix()
     */
    
    default String commonSuffix() {

        return collectors().commonSuffix();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.reactiveStream.Collector)
     */
    
    default <R, A> R collect(final Collector<? super T, A, R> collector) {
        return collectors().collect(collector);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#count()
     */
    
    default long count() {
        return collectors().count();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#count(java.util.function.Predicate)
     */
    
    default long count(final Predicate<? super T> predicate) {
        return collectors().count(predicate);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinct(java.util.function.Predicate)
     */
    
    default long countDistinct(final Predicate<? super T> predicate) {
        return collectors().countDistinct(predicate);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinctBy(java.util.function.Function, java.util.function.Predicate)
     */
    
    default <U> long countDistinctBy(final Function<? super T, ? extends U> function, final Predicate<? super U> predicate) {
        return collectors().countDistinctBy(function, predicate);
    }

    /**
     * Narrow this class toNested a Collectable
     * 
     * @return Collectable
     */
    default Collectable<T> collectors(){
        return Seq.seq(this);
    }

    /**
     * Collect the collectable into a {@link Map}.
     */
    default <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper){
        return collectors().toMap(keyMapper,valueMapper);
    }

    /**
     * Collect the collectable into a {@link Map} with the given keys and the self element as value.
     */
    default <K> Map<K, T> toMap(Function<? super T, ? extends K> keyMapper){
        return collectors().toMap(keyMapper);
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinct()
     */
    
    default long countDistinct() {
        return collectors().countDistinct();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinctBy(java.util.function.Function)
     */
    
    default <U> long countDistinctBy(final Function<? super T, ? extends U> function) {
        return collectors().countDistinctBy(function);
    }




    /**
     * Get the sum of the elements in this collectable.
     */
    default <U> Optional<U> sum(Function<? super T, ? extends U> function){
        return collectors().sum(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumInt(java.util.function.ToIntFunction)
     */
    
    default int sumInt(final ToIntFunction<? super T> function) {

        return collectors().sumInt(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumLong(java.util.function.ToLongFunction)
     */
    
    default long sumLong(final ToLongFunction<? super T> function) {
        return collectors().sumLong(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumDouble(java.util.function.ToDoubleFunction)
     */
    
    default double sumDouble(final ToDoubleFunction<? super T> function) {
        return collectors().sumDouble(function);
    }


    /**
     * True if predicate matches all elements when Monad converted toNested a Stream
     * 
     * <pre>
     * {@code 
     * assertThat(ReactiveSeq.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
     * }
     * </pre>
     * 
     * @param c
     *            Predicate toNested check if all match
     */
    
    default boolean allMatch(final Predicate<? super T> c) {
        return collectors().allMatch(c);
    }

    /**
     * True if a singleUnsafe element matches when Monad converted toNested a Stream
     * 
     * <pre>
     * {@code 
     *     ReactiveSeq.of(1,2,3,4,5).anyMatch(it-> it.equals(3))
     *     //true
     * }
     * </pre>
     * 
     * @param c
     *            Predicate toNested check if any match
     */
    
    default boolean anyMatch(final Predicate<? super T> c) {
        return collectors().anyMatch(c);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#noneMatch(java.util.function.Predicate)
     */
    
    default boolean noneMatch(final Predicate<? super T> c) {
        return collectors().noneMatch(c);
    }




    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toString(java.lang.CharSequence)
     */
    
    default String toString(final CharSequence delimiter) {
        return collectors().toString(delimiter);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toString(java.lang.CharSequence, java.lang.CharSequence, java.lang.CharSequence)
     */
    
    default String toString(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        return collectors().toString(delimiter, prefix, suffix);
    }

}

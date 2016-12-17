package com.aol.cyclops.types.stream;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.*;
import java.util.stream.Collector;

import org.jooq.lambda.Collectable;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.types.Foldable;
import org.jooq.lambda.Seq;

/**
 * 
 * Wrapper around jool.Collectable type and Iterable
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements in this Collectable
 */
public interface CyclopsCollectable<T> extends  Iterable<T>, Foldable<T> { //Collectable<T>,

    /**
     * Perform an async fold on the provided executor
     *  
     *  <pre>
     *  {@code 
     *    FutureW<Integer> sum =  ListX.of(1,2,3)
     *                                 .map(this::load)
     *                                 .foldFuture(list->list.reduce(0,(a,b)->a+b),exec)
     *  
     *  }
     *  </pre>
     *  
     * Similar to @see {@link ReactiveSeq#futureOperations(Executor)}, but returns FutureW
     *  
     * @param fn Folding function
     * @param ex Executor to perform fold on
     * @return Future that will contain the result when complete
     */
    default <R> FutureW<R> foldFuture(Function<? super CyclopsCollectable<T>,? extends R> fn,Executor ex){
        return FutureW.ofSupplier(()->fn.apply(this),ex);
    }
    default <R> FutureW<Void> runFuture( Executor ex,Consumer<? super CyclopsCollectable<T>> fn){
        return FutureW.ofSupplier(()->{ fn.accept(this); return null;},ex);
    }
    /**
     * Perform a lazy caching fold (results are memoized)
     *  <pre>
     *  {@code 
     *    Eval<Integer> sum =  ListX.of(1,2,3)
     *                                 .map(this::load)
     *                                 .foldLazy(list->list.reduce(0,(a,b)->a+b))
     *  
     *  }
     *  </pre>
     *  
     *  Similar to @see {@link ReactiveSeq#lazyOperations()}, but always returns Eval (e.g. with nested Optionals)
     *   
     * @param fn Folding function
     * @return Eval that lazily performs the fold once
     */
    default <R> Eval<R> foldLazy(Function<? super CyclopsCollectable<T>,? extends R> fn){
        return Eval.later(()->fn.apply(this));
    }
   
    /**
     * Try a fold, capturing any unhandling execution exceptions (that match the provided classes)
     *  <pre>
     *  {@code 
     *    Try<Integer,Throwable> sum =  ListX.of(1,2,3)
     *                                       .map(this::load)
     *                                       .foldLazy(list->list.reduce(0,(a,b)->a+b),IOException.class)
     *  
     *  }
     *  </pre>
     * @param fn Folding function
     * @param classes Unhandled Exception types to capture in Try
     * @return Try that eagerly executes the fold and captures specified unhandled exceptions
     */
    default <R, X extends Throwable> Try<R, X> foldTry(Function<? super CyclopsCollectable<T>,? extends R> fn, 
                                                        final Class<X>... classes){
        return Try.catchExceptions(classes).tryThis(()->fn.apply(this));
    }
    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#modeBy(java.util.function.Function)
     */
    
    default <U> Optional<T> modeBy(final Function<? super T, ? extends U> function) {

        return collectable().modeBy(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#modeAll()
     */
    
    default ReactiveSeq<T> modeAll() {

        return ReactiveSeq.fromStream(collectable().modeAll());
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#modeAllBy(java.util.function.Function)
     */
    
    default <U> ReactiveSeq<T> modeAllBy(final Function<? super T, ? extends U> function) {

        return ReactiveSeq.fromStream(collectable().modeAllBy(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAll()
     */
    
    default ReactiveSeq<T> minAll() {

        return ReactiveSeq.fromStream(collectable().minAll());
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAll(java.util.Comparator)
     */
    
    default ReactiveSeq<T> minAll(final Comparator<? super T> comparator) {

        return ReactiveSeq.fromStream(collectable().minAll(comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAll(java.util.function.Function)
     */
    
    default <U extends Comparable<? super U>> ReactiveSeq<U> minAll(final Function<? super T, ? extends U> function) {

        return ReactiveSeq.fromStream(collectable().minAll(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAll(java.util.function.Function, java.util.Comparator)
     */
    
    default <U> ReactiveSeq<U> minAll(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {

        return ReactiveSeq.fromStream(collectable().minAll(function, comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAllBy(java.util.function.Function)
     */
    
    default <U extends Comparable<? super U>> ReactiveSeq<T> minAllBy(final Function<? super T, ? extends U> function) {

        return ReactiveSeq.fromStream(collectable().minAllBy(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minAllBy(java.util.function.Function, java.util.Comparator)
     */
    
    default <U> ReactiveSeq<T> minAllBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {

        return ReactiveSeq.fromStream(collectable().minAllBy(function, comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAll()
     */
    
    default ReactiveSeq<T> maxAll() {

        return ReactiveSeq.fromStream(collectable().maxAll());
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAll(java.util.Comparator)
     */
    
    default ReactiveSeq<T> maxAll(final Comparator<? super T> comparator) {

        return ReactiveSeq.fromStream(collectable().maxAll(comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAll(java.util.function.Function)
     */
    
    default <U extends Comparable<? super U>> ReactiveSeq<U> maxAll(final Function<? super T, ? extends U> function) {

        return ReactiveSeq.fromStream(collectable().maxAll(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAll(java.util.function.Function, java.util.Comparator)
     */
    
    default <U> ReactiveSeq<U> maxAll(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {

        return ReactiveSeq.fromStream(collectable().maxAll(function, comparator));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAllBy(java.util.function.Function)
     */
    
    default <U extends Comparable<? super U>> ReactiveSeq<T> maxAllBy(final Function<? super T, ? extends U> function) {

        return ReactiveSeq.fromStream(collectable().maxAllBy(function));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxAllBy(java.util.function.Function, java.util.Comparator)
     */
    
    default <U> ReactiveSeq<T> maxAllBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {

        return ReactiveSeq.fromStream(collectable().maxAllBy(function, comparator));
    }


    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitAnd(java.util.function.Function)
     */
    
    default <U> Optional<U> bitAnd(final Function<? super T, ? extends U> function) {

        return collectable().bitAnd(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitAndInt(java.util.function.ToIntFunction)
     */
    
    default int bitAndInt(final ToIntFunction<? super T> function) {

        return collectable().bitAndInt(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitAndLong(java.util.function.ToLongFunction)
     */
    
    default long bitAndLong(final ToLongFunction<? super T> function) {

        return collectable().bitAndLong(function);
    }



    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitOr(java.util.function.Function)
     */
    
    default <U extends Number> Optional<U> bitOr(final Function<? super T, ? extends U> function) {

        return collectable().bitOr(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitOrInt(java.util.function.ToIntFunction)
     */
    
    default int bitOrInt(final ToIntFunction<? super T> function) {

        return collectable().bitOrInt(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#bitOrLong(java.util.function.ToLongFunction)
     */
    
    default long bitOrLong(final ToLongFunction<? super T> function) {

        return collectable().bitOrLong(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toMap(java.util.function.Function)
     */
    
    default <K> Map<K, T> toMap(final Function<? super T, ? extends K> keyMapper) {

        return collectable().toMap(keyMapper);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#commonPrefix()
     */
    
    default String commonPrefix() {

        return collectable().commonPrefix();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#commonSuffix()
     */
    
    default String commonSuffix() {

        return collectable().commonSuffix();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.stream.Collector)
     */
    
    default <R, A> R collect(final Collector<? super T, A, R> collector) {
        return collectable().collect(collector);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#count()
     */
    
    default long count() {
        return collectable().count();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#count(java.util.function.Predicate)
     */
    
    default long count(final Predicate<? super T> predicate) {
        return collectable().count(predicate);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinct(java.util.function.Predicate)
     */
    
    default long countDistinct(final Predicate<? super T> predicate) {
        return collectable().countDistinct(predicate);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinctBy(java.util.function.Function, java.util.function.Predicate)
     */
    
    default <U> long countDistinctBy(final Function<? super T, ? extends U> function, final Predicate<? super U> predicate) {
        return collectable().countDistinctBy(function, predicate);
    }

    /**
     * Narrow this class to a Collectable
     * 
     * @return Collectable
     */
    default Collectable<T> collectable(){
        return Seq.seq(this);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinct()
     */
    
    default long countDistinct() {
        return collectable().countDistinct();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#countDistinctBy(java.util.function.Function)
     */
    
    default <U> long countDistinctBy(final Function<? super T, ? extends U> function) {
        return collectable().countDistinctBy(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#mode()
     */
    
    default Optional<T> mode() {
        return collectable().mode();
    }



    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sum(java.util.function.Function)
     */
    
    default <U extends Number> Optional<U> sum(final Function<? super T, ? extends U> function) {
        return collectable().sum(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumInt(java.util.function.ToIntFunction)
     */
    
    default int sumInt(final ToIntFunction<? super T> function) {
        return collectable().sumInt(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumLong(java.util.function.ToLongFunction)
     */
    
    default long sumLong(final ToLongFunction<? super T> function) {
        return collectable().sumLong(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#sumDouble(java.util.function.ToDoubleFunction)
     */
    
    default double sumDouble(final ToDoubleFunction<? super T> function) {
        return collectable().sumDouble(function);
    }



    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#avg(java.util.function.Function)
     */
    
    default <U extends Number> Optional<U> avg(final Function<? super T, ? extends U> function) {
        return collectable().avg(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#avgInt(java.util.function.ToIntFunction)
     */
    
    default double avgInt(final ToIntFunction<? super T> function) {
        return collectable().avgInt(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#avgLong(java.util.function.ToLongFunction)
     */
    
    default double avgLong(final ToLongFunction<? super T> function) {
        return collectable().avgLong(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#avgDouble(java.util.function.ToDoubleFunction)
     */
    
    default double avgDouble(final ToDoubleFunction<? super T> function) {
        return collectable().avgDouble(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#min()
     */
    
    default Optional<T> min() {
        return collectable().min();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#min(java.util.Comparator)
     */
    
    default Optional<T> min(final Comparator<? super T> comparator) {
        return collectable().min(comparator);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#min(java.util.function.Function)
     */
    
    default <U extends Comparable<? super U>> Optional<U> min(final Function<? super T, ? extends U> function) {
        return collectable().min(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#min(java.util.function.Function, java.util.Comparator)
     */
    
    default <U> Optional<U> min(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return collectable().min(function, comparator);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minBy(java.util.function.Function)
     */
    
    default <U extends Comparable<? super U>> Optional<T> minBy(final Function<? super T, ? extends U> function) {
        return collectable().minBy(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#minBy(java.util.function.Function, java.util.Comparator)
     */
    
    default <U> Optional<T> minBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return collectable().minBy(function, comparator);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#max()
     */
    
    default Optional<T> max() {
        return collectable().max();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#max(java.util.Comparator)
     */
    
    default Optional<T> max(final Comparator<? super T> comparator) {
        return collectable().max(comparator);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#max(java.util.function.Function)
     */
    
    default <U extends Comparable<? super U>> Optional<U> max(final Function<? super T, ? extends U> function) {
        return collectable().max(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#max(java.util.function.Function, java.util.Comparator)
     */
    
    default <U> Optional<U> max(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return collectable().max(function, comparator);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxBy(java.util.function.Function)
     */
    
    default <U extends Comparable<? super U>> Optional<T> maxBy(final Function<? super T, ? extends U> function) {
        return collectable().maxBy(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#maxBy(java.util.function.Function, java.util.Comparator)
     */
    
    default <U> Optional<T> maxBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return collectable().maxBy(function, comparator);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#median()
     */

    default Optional<T> median() {
        return collectable().median();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#median(java.util.Comparator)
     */
    
    default Optional<T> median(final Comparator<? super T> comparator) {
        return collectable().median(comparator);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#medianBy(java.util.function.Function)
     */
    
    default <U extends Comparable<? super U>> Optional<T> medianBy(final Function<? super T, ? extends U> function) {
        return collectable().medianBy(function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#medianBy(java.util.function.Function, java.util.Comparator)
     */
    
    default <U> Optional<T> medianBy(final Function<? super T, ? extends U> function, final Comparator<? super U> comparator) {
        return collectable().medianBy(function, comparator);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#percentile(double)
     */
    
    default Optional<T> percentile(final double percentile) {
        return collectable().percentile(percentile);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#percentile(double, java.util.Comparator)
     */
    
    default Optional<T> percentile(final double percentile, final Comparator<? super T> comparator) {
        return collectable().percentile(percentile, comparator);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#percentileBy(double, java.util.function.Function)
     */
    
    default <U extends Comparable<? super U>> Optional<T> percentileBy(final double percentile, final Function<? super T, ? extends U> function) {
        return collectable().percentileBy(percentile, function);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#percentileBy(double, java.util.function.Function, java.util.Comparator)
     */
    
    default <U> Optional<T> percentileBy(final double percentile, final Function<? super T, ? extends U> function,
            final Comparator<? super U> comparator) {
        return collectable().percentileBy(percentile, function, comparator);
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
        return collectable().allMatch(c);
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
        return collectable().anyMatch(c);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#noneMatch(java.util.function.Predicate)
     */
    
    default boolean noneMatch(final Predicate<? super T> c) {
        return collectable().noneMatch(c);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toList()
     */
    
    default List<T> toList() {
        if (this instanceof List)
            return (List) this;
        return collectable().toList();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toList(java.util.function.Supplier)
     */
    
    default <L extends List<T>> L toList(final Supplier<L> factory) {
        return collectable().toList(factory);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toSet()
     */
    
    default Set<T> toSet() {
        if (this instanceof Set)
            return (Set) this;
        return collectable().toSet();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toSet(java.util.function.Supplier)
     */
    
    default <S extends Set<T>> S toSet(final Supplier<S> factory) {
        return collectable().toSet(factory);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toCollection(java.util.function.Supplier)
     */
    
    default <C extends Collection<T>> C toCollection(final Supplier<C> factory) {
        return collectable().toCollection(factory);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toMap(java.util.function.Function, java.util.function.Function)
     */
    
    default <K, V> Map<K, V> toMap(final Function<? super T, ? extends K> keyMapper, final Function<? super T, ? extends V> valueMapper) {
        return collectable().toMap(keyMapper, valueMapper);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toString(java.lang.CharSequence)
     */
    
    default String toString(final CharSequence delimiter) {
        return collectable().toString(delimiter);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#toString(java.lang.CharSequence, java.lang.CharSequence, java.lang.CharSequence)
     */
    
    default String toString(final CharSequence delimiter, final CharSequence prefix, final CharSequence suffix) {
        return collectable().toString(delimiter, prefix, suffix);
    }

}

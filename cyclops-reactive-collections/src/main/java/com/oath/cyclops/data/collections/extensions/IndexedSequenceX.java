package com.oath.cyclops.data.collections.extensions;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Interface that represents a FluentSequence of data
 * <p>
 * Supports operations such as adding / removing elements via a Fluent API
 *
 * @param <T> the type of elements held in this toX
 * @author johnmcclean
 */
public interface IndexedSequenceX<T> extends FluentCollectionX<T> {
    T getOrElse(int index, T value);
    T getOrElseGet(int index,Supplier<? extends T> supplier);
    /* (non-Javadoc)
     * @see FluentCollectionX#plus(java.lang.Object)
     */
    @Override
    public IndexedSequenceX<T> plus(T e);

    /* (non-Javadoc)
     * @see FluentCollectionX#insertAt(java.util.Collection)
     */
    @Override
    public IndexedSequenceX<T> plusAll(Iterable<? extends T> list);

    /**
     * Replace the value at the specifed index with the supplied value
     *
     * @param i Index to one value at
     * @param e Value to use
     * @return FluentSequence with value replaced
     */
    public IndexedSequenceX<T> insertAt(int i, T e);



    /* (non-Javadoc)
     * @see FluentCollectionX#removeValue(java.lang.Object)
     */
    @Override
    public IndexedSequenceX<T> removeValue(T e);

    /* (non-Javadoc)
     * @see FluentCollectionX#removeAll(java.util.Collection)
     */
    @Override
    public IndexedSequenceX<T> removeAll(Iterable<? extends T> list);

    /**
     * Remove the element at the supplied index
     *
     * @param i Index at which to remvoe element
     * @return FluentSequence with element removed
     */
    public IndexedSequenceX<T> removeAt(long i);



    /* (non-Javadoc)
     * @see CollectionX#filter(java.util.function.Predicate)
     */
    @Override
    IndexedSequenceX<T> filter(Predicate<? super T> pred);

    /* (non-Javadoc)
     * @see CollectionX#map(java.util.function.Function)
     */
    @Override
    <R> IndexedSequenceX<R> map(Function<? super T, ? extends R> mapper);

    /* (non-Javadoc)
     * @see CollectionX#flatMap(java.util.function.Function)
     */
    @Override
    <R> IndexedSequenceX<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);


}

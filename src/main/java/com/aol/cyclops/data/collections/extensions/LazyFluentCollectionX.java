package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import cyclops.stream.ReactiveSeq;


/**
 * A Lazy Collection with a fluent api. Extended operators act eagerly, direct operations on a collection
 * to add, remove or retrieve elements should be eager unless otherwise stated.
 *
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public interface LazyFluentCollectionX<T> extends FluentCollectionX<T> {


    default FluentCollectionX<T> plusLoop(int max, IntFunction<T> value){
        FluentCollectionX<T> toUse = this;
        for(int i=0;i<max;i++){
            toUse = toUse.plus(value.apply(i));
        }
        return toUse;
    }
    default FluentCollectionX<T> plusLoop(Supplier<Optional<T>> supplier){
        FluentCollectionX<T> toUse = this;
        Optional<T> next =  supplier.get();
        while(next.isPresent()){
            toUse = toUse.plus(next.get());
            next = supplier.get();
        }
        return toUse;
    }

    /**
     * @return This collection with any queued Lazy Operations materialized
     */
    LazyFluentCollectionX<T> materialize();
    /**
     * Create a LazyFluentCollection from a Flux.
     * The created LazyFluentCollection will be of the same type as the object this method is called on.
     * i.e. Calling stream(Flux) on a LazyListX results in a LazyListX
     *
     *
     * <pre>
     * {@code
     *
     *     LazyListX<Integer> lazyInts = LazyListX.of(1,2,3);
     *     LazyListX<String> lazyStrs = lazyInts.stream(Flux.just("hello","world"));
     *
     * }
     * </pre>
     * Calling stream(Flux) on a LazySetX results in a LazySetX etc.
     *
     * The same collection / reduction method will be used in the newly created Object. I.e. Calling  stream(Flux) on
     * a collection which as an Immutable Collector  will result in an Immutable Collection.
     *

     *
     * @param stream Flux to create new collection from
     * @return New collection from Flux
     */
    <X> FluentCollectionX<X> stream(ReactiveSeq<X> stream);

    /**
     * Lazily add an element to this Collection.
     * The Collection will not be materialized (unlike via @see {@link LazyFluentCollectionX#plus(Object)}
     * <pre>
     * {@code
     *    LazyListX<Integer> lazy = LazyListX.of(1,2,3)
     *                                       .map(i->i*2)
     *                                       .plusLazy(5);
     *
     *   //Lazy List that will contain [2,4,6,5] when triggered
     * }
     * </pre>
     *
     * @param e Element to add
     * @return LazyFluentCollectionX with element added
     */
    default LazyFluentCollectionX<T> plusLazy(T e) {
        add(e);
        return this;
    }

    /**
     * Lazily add all the elements in the supplied Collection to this Collection.
     * The Collection will not be materialized (unlike via @see {@link LazyFluentCollectionX#plusAll(Object)}
     * <pre>
     * {@code
     *    LazyListX<Integer> lazy = LazyListX.of(1,2,3)
     *                                       .map(i->i*2)
     *                                       .plusAllLazy(ListX.of(5,10));
     *
     *   //Lazy List that will contain [2,4,6,5,10] when triggered
     * }
     * </pre>
     * @param col Collection to add
     * @return LazyFluentCollectionX with Collection added
     */
    default LazyFluentCollectionX<T> plusAllLazy(Collection<? extends T> col) {
        addAll(col);
        return this;
    }

    /**
     * Lazily remove an element from this Collection.
     * The Collection will not be materialized (unlike via @see {@link LazyFluentCollectionX#minus(Object)}
     * <pre>
     * {@code
     *    LazyListX<Integer> lazy = LazyListX.of(1,2,3)
     *                                       .map(i->i*2)
     *                                       .minusLazy(4);
     *
     *   //Lazy List that will contain [2,6] when triggered
     * }
     * </pre>
     *
     * This is an equivalent operation to filtering by equality e.g.
     * <pre>
     * {@code
     *    LazyListX<Integer> lazy = LazyListX.of(1,2,3)
     *                                       .map(i->i*2)
     *                                       .filter(i->i==4);
     *
     *   //Lazy List that will contain [2,6] when triggered
     * }
     * </pre>
     *
     * @param e Element to remove
     * @return LazyFluentCollectionX with element removed
     */
    default LazyFluentCollectionX<T> minusLazy(Object e) {
        remove(e);
        return this;
    }

    /**
     * Lazily remove the elements in the supplied Collection from this Collection.
     * The Collection will not be materialized (unlike via @see {@link LazyFluentCollectionX#minusAll(Collection)}
     * <pre>
     * {@code
     *    LazyListX<Integer> lazy = LazyListX.of(1,2,3)
     *                                       .map(i->i*2)
     *                                       .minusAllLazy(ListX.of(4));
     *
     *   //Lazy List that will contain [2,6] when triggered
     * }
     * </pre>
     *
     * This is an equivalent operation to filtering by equality e.g.
     * <pre>
     * {@code
     *    LazyListX<Integer> lazy = LazyListX.of(1,2,3)
     *                                       .map(i->i*2)
     *                                       .filter(i->ListX.of(4).contains(i));
     *
     *   //Lazy List that will contain [2,6] when triggered
     * }
     * </pre>
     * @param list of elements to remove
     * @return  LazyFluentCollectionX with elements removed
     */
    default LazyFluentCollectionX<T> minusAllLazy(Collection<?> list) {
        removeAll(list);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops.data.collections.extensions.FluentCollectionX#plusInOrder
     * (java.lang.Object)
     */
    @Override
    default FluentCollectionX<T> plusInOrder(T e) {
        return plus(e);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops.data.collections.extensions.FluentCollectionX#plus(java.
     * lang.Object)
     */
    @Override
    public FluentCollectionX<T> plus(T e);

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops.data.collections.extensions.FluentCollectionX#plusAll(
     * java.util.Collection)
     */
    @Override
    public FluentCollectionX<T> plusAll(Collection<? extends T> list);

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops.data.collections.extensions.FluentCollectionX#minus(java.
     * lang.Object)
     */
    @Override
    public FluentCollectionX<T> minus(Object e);

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops.data.collections.extensions.FluentCollectionX#minusAll(
     * java.util.Collection)
     */
    @Override
    public FluentCollectionX<T> minusAll(Collection<?> list);

    /*
     * (non-Javadoc)
     *
     * @see
     * com.aol.cyclops.data.collections.extensions.FluentCollectionX#unit(java.
     * util.Collection)
     */
    @Override
    public <R> FluentCollectionX<R> unit(Collection<R> col);
}
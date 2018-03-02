package com.oath.cyclops.data.collections.extensions;

import java.util.Collection;

import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.ReactiveSeq;


/**
 * A Lazy Collection with a fluent api. Extended operators act eagerly, direct operations on a toX
 * to add, removeValue or retrieve elements should be eager unless otherwise stated.
 *
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this toX
 */
public interface LazyFluentCollectionX<T> extends FluentCollectionX<T> {




    /**
     * @return This toX with any queued Lazy Operations materialized
     */
    LazyFluentCollectionX<T> materialize();
    /**
     * Create a LazyFluentCollection from a Flux.
     * The created LazyFluentCollection will be of the same type as the object this method is called on.
     * i.e. Calling stream(Flux) on a StreamX results in a StreamX
     *
     *
     * <pre>
     * {@code
     *
     *     StreamX<Integer> lazyInts = StreamX.of(1,2,3);
     *     StreamX<String> lazyStrs = lazyInts.stream(Flux.just("hello","world"));
     *
     * }
     * </pre>
     * Calling stream(Flux) on a LazySetX results in a LazySetX etc.
     *
     * The same toX / reduction method will be used in the newly created Object. I.e. Calling  stream(Flux) on
     * a toX which as an Immutable Collector  will result in an Immutable Collection.
     *

     *
     * @param stream Flux to create new toX from
     * @return New toX from Flux
     */
    <X> FluentCollectionX<X> stream(ReactiveSeq<X> stream);

    /**
     * Lazily add an element to this Collection.
     * The Collection will not be materialized (unlike via @see {@link LazyFluentCollectionX#plus(Object)}
     * <pre>
     * {@code
     *    StreamX<Integer> lazy = StreamX.of(1,2,3)
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
     *    StreamX<Integer> lazy = StreamX.of(1,2,3)
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
     * Lazily removeValue an element from this Collection.
     * The Collection will not be materialized (unlike via @see {@link LazyFluentCollectionX#removeValue(Object)}
     * <pre>
     * {@code
     *    StreamX<Integer> lazy = StreamX.of(1,2,3)
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
     *    StreamX<Integer> lazy = StreamX.of(1,2,3)
     *                                       .map(i->i*2)
     *                                       .filter(i->i==4);
     *
     *   //Lazy List that will contain [2,6] when triggered
     * }
     * </pre>
     *
     * @param e Element to removeValue
     * @return LazyFluentCollectionX with element removed
     */
    default LazyFluentCollectionX<T> minusLazy(Object e) {
        remove(e);
        return this;
    }

    /**
     * Lazily removeValue the elements in the supplied Collection from this Collection.
     * The Collection will not be materialized (unlike via @see {@link LazyFluentCollectionX#minusAll(Collection)}
     * <pre>
     * {@code
     *    StreamX<Integer> lazy = StreamX.of(1,2,3)
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
     *    StreamX<Integer> lazy = StreamX.of(1,2,3)
     *                                       .map(i->i*2)
     *                                       .filter(i->ListX.of(4).contains(i));
     *
     *   //Lazy List that will contain [2,6] when triggered
     * }
     * </pre>
     * @param list of elements to removeValue
     * @return  LazyFluentCollectionX with elements removed
     */
    default LazyFluentCollectionX<T> minusAllLazy(Iterable<? extends T> list) {
        removeAll((Iterable)(ListX.fromIterable(list)));
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * FluentCollectionX#plusInOrder
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
     * FluentCollectionX#plus(java.
     * lang.Object)
     */
    @Override
    public FluentCollectionX<T> plus(T e);

    /*
     * (non-Javadoc)
     *
     * @see
     * FluentCollectionX#insertAt(
     * java.util.Collection)
     */
    @Override
    public FluentCollectionX<T> plusAll(Iterable<? extends T> list);

    /*
     * (non-Javadoc)
     *
     * @see
     * FluentCollectionX#removeValue(java.
     * lang.Object)
     */
    @Override
    public FluentCollectionX<T> removeValue(T e);

    /*
     * (non-Javadoc)
     *
     * @see
     * FluentCollectionX#removeAll(
     * java.util.Collection)
     */
    @Override
    public FluentCollectionX<T> removeAll(Iterable<? extends T> list);

    /*
     * (non-Javadoc)
     *
     * @see
     * FluentCollectionX#unit(java.
     * util.Collection)
     */
    @Override
    public <R> FluentCollectionX<R> unit(Iterable<R> col);
}

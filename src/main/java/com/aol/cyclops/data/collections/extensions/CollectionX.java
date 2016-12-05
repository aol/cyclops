package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.types.ExtendedTraversable;
import com.aol.cyclops.types.IterableFilterable;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.IterableFunctor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.TriFunction;

/**
 * An interface that extends JDK Collection interface with a significant number of new operators
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface CollectionX<T> extends ExtendedTraversable<T>, Iterable<T>, Sequential<T>, IterableFunctor<T>, IterableFoldable<T>,
        IterableFilterable<T>, ZippingApplicativable<T>, Unit<T>, Collection<T>, CyclopsCollectable<T> {

    /**
     * Create a CollectionX from the supplied Collection
     * 
     * @param col
     * @return
     */
    static <T> CollectionX<T> fromCollection(final Collection<T> col) {

        return new CollectionXImpl<>(
                                     col);
    }
    <R> CollectionX<R> unit(R r);
  

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    <U> CollectionX<Tuple2<T, U>> zip(Seq<? extends U> other);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    <C extends Collection<? super T>> CollectionX<C> grouped(int size, Supplier<C> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    CollectionX<ListX<T>> groupedUntil(Predicate<? super T> predicate);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    CollectionX<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    CollectionX<ListX<T>> groupedWhile(Predicate<? super T> predicate);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    <C extends Collection<? super T>> CollectionX<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    <C extends Collection<? super T>> CollectionX<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#intersperse(java.lang.Object)
     */
    @Override
    CollectionX<T> intersperse(T value);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle()
     */
    @Override
    CollectionX<T> shuffle();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#shuffle(java.util.Random)
     */
    @Override
    CollectionX<T> shuffle(Random random);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    <S, U> CollectionX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    <T2, T3, T4> CollectionX<Tuple4<T, T2, T3, T4>> zip4(Stream<? extends T2> second, Stream<? extends T3> third, Stream<? extends T4> fourth);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> limitWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> limitUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limitLast(int)
     */
    @Override
    CollectionX<T> limitLast(int num);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> skipWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> skipUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skipLast(int)
     */
    @Override
    CollectionX<T> skipLast(int num);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(int)
     */
    @Override
    CollectionX<T> cycle(int times);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    CollectionX<T> cycle(Monoid<T> m, int times);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> cycleWhile(Predicate<? super T> predicate);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> cycleUntil(Predicate<? super T> predicate);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    CollectionX<T> onEmpty(T value);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    CollectionX<T> onEmptyGet(Supplier<? extends T> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    <X extends Throwable> CollectionX<T> onEmptyThrow(Supplier<? extends X> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.ExtendedTraversable#stream()
     */
    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#collectable()
     */
    @Override
    default CyclopsCollectable<T> collectable() {
        return stream();
    }

    /**
     * Get the element at the specified index (if it exists)
     * 
     * @param index to look up element
     * @return Optional.empty if the index does not exist, otherwise the element at the index supplied is returned
     */
    default Optional<T> getAtIndex(final int index) {
        return stream().get(index);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#headAndTail()
     */
    @Override
    default HeadAndTail<T> headAndTail() {
        return new HeadAndTail<>(
                                 iterator());
    }

    /**
     * @return The head of this collection
     */
    default T head() {
        return iterator().next();
    }

    /**
     * Conctruct an Extended Collection from a standard Collection
     * 
     * @param c Collection to extend
     * @return Extended Collection
     */
    <T1> CollectionX<T1> from(Collection<T1> c);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#reverse()
     */
    @Override
    CollectionX<T> reverse();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#single()
     */
    @Override
    default T single() {

        final Iterator<T> it = iterator();
        if (it.hasNext()) {
            final T result = it.next();
            if (!it.hasNext())
                return result;
        }
        throw new UnsupportedOperationException(
                                                "single only works for Streams with a single value");

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#single(java.util.function.Predicate)
     */
    @Override
    default T single(final Predicate<? super T> predicate) {
        return this.filter(predicate)
                   .single();

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#singleOptional()
     */
    @Override
    default Optional<T> singleOptional() {
        final Iterator<T> it = iterator();
        if (it.hasNext()) {
            final T result = it.next();
            if (!it.hasNext())
                return Optional.of(result);
        }
        return Optional.empty();

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#findFirst()
     */
    @Override
    default Optional<T> findFirst() {
        return stream().findFirst();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#findAny()
     */
    @Override
    default Optional<T> findAny() {
        return stream().findAny();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Foldable#groupBy(java.util.function.Function)
     */
    @Override
    default <K> MapX<K, List<T>> groupBy(final Function<? super T, ? extends K> classifier) {
        return stream().groupBy(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> filter(Predicate<? super T> pred);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#map(java.util.function.Function)
     */
    @Override
    <R> CollectionX<R> map(Function<? super T, ? extends R> mapper);

    /**
     * Perform a flatMap operation on this collection. Results from the returned Iterables (from the
     * provided transformation function) are flattened into the resulting collection.
     * 
     * @param mapper Transformation function to be applied (and flattened)
     * @return A collection containing the flattened results of the transformation function
     */
    <R> CollectionX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#limit(long)
     */
    @Override
    CollectionX<T> limit(long num);
    @Override
    default CollectionX<T> take(long num){
        return limit(num);
    }
    @Override
    default CollectionX<T> drop(long num){
        return skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#skip(long)
     */
    @Override
    CollectionX<T> skip(long num);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> takeWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> dropWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> takeUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> dropUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#dropRight(int)
     */
    @Override
    CollectionX<T> dropRight(int num);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#takeRight(int)
     */
    @Override
    CollectionX<T> takeRight(int num);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#peek(java.util.function.Consumer)
     */
    @Override
    default CollectionX<T> peek(final Consumer<? super T> c) {
        return (CollectionX<T>) ZippingApplicativable.super.peek(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(int)
     */
    @Override
    CollectionX<ListX<T>> grouped(int groupSize);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    <K, A, D> CollectionX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#grouped(java.util.function.Function)
     */
    @Override
    <K> CollectionX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    CollectionX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op);

    /**
     * Zip (merge) this collection with the supplied Iterable into a Colleciton containing Tuples
     * Each Tuple contains one element from this collection and one from the other
     * 
     * @param other Collection to merge with this one
     * @return Merged collection
     */
    @Override
    <U> CollectionX<Tuple2<T, U>> zip(Iterable<? extends U> other);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    <U, R> CollectionX<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    <U, R> CollectionX<R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    <U, R> CollectionX<R> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    <U> CollectionX<Tuple2<T, U>> zip(Stream<? extends U> other);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#zipWithIndex()
     */
    @Override
    CollectionX<Tuple2<T, Long>> zipWithIndex();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int)
     */
    @Override
    CollectionX<ListX<T>> sliding(int windowSize);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sliding(int, int)
     */
    @Override
    CollectionX<ListX<T>> sliding(int windowSize, int increment);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    CollectionX<T> scanLeft(Monoid<T> monoid);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> CollectionX<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    CollectionX<T> scanRight(Monoid<T> monoid);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> CollectionX<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#distinct()
     */
    @Override
    CollectionX<T> distinct();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted()
     */
    @Override
    CollectionX<T> sorted();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFilterable#removeAll(java.util.stream.Stream)
     */
    @Override
    CollectionX<T> removeAll(Stream<? extends T> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFilterable#removeAll(java.lang.Iterable)
     */
    @Override
    CollectionX<T> removeAll(Iterable<? extends T> it);

    /**
     * Remove all elements from this collection that match the values in the provided Seq
     * 
     * @param seq Stream of values to remove
     * @return Collection with the values removed
     */
    CollectionX<T> removeAll(Seq<? extends T> seq);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFilterable#removeAll(java.lang.Object[])
     */
    @Override
    CollectionX<T> removeAll(T... values);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFilterable#retainAll(java.lang.Iterable)
     */
    @Override
    CollectionX<T> retainAll(Iterable<? extends T> it);

    /**
     * Retain all elements from this collection that match the values in the provided Seq
     * 
     * @param seq Stream of values to retain
     * @return Collection with the values retained
     */
    CollectionX<T> retainAll(Seq<? extends T> seq);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFilterable#retainAll(java.util.stream.Stream)
     */
    @Override
    CollectionX<T> retainAll(Stream<? extends T> seq);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFilterable#retainAll(java.lang.Object[])
     */
    @Override
    CollectionX<T> retainAll(T... values);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    CollectionX<T> filterNot(Predicate<? super T> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    CollectionX<T> notNull();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    <R> CollectionX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper);
    /**
     * Perform a three level nested internal iteration over this Stream and the
     * supplied streams
     *
     * <pre>
     * {@code 
     *   
     *   //collectionX [1,2]
     *   
     *   collectionX.forEach4(a->ListX.range(10,13),
     *                        (a,b)->ListX.of(""+(a+b),"hello world"),
     *                        (a,b,c)->ListX.of(a,b,c)),
     *                        (a,b,c,d)->c+":"a+":"+b);
     *                                  

     * }
     * </pre>
     * 
     * @param iterable1
     *            Nested Stream to iterate over
     * @param iterable2
     *            Nested Stream to iterate over
     * @param iterable3
     *            Nested Stream to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return CollectionX with elements generated via nested iteration
     */
    default <R1, R2, R3,R> CollectionX<R> forEach4(final Function<? super T, ? extends Iterable<R1>> iterable1,
                        final BiFunction<? super T,? super R1, ? extends Iterable<R2>> iterable2,
                            final TriFunction<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3,
                            final QuadFunction<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMap(in -> {

            ReactiveSeq<R1> a = ReactiveSeq.fromIterable(iterable1.apply(in));
            return a.flatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                return b.flatMap(inb -> {
                    ReactiveSeq<R3> c = ReactiveSeq.fromIterable(iterable3.apply(in, ina, inb));
                    return c.map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }


    /**
     * Perform a three level nested internal iteration over this Stream and the
     * supplied streams
     * 
     * <pre>
     * {@code 
     *  //collectionX [1,2,3]
     *  
     * collectionX.forEach4(a->ListX.range(10,13),
     *                     (a,b)->ListX.of(""+(a+b),"hello world"),
     *                     (a,b,c)->ListX.of(a,b,c),
     *                     (a,b,c,d)-> c!=3,
     *                      (a,b,c)->c+":"a+":"+b);
     *                                  
     * 
     *  
     * }
     * </pre>
     * 
     * 
     * @param iterable1
     *            Nested Stream to iterate over
     * @param iterable2
     *            Nested Stream to iterate over
     * @param iterable3
     *            Nested Stream to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return CollectionX with elements generated via nested iteration
     */
    default <R1, R2, R3, R> CollectionX<R> forEach4(final Function<? super T, ? extends Iterable<R1>> iterable1,
            final BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2,
            final TriFunction<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3,
            final QuadFunction<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            final QuadFunction<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.flatMap(in -> {

            ReactiveSeq<R1> a = ReactiveSeq.fromIterable(iterable1.apply(in));
            return a.flatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                return b.flatMap(inb -> {
                    ReactiveSeq<R3> c = ReactiveSeq.fromIterable(iterable3.apply(in, ina, inb));
                    return c.filter(in2 -> filterFunction.apply(in, ina, inb, in2))
                            .map(in2 -> yieldingFunction.apply(in, ina, inb, in2));
                });

            });

        });
    }

    /**
     * Perform a three level nested internal iteration over this Stream and the
     * supplied streams
     *
     * <pre>
     * {@code 
     *   
     *   //collectionX [1,2]
     *   
     *   collectionX.forEach3(a->IntStream.range(10,13),
     *                        (a,b)->Stream.of(""+(a+b),"hello world"),
     *                        (a,b,c)->c+":"a+":"+b);
     *                                  
     * 
     *  //CollectionX[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
     * }
     * </pre>
     * 
     * @param iterable1
     *            Nested Stream to iterate over
     * @param iterable2
     *            Nested Stream to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R2, R> CollectionX<R> forEach3(final Function<? super T, ? extends Iterable<R1>> iterable1,
                                                final BiFunction<? super T,? super R1, ? extends Iterable<R2>> iterable2,
                                                final TriFunction<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return this.flatMap(in -> {

            Iterable<R1> a = iterable1.apply(in);
            return ReactiveSeq.fromIterable(a)
                              .flatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                return b.map(in2 -> yieldingFunction.apply(in, ina, in2));
            });

        });
    }


    /**
     * Perform a three level nested internal iteration over this Stream and the
     * supplied streams
     * 
     * <pre>
     * {@code 
     *  //collectionX [1,2,3]
     *  
     * collectionX.forEach3(a->ListX.range(10,13),
     *                     (a,b)->Stream.of(""+(a+b),"hello world"),
     *                     (a,b,c)-> c!=3,
     *                      (a,b,c)->c+":"a+":"+b);
     *                                  
     * 
     *  //CollectionX[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
     * }
     * </pre>
     * 
     * 
     * @param stream1
     *            Nested Stream to iterate over
     * @param stream2
     *            Nested Stream to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R2, R> CollectionX<R> forEach3(final Function<? super T, ? extends Iterable<R1>> iterable1,
            final BiFunction<? super T,? super R1, ? extends Iterable<R2>> iterable2,
                    final TriFunction<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                    final TriFunction<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return this.flatMap(in -> {

            Iterable<R1> a = iterable1.apply(in);
            return ReactiveSeq.fromIterable(a)
                              .flatMap(ina -> {
                ReactiveSeq<R2> b = ReactiveSeq.fromIterable(iterable2.apply(in, ina));
                return b.filter(in2 -> filterFunction.apply(in, ina, in2))
                        .map(in2 -> yieldingFunction.apply(in, ina, in2));
            });

        });
    }

    /**
     * Perform a two level nested internal iteration over this Stream and the
     * supplied stream
     * 
     * <pre>
     * {@code 
     *  //collectionX [1,2,3]
     *  
     * collectionX.of(1,2,3).forEach2(a->ListX.range(10,13),
     *                                (a,b)->a+b);
     *                                  
     * 
     *  //ReactiveSeq[11,14,12,15,13,16]
     * }
     * </pre>
     * 
     * 
     * @param iterable1
     *            Nested Iterable to iterate over
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R> CollectionX<R> forEach2(final Function<? super T,? extends Iterable<R1>> iterable1,
            final BiFunction<? super T,? super R1, ? extends R> yieldingFunction) {

        return this.flatMap(in-> { 
                    
                    Iterable<? extends R1> b = iterable1.apply(in);
                    return ReactiveSeq.fromIterable(b)
                                      .map(in2->yieldingFunction.apply(in, in2));
                });
    }

    /**
     * Perform a two level nested internal iteration over this Stream and the
     * supplied stream
     * 
     * <pre>
     * {@code 
     * 
     * //collectionX [1,2,3]
     *  
     * collectionX.of(1,2,3).forEach2(a->ListX.range(10,13),
     *                                  (a,b)-> a<3 && b>10,
     *                                  (a,b)->a+b);
     *                                  
     * 
     *  //CollectionX[14,15]
     * }
     * </pre>
     * 
     * @param iterable1
     *            Nested Stream to iterate over
     * @param filterFunction
     *            Filter to apply over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R> CollectionX<R> forEach2(final Function<? super T, ? extends Iterable<R1>> iterable1,
            final BiFunction<? super T,? super R1,  Boolean> filterFunction,
                    final BiFunction<? super T,? super R1, ? extends R> yieldingFunction) {
        return this.flatMap(in-> { 
            
            Iterable<? extends R1> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                             .filter(in2-> filterFunction.apply(in,in2))
                             .map(in2->yieldingFunction.apply(in, in2));
        });

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#slice(long, long)
     */
    @Override
    CollectionX<T> slice(long from, long to);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.function.Function)
     */
    @Override
    <U extends Comparable<? super U>> CollectionX<T> sorted(Function<? super T, ? extends U> function);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Traversable#sorted(java.util.Comparator)
     */
    @Override
    CollectionX<T> sorted(Comparator<? super T> c);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.ExtendedTraversable#permutations()
     */
    @Override
    CollectionX<ReactiveSeq<T>> permutations();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.ExtendedTraversable#combinations(int)
     */
    @Override
    CollectionX<ReactiveSeq<T>> combinations(int size);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.ExtendedTraversable#combinations()
     */
    @Override
    CollectionX<ReactiveSeq<T>> combinations();

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> CollectionX<U> cast(final Class<? extends U> type) {

        return (CollectionX<U>) ZippingApplicativable.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> CollectionX<R> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {

        return (CollectionX<R>) ZippingApplicativable.super.patternMatch(case1, otherwise);
    }

}

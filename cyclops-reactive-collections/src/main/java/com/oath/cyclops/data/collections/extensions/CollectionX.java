package com.oath.cyclops.data.collections.extensions;

import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.types.factory.Unit;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.functor.ReactiveTransformable;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.traversable.IterableX;
import com.oath.cyclops.types.Unwrappable;
import com.oath.cyclops.types.traversable.RecoverableTraversable;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.reactive.collections.mutable.SetX;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * An interface that extends JDK Collection interface with a significant number of new operators
 *
 * @author johnmcclean
 *
 * @param <T>
 */
public interface CollectionX<T> extends IterableX<T>,
                                        Collection<T> ,
                                        Unwrappable,
                                        RecoverableTraversable<T>,
                                        ReactiveTransformable<T>,
                                        Unit<T> {

    boolean isLazy();
    boolean isEager();
    Evaluation evaluation();

    CollectionX<T> lazy();
    CollectionX<T> eager();
    default <R> CollectionX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (CollectionX<R>)ReactiveTransformable.super.retry(fn,retries,delay,timeUnit);
    }
    default <R> CollectionX<R> retry(final Function<? super T, ? extends R> fn) {
        return (CollectionX<R>)ReactiveTransformable.super.retry(fn, 7, 2, TimeUnit.SECONDS);
    }
    @Override
    default ReactiveConvertableSequence<T> to(){
        return new ReactiveConvertableSequence<>(this);
    }

    default ListX<T> toListX(){
        return to().listX();
    }
    default SetX<T> toSetX(){
        return to().setX();
    }

    default <R> R toX(Function<? super CollectionX<T>,? extends R> fn){
        return fn.apply(this);
    }
    @Override
    Iterator<T> iterator();

    @Override
    boolean isEmpty();

    /*
     * flatMap operation that maps to and flattens a Stream
     * to map to and flatten and Iterable see {@link concatMap}
     * to map to and merge a reactive-streams publisher see {@link mergeMap}
     */
    default <R> CollectionX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn){
        return this.concatMap(fn.andThen(ReactiveSeq::fromStream));
    }


    <R> CollectionX<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn);
    <R> CollectionX<R> mergeMap(int maxConcurecy, Function<? super T, ? extends Publisher<? extends R>> fn);
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



    @Override
    <C extends PersistentCollection<? super T>> CollectionX<C> grouped(int size, Supplier<C> supplier);


    @Override
    CollectionX<Vector<T>> groupedUntil(Predicate<? super T> predicate);

    @Override
    CollectionX<Vector<T>> groupedUntil(BiPredicate<Vector<? super T>, ? super T> predicate);


    @Override
    CollectionX<Vector<T>> groupedWhile(Predicate<? super T> predicate);


    @Override
    <C extends PersistentCollection<? super T>> CollectionX<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory);


    @Override
    <C extends PersistentCollection<? super T>> CollectionX<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory);


    @Override
    CollectionX<T> intersperse(T value);


    @Override
    CollectionX<T> shuffle();


    @Override
    CollectionX<T> shuffle(Random random);


    @Override
    <S, U> CollectionX<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third);


    @Override
    <T2, T3, T4> CollectionX<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth);


    CollectionX<T> takeWhile(Predicate<? super T> p);


    CollectionX<T> takeUntil(Predicate<? super T> p);


    CollectionX<T> takeRight(int num);


    CollectionX<T> dropWhile(Predicate<? super T> p);


    CollectionX<T> dropUntil(Predicate<? super T> p);


    CollectionX<T> dropRight(int num);

    @Override
    CollectionX<T> cycle(long times);


    @Override
    CollectionX<T> cycle(Monoid<T> m, long times);


    @Override
    CollectionX<T> cycleWhile(Predicate<? super T> predicate);


    @Override
    CollectionX<T> cycleUntil(Predicate<? super T> predicate);


    @Override
    CollectionX<T> onEmpty(T value);


    @Override
    CollectionX<T> onEmptyGet(Supplier<? extends T> supplier);


    <X extends Throwable> CollectionX<T> onEmptyError(Supplier<? extends X> supplier);


    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    boolean isMaterialized();
    default CollectionX<T> materialize(){

        return this;

    }



    /**
     * Conctruct an Extended Collection from a standard Collection
     *
     * @param c Collection to extend
     * @return Extended Collection
     */
    <T1> CollectionX<T1> from(Iterable<T1> c);

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#reverse()
     */
    @Override
    CollectionX<T> reverse();


    @Override
    default T singleOrElse(T alt) {

        final Iterator<T> it = iterator();
        if (it.hasNext()) {
            final T result = it.next();
            if (!it.hasNext())
                return result;
        }
       return alt;

    }


    @Override
    default Maybe<T> single(final Predicate<? super T> predicate) {
        return this.filter(predicate)
                   .single();

    }

    @Override
    default Maybe<T> single() {
       return stream().single();
    }
    @Override
    default Maybe<T> takeOne() {
        return stream().takeOne();
    }



    @Override
    default <K> cyclops.data.HashMap<K, Vector<T>> groupBy(final Function<? super T, ? extends K> classifier) {
        return stream().groupBy(classifier);
    }
    @Override
    default Object[] toArray(){
        return stream().toArray();
    }
    @Override
    default  <T1> T1[] toArray(T1[] a){
        return stream().toArray(i->(T1[])java.lang.reflect.Array
                        .newInstance(a.getClass().getComponentType(), i));
    }
    @Override
    int size();


    @Override
    CollectionX<T> filter(Predicate<? super T> pred);


    @Override
    <R> CollectionX<R> map(Function<? super T, ? extends R> mapper);

    /**
     * Perform a flatMap operation on this CollectionX. Results from the returned Iterables (from the
     * provided transformation function) are flattened into the resulting toX.
     *
     * @param mapper Transformation function to be applied (and flattened)
     * @return A CollectionX containing the flattened results of the transformation function
     */
    <R> CollectionX<R> concatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);


    CollectionX<T> take(long num);

    CollectionX<T> drop(long num);



    @Override
    default CollectionX<T> peek(final Consumer<? super T> c) {
        return (CollectionX<T>) IterableX.super.peek(c);
    }


    @Override
    IterableX<Vector<T>> grouped(int groupSize);



    @Override
    CollectionX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op);


    @Override
    <U> CollectionX<Tuple2<T, U>> zip(Iterable<? extends U> other);


    @Override
    <U, R> CollectionX<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

     @Override
     <T2, R> CollectionX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher);

      @Override
      <U> CollectionX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other);

      @Override
      <S, U, R> CollectionX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) ;

      <T2, T3, T4, R> CollectionX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn);


    <U> CollectionX<Tuple2<T, U>> zipWithStream(Stream<? extends U> other);


    @Override
    CollectionX<Tuple2<T, Long>> zipWithIndex();


    @Override
    CollectionX<Seq<T>> sliding(int windowSize);


    @Override
    CollectionX<Seq<T>> sliding(int windowSize, int increment);

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#scanLeft(cyclops2.function.Monoid)
     */
    @Override
    CollectionX<T> scanLeft(Monoid<T> monoid);

    @Override
    <U> CollectionX<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function);


    @Override
    CollectionX<T> scanRight(Monoid<T> monoid);


    @Override
    <U> CollectionX<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner);


    @Override
    CollectionX<T> distinct();


    @Override
    CollectionX<T> sorted();

    @Override
    CollectionX<T> removeStream(Stream<? extends T> stream);


  CollectionX<T> removeAll(Iterable<? extends T> it);

  default CollectionX<T> removeAll(CollectionX<? extends T> it){
    return removeAll((Iterable<T>)it);
  }
  default Iterable<T> narrowIterable(){
    return (Iterable<T>)this;
  }


    CollectionX<T> removeAll(T... values);


    CollectionX<T> retainAll(Iterable<? extends T> it);


    @Override
    CollectionX<T> retainStream(Stream<? extends T> seq);


    CollectionX<T> retainAll(T... values);


    @Override
    CollectionX<T> filterNot(Predicate<? super T> fn);



    @Override
    CollectionX<T> notNull();


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
                            final Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3,
                            final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.concatMap(in -> {

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
     *            Filter to applyHKT over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return CollectionX with elements generated via nested iteration
     */
    default <R1, R2, R3, R> CollectionX<R> forEach4(final Function<? super T, ? extends Iterable<R1>> iterable1,
            final BiFunction<? super T, ? super R1, ? extends Iterable<R2>> iterable2,
            final Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> iterable3,
            final Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            final Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {
        return this.concatMap(in -> {

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
                                                final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return this.concatMap(in -> {

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
     * @param iterable1
     *            Nested Stream to iterate over
     * @param iterable2
     *            Nested Stream to iterate over
     * @param filterFunction
     *            Filter to applyHKT over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R2, R> CollectionX<R> forEach3(final Function<? super T, ? extends Iterable<R1>> iterable1,
            final BiFunction<? super T,? super R1, ? extends Iterable<R2>> iterable2,
                    final Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
                    final Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {
        return this.concatMap(in -> {

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

        return this.concatMap(in-> {

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
     *            Filter to applyHKT over elements before passing non-filtered
     *            values to the yielding function
     * @param yieldingFunction
     *            Function with pointers to the current element from both
     *            Streams that generates the new elements
     * @return ReactiveSeq with elements generated via nested iteration
     */
    default <R1, R> CollectionX<R> forEach2(final Function<? super T, ? extends Iterable<R1>> iterable1,
            final BiFunction<? super T,? super R1,  Boolean> filterFunction,
                    final BiFunction<? super T,? super R1, ? extends R> yieldingFunction) {
        return this.concatMap(in-> {

            Iterable<? extends R1> b = iterable1.apply(in);
            return ReactiveSeq.fromIterable(b)
                             .filter(in2-> filterFunction.apply(in,in2))
                             .map(in2->yieldingFunction.apply(in, in2));
        });

    }


    @Override
    CollectionX<T> slice(long from, long to);


    @Override
    <U extends Comparable<? super U>> CollectionX<T> sorted(Function<? super T, ? extends U> function);


    @Override
    CollectionX<T> sorted(Comparator<? super T> c);


    @Override
    CollectionX<ReactiveSeq<T>> permutations();


    @Override
    CollectionX<ReactiveSeq<T>> combinations(int size);


    @Override
    CollectionX<ReactiveSeq<T>> combinations();




}

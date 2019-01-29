package com.oath.cyclops.types.traversable;

import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.recoverable.OnEmpty;
import com.oath.cyclops.types.Zippable;
import com.oath.cyclops.types.functor.FilterableTransformable;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.*;

/**
 * A non-scalar navigatable data type
 *
 * @author johnmcclean
 *
 * @param <T> The data type of the elements in this Traversable
 */
public interface Traversable<T> extends Publisher<T>,
                                        OnEmpty<T>,
                                        Zippable<T>,
                                        IterableFilterable<T>,
                                        FilterableTransformable<T>{



    @Override
    <R> Traversable<R> map(Function<? super T, ? extends R> fn);

    @Override
    default Traversable<T> filterNot(final Predicate<? super T> predicate) {
        return (Traversable<T>)FilterableTransformable.super.filterNot(predicate);
    }

    @Override
    default <T2, R> Traversable<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return traversable().zip(fn, publisher);
    }

    default <U> Traversable<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {
      return traversable().zipWithStream(other);
    }
    default <T2,R> Traversable<R> zipWithStream(final Stream<? extends T2> other,final BiFunction<? super T, ? super T2, ? extends R> fn) {
       return traversable().zipWithStream(other,fn);
    }

    @Override
    default <U> Traversable<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return traversable().zipWithPublisher(other);
    }

    @Override
    default <U> Traversable<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return traversable().zip(other);
    }

    @Override
    default <S, U, R> Traversable<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return traversable().zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> Traversable<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return traversable().zip4(second,third,fourth,fn);
    }

    /**
     * @return This Traversable converted to a Stream
     */
    default ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(this);
    }


    <U> Traversable<U> unitIterable(Iterable<U> U);

    @Override
    default void subscribe(final Subscriber<? super T> s) {
        traversable().subscribe(s);
    }

    /**
     * Combine two adjacent elements in a traversable using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbour
     * <pre>
     * {@code
     *  ReactiveSeq.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),SemigroupK.intSum)
                   .listX()

     *  //Seq(3,4)
     * }</pre>
     *
     * Can be used to implement terminating lazy folds on lazy data types
     *
     * <pre>
     *    {@code
     *    ReactiveSeq.generate(this::process)
                     .map(data->data.isSuccess())
                     .combine((a,b)-> a ? false : true, (a,b) -> a|b)
                    .findFirst(); //terminating reduction on infinite data structure
     *    }
     *
     *
     * </pre>
     *
     * @param predicate Test to see if two neighbours should be joined. The first parameter to the bi-predicate is the currently
     *                  accumulated result and the second is the next element
     * @param op BinaryOperator to combine neighbours
     * @return Combined / Partially Reduced Traversable
     */
    default Traversable<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return traversable().combine(predicate, op);
    }

    /**
     * Combine two adjacent elements in a traversable using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The emitted result of a combination may in turn be combined
     * with it's neighbour
     * <pre>
     * {@code
     *  ReactiveSeq.of(1,1,2,3)
                  .combine(Monoids.intMult,(a, b)->a.equals(b))
                  .listX()

     *  //Seq(1)
     * }</pre>
     *
     * Simalar to @see {@link Traversable#combine(BiPredicate, BinaryOperator)} but differs in that the first comparison is always to the Monoid zero
     * This allows us to terminate with just a single value
     *
     * @param predicate Test to see if two neighbours should be joined. The first parameter to the bi-predicate is the currently
     *                  accumulated result and the second is the next element
     * @param op Monoid to combine neighbours
     * @return Combined / Partially Reduced Traversable
     */
    default Traversable<T> combine(final Monoid<T> op,final BiPredicate<? super T, ? super T> predicate) {

        boolean[] firstFailed = {false};
        boolean[] first = {false};
        int[] dropped = {0};
        BiPredicate<? super T, ? super T> toUse = (a,b)->{
            if(!first[0]){
                firstFailed[0] = !predicate.test(a,b);
                first[0]=true;
                return !firstFailed[0];
            }
            return predicate.test(a,b);
        };

        return prepend(op.zero()).traversable()
                                 .combine(toUse, op)
                                 .dropWhile(i->firstFailed[0] && (dropped[0]++==0));
    }


    default Traversable<T> cycle(final long times) {
        return traversable().cycle(times);
    }


    default Traversable<T> cycle(final Monoid<T> m, final long times) {
        return traversable().cycle(m, times);
    }


    default Traversable<T> cycleWhile(final Predicate<? super T> predicate) {
        return traversable().cycleWhile(predicate);
    }


    default Traversable<T> cycleUntil(final Predicate<? super T> predicate) {
        return traversable().cycleUntil(predicate);
    }


    @Override
    default <U, R> Traversable<R> zip(final Iterable<? extends U> other,final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return traversable().zip(other, zipper);
    }




    default <S, U> Traversable<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {
        return traversable().zip3(second, third);
    }


    default <T2, T3, T4> Traversable<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {
        return traversable().zip4(second, third, fourth);
    }


    default Traversable<Tuple2<T, Long>> zipWithIndex() {
        return traversable().zipWithIndex();
    }

    /**
     * Create a sliding view
     **/
    default Traversable<Seq<T>> sliding(final int windowSize) {
        return traversable().sliding(windowSize);
    }

    /**
     * Create a sliding view
     **/
    default Traversable<Seq<T>> sliding(final int windowSize, final int increment) {
        return traversable().sliding(windowSize, increment);
    }

    /**
     * Group the elements of this traversable
     *
     * <pre>
     * {@code
     * ReactiveSeq.of(1,1,1,1,1,1)
     *            .grouped(3,()->new TreeSet<>())
     *            .toList()
     *            .getValue(0)
     *            .size();
     * //1
     *
     * ReactiveSeq.of(1,2,3,4,5,6)
     *            .grouped(3,()->new TreeSet<>())
     *            .toList()
     *            .getValue(0)
     *            .size();
     * //3
     *
     * }
     * </pre>
     * @param size batch size
     * @param supplier Collection factory
     * @return Traversable grouped by size
     */
    default <C extends PersistentCollection<? super T>> Traversable<C> grouped(final int size, final Supplier<C> supplier) {
        return traversable().grouped(size, supplier);
    }

    /**
     *
     * <pre>
     * {@code
     *  ReactiveSeq.of(1,2,3,4,5,6)
     *              .groupedUntil(i->i%3==0)
     *              .toList()
     *              .size();
     *   //2
     * }
     * </pre>
     *
     * @param predicate
     *            group until predicate holds
     * @return Traversable batched into Vectors determined by the predicate supplied
     */
    default Traversable<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {
        return traversable().groupedUntil(predicate);
    }

    /**
     * Create Travesable of Lists where
     * each List is populated while the supplied bipredicate holds. The
     * bipredicate recieves the List from the last window as well as the
     * current value and can choose to aggregate the current value or create a
     * new window
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .groupedUntil((s,i)->s.contains(4) ? true : false)
     *              .toList().size(),equalTo(5));
     * }
     * </pre>
     *
     * @param predicate
     *            Window while true
     * @return Traversable windowed while predicate holds
     */
    default Traversable<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {
        return traversable().groupedUntil(predicate);
    }

  /**
     * Create a Traversable batched by List, where each batch is populated while
     * the predicate holds
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .groupedWhile(i->i%3!=0)
     *              .toList().size(),equalTo(2));
     *
     * }
     * </pre>
     *
     * @param predicate
     *            Batch while predicate holds, applyHKT open next batch
     * @return SequenceM batched into lists determined by the predicate supplied
     */
    default Traversable<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {
        return traversable().groupedWhile(predicate);
    }

    /**
     * Create a SequenceM batched by a Collection, where each batch is populated
     * while the predicate holds
     *
     * <pre>
     * {@code
     * assertThat(ReactiveSeq.of(1,2,3,4,5,6)
     *              .groupedWhile(i->i%3!=0)
     *              .toList()
     *              .size(),equalTo(2));
     * }
     * </pre>
     *
     * @param predicate
     *            Batch while predicate holds, applyHKT open next batch
     * @param factory
     *            Collection factory
     * @return SequenceM batched into collections determined by the predicate
     *         supplied
     */
    default <C extends PersistentCollection<? super T>> Traversable<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return traversable().groupedWhile(predicate, factory);
    }

    /**
     * Create a ReactiveSeq batched by a Collection, where each batch is populated
     * until the predicate holds
     *
     * <pre>
     * {@code
     *  ReactiveSeq.of(1,2,3,4,5,6)
     *             .groupedUntil(i->i%3!=0)
     *             .toList()
     *
     *  //2
     * }
     * </pre>
     *
     *
     * @param predicate
     *            Batch until predicate holds, applyHKT open next batch
     * @param factory
     *            Collection factory
     * @return SequenceM batched into collections determined by the predicate
     *         supplied
     */
    default <C extends PersistentCollection<? super T>> Traversable<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return traversable().groupedUntil(predicate, factory);
    }

    /**
     * Group elements in a Stream
     *
     * <pre>
     * {@code
     *  List<List<Integer>> list = ReactiveSeq.of(1, 2, 3, 4, 5, 6).grouped(3).collect(CyclopsCollectors.toList());
     *
     *  assertThat(list.getValue(0), hasItems(1, 2, 3));
     *  assertThat(list.getValue(1), hasItems(4, 5, 6));
     *
     * }
     * </pre>
     *
     * @param groupSize
     *            Size of each Group
     * @return Stream with elements grouped by size
     */
    default Traversable<Vector<T>> grouped(final int groupSize) {
        return traversable().grouped(groupSize);
    }



    /*
     * Return the distinct Stream of elements
     *
     * <pre> {@code List<Integer> list = ReactiveSeq.of(1,2,2,2,5,6) .distinct()
     * .collect(CyclopsCollectors.toList()); }</pre>
     */
    default Traversable<T> distinct() {
        return traversable().distinct();
    }

    /**
     * Scan left using supplied Monoid
     *
     * <pre>
     * {@code
     *
     * 	ReactiveSeq.of("a", "b", "c")
     * 			   .scanLeft(Reducers.toString(""))
     * 			   .toList();
     *  //asList("", "a", "ab", "abc")
     *
     * </pre>
     *
     * @param monoid
     * @return
     */
    default Traversable<T> scanLeft(final Monoid<T> monoid) {
        return traversable().scanLeft(monoid);
    }

    /**
     * Scan left
     *
     * <pre>
     * {@code
     *  ReactiveSeq.of("a", "b", "c")
     *             .scanLeft("", String::concat)
     *             .toList();
     *   //[, a, ab, abc]
     * }
     * </pre>
     */
    default <U> Traversable<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return traversable().scanLeft(seed, function);
    }

    /**
     * Scan right
     *
     * <pre>
     * {@code
     * ReactiveSeq.of("a", "b", "c")
     *            .scanRight(Monoid.of("", String::concat))
     *            .toList()
     *
     * //asList("", "c", "bc", "abc")
     * }
     * </pre>
     */
    default Traversable<T> scanRight(final Monoid<T> monoid) {
        return traversable().scanRight(monoid);
    }

    /**
     * Scan right
     *
     * <pre>
     * {@code
     * ReactiveSeq.of("a", "ab", "abc")
     *            .map(str->str.length())
     *            .scanRight(0, (t, u) -> u + t)
     *            .toList();
     * //asList(0, 3, 5, 6);
     *
     * }
     * </pre>
     */
    default <U> Traversable<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return traversable().scanRight(identity, combiner);
    }

    /**
     * <pre>
     * {@code ReactiveSeq.of(4,3,6,7))
     *                   .sorted()
     *                   .toList()
     *
     *   //Arrays.asList(3,4,6,7)
     * </pre>
     *
     */
    default Traversable<T> sorted() {
        return traversable().sorted();
    }

    /**
     * <pre>
     * {@code
     * 	assertThat(ReactiveSeq.of(4,3,6,7).sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
     * }
     * </pre>
     *
     * @param c
     *            Compartor to sort with
     * @return Sorted Stream
     */
    default Traversable<T> sorted(final Comparator<? super T> c) {
        return traversable().sorted(c);
    }

    /**
     * Generate a new Traversable that takes elements from this Traversable as long as the predicate holds
     *
     * <pre>
     * {@code
     *     Seq.of(1,2,3).takeWhile(i<3);
     *     //[1,2]
     * }
     * </pre>
     *
     * @param p Predicate to determine when values should be taken
     * @return Traversable generated by application of the predicate to the elements in this Traversable in order
     */
    default Traversable<T> takeWhile(final Predicate<? super T> p) {
        return traversable().takeWhile(p);
    }

    /**
     * Generate a new Traversable that drops elements from this Traversable as long as the predicate holds
     * <pre>
     * {@code
     *     Seq.of(1,2,3).dropWhile(i<3);
     *     //[3]
     * }
     * </pre>
     * @param p Predicate to determine when values should be dropped
     * @return Traversable generated by application of the predicate to the elements in this Traversable in order
     */
    default Traversable<T> dropWhile(final Predicate<? super T> p) {
        return traversable().dropWhile(p);
    }

    /**
     * Generate a new Traversable that takes elements from this Traversable until the predicate holds
      * <pre>
     * {@code
     *     Seq.of(1,2,3).takeUntil(i<2);
     *     //[1,2]
     * }
     * </pre>
     *
     * @param p Predicate to determine when values should be taken until
     * @return  Traversable generated by application of the predicate to the elements in this Traversable in order
     */
    default Traversable<T> takeUntil(final Predicate<? super T> p) {
        return traversable().takeUntil(p);
    }

    /**
     * Generate a new Traversable that drops elements from this Traversable until the predicate holds
     * <pre>
     * {@code
     *     Seq.of(1,2,3).dropUntil(i>2);
     *     //[3]
     * }
     * </pre>
     * @param p Predicate to determine when values should be dropped
     * @return Traversable generated by application of the predicate to the elements in this Traversable in order
     */
    default Traversable<T> dropUntil(final Predicate<? super T> p) {
        return traversable().dropUntil(p);
    }

    /**
     * Generate a new Traversable that drops the specified number elements from the take of this Traversable
     * <pre>
     * {@code
     *     Seq.of(1,2,3).dropRight(2);
     *     //[1]
     * }
     * </pre>
     * @param num Drop this number of elements from the take of this Traversable
     * @return Traversable generated by application of the predicate to the elements in this Traversable in order
     */
    default Traversable<T> dropRight(final int num) {
        return traversable().dropRight(num);
    }

    /**
     * Generate a new Traversable that takes the specified number elements from the take of this Traversable
     * <pre>
     * {@code
     *     Seq.of(1,2,3).takeRight(2);
     *     //[2,3]
     * }
     * </pre>
     * @param num Take this number of elements from the take of this Traversable
     * @return Traversable generated by application of the predicate to the elements in this Traversable in order
     */
    default Traversable<T> takeRight(final int num) {
        return traversable().takeRight(num);
    }
    /**
     * <pre>
     * {@code assertThat(ReactiveSeq.of(4,3,6,7).drop(2).toList(),equalTo(Arrays.asList(6,7))); }
     * </pre>
     *
     *
     *
     * @param num
     *            Number of elemenets to drop
     * @return Traversable with specified number of elements skipped
     */
    default Traversable<T> drop(final long num) {
        return traversable().drop(num);
    }



    /**
     *
     *
     * <pre>
     * {@code
     *  ReactiveSeq.of(4,3,6,7)
     *             .take(2)
     *             .toList()
     *  asList(4,3)
     * </pre>
     *
     * @param num
     *            Elements to take
     * @return Traversable with specified number of elements
     */
    default Traversable<T> take(final long num) {
        return traversable().take(num);
    }







    /**
     * Returns a Traversable with a given value interspersed between any two values.
     *
     *
     * <pre>
     *    {@code
     *      ReactiveSeq.of(1, 2, 3, 4).intersperse(0)
     *      // (1, 0, 2, 0, 3, 0, 4)
     * }
     *</pre>
     */
    default Traversable<T> intersperse(final T value) {
        return traversable().intersperse(value);
    }


    default Traversable<T> reverse() {
        return traversable().reverse();
    }


    default Traversable<T> shuffle() {
        return traversable().shuffle();
    }





    @Override
    default Traversable<T> onEmpty(final T value) {
        return traversable().onEmpty(value);
    }


    @Override
    default Traversable<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return traversable().onEmptyGet(supplier);
    }



    default Traversable<T> shuffle(final Random random) {
        return traversable().shuffle(random);
    }


    default Traversable<T> slice(final long from, final long to) {
        return traversable().slice(from, to);
    }


    default <U extends Comparable<? super U>> Traversable<T> sorted(final Function<? super T, ? extends U> function) {
        return traversable().sorted(function);
    }


    default Traversable<T> traversable() {
        return stream();
    }


    default Traversable<T> prependStream(Stream<? extends T> stream){
        return traversable().prependStream(stream);
    }


    default Traversable<T> appendAll(T... values){
        return traversable().appendAll(values);
    }
    default Traversable<T> removeFirst(Predicate<? super T> pred){
        return traversable().removeFirst(pred);
    }


    default Traversable<T> append(T value){
        return traversable().append(value);
    }


    default Traversable<T> appendAll(Iterable<? extends T> value){
        return traversable().appendAll(value);
    }
    default Traversable<T> prependAll(Iterable<? extends T> value){
        return traversable().prependAll(value);
    }
    default Traversable<T> prepend(T value){
        return traversable().prepend(value);
    }


    /**
     *
     * <pre>
     * {@code
     * List<String> result = 	ReactiveSeq.of(1,2,3)
     * 									   .prependAll(100,200,300)
     * 									   .map(it ->it+"!!")
     * 									   .collect(CyclopsCollectors.toList());
     *
     *
     * //asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * }
     * @param values to prependAll
     * @return ReactiveSeq with values prepended
     */
    default Traversable<T> prependAll(T... values){
        return traversable().prependAll(values);
    }

    /**
     * Insert data into a traversable at given position
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3)
     *                                   .insertAt(1, 100, 200, 300)
     *                                   .map(it -> it + "!!")
     *                                   .collect(CyclopsCollectors.toList());
     *
     *  //Arrays.asList("1!!", "100!!", "200!!", "300!!", "2!!", "3!!")))
     * }
     * </pre>
     *
     * @param pos
     *            to insert data at
     * @param values
     *            to insert
     * @return Traversable with new data inserted
     */
    default Traversable<T> insertAt(int pos, T... values){
        return traversable().insertAt(pos,values);
    }

    default Traversable<T> insertAt(int pos,Iterable<? extends T> values){
        return traversable().insertAt(pos,values);
    }
    default Traversable<T> insertAt(int pos,ReactiveSeq<? extends T> values){
        return traversable().insertAt(pos,values);
    }
    default Traversable<T> updateAt(int i, T e){
        if(i<0)
            return this;
        return traversable().updateAt(i,e);
    }
    default Traversable<T> removeAt(long pos){
        return traversable().removeAt(pos);
    }
    /**
     * Delete elements between given indexes
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3, 4, 5, 6)
     *                                   .deleteBetween(2, 4).map(it -> it + "!!")
     *                                   .collect(CyclopsCollectors.toList());
     *
     *  //Arrays.asList("1!!", "2!!", "5!!", "6!!")));
     * </pre>
     *
     * @param start
     *            index
     * @param end
     *            index
     * @return Stream with elements removed
     */
    default Traversable<T> deleteBetween(int start, int end){
        return traversable().deleteBetween(start,end);
    }

    /**
     * Insert a Stream into the middle of this traversable at the specified position
     *
     * <pre>
     * {@code
     *  List<String> result = ReactiveSeq.of(1, 2, 3)
     *                                   .insertAt(1, of(100, 200, 300))
     *                                   .map(it -> it + "!!")
     *                                   .collect(CyclopsCollectors.toList());
     *
     *  //Arrays.asList("1!!", "100!!", "200!!", "300!!", "2!!", "3!!")));
     * }
     * </pre>
     *
     * @param pos
     *            to insert Stream at
     * @param stream
     *            to insert
     * @return newly conjoined Traversable
     */
    default Traversable<T> insertStreamAt(int pos, Stream<T> stream){
        return traversable().insertStreamAt(pos,stream);
    }


    /**
     * emit x elements per time period
     *
     * <pre>
     * {@code
     *  SimpleTimer timer = new SimpleTimer();
        ReactiveSeq.of(1, 2, 3, 4, 5, 6)
                   .xPer(6, 100000000, TimeUnit.NANOSECONDS)
                   .collect(CyclopsCollectors.toList())
                   .size()

        //6
     *
     * }
     * </pre>
     *
     * @param x
     *            number of elements to emit
     * @param time
     *            period
     * @param t
     *            Time unit
     * @return ReactiveSeq that emits x elements per time period
     */
    default ReactiveSeq<T> xPer(final int x, final long time, final TimeUnit t) {
        return stream().xPer(x, time, t);
    }

    /**
     * emit one element per time period
     *
     * <pre>
     * {@code
     * ReactiveSeq.iterate("", last -> "next")
     *              .limit(100)
     *              .batchBySize(10)
     *              .onePer(1, TimeUnit.MICROSECONDS)
     *              .peek(batch -> System.out.println("batched : " + batch))
     *              .flatMap(Collection::stream)
     *              .peek(individual -> System.out.println("Flattened : "
     *                      + individual))
     *              .forEach(a->{});
     * }
     * </pre>
     * @param time period
     * @param t Time unit
     * @return ReactiveSeq that emits 1 element per time period
     */
    default ReactiveSeq<T> onePer(final long time, final TimeUnit t) {
        return stream().onePer(time, t);
    }

    /**
     * emit elements after a fixed delay
     *
     * <pre>
     * {@code
     *  SimpleTimer timer = new SimpleTimer();
     *  ReactiveSeq.of(1, 2, 3, 4, 5, 6)
     *             .fixedDelay(10000, TimeUnit.NANOSECONDS)
     *             .collect(CyclopsCollectors.toList())
     *             .size()
     *  //6
     *  //timer.getElapsedNanoseconds() > greaterThan(60000l)
     * }
     * </pre>
     *
     * @param l
     *            time length in nanos of the delay
     * @param unit
     *            for the delay
     * @return ReactiveSeq that emits each element after a fixed delay
     */
    default ReactiveSeq<T> fixedDelay(final long l, final TimeUnit unit) {
        return stream().fixedDelay(l, unit);
    }
}

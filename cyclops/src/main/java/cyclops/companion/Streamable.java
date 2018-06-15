package cyclops.companion;


import com.oath.cyclops.internal.stream.StreamableImpl;
import com.oath.cyclops.types.factory.Unit;
import com.oath.cyclops.types.foldable.Contains;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.stream.HotStream;
import com.oath.cyclops.types.stream.ToStream;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.data.Seq;

import cyclops.data.HashMap;
import cyclops.data.Vector;
import cyclops.control.Option;
import cyclops.control.Maybe;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.*;

/**
 * Represents something that can generate a Stream, repeatedly
 *
 * @author johnmcclean
 *
 * @param <T> Data type for Stream
 */
@FunctionalInterface
public interface Streamable<T> extends To<Streamable<T>>,
                                        ToStream<T>,
                                        IterableX<T>,
                                        Contains<T>,
                                        Unit<T> {


    Iterable<T> getStreamable();

    @Override
    default ReactiveSeq<T> stream() {
        return Streams.oneShotStream(this);
    }


    /**
     * (Lazily) Construct a Streamable from a Stream.
     *
     * @param stream to construct Streamable from
     * @return Streamable
     */
    public static <T> Streamable<T> fromStream(final Stream<T> stream) {
        return new StreamableImpl(new PrintableIterable<>(Streams.toLazyCollection(stream)));
    }

    /**
     * (Lazily) Construct a Streamable from an Iterable.
     *
     * @param iterable to construct Streamable from
     * @return Streamable
     */
    public static <T> Streamable<T> fromIterable(final Iterable<T> iterable) {
        if(iterable instanceof Streamable)
            return (Streamable<T>)iterable;
        return new StreamableImpl(iterable);
    }



    @AllArgsConstructor
    static class PrintableIterable<T> implements Iterable<T> {
        private final Collection c;

        @Override
        public Iterator<T> iterator() {
            return c.iterator();
        }

        @Override
        public String toString() {
            return String.format("%s", c);
        }
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default Streamable<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return Streamable.fromIterable(IterableX.super.combine(predicate, op));
    }
    @Override
    default Streamable<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (Streamable<T>)IterableX.super.combine(op,predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Streamable<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return Streamable.fromIterable(IterableX.super.zip(other, zipper));
    }

    default <U, R> Streamable<R> zipWithStream(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return Streamable.fromIterable(IterableX.super.zipWithStream(other, zipper));
    }

  @Override
  default <T2, R> Streamable<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
    return Streamable.fromIterable(IterableX.super.zip(fn,publisher));
  }

  @Override
  default <U> Streamable<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
    return Streamable.fromIterable(IterableX.super.zipWithPublisher(other));
  }

  @Override
  default <S, U, R> Streamable<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
    return Streamable.fromIterable(IterableX.super.zip3(second,third,fn3));
  }

  @Override
  default <T2, T3, T4, R> Streamable<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
    return Streamable.fromIterable(IterableX.super.zip4(second,third,fourth,fn));
  }

  /* (non-Javadoc)
       * @see com.oath.cyclops.types.traversable.Traversable#zip(java.util.stream.Stream)
       */
    default <U> Streamable<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return Streamable.fromIterable(IterableX.super.zipWithStream(other));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> Streamable<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return Streamable.fromIterable(IterableX.super.zip3(second, third));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> Streamable<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return Streamable.fromIterable(IterableX.super.zip4(second, third, fourth));
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default Streamable<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return Streamable.fromIterable(IterableX.super.groupedUntil(predicate));
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default Streamable<T> takeWhile(final Predicate<? super T> p) {

        return (Streamable<T>) IterableX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default Streamable<T> dropWhile(final Predicate<? super T> p) {

        return (Streamable<T>) IterableX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default Streamable<T> takeUntil(final Predicate<? super T> p) {

        return (Streamable<T>) IterableX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default Streamable<T> dropUntil(final Predicate<? super T> p) {

        return (Streamable<T>) IterableX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#dropRight(int)
     */
    @Override
    default Streamable<T> dropRight(final int num) {

        return (Streamable<T>) IterableX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.traversable.Traversable#takeRight(int)
     */
    @Override
    default Streamable<T> takeRight(final int num) {

        return (Streamable<T>) IterableX.super.takeRight(num);
    }

    /**
     * Construct a FutureStream from an Publisher
     *
     * @param publisher
     *            to construct ReactiveSeq from
     * @return FutureStream
     */
    public static <T> Streamable<T> fromPublisher(final Publisher<? extends T> publisher) {
        Objects.requireNonNull(publisher);

        return fromStream(Spouts.from(publisher));
    }

    public static <T> Streamable<T> fromIterator(final Iterator<T> it) {

        return Streamable.fromIterable(() -> it);
    }

    @Override
    default <T> Streamable<T> unit(final T t) {
        return of(t);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.IterableFunctor#unitIterable(java.util.Iterator)
     */
    @Override
    default <T> Streamable<T> unitIterator(final Iterator<T> it) {
        return Streamable.fromIterator(it);
    }

    /**
     * Construct a Streamable that returns a Stream
     *
     * @param values to construct Streamable from
     * @return Streamable
     */
    public static <T> Streamable<T> of(final T... values) {
        final Iterable<T> it = Arrays.asList(values);
        return new Streamable<T>() {

            @Override
            public Iterable<T> getStreamable() {
                return it;
            }

            @Override
            public ReactiveSeq<T> stream() {
                return Streams.oneShotStream(Stream.of(values));
            }

        };
    }

    public static <T> Streamable<T> empty() {
        return of();
    }





    /**
     * Create a new Streamablw with all elements in this Streamable followed by the elements in the provided Streamable
     *
     * <pre>
     * {@code
     * 	Streamable.of(1,2,3).appendAll(Streamable.of(4,5,6))
     *
     *   //Streamable[1,2,3,4,5,6]
     * }
     * </pre>
     *
     * @param t Streamable to append
     * @return New Streamable with provided Streamable appended
     */
    default Streamable<T> appendAll(final Streamable<T> t) {
        return Streamable.fromStream(this.stream().appendStream(t.stream()));
    }

    /**
     * Remove all occurances of the specified element from the Streamable
     * <pre>
     * {@code
     * 	Streamable.of(1,2,3,4,5,1,2,3).removeValue(1)
     *
     *  //Streamable[2,3,4,5,2,3]
     * }
     * </pre>
     *
     * @param t element to removeValue
     * @return Filtered Streamable
     */
    default Streamable<T> removeValue(final T t) {
        return Streamable.fromStream(this.stream().removeValue(t));
    }

    /**
     * Prepend given values to the skip of the Stream
     * <pre>
     * {@code
     * List<String> result = 	Streamable.of(1,2,3)
     * 									 .prependAll(100,200,300)
    									 .map(it ->it+"!!")
    									 .collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * }
     * </pre>
     * @param t value to prependAll
     * @return Streamable with values prepended
     */
    default Streamable<T> prepend(final T t) {
        return Streamable.fromStream(this.stream().prepend(t));
    }

    /*
     * Return the distinct Stream of elements
     *
     * <pre>
     * {@code List<Integer> list =  Streamable.of(1,2,2,2,5,6)
     *           	 						 .distinct()
     *				 						 .collect(CyclopsCollectors.toList());
     * }
     *</pre>
     */
    @Override
    default Streamable<T> distinct() {
        return Streamable.fromStream(this.stream().distinct());
    }

    /**
     * Fold a Streamable Left
     * <pre>
     * {@code
     *   Streamable.of("hello","world")
     *   			.foldLeft("",(a,b)->a+":"+b);
     *
     *   //"hello:world"
     * }
     * </pre>
     *
     * @param identity - identity value
     * @param function folding function
     * @return Value from reduction
     */
    default <U> U foldLeft(final U identity, final BiFunction<U, ? super T, U> function) {
        return this.stream().foldLeft(identity, function);
    }

    /**
     * Fold a Streamable fromt the right
     * <pre>
    * {@code
    *   Streamable.of("hello","world")
    *   			.foldRight("",(a,b)->a+":"+b);
    *
    *   //"world:hello"
    * }
    * </pre>
    *
    * @param seed - identity value
    * @param function folding function
    * @return Active reduced value
    */
    @Override
    default <U> U foldRight(final U seed, final BiFunction<? super T, ? super U, ? extends U> function) {
        return this.stream().foldRight(seed, function);
    }

    /**
     * Map the values in the Streamable from one set of values / types to another
     *
     * <pre>
     * {@code
     * 	Streamable.of(1,2,3).map(i->i+2);
     *  //Streamable[3,4,5]
     *
     *  Streamable.of(1,2,3).map(i->"hello"+(i+2));
     *
     *   //Streamable["hello3","hello4","hello5"]
     * }
     * </pre>
     *
     * @param fn mapper function
     * @return Mapped Streamable
     */
    @Override
    default <R> Streamable<R> map(final Function<? super T, ? extends R> fn) {
        return Streamable.fromStream(this.stream().map(fn));
    }

    /**
     * Peek at each value in a Streamable as it passes through unchanged
     *
     * <pre>
     * {@code
     *    Streamable.of(1,2,3)
     *              .peek(System.out::println)
     *              .map(i->i+2);
     * }
     * </pre>
     *
     * @param fn Consumer to peek with
     * @return Streamable that will peek at values as they pass through
     */
    @Override
    default Streamable<T> peek(final Consumer<? super T> fn) {
        return Streamable.fromStream(this.stream().peek(fn));
    }

    /* (non-Javadoc)
     * @see java.util.stream.Stream#filtered(java.util.function.Predicate)
     */
    @Override
    default Streamable<T> filter(final Predicate<? super T> fn) {
        return Streamable.fromStream(this.stream().filter(fn));
    }


    default <R> Streamable<R> flatMap(final Function<? super T, Streamable<? extends R>> fn) {
        return Streamable.fromStream(stream().flatMap(i -> fn.apply(i).stream()));
    }
    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *      ReactiveSeq.of(1,2,3)
     *                 .map(i->i*2)
     *                 .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *      //ReactiveSeq[12]
     * }
     * </pre>
     *
     *
     * @param fn
     * @return
     */
    default <R> Streamable<R> coflatMap(Function<? super Streamable<T>, ? extends R> fn){
        return Streamable.fromStream(this.stream().coflatMap(i -> fn.apply(Streamable.fromStream(i))));

    }
    /**
     * @return number of elements in this Streamable
     */
    @Override
    default long count() {
        return this.stream().count();
    }

    /* (non-Javadoc)
     * @see java.util.stream.Stream#forEachOrdered(java.util.function.Consumer)
     */
    default void forEachOrdered(final Consumer<? super T> action) {
        this.stream().forEachOrdered(action);
    }

    /* (non-Javadoc)
     * @see java.util.stream.Stream#toArray()
     */
    default Object[] toArray() {
        return this.stream().toArray();
    }

    /* (non-Javadoc)
     * @see java.util.stream.Stream#toArray(java.util.function.IntFunction)
     */
    default <A> A[] toArray(final IntFunction<A[]> generator) {
        return this.stream().toArray(generator);
    }

    /**
     * <pre>
     * {@code
     *   Streamable.of(1,2,3)
     *             .toList();
     *
     *  //List[1,2,3]
     * }
     * </pre>
     *
     * @return Streamable converted to a List
     */
    default List<T> toList() {
        return this.stream().toList();
    }

    default <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
        return this.stream().collect(supplier, accumulator, combiner);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.stream.CyclopsCollectable#collect(java.util.stream.Collector)
     */
    @Override
    default <R, A> R collect(final Collector<? super T, A, R> collector) {

        return this.stream().collect(collector);
    }



    /**
     * Generate the permutations based on values in the Streamable
     * @return  Streamable containing the permutations in this Streamable

     *
     */
    default Streamable<ReactiveSeq<T>> permutations() {
       return Streamable.fromStream(stream().permutations());
    }

    /**
     * Return a Streamable with elements before the provided skip index removed, and elements after the provided
     * take index removed
     *
     * <pre>
     * {@code
     *   Streamable.of(1,2,3,4,5,6).subStream(1,3);
     *
     *
     *   //Streamable[2,3]
     * }
     * </pre>
     *
     * @param start index inclusive
     * @param end index exclusive
     * @return Sequence between supplied indexes of original Sequence
     */
    default Streamable<T> subStream(final int start, final int end) {
        return Streamable.fromStream(this.stream().subStream(start, end));
    }



    /**
     * [equivalent to count]
     *
     * @return size
     */
    default int size() {
        return this.stream().size();
    }

    /**
     * <pre>
     * {@code
     *   Streamable.of(1,2,3).combinations(2)
     *
     *   //Streamable[Streamable[1,2],Streamable[1,3],Streamable[2,3]]
     * }
     * </pre>
     *
     *
     * @param size of combinations
     * @return All combinations of the elements in this stream of the specified size
     */
    default Streamable<ReactiveSeq<T>> combinations(final int size) {



        if (size == 0) {
            return Streamable.of(ReactiveSeq.empty());
        } else {
            return Streamable.fromStream( stream().combinations(size));
        }
    }

    /**
     * <pre>
     * {@code
     *   Streamable.of(1,2,3).combinations()
     *
     *   //Streamable[Streamable[],Streamable[1],Streamable[2],Streamable[3],Streamable[1,2],Streamable[1,3],Streamable[2,3]
     *   			,Streamable[1,2,3]]
     * }
     * </pre>
     *
     *
     * @return All combinations of the elements in this stream
     */
    default Streamable<ReactiveSeq<T>> combinations() {
        return range(0, size() + 1).map(this::combinations)
                                   .flatMap(s -> s);

    }

    /**
     * join / flatten one level of a nest hierarchy
     *
     * <pre>
     * {@code
     *  Streamable.of(Arrays.asList(1,2)).flatten();
     *
     *  //stream of (1,  2);
     *
     * }
     *
     * </pre>
     *
     * @return Flattened / joined one level
     */
    public static <T1> Streamable<T1> flatten(Streamable<? extends Streamable<T1>> nested) {
        return nested.flatMap(Function.identity());
    }

    /**
     * Type safe unwrap
     * <pre>
     * {@code
     * Optional<List<String>> stream = Streamable.of("hello","world")
    											.optional();

    	assertThat(stream.getValue(),equalTo(Arrays.asList("hello","world")));
     * }
     *
     * </pre>
     * @return this Streamable converted to an Optional List

    @Override
    default Optional<Seq<T>> optional() {
        return reactiveSeq().optional();
    }
     */

    /**
     * Convert to a Stream with the values repeated specified times
     *
     * <pre>
     * {@code
     * 		assertThat(Streamable.of(1,2,2)
    							.cycle(3)
    							.collect(CyclopsCollectors.toList()),
    							equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));

     *
     * }
     * </pre>
     * @param times
     *            Times values should be repeated within a Stream
     * @return Streamable with values repeated
     */
    @Override
    default Streamable<T> cycle(final long times) {
        return Streamable.fromStream(this.stream().cycle(times));
    }

    /**
     * Convert to a Stream with the values infinitely cycled
     *
     * <pre>
     * {@code
     *   assertEquals(asList(1, 1, 1, 1, 1,1),Streamable.of(1).cycle().limit(6).toList());
     *   }
     * </pre>
     *
     * @return Stream with values repeated
     */
    default Streamable<T> cycle() {
        return Streamable.fromStream(this.stream().cycle());
    }

    /**
     * Duplicate a Stream, buffers intermediate values, leaders may change positions so a limit
     * can be safely applied to the leading stream. Not thread-safe.
     * <pre>
     * {@code
     *  Tuple2<Streamable<Integer>, Streamable<Integer>> copies =of(1,2,3,4,5,6).duplicate();
    	 assertTrue(copies._1.anyMatch(i->i==2));
    	 assertTrue(copies._2.anyMatch(i->i==2));
     *
     * }
     * </pre>
     *
     * @return duplicated stream
     */
    default Tuple2<Streamable<T>, Streamable<T>> duplicate() {
        return Tuple.tuple(this, this);
    }

    default Tuple3<Streamable<T>, Streamable<T>, Streamable<T>> triplicate() {
        return Tuple.tuple(this, this, this);
    }

    default Tuple4<Streamable<T>, Streamable<T>, Streamable<T>, Streamable<T>> quadruplicate() {
        return Tuple.tuple(this, this, this, this);
    }

    /**
     * Split at supplied location
     * <pre>
     * {@code
     * Streamable.of(1,2,3).splitAt(1)
     *
     *  //Streamable[1], Streamable[2,3]
     * }
     *
     * </pre>
     */
    default Tuple2<Streamable<T>, Streamable<T>> splitAt(final int where) {

        return this.stream().splitAt(where)
                            .map1(s -> fromStream(s))
                            .map2(s -> fromStream(s));
    }

    default Tuple2<Option<T>, Streamable<T>> splitAtHead(){
        return this.stream().splitAtHead().map2(s->Streamable.fromStream(s));
    }

    /**
     * Split this Streamable after the first element (if present)
     *
     * <pre>
     * {@code
     *  Streamable.of(1,2,3).splitAtHead()
     *
     *  //Tuple[1,Streamable[2,3]]
     *
     * }</pre>
     *
     *
     * @return Split Streamable

    default Tuple2<Optional<T>, Streamable<T>> splitAtHead() {
        return reactiveSeq().splitAtHead()
                            .zip(s -> fromStream(s));
    }*/

    /**
     * Split stream at point where predicate no longer holds
     * <pre>
     * {@code
     *   Streamable.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)
     *
     *   //Streamable[1,2,3] Streamable[4,5,6]
     * }
     * </pre>
     */
    default Tuple2<Streamable<T>, Streamable<T>> splitBy(final Predicate<T> splitter) {
        return this.stream().splitBy(splitter)
                            .map1(s -> fromStream(s))
                            .map2(s -> fromStream(s));
    }

    /**
     * Partition a Stream into two one a per element basis, based on predicate's boolean value
     * <pre>
     * {@code
     *  Streamable.of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)
     *
     *  //Streamable[1,3,5], Streamable[2,4,6]
     * }
     *
     * </pre>
     */
    default Tuple2<Streamable<T>, Streamable<T>> partition(final Predicate<T> splitter) {
        return this.stream().partition(splitter)
                            .map1(s -> fromStream(s))
                            .map2(s -> fromStream(s));
    }

    /**
     * Convert to a Stream with the result of a reduction operation repeated
     * specified times
     *
     * <pre>
     * {@code
     *   		List<Integer> list = AsGenericMonad,asMonad(Stream.of(1,2,2))
     * 										.cycle(Reducers.toCountInt(),3)
     * 										.collect(CyclopsCollectors.toList());
     * 	//is asList(3,3,3);
     *   }
     * </pre>
     *
     * @param m
     *            Monoid to be used in reduction
     * @param times
     *            Number of times value should be repeated
     * @return Stream with reduced values repeated
     */
    @Override
    default Streamable<T> cycle(final Monoid<T> m, final long times) {
        return fromStream(this.stream().cycle(m, times));
    }

    /**
     * Repeat in a Stream while specified predicate holds
     *
     * <pre>
     * {@code
     *
     *   count =0;
    	assertThat(Streamable.of(1,2,2)
    						.cycleWhile(next -> count++<6)
    						.collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
     * }
     * </pre>
     *
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    @Override
    default Streamable<T> cycleWhile(final Predicate<? super T> predicate) {
        return Streamable.fromStream(this.stream().cycleWhile(predicate));
    }

    /**
     * Repeat in a Stream until specified predicate holds
     * <pre>
     * {@code
     * 	count =0;
    	assertThat(Streamable.of(1,2,2)
    						.cycleUntil(next -> count++>6)
    						.collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2,1)));

     *
     * }
     * </pre>
     *
     * @param predicate
     *            repeat while true
     * @return Repeating Stream
     */
    @Override
    default Streamable<T> cycleUntil(final Predicate<? super T> predicate) {
        return Streamable.fromStream(this.stream().cycleUntil(predicate));
    }

    /**
     * Zip 2 streams into one
     *
     * <pre>
     * {@code
     * List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();
       // [[1,"a"],[2,"b"]]
    	 }
     * </pre>
     *
     */
    @Override
    default <U> Streamable<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return fromStream(this.stream().zip(other));
    }


    /**
     * zip 3 Streams into one
     * <pre>
     * {@code
     * List<Tuple3<Integer,Integer,Character>> list =
    			of(1,2,3,4,5,6).zip3(of(100,200,300,400),of('a','b','c'))
    										.collect(CyclopsCollectors.toList());
     *
     * //[[1,100,'a'],[2,200,'b'],[3,300,'c']]
     * }
     *
     *</pre>
     */
    default <S, U> Streamable<Tuple3<T, S, U>> zip3(final Streamable<? extends S> second, final Streamable<? extends U> third) {
        return fromStream(stream().zip3(second.stream(), third.stream()));
    }

    /**
     * zip 4 Streams into 1
     *
     * <pre>
     * {@code
     * List<Tuple4<Integer,Integer,Character,String>> list =
    			of(1,2,3,4,5,6).zip4(of(100,200,300,400),of('a','b','c'),of("hello","world"))
    											.collect(CyclopsCollectors.toList());

     * }
     *  //[[1,100,'a',"hello"],[2,200,'b',"world"]]
     * </pre>
     */
    default <T2, T3, T4> Streamable<Tuple4<T, T2, T3, T4>> zip4(final Streamable<? extends T2> second, final Streamable<? extends T3> third,
            final Streamable<? extends T4> fourth) {
        return fromStream(this.stream().zip4(second.stream(), third.stream(), fourth.stream()));
    }

    /**
     * Add an index to the current Stream
     *
     * <pre>
     * {@code
     * assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
     * }
     * </pre>
     */
    @Override
    default Streamable<Tuple2<T, Long>> zipWithIndex() {
        return fromStream(this.stream().zipWithIndex());
    }



    /**
     * Create a sliding view over this Sequence
     *
     * <pre>
     * {@code
     * List<List<Integer>> list = fromEither5(Stream.of(1,2,3,4,5,6))
    								.asSequence()
    								.sliding(2)
    								.collect(CyclopsCollectors.toList());


    	assertThat(list.getValue(0),hasItems(1,2));
    	assertThat(list.getValue(1),hasItems(2,3));
     *
     * }
     *
     * </pre>
     * @param windowSize
     *            Size of sliding window
     * @return Streamable with sliding view
     */
    @Override
    default Streamable<Seq<T>> sliding(final int windowSize) {
        return fromStream(this.stream().sliding(windowSize));
    }

    /**
     *  Create a sliding view over this Sequence
     * <pre>
     * {@code
     * List<List<Integer>> list = fromEither5(Stream.of(1,2,3,4,5,6))
    								.asSequence()
    								.sliding(3,2)
    								.collect(CyclopsCollectors.toList());


    	assertThat(list.getValue(0),hasItems(1,2,3));
    	assertThat(list.getValue(1),hasItems(3,4,5));
     *
     * }
     *
     * </pre>
     *
     * @param windowSize number of elements in each batch
     * @param increment for each window
     * @return Streamable with sliding view
     */
    @Override
    default Streamable<Seq<T>> sliding(final int windowSize, final int increment) {
        return fromStream(this.stream().sliding(windowSize, increment));
    }

    /**
     * Group elements in a Stream
     *
     * <pre>
     * {@code
     *  List<List<Integer>> list = monad(Stream.of(1, 2, 3, 4, 5, 6)).grouped(3)
     *          .collect(CyclopsCollectors.toList());
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
    @Override
    default Streamable<Vector<T>> grouped(final int groupSize) {
        return fromStream(this.stream().grouped(groupSize));
    }

    /**
     * Use classifier function to group elements in this Sequence into a Map
     * <pre>
     * {@code
     * Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
    	        assertEquals(asList(2, 4), map1.getValue(0));
    	        assertEquals(asList(1, 3), map1.getValue(1));
    	        assertEquals(2, map1.size());
     *
     * }
     *
     * </pre>
     */
    @Override
    default <K> HashMap<K, Vector<T>> groupBy(final Function<? super T, ? extends K> classifier) {
        return this.stream().groupBy(classifier);
    }

    /**
     * Scan left using supplied Monoid
     *
     * <pre>
     * {@code
     *
     * 	assertEquals(asList("", "a", "ab", "abc"),Streamable.of("a", "b", "c")
     * 													.scanLeft(Reducers.toString("")).toList());
     *
     *         }
     * </pre>
     *
     * @param monoid To combine values
     * @return Streamable
     */
    @Override
    default Streamable<T> scanLeft(final Monoid<T> monoid) {
        return fromStream(this.stream().scanLeft(monoid));
    }

    /**
     * Scan left
     * <pre>
     * {@code
     *  assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
        		is(4));
     * }
     * </pre>
     */
    @Override
    default <U> Streamable<U> scanLeft(final U identity, final BiFunction<? super U, ? super T, ? extends U> function) {
        return fromStream(this.stream().scanLeft(identity, function));
    }

    /**
     * Scan right
     * <pre>
     * {@code
     * assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
     * }
     * </pre>
     */
    @Override
    default Streamable<T> scanRight(final Monoid<T> monoid) {
        return fromStream(this.stream().scanRight(monoid));
    }

    /**
     * Scan right
     *
     * <pre>
     * {@code
     * assertThat(of("a", "ab", "abc").map(str->str.length()).scanRight(0, (t, u) -> u + t).toList().size(),
            is(asList(0, 3, 5, 6).size()));
     *
     * }
     * </pre>
     */
    @Override
    default <U> Streamable<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return fromStream(this.stream().scanRight(identity, combiner));
    }

    /**
     * <pre>
     * {@code assertThat(Streamable.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
     * </pre>
     *
     */
    @Override
    default Streamable<T> sorted() {
        return fromStream(this.stream().sorted());
    }

    /**
     *<pre>
     * {@code
     * 	assertThat(Streamable.of(4,3,6,7).sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
     * }
     * </pre>
     * @param c
     *            Compartor to sort with
     * @return Sorted Monad
     */
    @Override
    default Streamable<T> sorted(final Comparator<? super T> c) {
        return fromStream(this.stream().sorted(c));
    }

    /**
     * <pre>
     * {@code assertThat(Streamable.of(4,3,6,7).skip(2).toList(),equalTo(Arrays.asList(6,7))); }
     * </pre>
     *

     *
     * @param num
     *            Number of elemenets to skip
     * @return Monad converted to Stream with specified number of elements
     *         skipped
     */
    @Override
    default Streamable<T> skip(final long num) {
        return fromStream(this.stream().skip(num));
    }

    /**
     *
     *
     * <pre>
     * {@code
     * assertThat(Streamable.of(4,3,6,7).sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
     * }
     * </pre>
     *
     * @param p
     *            Predicate to skip while true
     * @return Monad converted to Stream with elements skipped while predicate
     *         holds
     */
    @Override
    default Streamable<T> skipWhile(final Predicate<? super T> p) {
        return fromStream(this.stream().skipWhile(p));
    }

    /**
     *
     *
     * <pre>
     * {@code assertThat(Streamable.of(4,3,6,7).skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));}
     * </pre>
     *
     *
     * @param p
     *            Predicate to skip until true
     * @return Monad converted to Stream with elements skipped until predicate
     *         holds
     */
    @Override
    default Streamable<T> skipUntil(final Predicate<? super T> p) {
        return fromStream(this.stream().skipUntil(p));
    }

    /**
     *
     *
     * <pre>
     * {@code assertThat(Streamable.of(4,3,6,7).limit(2).toList(),equalTo(Arrays.asList(4,3));}
     * </pre>
     *
     * @param num
     *            Limit element size to num
     * @return Monad converted to Stream with elements up to num
     */
    @Override
    default Streamable<T> limit(final long num) {
        return fromStream(this.stream().limit(num));
    }

    /**
     *
     *
     * <pre>
     * {@code assertThat(Streamable.of(4,3,6,7).sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));}
     * </pre>
     *
     * @param p
     *            Limit while predicate is true
     * @return Monad converted to Stream with limited elements
     */
    @Override
    default Streamable<T> limitWhile(final Predicate<? super T> p) {
        return fromStream(this.stream().limitWhile(p));
    }

    /**
     *
     *
     * <pre>
     * {@code assertThat(Streamable.of(4,3,6,7).limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3))); }
     * </pre>
     *
     * @param p
     *            Limit until predicate is true
     * @return Monad converted to Stream with limited elements
     */
    @Override
    default Streamable<T> limitUntil(final Predicate<? super T> p) {
        return fromStream(this.stream().limitUntil(p));

    }

    /**
     * True if predicate matches all elements when Monad converted to a Stream
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
     * }
     * </pre>
     * @param c Predicate to check if all fold
     */
    @Override
    default boolean allMatch(final Predicate<? super T> c) {
        return this.stream().allMatch(c);
    }

    /**
     * True if a single element matches when Monad converted to a Stream
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
     * }
     * </pre>
     * @param c Predicate to check if any fold
     */
    @Override
    default boolean anyMatch(final Predicate<? super T> c) {
        return this.stream().anyMatch(c);
    }

    /**
     * Check that there are specified number of matches of predicate in the Stream
     *
     * <pre>
     * {@code
     *  assertTrue(Streamable.of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
     * }
     * </pre>
     *
     */
    @Override
    default boolean xMatch(final int num, final Predicate<? super T> c) {
        return this.stream().xMatch(num, c);
    }

    /*
     * <pre>
     * {@code
     * assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),equalTo(true));
     *
     * }
     * </pre>
     */
    @Override
    default boolean noneMatch(final Predicate<? super T> c) {
        return this.stream().noneMatch(c);
    }

    /**
     * <pre>
     * {@code
     *  assertEquals("123".length(),Streamable.of(1, 2, 3).join().length());
     * }
     * </pre>
     *
     * @return Stream as concatenated String
     */
    @Override
    default String join() {
        return this.stream().join();
    }

    /**
     * <pre>
     * {@code
     * assertEquals("1, 2, 3".length(), Streamable.of(1, 2, 3).join(", ").length());
     * }
     * </pre>
     * @return Stream as concatenated String
     */
    @Override
    default String join(final String sep) {
        return this.stream().join(sep);
    }

    /**
     * <pre>
     * {@code
     * assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
     * }
     * </pre>
     *  @return Stream as concatenated String
     */
    @Override
    default String join(final String sep, final String start, final String end) {
        return this.stream().join(sep, start, end);
    }




    /**
     * Attempt to transform this Sequence to the same type as the supplied Monoid (Reducer)
     * Then use Monoid to reduce values
     * <pre>
     * {@code
     * Streamable.of("hello","2","world","4").mapReduce(Reducers.toCountInt());
     *
     * //4
     * }
     * </pre>
     *
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    @Override
    default <R> R mapReduce(final Reducer<R,T> reducer) {
        return this.stream().mapReduce(reducer);
    }

    /**
     *  Attempt to transform this Monad to the same type as the supplied Monoid, using supplied function
     *  Then use Monoid to reduce values
     *
     *  <pre>
     *  {@code
     *  Streamable.of("one","two","three","four")
     *           .mapReduce(this::toInt,Reducers.toTotalInt());
     *
     *  //10
     *
     *  int toInt(String s){
    	if("one".equals(s))
    		return 1;
    	if("two".equals(s))
    		return 2;
    	if("three".equals(s))
    		return 3;
    	if("four".equals(s))
    		return 4;
    	return -1;
       }
     *  }
     *  </pre>
     *
     * @param mapper Function to transform Monad type
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    @Override
    default <R> R mapReduce(final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return this.stream().mapReduce(mapper, reducer);
    }

    /**
     * <pre>
     * {@code
     * Streamable.of("hello","2","world","4").reduce(Reducers.toString(","));
     *
     * //hello,2,world,4
     * }</pre>
     *
     * @param reducer Use supplied Monoid to reduce values
     * @return reduced values
     */
    @Override
    default T reduce(final Monoid<T> reducer) {
        return this.stream().reduce(reducer);
    }

    /*
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).getValue(),equalTo(1500));
     * }
     * </pre>
     *
     */
    @Override
    default Optional<T> reduce(final BinaryOperator<T> accumulator) {
        return this.stream().reduce(accumulator);
    }

    /* (non-Javadoc)
    * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BinaryOperator)
    */
    @Override
    default T reduce(final T identity, final BinaryOperator<T> accumulator) {
        return this.stream().reduce(identity, accumulator);
    }

    /* (non-Javadoc)
    * @see java.util.stream.Stream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
    */
    @Override
    default <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return this.stream().reduce(identity, accumulator, combiner);
    }



    /**
     * Reduce with multiple reducers in parallel
     * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
     * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().reduce(reducer)
     *
     * <pre>
     * {@code
     * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
    	Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
    	List<Integer> result = Streamable.of(1,2,3,4))
    									.reduce(Arrays.asList(sum,mult) );


    	assertThat(result,equalTo(Arrays.asList(10,24)));
     *
     * }
     * </pre>
     * @param reducers
     * @return
     */
    @Override
    default Seq<T> reduce(final Iterable<? extends Monoid<T>> reducers) {
        return this.stream().reduce(reducers);
    }

    /**
     *
     * <pre>
    	{@code
    	Streamable.of("a","b","c").foldRight(Reducers.toString(""));

        // "cab"
        }
        </pre>
     * @param reducer Use supplied Monoid to reduce values starting via foldRight
     * @return Reduced result
     */
    @Override
    default T foldRight(final Monoid<T> reducer) {
        return this.stream().foldRight(reducer);
    }

    /**
     * Immutable reduction from right to left
     * <pre>
     * {@code
     *  assertTrue(Streamable.of("a","b","c").foldRight("", String::concat).equals("cba"));
     * }
     * </pre>
     *
     * @param identity
     * @param accumulator
     * @return
     */
    @Override
    default T foldRight(final T identity, final BinaryOperator<T> accumulator) {
        return this.stream().foldRight(identity, accumulator);
    }

    /**
     *  Attempt to transform this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
     * Then use Monoid to reduce values
     * <pre>
    	{@code
    	Streamable.of(1,2,3).foldRightMapToType(Reducers.toString(""));

        // "321"
        }
        </pre>
     *
     *
     * @param reducer Monoid to reduce values
     * @return Reduce result
     */
    @Override
    default <R> R foldRightMapToType(final Reducer<R,T> reducer) {
        return this.stream().foldRightMapToType(reducer);
    }

    /**
     *
     * <pre>
     * {@code
     *  assertTrue(Streamable.of(1,2,3,4).startsWith(Arrays.asList(1,2,3)));
     * }</pre>
     *
     * @param iterable
     * @return True if Monad starts with Iterable sequence of data
     */
    @Override
    default boolean startsWith(final Iterable<T> iterable) {
        return this.stream().startsWith(iterable);
    }




    public static <T> Streamable<T> narrow(Streamable<? extends T> broad){
        return (Streamable<T>)broad;
    }
    /**
     * FlatMap where the result is a Collection, flattens the resultant collections into the
     * host Streamable
     * <pre>
     * {@code
     * 	Streamable.of(1,2)
     * 			.flatMap(i -> asList(i, -i))
     *          .toList();
     *
     *   //1,-1,2,-2
     * }
     * </pre>
     *
     * @param fn
     * @return
     */
    default <R> Streamable<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> fn) {
        return fromStream(this.stream().concatMap(fn));
    }

    /**
     * flatMap operation
     *
     * <pre>
     * {@code
     * 	assertThat(Streamable.of(1,2,3)
     *                      .flatMapStream(i->IntStream.of(i))
     *                      .toList(),equalTo(Arrays.asList(1,2,3)));

     * }
     * </pre>
     *
     * @param fn to be applied
     * @return new stage in Sequence with flatMap operation to be lazily applied
    */
    default <R> Streamable<R> flatMapStream(final Function<? super T, BaseStream<? extends R, ?>> fn) {
        return fromStream(this.stream().flatMapStream(fn));
    }

    /**
     * Returns a stream with a given value interspersed between any two values
     * of this stream.
     *
     *
     * // (1, 0, 2, 0, 3, 0, 4) Streamable.of(1, 2, 3, 4).intersperse(0)
     *
     */
    @Override
    default Streamable<T> intersperse(final T value) {
        return fromStream(this.stream().intersperse(value));
    }

    /**
     * Keep only those elements in a stream that are of a given type.
     *
     *
     * // (1, 2, 3) Streamable.of(1, "a", 2, "b",3).ofType(Integer.class)
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    default <U> Streamable<U> ofType(final Class<? extends U> type) {
        return fromStream(this.stream().ofType(type));
    }



    /*
     * Potentially efficient Sequence reversal. Is efficient if
     *
     * - Sequence created via a range
     * - Sequence created via a List
     * - Sequence created via an Array / var args
     *
     * Otherwise Sequence collected into a Collection prior to reversal
     *
     * <pre>
     * {@code
     *  assertThat( of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
     *  }
     * </pre>
     */
    @Override
    default Streamable<T> reverse() {
        return fromStream(this.stream().reverse());
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#shuffle()
     */
    @Override
    default Streamable<T> shuffle() {
        return fromStream(this.stream().shuffle());
    }

    /**
     * Append Stream to this Streamable
     *
     * <pre>
     * {@code
     * List<String> result = 	Streamable.of(1,2,3)
     *                                  .appendStream(Streamable.of(100,200,300))
    									.map(it ->it+"!!")
    									.collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
     * }
     * </pre>
     *
     * @param stream to append
     * @return Streamable with Stream appended
     */
    default Streamable<T> appendStreamable(final Streamable<T> stream) {
        return fromStream(this.stream().appendStream(stream.stream()));
    }

    /**
     * Prepend Stream to this Streamable
     *
     * <pre>
     * {@code
     * List<String> result = Streamable.of(1,2,3)
     * 								  .prependStream(of(100,200,300))
    								  .map(it ->it+"!!")
    								  .collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     *
     * }
     * </pre>
     *
     * @param stream to Prepend
     * @return Streamable with Stream prepended
     */
    default Streamable<T> prependStreamable(final Streamable<T> stream) {
        return fromStream(this.stream().prependStream(stream.stream()));
    }
    default Streamable<T> prependStream(final Stream<? extends T> stream) {
        return fromStream(this.stream().prependStream(stream));
    }

    @Override
    default Streamable<T> append(T value){
        return fromStream(this.stream().append(value));
    }

    @Override
    default Streamable<T> insertStreamAt(int pos, Stream<T> stream){
        return fromStream(this.stream().insertStreamAt(pos,stream));
    }

    /**
     * Append values to the take of this Streamable
     * <pre>
     * {@code
     * List<String> result = Streamable.of(1,2,3)
     * 								   .append(100,200,300)
    									.map(it ->it+"!!")
    									.collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
     * }
     * </pre>
     * @param values to append
     * @return Streamable with appended values
     */
    default Streamable<T> appendAll(final T... values) {
        return fromStream(this.stream().appendAll(values));
    }

    /**
     * Prepend given values to the skip of the Stream
     * <pre>
     * {@code
     * List<String> result = 	Streamable.of(1,2,3)
     * 									 .prependAll(100,200,300)
    									 .map(it ->it+"!!")
    									 .collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * }
     * </pre>
     * @param values to prependAll
     * @return Streamable with values prepended
     */
    default Streamable<T> prependAll(final T... values) {
        return fromStream(this.stream().prependAll(values));
    }
  default Streamable<T> prependAll(final Iterable<? extends T> value) {
    return fromStream(this.stream().prependAll(value));
  }

    /**
     * Insert data into a stream at given position
     * <pre>
     * {@code
     * List<String> result = 	Streamable.of(1,2,3)
     * 									 .insertAt(1,100,200,300)
    									 .map(it ->it+"!!")
    									 .collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
     *
     * }
     * </pre>
     * @param pos to insert data at
     * @param values to insert
     * @return Stream with new data inserted
     */
    default Streamable<T> insertAt(final int pos, final T... values) {
        return fromStream(this.stream().insertAt(pos, values));
    }

    /**
     * Delete elements between given indexes in a Stream
     * <pre>
     * {@code
     * List<String> result = 	Streamable.of(1,2,3,4,5,6)
     * 									 .deleteBetween(2,4)
    									 .map(it ->it+"!!")
    									 .collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("1!!","2!!","5!!","6!!")));
     * }
     * </pre>
     * @param start index
     * @param end index
     * @return Stream with elements removed
     */
    default Streamable<T> deleteBetween(final int start, final int end) {
        return fromStream(this.stream().deleteBetween(start, end));
    }

    /**
     * Insert a Stream into the middle of this stream at the specified position
     * <pre>
     * {@code
     * List<String> result = 	Streamable.of(1,2,3)
     * 									 .insertAt(1,of(100,200,300))
    									 .map(it ->it+"!!")
    									 .collect(CyclopsCollectors.toList());

    		assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
     * }
     * </pre>
     * @param pos to insert Stream at
     * @param stream to insert
     * @return newly conjoined Streamable
     */
    default Streamable<T> insertStreamableAt(final int pos, final Streamable<T> stream) {
        return fromStream(this.stream().insertStreamAt(pos, stream.stream()));
    }


    /**
     * <pre>
     * {@code
     *  assertTrue(Streamable.of(1,2,3,4,5,6)
    			.endsWith(Arrays.asList(5,6)));
     *
     * }
     * </pre>
     *
     * @param iterable Values to check
     * @return true if Streamable ends with values in the supplied iterable
     */
    @Override
    default boolean endsWith(final Iterable<T> iterable) {
        return this.stream().endsWith(iterable);
    }

    /**
     * <pre>
     * {@code
     * assertTrue(Streamable.of(1,2,3,4,5,6)
    			.endsWith(Stream.of(5,6)));
     * }
     * </pre>
     *
     * @param stream Values to check
     * @return true if Streamable endswith values in the supplied Stream
     */
    default boolean endsWith(final Streamable<T> stream) {
        return this.stream().endsWith(stream);
    }

    /**
     * Skip all elements until specified time period has passed
     * <pre>
     * {@code
     * List<Integer> result = Streamable.of(1,2,3,4,5,6)
    									.peek(i->sleep(i*100))
    									.skip(1000,TimeUnit.MILLISECONDS)
    									.toList();


    	//[4,5,6]
     *
     * }
     * </pre>
     *
     * @param time Length of time
     * @param unit Time unit
     * @return Streamable that skips all elements until time period has elapsed
     */
    default Streamable<T> skip(final long time, final TimeUnit unit) {
        return fromStream(this.stream().skip(time, unit));
    }

    /**
     * Return all elements until specified time period has elapsed
     * <pre>
     * {@code
     * List<Integer> result = Streamable.of(1,2,3,4,5,6)
    									.peek(i->sleep(i*100))
    									.limit(1000,TimeUnit.MILLISECONDS)
    									.toList();


    	//[1,2,3,4]
     * }
     * </pre>
     * @param time Length of time
     * @param unit Time unit
     * @return Streamable that returns all elements until time period has elapsed
     */
    default Streamable<T> limit(final long time, final TimeUnit unit) {
        return fromStream(this.stream().limit(time, unit));
    }

    /**
     * assertThat(Streamable.of(1,2,3,4,5)
    						.skipLast(2)
    						.collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(1,2,3)));
     *
     * @param num
     * @return
     */
    @Override
    default Streamable<T> skipLast(final int num) {
        return fromStream(this.stream().skipLast(num));
    }

    /**
     * Limit results to the last x elements in a Streamable
     * <pre>
     * {@code
     * 	assertThat(Streamable.of(1,2,3,4,5)
    						.limitLast(2)
    						.collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(4,5)));
     *
     * }
     * </pre>
     *
     * @param num of elements to return (last elements)
     * @return Streamable limited to last num elements
     */
    @Override
    default Streamable<T> limitLast(final int num) {
        return fromStream(this.stream().limitLast(num));
    }

    /**
     * Turns this Streamable into a HotStream, a connectable Stream, being executed on a thread on the
     * supplied executor, that is producing data
     * <pre>
     * {@code
     *  HotStream<Integer> ints = Streamable.range(0,Integer.MAX_VALUE)
    										.hotStream(exec)


    	ints.connect().forEach(System.out::println);
     *  //print out all the ints
     *  //multiple consumers are possible, so other Streams can connect on different Threads
     *
     * }
     * </pre>
     * @param e Executor to execute this Streamable on
     * @return a Connectable HotStream
     */
    default HotStream<T> hotStream(final Executor e) {
        return this.stream().hotStream(e);
    }

    /**
     * <pre>
     * {@code
     * 	assertThat(Streamable.of(1,2,3,4)
    				.map(u->{throw new RuntimeException();})
    				.recover(e->"hello")
    				.firstValue(),equalTo("hello"));
     * }
     * </pre>
     * @return first value in this Stream
     * @param alt
     */
    @Override
    default T firstValue(T alt) {
        return this.stream().firstValue(null);
    }

    /**
     * <pre>
     * {@code
     * assertThat(Streamable.of(1).single(),equalTo(1));
     * }
     * </pre>
     *
     * @return a single value or an exception if 0/1 values in this Stream
     * @param alt
     */
    @Override
    default T singleOrElse(T alt) {
        return this.stream().singleOrElse(alt);

    }

    /**
     * Return the elementAt index or Optional.empty
     * <pre>
     * {@code
     * 	assertThat(Streamable.of(1,2,3,4,5).elementAt(2).getValue(),equalTo(3));
     * }
     * </pre>
     * @param index to extract element from
     * @return elementAt index
     */
    @Override
    default Maybe<T> elementAt(final long index) {
        return this.stream().elementAt(index);
    }

    /**
     * Gets the element at index, and returns a Tuple containing the element (it must be present)
     * and a maybe copy of the Sequence for further processing.
     *
     * <pre>
     * {@code
     * Streamable.of(1,2,3,4,5).getValue(2)._1
     * //3
     * }
     * </pre>
     *
     * @param index to extract element from
     * @return Element and Sequence
     */
    default Tuple2<T, Streamable<T>> elementAtAndStream(final long index) {
        return this.stream().elementAtAndStream(index)
                            .map2(s -> fromStream(s));
    }

    /**
     * <pre>
     * {@code
     * Streamable.of(1,2,3,4,5)
    			 .elapsed()
    			 .forEach(System.out::println);
     * }
     * </pre>
     *
     * @return Sequence that adds the time between elements in millis to each element
     */
    default Streamable<Tuple2<T, Long>> elapsed() {
        return fromStream(this.stream().elapsed());
    }

    /**
     * <pre>
     * {@code
     *    Streamable.of(1,2,3,4,5)
    			   .timestamp()
    			   .forEach(System.out::println)
     *
     * }
     *
     * </pre>
     *
     * @return Sequence that adds a timestamp to each element
     */
    default Streamable<Tuple2<T, Long>> timestamp() {
        return fromStream(this.stream().timestamp());
    }

    /**
     * Construct a Reveresed Sequence from the provided elements
     * Can be reversed (again) efficiently
     * @param elements To Construct sequence from
     * @return
     */
    public static <T> Streamable<T> reversedOf(final T... elements) {
        return fromStream(ReactiveSeq.reversedOf(elements));

    }

    /**
     * Construct a Reveresed Sequence from the provided elements
     * Can be reversed (again) efficiently
     * @param elements To Construct sequence from
     * @return
     */
    public static <T> Streamable<T> reversedListOf(final List<T> elements) {
        Objects.requireNonNull(elements);
        return fromStream(ReactiveSeq.reversedListOf(elements));

    }

    /**
     * Create an efficiently reversable Sequence that produces the integers between skip
     * and take
     * @param start Number of range to skip from
     * @param end Number for range to take at
     * @return Range Streamable
     */
    public static Streamable<Integer> range(final int start, final int end) {
        return fromStream(ReactiveSeq.range(start, end));

    }

    /**
     * Create an efficiently reversable Sequence that produces the integers between skip
     * and take
     * @param start Number of range to skip from
     * @param end Number for range to take at
     * @return Range Streamable
     */
    public static Streamable<Long> rangeLong(final long start, final long end) {
        return fromStream(ReactiveSeq.rangeLong(start, end));

    }

    /**
     * Construct a Sequence from a Stream
     * @param stream Stream to construct Sequence from
     * @return
     */
    public static Streamable<Integer> fromIntStream(final IntStream stream) {
        Objects.requireNonNull(stream);
        return fromStream(ReactiveSeq.fromIntStream(stream));
    }

    /**
     * Construct a Sequence from a Stream
     *
     * @param stream Stream to construct Sequence from
     * @return
     */
    public static Streamable<Long> fromLongStream(final LongStream stream) {
        Objects.requireNonNull(stream);
        return fromStream(ReactiveSeq.fromLongStream(stream));
    }

    /**
     * Construct a Sequence from a Stream
     * @param stream Stream to construct Sequence from
     * @return
     */
    public static Streamable<Double> fromDoubleStream(final DoubleStream stream) {
        Objects.requireNonNull(stream);
        return fromStream(ReactiveSeq.fromDoubleStream(stream));
    }

    public static <T> Streamable<T> fromList(final List<T> list) {
        Objects.requireNonNull(list);
        return Streamable.fromIterable(list);
    }

    /**
     * @see Stream#iterate(Object, UnaryOperator)
     */
    static <T> Streamable<T> iterate(final T seed, final UnaryOperator<T> f) {
        Objects.requireNonNull(f);
        return fromStream(ReactiveSeq.iterate(seed, f));
    }

    /**
     * @see Stream#generate(Supplier)
     */
    static <T> Streamable<T> generate(final Supplier<T> s) {
        Objects.requireNonNull(s);
        return fromStream(ReactiveSeq.generate(s));
    }

    /**
     * Unzip a zipped Stream
     *
     * <pre>
     * {@code
     *  unzip(Streamable.of(new Tuple2(1, "a"), new Tuple2(2, "b"), new Tuple2(3, "c")))
     *
     *  // Streamable[1,2,3], Streamable[a,b,c]
     * }
     *
     * </pre>
     *
     */
    public static <T, U> Tuple2<Streamable<T>, Streamable<U>> unzip(final Streamable<Tuple2<T, U>> sequence) {
        return ReactiveSeq.unzip(sequence.stream())
                          .map1(s -> fromStream(s))
                          .map2(s -> fromStream(s));
    }

    /**
     * Unzip a zipped Stream into 3
     * <pre>
     * {@code
     *    unzip3(Streamable.of(new Tuple3(1, "a", 2l), new Tuple3(2, "b", 3l), new Tuple3(3,"c", 4l)))
     * }
     * // Streamable[1,2,3], Streamable[a,b,c], Streamable[2l,3l,4l]
     * </pre>
     */
    public static <T1, T2, T3> Tuple3<Streamable<T1>, Streamable<T2>, Streamable<T3>> unzip3(final Streamable<Tuple3<T1, T2, T3>> sequence) {
        return ReactiveSeq.unzip3(sequence.stream())
                          .map1(s -> fromStream(s))
                          .map2(s -> fromStream(s))
                          .map3(s -> fromStream(s));
    }

    /**
     * Unzip a zipped Stream into 4
     *
     * <pre>
     * {@code
     * unzip4(Streamable.of(new Tuple4(1, "a", 2l,'µ'), new Tuple4(2, "b", 3l,'y'), new Tuple4(3,
    					"c", 4l,'x')));
    	}
    	// Streamable[1,2,3], Streamable[a,b,c], Streamable[2l,3l,4l], Streamable[µ,y,x]
     * </pre>
     */
    public static <T1, T2, T3, T4> Tuple4<Streamable<T1>, Streamable<T2>, Streamable<T3>, Streamable<T4>> unzip4(
            final Streamable<Tuple4<T1, T2, T3, T4>> sequence) {
        return ReactiveSeq.unzip4(sequence.stream())
                          .map1(s -> fromStream(s))
                          .map2(s -> fromStream(s))
                          .map3(s -> fromStream(s))
                          .map4(s -> fromStream(s));
    }





    /** If this Streamable is empty one it with a another Stream
     *
     * <pre>
     * {@code
     * assertThat(Streamable.of(4,5,6)
    						.onEmptySwitch(()->Streamable.of(1,2,3))
    						.toList(),
    						equalTo(Arrays.asList(4,5,6)));
     * }
     * </pre>
     * @param switchTo Supplier that will generate the alternative Stream
     * @return Streamable that will switch to an alternative Stream if empty
     */
    default Streamable<T> onEmptySwitch(final Supplier<Streamable<T>> switchTo) {
        return fromStream(this.stream().onEmptySwitch(() -> switchTo.get().stream()));
    }

    @Override
    default Streamable<T> onEmpty(final T value) {
        return fromStream(this.stream().onEmpty(value));
    }

    @Override
    default Streamable<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return fromStream(this.stream().onEmptyGet(supplier));
    }


    default <X extends Throwable> Streamable<T> onEmptyError(final Supplier<? extends X> supplier) {
        return fromStream(this.stream().onEmptyError(supplier));
    }

    default Streamable<T> concat(final Streamable<T> other) {
        return fromStream(this.stream().appendAll(other));
    }

    default Streamable<T> concat(final T other) {
        return fromStream(this.stream().append(other));
    }

    default Streamable<T> concat(final T... other) {
        return fromStream(this.stream().appendAll(other));
    }

    default <U> Streamable<T> distinct(final Function<? super T, ? extends U> keyExtractor) {
        return fromStream(this.stream().distinct(keyExtractor));
    }

    @Override
    default Streamable<T> shuffle(final Random random) {
        return fromStream(this.stream().shuffle(random));

    }

    @Override
    default Streamable<T> slice(final long from, final long to) {
        return fromStream(this.stream().slice(from, to));

    }

    @Override
    default <U extends Comparable<? super U>> Streamable<T> sorted(final Function<? super T, ? extends U> function) {
        return fromStream(this.stream().sorted(function));

    }

    /**
     * emit x elements per time period
     *
     * <pre>
     * {@code
     *  SimpleTimer timer = new SimpleTimer();
    	assertThat(Streamable.of(1,2,3,4,5,6)
    	                    .xPer(6,100000000,TimeUnit.NANOSECONDS)
    	                    .collect(CyclopsCollectors.toList()).size(),is(6));

     * }
     * </pre>
     * @param x number of elements to emit
     * @param time period
     * @param t Time unit
     * @return Streamable that emits x elements per time period
     */
    default ReactiveSeq<T> xPer(final int x, final long time, final TimeUnit t) {
        return this.stream().xPer(x, time, t);

    }

    /**
     * emit one element per time period
     * <pre>
     * {@code
     * Streamable.iterate("", last -> "next")
    			.limit(100)
    			.batchBySize(10)
    			.onePer(1, TimeUnit.MICROSECONDS)
    			.peek(batch -> System.out.println("batched : " + batch))
    			.flatMap(Collection::stream)
    			.peek(individual -> System.out.println("Flattened : "
    					+ individual))
    			.forEach(a->{});
     * }
     * </pre>
     * @param time period
     * @param t Time unit
     * @return Streamable that emits 1 element per time period
     */
    default ReactiveSeq<T> onePer(final long time, final TimeUnit t) {
        return this.stream().onePer(time, t);

    }

    /**
     * Allow one element through per time period, drop all other
     * elements in that time period
     *
     * <pre>
     * {@code
     * Streamable.of(1,2,3,4,5,6)
     *          .debounce(1000,TimeUnit.SECONDS).toList();
     *
     * // 1
     * }</pre>
     *
     * @param time
     * @param t
     * @return
     */
    default Streamable<T> debounce(final long time, final TimeUnit t) {
        return fromStream(this.stream().debounce(time, t));
    }

    /**
     * Batch elements by size into a List
     *
     * <pre>
     * {@code
     * Streamable.of(1,2,3,4,5,6)
    			.batchBySizeAndTime(3,10,TimeUnit.SECONDS)
    			.toList();

     * //[[1,2,3],[4,5,6]]
     * }
     * </pre>
     *
     * @param size Max size of a batch
     * @param time (Max) time period to build a single batch in
     * @param t time unit for batch
     * @return Streamable batched by size and time
     */
    default Streamable<Vector<T>> groupedBySizeAndTime(final int size, final long time, final TimeUnit t) {
        return fromStream(this.stream().groupedBySizeAndTime(size, time, t));
    }

    /**
     *  Batch elements by size into a toX created by the supplied factory
     * <pre>
     * {@code
     * List<Vector<Integer>> list = of(1,2,3,4,5,6)
    				.batchBySizeAndTime(10,1,TimeUnit.MICROSECONDS,()->Vector.empty())
    				.toList();
     * }
     * </pre>
     * @param size Max size of a batch
     * @param time (Max) time period to build a single batch in
     * @param unit time unit for batch
     * @param factory Collection factory
     * @return Streamable batched by size and time
     */
    default <C extends PersistentCollection<? super T>> Streamable<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
                                                                                                      final Supplier<C> factory) {
        return fromStream(this.stream().groupedBySizeAndTime(size, time, unit, factory));
    }

    /**
     * Batch elements in a Stream by time period
     *
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.SECONDS).collect(CyclopsCollectors.toList()).size(),is(1));
     * assertThat(Streamable.of(1,2,3,4,5,6).batchByTime(1,TimeUnit.NANOSECONDS).collect(CyclopsCollectors.toList()).size(),greaterThan(5));
     * }
     * </pre>
     *
     * @param time - time period to build a single batch in
     * @param t  time unit for batch
     * @return Streamable batched into lists by time period
     */
    default Streamable<Vector<T>> groupedByTime(final long time, final TimeUnit t) {
        return fromStream(this.stream().groupedByTime(time, t));
    }

    /**
     * Batch elements by time into a toX created by the supplied factory
     *
     * <pre>
     * {@code
     *   assertThat(Streamable.of(1,1,1,1,1,1)
     *                       .batchByTime(1500,TimeUnit.MICROSECONDS,()->TreeSet.empty())
     *                       .toList()
     *                       .getValue(0)
     *                       .size(),is(1));
     * }
     * </pre>
     *
     * @param time - time period to build a single batch in
     * @param unit time unit for batch
     * @param factory Collection factory
     * @return Streamable batched into toX types by time period
     */
    default <C extends PersistentCollection<? super T>> Streamable<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return fromStream(this.stream().groupedByTime(time, unit, factory));
    }

    /**
     * Batch elements in a Stream by size into a toX created by the supplied factory
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,1,1,1,1,1)
     * 						.batchBySize(3,()->new TreeSet<>())
     * 						.toList()
     * 						.getValue(0)
     * 						.size(),is(1));
     * }
     * </pre>
     *
     * @param size batch size
     * @param supplier Collection factory
     * @return Streamable batched into toX types by size
     */
    @Override
    default <C extends PersistentCollection<? super T>> Streamable<C> grouped(final int size, final Supplier<C> supplier) {
        return fromStream(this.stream().grouped(size, supplier));
    }

    /**
     * emit elements after a fixed delay
     * <pre>
     * {@code
     * 	SimpleTimer timer = new SimpleTimer();
    	assertThat(Streamable.of(1,2,3,4,5,6)
    						.fixedDelay(10000,TimeUnit.NANOSECONDS)
    						.collect(CyclopsCollectors.toList())
    						.size(),is(6));
    	assertThat(timer.getElapsedNanoseconds(),greaterThan(60000l));
     * }
     * </pre>
     * @param l time length in nanos of the delay
     * @param unit for the delay
     * @return Streamable that emits each element after a fixed delay
     */
    default ReactiveSeq<T> fixedDelay(final long l, final TimeUnit unit) {
        return this.stream().fixedDelay(l, unit);
    }

    /**
     * Introduce a random jitter / time delay between the emission of elements
     * <pre>
     * {@code
     * SimpleTimer timer = new SimpleTimer();
    	assertThat(Streamable.of(1,2,3,4,5,6)
    						.jitter(10000)
    						.collect(CyclopsCollectors.toList())
    						.size(),is(6));
    	assertThat(timer.getElapsedNanoseconds(),greaterThan(20000l));
     * }
     * </pre>
     * @param maxJitterPeriodInNanos - random number less than this is used for each jitter
     * @return Sequence with a random jitter between element emission
     */
    default Streamable<T> jitter(final long maxJitterPeriodInNanos) {
        return fromStream(this.stream().jitter(maxJitterPeriodInNanos));
    }

    /**
     * Create a Streamable batched by List, where each batch is populated until the predicate holds
     * <pre>
     * {@code
     *  assertThat(Streamable.of(1,2,3,4,5,6)
    			.batchUntil(i->i%3==0)
    			.toList()
    			.size(),equalTo(2));
     * }
     * </pre>
     * @param predicate Batch until predicate holds, transform open next batch
     * @return Streamable batched into lists determined by the predicate supplied
     */

    @Override
    default IterableX<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {
        return fromStream(this.stream().groupedUntil(predicate));
    }

    /**
     * Create a Streamable batched by List, where each batch is populated while the predicate holds
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,2,3,4,5,6)
    			.batchWhile(i->i%3!=0)
    			.toList().size(),equalTo(2));

     * }
     * </pre>
     * @param predicate Batch while predicate holds, transform open next batch
     * @return Streamable batched into lists determined by the predicate supplied
     */
    @Override
    default Streamable<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {
        return fromStream(this.stream().groupedWhile(predicate));
    }

    /**
     * Create a Streamable batched by a Collection, where each batch is populated while the predicate holds
     *
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,2,3,4,5,6)
    			.batchWhile(i->i%3!=0)
    			.toList()
    			.size(),equalTo(2));
     * }
     * </pre>
     * @param predicate Batch while predicate holds, transform open next batch
     * @param factory Collection factory
     * @return Streamable batched into collections determined by the predicate supplied
     */
    @Override
    default <C extends PersistentCollection<? super T>> Streamable<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return fromStream(this.stream().groupedWhile(predicate, factory));
    }

    /**
     * Create a Streamable batched by a Collection, where each batch is populated until the predicate holds
     *
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,2,3,4,5,6)
    			.batchUntil(i->i%3!=0)
    			.toList()
    			.size(),equalTo(2));
     * }
     * </pre>
     *
     *
     * @param predicate Batch until predicate holds, transform open next batch
     * @param factory Collection factory
     * @return Streamable batched into collections determined by the predicate supplied
     */
    @Override
    default <C extends PersistentCollection<? super T>> Streamable<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return fromStream(this.stream().groupedUntil(predicate, factory));

    }

    /**
     * Recover from an exception with an alternative value
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,2,3,4)
    					   .map(i->i+2)
    					   .map(u->{throw new RuntimeException();})
    					   .recover(e->"hello")
    					   .firstValue(),equalTo("hello"));
     * }
     * </pre>
     * @param fn Function that accepts a Throwable and returns an alternative value
     * @return Streamable that can recover from an Exception
     */
    default Streamable<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return fromStream(this.stream().recover(fn));
    }

    /**
     * Recover from a particular exception type
     *
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,2,3,4)
    				.map(i->i+2)
    				.map(u->{ExceptionSoftener.throwSoftenedException( new IOException()); return null;})
    				.recover(IOException.class,e->"hello")
    				.firstValue(),equalTo("hello"));
     *
     * }
     * </pre>
     *
     * @param exceptionClass Type to recover from
     * @param fn That accepts an error and returns an alternative value
     * @return Streamable that can recover from a particular exception
     */
    default <EX extends Throwable> Streamable<T> recover(final Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return fromStream(this.stream().recover(exceptionClass, fn));

    }



    /**
     * True if a streamable contains element t
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,2,3,4,5).contains(3),equalTo(true));
     * }
     * </pre>
     * @param t element to check for
     */
    default boolean containsValue(final T t) {
        return stream().anyMatch(c -> t.equals(c));
    }



    @Override
    default boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    default Iterator<T> iterator() {
        return this.getStreamable().iterator();
    }

}

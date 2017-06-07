package cyclops.stream;

import com.aol.cyclops2.internal.stream.SeqUtils;
import com.aol.cyclops2.internal.stream.StreamableImpl;
import com.aol.cyclops2.types.*;
import com.aol.cyclops2.types.factory.Unit;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import com.aol.cyclops2.types.foldable.CyclopsCollectable;
import com.aol.cyclops2.types.stream.HotStream;
import com.aol.cyclops2.types.stream.ToStream;
import com.aol.cyclops2.types.traversable.FoldableTraversable;
import com.aol.cyclops2.types.traversable.Traversable;
import cyclops.collections.immutable.VectorX;
import cyclops.companion.Streams;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.MapX;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import lombok.AllArgsConstructor;
import lombok.val;
import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
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
public interface Streamable<T> extends To<Streamable<T>>,
                                        ToStream<T>,
                                        FoldableTraversable<T>,
                                        CyclopsCollectable<T>,
        Transformable<T>,
                                        Filters<T>,
                                        Traversable<T>,
        Unit<T>,
                                        Zippable<T> {

    public static <T> Streamable<T> fromObject(final Object toCoerce) {
        return new StreamableImpl(
                                  Impl.collectStream(toCoerce));
    }

    @Override
    default ReactiveSeq<T> reactiveSeq() {
        return Streams.oneShotStream(StreamSupport.stream(this.spliterator(),false));
    }

    /**
     * (Lazily) Construct a Streamable from a Stream.
     * 
     * @param stream toNested construct Streamable from
     * @return Streamable
     */
    public static <T> Streamable<T> fromStream(final Stream<T> stream) {
        return new StreamableImpl(
                                  Impl.collectStream(stream));
    }

    /**
     * (Lazily) Construct a Streamable from an Iterable.
     * 
     * @param iterable toNested construct Streamable from
     * @return Streamable
     */
    public static <T> Streamable<T> fromIterable(final Iterable<T> iterable) {
        if(iterable instanceof Streamable)
            return (Streamable<T>)iterable;
        return new StreamableImpl(
                                  Impl.collectStream(iterable));
    }

    /**
     * @param toCoerce Efficiently / lazily Makes Stream repeatable, guards iteration with locks on initial iteration
     * @return
     */
    public static <T> Streamable<T> synchronizedFromStream(final Stream<T> toCoerce) {
        return new StreamableImpl(
                                  Impl.collectStreamConcurrent(toCoerce));
    }

    public static <T> Streamable<T> synchronizedFromIterable(final Iterable<T> toCoerce) {
        return new StreamableImpl(
                                  Impl.collectStreamConcurrent(toCoerce));
    }

    static class Impl {

        private static <T> Iterable<T> collectStreamConcurrent(final T object) {
            if (object instanceof Stream) {

                final Collection c = SeqUtils.toConcurrentLazyCollection((Stream) object);
                return new PrintableIterable<T>(
                                                c);
            }
            if (object instanceof Object[]) {
                return (Iterable<T>) Arrays.asList((Object[]) object);
            }
            if (object instanceof Iterable)
                return (Iterable<T>) object;

            return Arrays.asList(object);
        }

        private static <T> Iterable<T> collectStream(final T object) {
            if (object instanceof Stream) {

                final Collection c = SeqUtils.toLazyCollection((Stream) object);
                return new PrintableIterable<T>(
                                                c);
            }
            if (object instanceof Object[]) {
                return (Iterable<T>) Arrays.asList((Object[]) object);
            }
            if (object instanceof Iterable)
                return (Iterable<T>) object;

            return Arrays.asList(object);
        }
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
     * @see com.aol.cyclops2.types.reactiveStream.CyclopsCollectable#collectors()
     */
    @Override
    default Collectable<T> collectors() {

        return Seq.seq((Stream<T>)stream());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default Streamable<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return Streamable.fromIterable(FoldableTraversable.super.combine(predicate, op));
    }
    @Override
    default Streamable<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (Streamable<T>)FoldableTraversable.super.combine(op,predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> Streamable<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return Streamable.fromIterable(FoldableTraversable.super.zip(other, zipper));
    }



    @Override
    default <U, R> Streamable<R> zipS(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return Streamable.fromIterable(FoldableTraversable.super.zipS(other, zipper));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zip(java.util.reactiveStream.Stream)
     */
    @Override
    default <U> Streamable<Tuple2<T, U>> zipS(final Stream<? extends U> other) {

        return Streamable.fromIterable(FoldableTraversable.super.zipS(other));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zip3(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <S, U> Streamable<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return Streamable.fromIterable(FoldableTraversable.super.zip3(second, third));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#zip4(java.util.reactiveStream.Stream, java.util.reactiveStream.Stream, java.util.reactiveStream.Stream)
     */
    @Override
    default <T2, T3, T4> Streamable<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return Streamable.fromIterable(FoldableTraversable.super.zip4(second, third, fourth));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    default Streamable<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return Streamable.fromIterable(FoldableTraversable.super.groupedStatefullyUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#grouped(java.util.function.Function, java.util.reactiveStream.Collector)
     */
    @Override
    default <K, A, D> Streamable<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {
        return Streamable.fromIterable(FoldableTraversable.super.grouped(classifier, downstream));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#grouped(java.util.function.Function)
     */
    @Override
    default <K> Streamable<Tuple2<K, ReactiveSeq<T>>> grouped(final Function<? super T, ? extends K> classifier) {

        return Streamable.fromIterable(FoldableTraversable.super.grouped(classifier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    default Streamable<T> takeWhile(final Predicate<? super T> p) {

        return (Streamable<T>) FoldableTraversable.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    default Streamable<T> dropWhile(final Predicate<? super T> p) {

        return (Streamable<T>) FoldableTraversable.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    default Streamable<T> takeUntil(final Predicate<? super T> p) {

        return (Streamable<T>) FoldableTraversable.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    default Streamable<T> dropUntil(final Predicate<? super T> p) {

        return (Streamable<T>) FoldableTraversable.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#dropRight(int)
     */
    @Override
    default Streamable<T> dropRight(final int num) {

        return (Streamable<T>) FoldableTraversable.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.traversable.Traversable#takeRight(int)
     */
    @Override
    default Streamable<T> takeRight(final int num) {

        return (Streamable<T>) FoldableTraversable.super.takeRight(num);
    }

    /**
     * Construct a FutureStream from an Publisher
     * 
     * @param publisher
     *            toNested construct ReactiveSeq from
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
     * @see com.aol.cyclops2.lambda.monads.IterableFunctor#unitIterable(java.util.Iterator)
     */
    @Override
    default <T> Streamable<T> unitIterator(final Iterator<T> it) {
        return Streamable.fromIterator(it);
    }

    /**
     * Construct a Streamable that returns a Stream
     * 
     * @param values toNested construct Streamable from
     * @return Streamable
     */
    public static <T> Streamable<T> of(final T... values) {
        final Iterable<T> it = Arrays.asList(values);
        return new Streamable<T>() {
            @Override
            public ReactiveSeq<T> stream() {
                return Streams.oneShotStream(Stream.of(values));
            }

            @Override
            public Iterable<T> getStreamable() {
                return it;
            }

            @Override
            public ReactiveSeq<T> reactiveSeq() {
                return Streams.oneShotStream(Stream.of(values));
            }

        };
    }

    public static <T> Streamable<T> empty() {
        return of();
    }

    /**
     * <pre>
     * {@code 
     *   Streamable.of(1,2,3,4,5).tail()
     *   
     *   //Streamable[2,3,4,5]
     * }</pre>
     * 
     * @return The tail of this Streamable
     */
    default Streamable<T> tail() {
        return Streamable.fromStream(reactiveSeq().headAndTail()
                                                  .tail());
    }

    /**
     * <pre>
     * {@code 
     * Streamable.of(1,2,3,4,5).head()
     *  
     *  //1
     * }</pre>
     * @return The head of this Streamable
     */
    default T head() {
        return reactiveSeq().headAndTail()
                            .head();
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
     * @param t Streamable toNested append
     * @return New Streamable with provided Streamable appended
     */
    default Streamable<T> appendAll(final Streamable<T> t) {
        return Streamable.fromStream(reactiveSeq().appendS(t.reactiveSeq()));
    }

    /**
     * Remove all occurances of the specified element from the Streamable
     * <pre>
     * {@code
     * 	Streamable.of(1,2,3,4,5,1,2,3).remove(1)
     * 
     *  //Streamable[2,3,4,5,2,3]
     * }
     * </pre>
     * 
     * @param t element toNested remove
     * @return Filtered Streamable
     */
    default Streamable<T> remove(final T t) {
        return Streamable.fromStream(reactiveSeq().remove(t));
    }

    /**
     * Prepend given values toNested the skip of the Stream
     * <pre>
     * {@code 
     * List<String> result = 	Streamable.of(1,2,3)
     * 									 .prepend(100,200,300)
    									 .map(it ->it+"!!")
    									 .collect(CyclopsCollectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * }
     * </pre>
     * @param t value toNested prepend
     * @return Streamable with values prepended
     */
    default Streamable<T> prepend(final T t) {
        return Streamable.fromStream(reactiveSeq().prepend(t));
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
        return Streamable.fromStream(reactiveSeq().distinct());
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
        return reactiveSeq().foldLeft(identity, function);
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
    * @return Single reduced value
    */
    @Override
    default <U> U foldRight(final U seed, final BiFunction<? super T, ? super U, ? extends U> function) {
        return reactiveSeq().foldRight(seed, function);
    }

    /**
     * Map the values in the Streamable from one set of values / types toNested another
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
        return Streamable.fromStream(reactiveSeq().map(fn));
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
     * @param fn Consumer toNested peek with
     * @return Streamable that will peek at values as they pass through
     */
    @Override
    default Streamable<T> peek(final Consumer<? super T> fn) {
        return Streamable.fromStream(reactiveSeq().peek(fn));
    }

    /* (non-Javadoc)
     * @see java.util.reactiveStream.Stream#filtered(java.util.function.Predicate)
     */
    @Override
    default Streamable<T> filter(final Predicate<? super T> fn) {
        return Streamable.fromStream(reactiveSeq().filter(fn));
    }

    /* (non-Javadoc)
     * @see java.util.reactiveStream.Stream#flatMap(java.util.function.Function)
     */
    default <R> Streamable<R> flatMap(final Function<? super T, Streamable<? extends R>> fn) {
        return Streamable.fromStream(reactiveSeq().flatMap(i -> fn.apply(i)
                                                                  .reactiveSeq()));
    }
    /**
     * coflatMap pattern, can be used toNested perform maybe reductions / collections / folds and other terminal operations
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
        return Streamable.fromStream(reactiveSeq().coflatMap(i -> fn.apply(Streamable.fromStream(i))));

    }
    /**
     * @return number of elements in this Streamable
     */
    @Override
    default long count() {
        return reactiveSeq().count();
    }

    /* (non-Javadoc)
     * @see java.util.reactiveStream.Stream#forEachOrdered(java.util.function.Consumer)
     */
    default void forEachOrdered(final Consumer<? super T> action) {
        reactiveSeq().forEachOrdered(action);
    }

    /* (non-Javadoc)
     * @see java.util.reactiveStream.Stream#toArray()
     */
    default Object[] toArray() {
        return reactiveSeq().toArray();
    }

    /* (non-Javadoc)
     * @see java.util.reactiveStream.Stream#toArray(java.util.function.IntFunction)
     */
    default <A> A[] toArray(final IntFunction<A[]> generator) {
        return reactiveSeq().toArray(generator);
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
     * @return Streamable converted toNested a List
     */
    default List<T> toList() {

        if (getStreamable() instanceof List)
            return ListX.fromIterable((List) getStreamable());
        return reactiveSeq().toList();
    }

    default <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super T> accumulator, final BiConsumer<R, R> combiner) {
        return reactiveSeq().collect(supplier, accumulator, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.reactiveStream.CyclopsCollectable#collect(java.util.reactiveStream.Collector)
     */
    @Override
    default <R, A> R collect(final Collector<? super T, A, R> collector) {

        return reactiveSeq().collect(collector);
    }

  

    /**
     * Generate the permutations based on values in the Streamable
     * @return  Streamable containing the permutations in this Streamable
    
     * 
     */
    default Streamable<ReactiveSeq<T>> permutations() {
        if (isEmpty()) {
            return Streamable.empty();
        } else {
            final Streamable<T> tail = tail();
            if (tail.isEmpty()) {
                return Streamable.of(this.stream());
            } else {
                final Streamable<ReactiveSeq<T>> zero = Streamable.empty();
                return distinct().foldLeft(zero, (xs, x) -> {
                    final Function<ReactiveSeq<T>, ReactiveSeq<T>> prepend = l -> l.prepend(x);
                    return xs.appendAll(remove(x).permutations()
                                                 .map(prepend));
                });
            }
        }
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
        return Streamable.fromStream(reactiveSeq().subStream(start, end));
    }

    /**
     * Gets the element at index (it must be present)
     * 
     * <pre>
     * {@code 
     * Streamable.of(1,2,3,4,5).get(2)
     * //3
     * }
     * </pre>
     * 
     * @param index toNested extract element from
     * @return Element and Sequence
     */
    default T elementAt(final int index) {
        return reactiveSeq().elementAt(index).v1;
    }

    /**
     * [equivalent toNested count]
     * 
     * @return size
     */
    default int size() {
        return reactiveSeq().size();
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
     * @return All combinations of the elements in this reactiveStream of the specified size
     */
    default Streamable<ReactiveSeq<T>> combinations(final int size) {



        if (size == 0) {
            return Streamable.of(ReactiveSeq.empty());
        } else {
           val combs = IntStream.range(0, size())
                                                  .boxed()
                    .<ReactiveSeq<T>> flatMap(i -> subStream(i + 1, size()).combinations(size - 1)
                                                                      .map(t -> t.prepend(elementAt(i))).reactiveSeq());
            return Streamable.fromStream(combs);
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
     * @return All combinations of the elements in this reactiveStream
     */
    default Streamable<ReactiveSeq<T>> combinations() {
        return range(0, size() + 1).map(this::combinations)
                                   .flatMap(s -> s);

    }

    /**
     * join / flatten one level of a nested hierarchy
     * 
     * <pre>
     * {@code 
     *  Streamable.of(Arrays.asList(1,2)).flatten();
     *  
     *  //reactiveStream of (1,  2);
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
     * Optional<List<String>> reactiveStream = Streamable.of("hello","world")
    											.optional();
    											
    	assertThat(reactiveStream.get(),equalTo(Arrays.asList("hello","world")));
     * }
     * 
     * </pre>
     * @return this Streamable converted toNested an Optional List

    @Override
    default Optional<ListX<T>> optional() {
        return reactiveSeq().optional();
    }
     */

    /**
     * Convert toNested a Stream with the values repeated specified times
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
        return Streamable.fromStream(reactiveSeq().cycle(times));
    }

    /**
     * Convert toNested a Stream with the values infinitely cycled
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
        return Streamable.fromStream(reactiveSeq().cycle());
    }

    /**
     * Duplicate a Stream, buffers intermediate values, leaders may change positions so a limit
     * can be safely applied toNested the leading reactiveStream. Not thread-safe.
     * <pre>
     * {@code 
     *  Tuple2<Streamable<Integer>, Streamable<Integer>> copies =of(1,2,3,4,5,6).duplicate();
    	 assertTrue(copies.v1.anyMatch(i->i==2));
    	 assertTrue(copies.v2.anyMatch(i->i==2));
     * 
     * }
     * </pre>
     * 
     * @return duplicated reactiveStream
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

        return reactiveSeq().splitAt(where)
                            .map1(s -> fromStream(s))
                            .map2(s -> fromStream(s));
    }

    default Tuple2<Optional<T>, Streamable<T>> splitAtHead(){
        return reactiveSeq().splitAtHead().map2(s->s.to().streamable());
    }

    /**
     * Split this Streamable after the takeOne element (if present)
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
                            .map2(s -> fromStream(s));
    }*/

    /**
     * Split reactiveStream at point where predicate no longer holds
     * <pre>
     * {@code
     *   Streamable.of(1, 2, 3, 4, 5, 6).splitBy(i->i<4)
     *   
     *   //Streamable[1,2,3] Streamable[4,5,6]
     * }
     * </pre>
     */
    default Tuple2<Streamable<T>, Streamable<T>> splitBy(final Predicate<T> splitter) {
        return reactiveSeq().splitBy(splitter)
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
        return reactiveSeq().partition(splitter)
                            .map1(s -> fromStream(s))
                            .map2(s -> fromStream(s));
    }

    /**
     * Convert toNested a Stream with the result of a reduction operation repeated
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
     *            Monoid toNested be used in reduction
     * @param times
     *            Number of times value should be repeated
     * @return Stream with reduced values repeated
     */
    @Override
    default Streamable<T> cycle(final Monoid<T> m, final long times) {
        return fromStream(reactiveSeq().cycle(m, times));
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
        return Streamable.fromStream(reactiveSeq().cycleWhile(predicate));
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
        return Streamable.fromStream(reactiveSeq().cycleUntil(predicate));
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
        return fromStream(reactiveSeq().zip(other));
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
        return fromStream(reactiveSeq().zip3(second.reactiveSeq(), third.reactiveSeq()));
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
        return fromStream(reactiveSeq().zip4(second.reactiveSeq(), third.reactiveSeq(), fourth.reactiveSeq()));
    }

    /** 
     * Add an index toNested the current Stream
     * 
     * <pre>
     * {@code 
     * assertEquals(asList(new Tuple2("a", 0L), new Tuple2("b", 1L)), of("a", "b").zipWithIndex().toList());
     * }
     * </pre>
     */
    @Override
    default Streamable<Tuple2<T, Long>> zipWithIndex() {
        return fromStream(reactiveSeq().zipWithIndex());
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
    	
    
    	assertThat(list.get(0),hasItems(1,2));
    	assertThat(list.get(1),hasItems(2,3));
     * 
     * }
     * 
     * </pre>
     * @param windowSize
     *            Size of sliding window
     * @return Streamable with sliding view
     */
    @Override
    default Streamable<VectorX<T>> sliding(final int windowSize) {
        return fromStream(reactiveSeq().sliding(windowSize));
    }

    /**
     *  Create a sliding view over this Sequence
     * <pre>
     * {@code 
     * List<List<Integer>> list = fromEither5(Stream.of(1,2,3,4,5,6))
    								.asSequence()
    								.sliding(3,2)
    								.collect(CyclopsCollectors.toList());
    	
    
    	assertThat(list.get(0),hasItems(1,2,3));
    	assertThat(list.get(1),hasItems(3,4,5));
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
    default Streamable<VectorX<T>> sliding(final int windowSize, final int increment) {
        return fromStream(reactiveSeq().sliding(windowSize, increment));
    }

    /**
     * Group elements in a Stream
     * 
     * <pre>
     * {@code
     *  List<List<Integer>> list = monad(Stream.of(1, 2, 3, 4, 5, 6)).grouped(3)
     *          .collect(CyclopsCollectors.toList());
     * 
     *  assertThat(list.get(0), hasItems(1, 2, 3));
     *  assertThat(list.get(1), hasItems(4, 5, 6));
     * 
     * }
     * </pre>
     * 
     * @param groupSize
     *            Size of each Group
     * @return Stream with elements grouped by size
     */
    @Override
    default Streamable<ListX<T>> grouped(final int groupSize) {
        return fromStream(reactiveSeq().grouped(groupSize));
    }

    /**
     * Use classifier function toNested group elements in this Sequence into a Map
     * <pre>
     * {@code 
     * Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
    	        assertEquals(asList(2, 4), map1.get(0));
    	        assertEquals(asList(1, 3), map1.get(1));
    	        assertEquals(2, map1.size());
     * 
     * }
     * 
     * </pre>
     */
    @Override
    default <K> MapX<K, ListX<T>> groupBy(final Function<? super T, ? extends K> classifier) {
        return reactiveSeq().groupBy(classifier);
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
        return fromStream(reactiveSeq().scanLeft(monoid));
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
        return fromStream(reactiveSeq().scanLeft(identity, function));
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
        return fromStream(reactiveSeq().scanRight(monoid));
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
        return fromStream(reactiveSeq().scanRight(identity, combiner));
    }

    /**
     * <pre>
     * {@code assertThat(Streamable.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
     * </pre>
     * 
     */
    @Override
    default Streamable<T> sorted() {
        return fromStream(reactiveSeq().sorted());
    }

    /**
     *<pre>
     * {@code 
     * 	assertThat(Streamable.of(4,3,6,7).sorted((a,b) -> b-a).toList(),equalTo(Arrays.asList(7,6,4,3)));
     * }
     * </pre>
     * @param c
     *            Compartor toNested sort with
     * @return Sorted Monad
     */
    @Override
    default Streamable<T> sorted(final Comparator<? super T> c) {
        return fromStream(reactiveSeq().sorted(c));
    }

    /**
     * <pre>
     * {@code assertThat(Streamable.of(4,3,6,7).skip(2).toList(),equalTo(Arrays.asList(6,7))); }
     * </pre>
     * 
    
     * 
     * @param num
     *            Number of elemenets toNested skip
     * @return Monad converted toNested Stream with specified number of elements
     *         skipped
     */
    @Override
    default Streamable<T> skip(final long num) {
        return fromStream(reactiveSeq().skip(num));
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
     *            Predicate toNested skip while true
     * @return Monad converted toNested Stream with elements skipped while predicate
     *         holds
     */
    @Override
    default Streamable<T> skipWhile(final Predicate<? super T> p) {
        return fromStream(reactiveSeq().skipWhile(p));
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
     *            Predicate toNested skip until true
     * @return Monad converted toNested Stream with elements skipped until predicate
     *         holds
     */
    @Override
    default Streamable<T> skipUntil(final Predicate<? super T> p) {
        return fromStream(reactiveSeq().skipUntil(p));
    }

    /**
     * 
     * 
     * <pre>
     * {@code assertThat(Streamable.of(4,3,6,7).limit(2).toList(),equalTo(Arrays.asList(4,3));}
     * </pre>
     * 
     * @param num
     *            Limit element size toNested num
     * @return Monad converted toNested Stream with elements up toNested num
     */
    @Override
    default Streamable<T> limit(final long num) {
        return fromStream(reactiveSeq().limit(num));
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
     * @return Monad converted toNested Stream with limited elements
     */
    @Override
    default Streamable<T> limitWhile(final Predicate<? super T> p) {
        return fromStream(reactiveSeq().limitWhile(p));
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
     * @return Monad converted toNested Stream with limited elements
     */
    @Override
    default Streamable<T> limitUntil(final Predicate<? super T> p) {
        return fromStream(reactiveSeq().limitUntil(p));

    }

    /**
     * True if predicate matches all elements when Monad converted toNested a Stream
     * <pre>
     * {@code 
     * assertThat(Streamable.of(1,2,3,4,5).allMatch(it-> it>0 && it <6),equalTo(true));
     * }
     * </pre>
     * @param c Predicate toNested check if all match
     */
    @Override
    default boolean allMatch(final Predicate<? super T> c) {
        return reactiveSeq().allMatch(c);
    }

    /**
     * True if a singleUnsafe element matches when Monad converted toNested a Stream
     * <pre>
     * {@code 
     * assertThat(Streamable.of(1,2,3,4,5).anyMatch(it-> it.equals(3)),equalTo(true));
     * }
     * </pre>
     * @param c Predicate toNested check if any match
     */
    @Override
    default boolean anyMatch(final Predicate<? super T> c) {
        return reactiveSeq().anyMatch(c);
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
        return reactiveSeq().xMatch(num, c);
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
        return reactiveSeq().noneMatch(c);
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
        return reactiveSeq().join();
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
        return reactiveSeq().join(sep);
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
        return reactiveSeq().join(sep, start, end);
    }

    /**
     * Extract the minimum as determined by supplied function
     * 
     */
    @Override
    default <C extends Comparable<? super C>> Optional<T> minBy(final Function<? super T, ? extends C> f) {
        return reactiveSeq().minBy(f);
    }

    /* (non-Javadoc)
     * @see java.util.reactiveStream.Stream#min(java.util.Comparator)
     */
    @Override
    default Optional<T> min(final Comparator<? super T> comparator) {
        return reactiveSeq().min(comparator);
    }

    /**
     * Extract the maximum as determined by the supplied function
     * 
     */
    @Override
    default <C extends Comparable<? super C>> Optional<T> maxBy(final Function<? super T, ? extends C> f) {
        return reactiveSeq().maxBy(f);
    }

    /* (non-Javadoc)
     * @see java.util.reactiveStream.Stream#max(java.util.Comparator)
     */
    @Override
    default Optional<T> max(final Comparator<? super T> comparator) {
        return reactiveSeq().max(comparator);
    }

    /**
     * @return First matching element in sequential order
     * <pre>
     * {@code
     * Streamable.of(1,2,3,4,5).filter(it -> it <3).findFirst().get();
     * 
     * //3
     * }
     * </pre>
     * (deterministic)
     * 
     */
    @Override
    default Optional<T> findFirst() {
        return reactiveSeq().findFirst();
    }

    /**
     * @return takeOne matching element,  but order is not guaranteed
     * <pre>
     * {@code
     * Streamable.of(1,2,3,4,5).filter(it -> it <3).findAny().get();
     * 
     * //3
     * }
     * </pre>
     * 
     * 
     * (non-deterministic) 
     */
    @Override
    default Optional<T> findAny() {
        return reactiveSeq().findAny();
    }

    /**
     * Attempt toNested map this Sequence toNested the same type as the supplied Monoid (Reducer)
     * Then use Monoid toNested reduce values
     * <pre>
     * {@code 
     * Streamable.of("hello","2","world","4").mapReduce(Reducers.toCountInt());
     * 
     * //4
     * }
     * </pre>
     * 
     * @param reducer Monoid toNested reduce values
     * @return Reduce result
     */
    @Override
    default <R> R mapReduce(final Reducer<R> reducer) {
        return reactiveSeq().mapReduce(reducer);
    }

    /**
     *  Attempt toNested map this Monad toNested the same type as the supplied Monoid, using supplied function
     *  Then use Monoid toNested reduce values
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
     * @param mapper Function toNested map Monad type
     * @param reducer Monoid toNested reduce values
     * @return Reduce result
     */
    @Override
    default <R> R mapReduce(final Function<? super T, ? extends R> mapper, final Monoid<R> reducer) {
        return reactiveSeq().mapReduce(mapper, reducer);
    }

    /**
     * <pre>
     * {@code 
     * Streamable.of("hello","2","world","4").reduce(Reducers.toString(","));
     * 
     * //hello,2,world,4
     * }</pre>
     * 
     * @param reducer Use supplied Monoid toNested reduce values
     * @return reduced values
     */
    @Override
    default T reduce(final Monoid<T> reducer) {
        return reactiveSeq().reduce(reducer);
    }

    /* 
     * <pre>
     * {@code 
     * assertThat(Streamable.of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),equalTo(1500));
     * }
     * </pre>
     * 
     */
    @Override
    default Optional<T> reduce(final BinaryOperator<T> accumulator) {
        return reactiveSeq().reduce(accumulator);
    }

    /* (non-Javadoc)
    * @see java.util.reactiveStream.Stream#reduce(java.lang.Object, java.util.function.BinaryOperator)
    */
    @Override
    default T reduce(final T identity, final BinaryOperator<T> accumulator) {
        return reactiveSeq().reduce(identity, accumulator);
    }

    /* (non-Javadoc)
    * @see java.util.reactiveStream.Stream#reduce(java.lang.Object, java.util.function.BiFunction, java.util.function.BinaryOperator)
    */
    @Override
    default <U> U reduce(final U identity, final BiFunction<U, ? super T, U> accumulator, final BinaryOperator<U> combiner) {
        return reactiveSeq().reduce(identity, accumulator, combiner);
    }

    /**
     * Reduce with multiple reducers in parallel
     * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
     * To reduce over the values on the list, called streamedMonad() takeOne. I.e. streamedMonad().reduce(reducer)
     * 
     * <pre>
     * {@code 
     * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
       Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
       List<Integer> result = Streamable.of(1,2,3,4)
    					.reduce(Arrays.asList(sum,mult).reactiveStream() );
    			
    	 
    	assertThat(result,equalTo(Arrays.asList(10,24)));
     * 
     * }
     * </pre>
     * 
     * 
     * @param reducers
     * @return
     */
    @Override
    default ListX<T> reduce(final Stream<? extends Monoid<T>> reducers) {
        return reactiveSeq().reduce(reducers);
    }

    /**
     * Reduce with multiple reducers in parallel
     * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
     * To reduce over the values on the list, called streamedMonad() takeOne. I.e. streamedMonad().reduce(reducer)
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
    default ListX<T> reduce(final Iterable<? extends Monoid<T>> reducers) {
        return reactiveSeq().reduce(reducers);
    }

    /**
     * 
     * <pre>
    	{@code
    	Streamable.of("a","b","c").foldRight(Reducers.toString(""));
       
        // "cab"
        }
        </pre>
     * @param reducer Use supplied Monoid toNested reduce values starting via foldRight
     * @return Reduced result
     */
    @Override
    default T foldRight(final Monoid<T> reducer) {
        return reactiveSeq().foldRight(reducer);
    }

    /**
     * Immutable reduction from right toNested left
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
        return reactiveSeq().foldRight(identity, accumulator);
    }

    /**
     *  Attempt toNested map this Monad toNested the same type as the supplied Monoid (using mapToType on the monoid interface)
     * Then use Monoid toNested reduce values
     * <pre>
    	{@code
    	Streamable.of(1,2,3).foldRightMapToType(Reducers.toString(""));
       
        // "321"
        }
        </pre>
     * 
     * 
     * @param reducer Monoid toNested reduce values
     * @return Reduce result
     */
    @Override
    default <T> T foldRightMapToType(final Reducer<T> reducer) {
        return reactiveSeq().foldRightMapToType(reducer);
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
    default boolean startsWithIterable(final Iterable<T> iterable) {
        return reactiveSeq().startsWithIterable(iterable);
    }

    /**
     * 	<pre>{@code assertTrue(Streamable.of(1,2,3,4).startsWith(Stream.of(1,2,3))) }</pre>
    
     * @param iterator
     * @return True if Monad starts with Iterators sequence of data
     */
    @Override
    default boolean startsWith(final Stream<T> iterator) {
        return reactiveSeq().startsWith(iterator);
    }

    /**
     * @return this Streamable converted toNested AnyM format
     */
    default AnyM<Witness.streamable,T> anyM() {
        return AnyM.fromStreamable(this);
    }

    /**
     * Allows flatMap return type toNested be any Monad type
     * <pre>
     * {@code 
     * 	assertThat(Streamable.of(1,2,3)).flatMapAnyM(i-> fromEither5(CompletableFuture.completedFuture(i+2))).toList(),equalTo(Arrays.asList(3,4,5)));
    
     * }</pre>
     * 
     * 
     * @param fn toNested be applied
     * @return new stage in Sequence with flatMap operation toNested be lazily applied
     */
    default <R> Streamable<R> flatMapAnyM(final Function<? super T, ? extends AnyM<Witness.streamable,? extends R>> fn) {
        return flatMap(fn.andThen(Witness::streamable));
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
    default <R> Streamable<R> flatMapIterable(final Function<? super T, ? extends Iterable<? extends R>> fn) {
        return fromStream(reactiveSeq().flatMapI(fn));
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
     * @param fn toNested be applied
     * @return new stage in Sequence with flatMap operation toNested be lazily applied
    */
    default <R> Streamable<R> flatMapStream(final Function<? super T, BaseStream<? extends R, ?>> fn) {
        return fromStream(reactiveSeq().flatMapStream(fn));
    }

    /**
     * Returns a reactiveStream with a given value interspersed between any two values
     * of this reactiveStream.
     * 
     * 
     * // (1, 0, 2, 0, 3, 0, 4) Streamable.of(1, 2, 3, 4).intersperse(0)
     * 
     */
    @Override
    default Streamable<T> intersperse(final T value) {
        return fromStream(reactiveSeq().intersperse(value));
    }

    /**
     * Keep only those elements in a reactiveStream that are of a given type.
     * 
     * 
     * // (1, 2, 3) Streamable.of(1, "a", 2, "b",3).ofType(Integer.class)
     * 
     */
    @Override
    @SuppressWarnings("unchecked")
    default <U> Streamable<U> ofType(final Class<? extends U> type) {
        return fromStream(reactiveSeq().ofType(type));
    }

    /**
     * Cast all elements in a reactiveStream toNested a given type, possibly throwing a
     * {@link ClassCastException}.
     * 
     * 
     * // ClassCastException Streamable.of(1, "a", 2, "b", 3).cast(Integer.class)
     * 
     */
    @Override
    default <U> Streamable<U> cast(final Class<? extends U> type) {
        return fromStream(reactiveSeq().cast(type));
    }


    /* 
     * Potentially efficient Sequence reversal. Is efficient if
     * 
     * - Sequence created via a range
     * - Sequence created via a List
     * - Sequence created via an Array / var args
     * 
     * Otherwise Sequence collected into a Collection prior toNested reversal
     * 
     * <pre>
     * {@code
     *  assertThat( of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
     *  }
     * </pre>
     */
    @Override
    default Streamable<T> reverse() {
        return fromStream(reactiveSeq().reverse());
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#shuffle()
     */
    @Override
    default Streamable<T> shuffle() {
        return fromStream(reactiveSeq().shuffle());
    }

    /**
     * Append Stream toNested this Streamable
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
     * @param stream toNested append
     * @return Streamable with Stream appended
     */
    default Streamable<T> appendStreamable(final Streamable<T> stream) {
        return fromStream(reactiveSeq().appendS(stream.reactiveSeq()));
    }

    /**
     * Prepend Stream toNested this Streamable
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
     * @param stream toNested Prepend
     * @return Streamable with Stream prepended
     */
    default Streamable<T> prependStreamable(final Streamable<T> stream) {
        return fromStream(reactiveSeq().prependS(stream.reactiveSeq()));
    }
    default Streamable<T> prependS(final Stream<? extends T> stream) {
        return fromStream(reactiveSeq().prependS(stream));
    }

    @Override
    default Streamable<T> append(T value){
        return fromStream(reactiveSeq().append(value));
    }

    @Override
    default Traversable<T> insertAtS(int pos, Stream<T> stream){
        return fromStream(reactiveSeq().insertAtS(pos,stream));
    }

    /**
     * Append values toNested the take of this Streamable
     * <pre>
     * {@code 
     * List<String> result = Streamable.of(1,2,3)
     * 								   .append(100,200,300)
    									.map(it ->it+"!!")
    									.collect(CyclopsCollectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("1!!","2!!","3!!","100!!","200!!","300!!")));
     * }
     * </pre>
     * @param values toNested append
     * @return Streamable with appended values
     */
    default Streamable<T> append(final T... values) {
        return fromStream(reactiveSeq().append(values));
    }

    /**
     * Prepend given values toNested the skip of the Stream
     * <pre>
     * {@code 
     * List<String> result = 	Streamable.of(1,2,3)
     * 									 .prepend(100,200,300)
    									 .map(it ->it+"!!")
    									 .collect(CyclopsCollectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("100!!","200!!","300!!","1!!","2!!","3!!")));
     * }
     * </pre>
     * @param values toNested prepend
     * @return Streamable with values prepended
     */
    default Streamable<T> prepend(final T... values) {
        return fromStream(reactiveSeq().prepend(values));
    }

    /**
     * Insert data into a reactiveStream at given position
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
     * @param pos toNested insert data at
     * @param values toNested insert
     * @return Stream with new data inserted
     */
    default Streamable<T> insertAt(final int pos, final T... values) {
        return fromStream(reactiveSeq().insertAt(pos, values));
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
        return fromStream(reactiveSeq().deleteBetween(start, end));
    }

    /**
     * Insert a Stream into the middle of this reactiveStream at the specified position
     * <pre>
     * {@code 
     * List<String> result = 	Streamable.of(1,2,3)
     * 									 .insertAtS(1,of(100,200,300))
    									 .map(it ->it+"!!")
    									 .collect(CyclopsCollectors.toList());
    
    		assertThat(result,equalTo(Arrays.asList("1!!","100!!","200!!","300!!","2!!","3!!")));
     * }
     * </pre>
     * @param pos toNested insert Stream at
     * @param stream toNested insert
     * @return newly conjoined Streamable
     */
    default Streamable<T> insertStreamableAt(final int pos, final Streamable<T> stream) {
        return fromStream(reactiveSeq().insertAtS(pos, stream.reactiveSeq()));
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
     * @param iterable Values toNested check
     * @return true if Streamable ends with values in the supplied iterable
     */
    @Override
    default boolean endsWithIterable(final Iterable<T> iterable) {
        return reactiveSeq().endsWithIterable(iterable);
    }

    /**
     * <pre>
     * {@code
     * assertTrue(Streamable.of(1,2,3,4,5,6)
    			.endsWith(Stream.of(5,6))); 
     * }
     * </pre>
     * 
     * @param stream Values toNested check
     * @return true if Streamable endswith values in the supplied Stream
     */
    default boolean endsWith(final Streamable<T> stream) {
        return reactiveSeq().endsWithIterable(stream);
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
        return fromStream(reactiveSeq().skip(time, unit));
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
        return fromStream(reactiveSeq().limit(time, unit));
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
        return fromStream(reactiveSeq().skipLast(num));
    }

    /**
     * Limit results toNested the last x elements in a Streamable
     * <pre>
     * {@code 
     * 	assertThat(Streamable.of(1,2,3,4,5)
    						.limitLast(2)
    						.collect(CyclopsCollectors.toList()),equalTo(Arrays.asList(4,5)));
     * 
     * }
     * </pre>
     * 
     * @param num of elements toNested return (last elements)
     * @return Streamable limited toNested last num elements
     */
    @Override
    default Streamable<T> limitLast(final int num) {
        return fromStream(reactiveSeq().limitLast(num));
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
     * @param e Executor toNested execute this Streamable on
     * @return a Connectable HotStream
     */
    default HotStream<T> hotStream(final Executor e) {
        return reactiveSeq().hotStream(e);
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
     * @return takeOne value in this Stream
     */
    @Override
    default T firstValue() {
        return reactiveSeq().firstValue();
    }

    /**
     * <pre>
     * {@code 
     * assertThat(Streamable.of(1).singleUnsafe(),equalTo(1));
     * }
     * </pre>
     * 
     * @return a singleUnsafe value or an exception if 0/1 values in this Stream
     */
    @Override
    default T singleUnsafe() {
        return reactiveSeq().singleUnsafe();

    }

    /**
     * Return the elementAt index or Optional.empty
     * <pre>
     * {@code
     * 	assertThat(Streamable.of(1,2,3,4,5).elementAt(2).get(),equalTo(3));
     * }
     * </pre>
     * @param index toNested extract element from
     * @return elementAt index
     */
    @Override
    default Maybe<T> get(final long index) {
        return reactiveSeq().get(index);
    }

    /**
     * Gets the element at index, and returns a Tuple containing the element (it must be present)
     * and a maybe copy of the Sequence for further processing.
     * 
     * <pre>
     * {@code 
     * Streamable.of(1,2,3,4,5).get(2).v1
     * //3
     * }
     * </pre>
     * 
     * @param index toNested extract element from
     * @return Element and Sequence
     */
    default Tuple2<T, Streamable<T>> elementAt(final long index) {
        return reactiveSeq().elementAt(index)
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
     * @return Sequence that adds the time between elements in millis toNested each element
     */
    default Streamable<Tuple2<T, Long>> elapsed() {
        return fromStream(reactiveSeq().elapsed());
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
     * @return Sequence that adds a timestamp toNested each element
     */
    default Streamable<Tuple2<T, Long>> timestamp() {
        return fromStream(reactiveSeq().timestamp());
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
     * @param start Number of range toNested skip from
     * @param end Number for range toNested take at
     * @return Range Streamable
     */
    public static Streamable<Integer> range(final int start, final int end) {
        return fromStream(ReactiveSeq.range(start, end));

    }

    /**
     * Create an efficiently reversable Sequence that produces the integers between skip
     * and take
     * @param start Number of range toNested skip from
     * @param end Number for range toNested take at
     * @return Range Streamable
     */
    public static Streamable<Long> rangeLong(final long start, final long end) {
        return fromStream(ReactiveSeq.rangeLong(start, end));

    }

    /**
     * Construct a Sequence from a Stream
     * @param stream Stream toNested construct Sequence from
     * @return
     */
    public static Streamable<Integer> fromIntStream(final IntStream stream) {
        Objects.requireNonNull(stream);
        return fromStream(ReactiveSeq.fromIntStream(stream));
    }

    /**
     * Construct a Sequence from a Stream
     * 
     * @param stream Stream toNested construct Sequence from
     * @return
     */
    public static Streamable<Long> fromLongStream(final LongStream stream) {
        Objects.requireNonNull(stream);
        return fromStream(ReactiveSeq.fromLongStream(stream));
    }

    /**
     * Construct a Sequence from a Stream
     * @param stream Stream toNested construct Sequence from
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
        return ReactiveSeq.unzip(sequence.reactiveSeq())
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
        return ReactiveSeq.unzip3(sequence.reactiveSeq())
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
        return ReactiveSeq.unzip4(sequence.reactiveSeq())
                          .map1(s -> fromStream(s))
                          .map2(s -> fromStream(s))
                          .map3(s -> fromStream(s))
                          .map4(s -> fromStream(s));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#crossJoin(java.util.reactiveStream.Stream)
     */
    default <U> Streamable<Tuple2<T, U>> crossJoin(final Streamable<U> other) {
        return fromStream(seq().crossJoin(other));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#innerJoin(java.util.reactiveStream.Stream, java.util.function.BiPredicate)
     */
    default <U> Streamable<Tuple2<T, U>> innerJoin(final Streamable<U> other, final BiPredicate<T, U> predicate) {
        return fromStream(seq().innerJoin(other, predicate));
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#leftOuterJoin(java.util.reactiveStream.Stream, java.util.function.BiPredicate)
     */
    default <U> Streamable<Tuple2<T, U>> leftOuterJoin(final Streamable<U> other, final BiPredicate<T, U> predicate) {
        return fromStream(reactiveSeq().jooλ(s->s.leftOuterJoin(other, predicate)));

    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Seq#rightOuterJoin(java.util.reactiveStream.Stream, java.util.function.BiPredicate)
     */
    default <U> Streamable<Tuple2<T, U>> rightOuterJoin(final Streamable<U> other, final BiPredicate<T, U> predicate) {
        return fromStream(reactiveSeq().jooλ(s->s.rightOuterJoin(other, predicate)));
    }

    /** If this Streamable is empty replace it with a another Stream
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
     * @return Streamable that will switch toNested an alternative Stream if empty
     */
    default Streamable<T> onEmptySwitch(final Supplier<Streamable<T>> switchTo) {
        return fromStream(reactiveSeq().onEmptySwitch(() -> switchTo.get()
                                                                    .reactiveSeq()));
    }

    @Override
    default Streamable<T> onEmpty(final T value) {
        return fromStream(reactiveSeq().onEmpty(value));
    }

    @Override
    default Streamable<T> onEmptyGet(final Supplier<? extends T> supplier) {
        return fromStream(reactiveSeq().onEmptyGet(supplier));
    }

    @Override
    default <X extends Throwable> Streamable<T> onEmptyThrow(final Supplier<? extends X> supplier) {
        return fromStream(reactiveSeq().onEmptyThrow(supplier));
    }

    default Streamable<T> concat(final Streamable<T> other) {
        return fromStream(seq().concat(other));
    }

    default Streamable<T> concat(final T other) {
        return fromStream(reactiveSeq().append(other));
    }

    default Streamable<T> concat(final T... other) {
        return fromStream(reactiveSeq().append(other));
    }

    default <U> Streamable<T> distinct(final Function<? super T, ? extends U> keyExtractor) {
        return fromStream(reactiveSeq().distinct(keyExtractor));
    }

    @Override
    default Streamable<T> shuffle(final Random random) {
        return fromStream(reactiveSeq().shuffle(random));

    }

    @Override
    default Streamable<T> slice(final long from, final long to) {
        return fromStream(reactiveSeq().slice(from, to));

    }

    @Override
    default <U extends Comparable<? super U>> Streamable<T> sorted(final Function<? super T, ? extends U> function) {
        return fromStream(reactiveSeq().sorted(function));

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
     * @param x number of elements toNested emit
     * @param time period
     * @param t Time unit
     * @return Streamable that emits x elements per time period
     */
    default ReactiveSeq<T> xPer(final int x, final long time, final TimeUnit t) {
        return reactiveSeq().xPer(x, time, t);

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
    			.flatMap(Collection::reactiveStream)
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
        return reactiveSeq().onePer(time, t);

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
        return fromStream(reactiveSeq().debounce(time, t));
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
     * @param time (Max) time period toNested build a singleUnsafe batch in
     * @param t time unit for batch
     * @return Streamable batched by size and time
     */
    default Streamable<ListX<T>> groupedBySizeAndTime(final int size, final long time, final TimeUnit t) {
        return fromStream(reactiveSeq().groupedBySizeAndTime(size, time, t));
    }

    /**
     *  Batch elements by size into a collection created by the supplied factory 
     * <pre>
     * {@code 
     * List<ArrayList<Integer>> list = of(1,2,3,4,5,6)
    				.batchBySizeAndTime(10,1,TimeUnit.MICROSECONDS,()->new ArrayList<>())
    				.toList();
     * }
     * </pre>
     * @param size Max size of a batch
     * @param time (Max) time period toNested build a singleUnsafe batch in
     * @param unit time unit for batch
     * @param factory Collection factory
     * @return Streamable batched by size and time
     */
    default <C extends Collection<? super T>> Streamable<C> groupedBySizeAndTime(final int size, final long time, final TimeUnit unit,
            final Supplier<C> factory) {
        return fromStream(reactiveSeq().groupedBySizeAndTime(size, time, unit, factory));
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
     * @param time - time period toNested build a singleUnsafe batch in
     * @param t  time unit for batch
     * @return Streamable batched into lists by time period
     */
    default Streamable<ListX<T>> groupedByTime(final long time, final TimeUnit t) {
        return fromStream(reactiveSeq().groupedByTime(time, t));
    }

    /**
     * Batch elements by time into a collection created by the supplied factory 
     * 
     * <pre>
     * {@code 
     *   assertThat(Streamable.of(1,1,1,1,1,1)
     *                       .batchByTime(1500,TimeUnit.MICROSECONDS,()-> new TreeSet<>())
     *                       .toList()
     *                       .get(0)
     *                       .size(),is(1));
     * }
     * </pre>
     * 
     * @param time - time period toNested build a singleUnsafe batch in
     * @param unit time unit for batch
     * @param factory Collection factory
     * @return Streamable batched into collection types by time period
     */
    default <C extends Collection<? super T>> Streamable<C> groupedByTime(final long time, final TimeUnit unit, final Supplier<C> factory) {
        return fromStream(reactiveSeq().groupedByTime(time, unit, factory));
    }

    /**
     * Batch elements in a Stream by size into a collection created by the supplied factory 
     * <pre>
     * {@code
     * assertThat(Streamable.of(1,1,1,1,1,1)
     * 						.batchBySize(3,()->new TreeSet<>())
     * 						.toList()
     * 						.get(0)
     * 						.size(),is(1));
     * }
     * </pre>
     * 
     * @param size batch size
     * @param supplier Collection factory
     * @return Streamable batched into collection types by size
     */
    @Override
    default <C extends Collection<? super T>> Streamable<C> grouped(final int size, final Supplier<C> supplier) {
        return fromStream(reactiveSeq().grouped(size, supplier));
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
        return reactiveSeq().fixedDelay(l, unit);
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
        return fromStream(reactiveSeq().jitter(maxJitterPeriodInNanos));
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
    default Streamable<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {
        return fromStream(reactiveSeq().groupedUntil(predicate));
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
    default Streamable<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {
        return fromStream(reactiveSeq().groupedWhile(predicate));
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
    default <C extends Collection<? super T>> Streamable<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return fromStream(reactiveSeq().groupedWhile(predicate, factory));
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
    default <C extends Collection<? super T>> Streamable<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {
        return fromStream(reactiveSeq().groupedUntil(predicate, factory));

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
        return fromStream(reactiveSeq().recover(fn));
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
     * @param exceptionClass Type toNested recover from
     * @param fn That accepts an error and returns an alternative value
     * @return Streamable that can recover from a particular exception
     */
    default <EX extends Throwable> Streamable<T> recover(final Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return fromStream(reactiveSeq().recover(exceptionClass, fn));

    }

    /**
     * Retry a transformation if it fails. Default settings are toNested retry up toNested 7 times, with an doubling
     * backoff period starting @ 2 seconds delay before retry.
     * 
     * <pre>
     * {@code 
     * given(serviceMock.apply(anyInt())).willThrow(
    			new RuntimeException(new SocketException("First")),
    			new RuntimeException(new IOException("Second"))).willReturn(
    			"42");
    
    
    	String result = Streamable.of( 1,  2, 3)
    			.retry(serviceMock)
    			.firstValue();
    
    	assertThat(result, is("42"));
     * }
     * </pre>
     * @param fn Function toNested retry if fails
     * 
     */
    @Override
    default <R> Streamable<R> retry(final Function<? super T,? extends  R> fn) {
        return fromStream(reactiveSeq().retry(fn));
    }

    /**
     * True if a streamable contains element t
     * <pre>
     * {@code 
     * assertThat(Streamable.of(1,2,3,4,5).contains(3),equalTo(true));
     * }
     * </pre>
     * @param t element toNested check for
     */
    default boolean contains(final T t) {
        return stream().anyMatch(c -> t.equals(c));
    }

    @Override
    default ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(this);
    }

    @Override
    default Iterator<T> iterator() {
        return this.getStreamable().iterator();
    }

}

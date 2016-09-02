package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad Transformer for Cyclops Streamables
 * 
 * StreamableT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Streamable
 * <pre>
 * {@code 
 * StreamableT<AnyM<*SOME_MONAD_TYPE*<Streamable<T>>>>
 * }</pre>
 * StreamableT allows the deeply wrapped Streamable to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public class StreamableTSeq<T> implements StreamableT<T> {

    final AnyMSeq<Streamable<T>> run;

    private StreamableTSeq(final AnyMSeq<Streamable<T>> run) {
        this.run = run;
    }

    public boolean isSeqPresent() {
        return !run.isEmpty();
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyM<Streamable<T>> unwrap() {
        return run;
    }

    /**
     * Peek at the current value of the Streamable
     * <pre>
     * {@code 
     *    StreamableT.of(AnyM.fromStream(Streamable.of(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of Streamable
     * @return StreamableT with peek call
     */
    public StreamableTSeq<T> peek(Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });

    }

    /**
     * Filter the wrapped Streamable
     * <pre>
     * {@code 
     *    StreamableT.of(AnyM.fromStream(Streamable.of(10,11))
     *             .filter(t->t!=10);
     *             
     *     //StreamableT<AnyM<Stream<Streamable[11]>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped Streamable
     * @return StreamableT that applies the provided filter
     */
    public StreamableTSeq<T> filter(Predicate<? super T> test) {
        return of(run.map(stream -> stream.filter(test)));
    }

    /**
     * Map the wrapped Streamable
     * 
     * <pre>
     * {@code 
     *  StreamableT.of(AnyM.fromStream(Streamable.of(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //StreamableT<AnyM<Stream<Streamable[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Streamable
     * @return StreamableT that applies the map function to the wrapped Streamable
     */
    public <B> StreamableTSeq<B> map(Function<? super T, ? extends B> f) {
        return new StreamableTSeq<B>(
                                     run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Streamable
      * <pre>
     * {@code 
     *  StreamableT.of(AnyM.fromStream(Arrays.asStreamable(10))
     *             .flatMap(t->Streamable.of(2));
     *  
     *  
     *  //StreamableT<AnyM<Stream<Streamable.[2]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return StreamableT that applies the flatMap function to the wrapped Streamable
     */
    public <B> StreamableTSeq<B> flatMapT(Function<? super T, StreamableTSeq<? extends B>> f) {
        return of(run.map(stream -> stream.flatMap(a -> Streamable.fromStream(f.apply(a).run.stream()))
                                          .<B> flatMap(a -> a)));
    }

    public <B> StreamableTSeq<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f) {

        return new StreamableTSeq<B>(
                                     run.map(o -> o.flatMapIterable(f)));

    }

    /**
     * Lift a function into one that accepts and returns an StreamableT
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling  / iteration (via Streamable) and iteration (via Stream) to an existing function
     * <pre>
     * {@code 
     *Function<Integer,Integer> add2 = i -> i+2;
    	Function<StreamableT<Integer>, StreamableT<Integer>> optTAdd2 = StreamableT.lift(add2);
    	
    	Stream<Integer> nums = Stream.of(1,2);
    	AnyM<Stream<Integer>> stream = AnyM.ofMonad(Optional.of(nums));
    	
    	List<Integer> results = optTAdd2.apply(StreamableT.fromStream(stream))
    									.unwrap()
    									.<Optional<Streamable<Integer>>>unwrap()
    									.get()
    									.collect(Collectors.toList());
    	
    	
    	//Streamable.of(3,4);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from Streamable and another monad type
     * @return Function that accepts and returns an StreamableT
     */
    public static <U, R> Function<StreamableTSeq<U>, StreamableTSeq<R>> lift(Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  StreamableTs
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling / iteration (via Streamable), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
     * to an existing function
     * 
     * <pre>
     * {@code 
     * BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<StreamableT<Integer>,StreamableT<Integer>, StreamableT<Integer>> optTAdd2 = StreamableT.lift2(add);
    	
    	Streamable<Integer> threeValues = Streamable.of(1,2,3);
    	AnyM<Integer> stream = AnyM.ofMonad(threeValues);
    	AnyM<Streamable<Integer>> streamOpt = stream.map(Streamable::of);
    	
    	CompletableFuture<Streamable<Integer>> two = CompletableFuture.completedFuture(Streamable.of(2));
    	AnyM<Streamable<Integer>> future=  AnyM.fromCompletableFuture(two);
    	List<Integer> results = optTAdd2.apply(StreamableT.of(streamOpt),StreamableT.of(future))
    									.unwrap()
    									.<Stream<Streamable<Integer>>>unwrap()
    									.flatMap(i->i.sequenceM())
    									.collect(Collectors.toList());
    		//Streamable.of(3,4);							
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Streamable and another monad type
     * @return Function that accepts and returns an StreamableT
     */
    public static <U1, U2, R> BiFunction<StreamableTSeq<U1>, StreamableTSeq<U2>, StreamableTSeq<R>> lift2(
            BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an StreamableT from an AnyM that contains a monad type that contains type other than Streamable
     * The values in the underlying monad will be mapped to Streamable<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Streamable
     * @return StreamableT
     */
    public static <A> StreamableTSeq<A> fromAnyM(AnyMSeq<A> anyM) {
        return of(anyM.map(Streamable::of));
    }

    /**
     * Construct an StreamableT from an AnyM that wraps a monad containing  Streamables
     * 
     * @param monads AnyM that contains a monad wrapping an Streamable
     * @return StreamableT
     */
    public static <A> StreamableTSeq<A> of(AnyMSeq<Streamable<A>> monads) {
        return new StreamableTSeq<>(
                                    monads);
    }

    public static <A> StreamableTSeq<A> of(Streamable<A> monads) {
        return StreamableT.fromIterable(Streamable.of(monads));
    }

    /**
     * Create a StreamableT from an AnyM that wraps a monad containing a Stream
     * 
     * @param monads
     * @return
     */
    public static <A> StreamableTSeq<A> fromStream(AnyMSeq<Stream<A>> monads) {
        return new StreamableTSeq<>(
                                    monads.map(Streamable::fromStream));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return String.format("StreamableTSeq[%s]", run);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    public <T> StreamableTSeq<T> unit(T unit) {
        return of(run.unit(Streamable.of(unit)));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream()
                  .flatMapIterable(e -> e);
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    public <R> StreamableTSeq<R> unitIterator(Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Streamable.of(i)));
    }

    @Override
    public <R> StreamableTSeq<R> empty() {
        return of(run.empty());
    }

    @Override
    public AnyM<? extends IterableFoldable<T>> nestedFoldables() {
        return run;

    }

    @Override
    public AnyM<? extends CyclopsCollectable<T>> nestedCollectables() {
        return run;

    }

    @Override
    public <T> StreamableTSeq<T> unitAnyM(AnyM<Traversable<T>> traversable) {

        return of((AnyMSeq) traversable.map(t -> Streamable.fromIterable(t)));
    }

    @Override
    public AnyMSeq<? extends Traversable<T>> transformerStream() {

        return run;
    }

    public static <T> StreamableTSeq<T> emptyStreamable() {
        return StreamableT.fromIterable(Streamable.empty());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public StreamableTSeq<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {

        return (StreamableTSeq<T>) StreamableT.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#cycle(int)
     */
    @Override
    public StreamableTSeq<T> cycle(int times) {

        return (StreamableTSeq<T>) StreamableT.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public StreamableTSeq<T> cycle(Monoid<T> m, int times) {

        return (StreamableTSeq<T>) StreamableT.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<T> cycleWhile(Predicate<? super T> predicate) {

        return (StreamableTSeq<T>) StreamableT.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<T> cycleUntil(Predicate<? super T> predicate) {

        return (StreamableTSeq<T>) StreamableT.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> StreamableTSeq<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (StreamableTSeq<R>) StreamableT.super.zip(other, zipper);
    }

    @Override
    public <U, R> StreamableTSeq<R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (StreamableTSeq<R>) StreamableT.super.zip(other, zipper);
    }

    @Override
    public <U, R> StreamableTSeq<R> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (StreamableTSeq<R>) StreamableT.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> StreamableTSeq<Tuple2<T, U>> zip(Stream<? extends U> other) {

        return (StreamableTSeq) StreamableT.super.zip(other);
    }

    @Override
    public <U> StreamableTSeq<Tuple2<T, U>> zip(Iterable<? extends U> other) {

        return (StreamableTSeq) StreamableT.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> StreamableTSeq<Tuple2<T, U>> zip(Seq<? extends U> other) {

        return (StreamableTSeq) StreamableT.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> StreamableTSeq<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {

        return (StreamableTSeq) StreamableT.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> StreamableTSeq<Tuple4<T, T2, T3, T4>> zip4(Stream<? extends T2> second, Stream<? extends T3> third,
            Stream<? extends T4> fourth) {

        return (StreamableTSeq) StreamableT.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#zipWithIndex()
     */
    @Override
    public StreamableTSeq<Tuple2<T, Long>> zipWithIndex() {

        return (StreamableTSeq<Tuple2<T, Long>>) StreamableT.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#sliding(int)
     */
    @Override
    public StreamableTSeq<ListX<T>> sliding(int windowSize) {

        return (StreamableTSeq<ListX<T>>) StreamableT.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#sliding(int, int)
     */
    @Override
    public StreamableTSeq<ListX<T>> sliding(int windowSize, int increment) {

        return (StreamableTSeq<ListX<T>>) StreamableT.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> StreamableTSeq<C> grouped(int size, Supplier<C> supplier) {

        return (StreamableTSeq<C>) StreamableT.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<ListX<T>> groupedUntil(Predicate<? super T> predicate) {

        return (StreamableTSeq<ListX<T>>) StreamableT.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public StreamableTSeq<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (StreamableTSeq<ListX<T>>) StreamableT.super.groupedStatefullyWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<ListX<T>> groupedWhile(Predicate<? super T> predicate) {

        return (StreamableTSeq<ListX<T>>) StreamableT.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> StreamableTSeq<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {

        return (StreamableTSeq<C>) StreamableT.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> StreamableTSeq<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {

        return (StreamableTSeq<C>) StreamableT.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#grouped(int)
     */
    @Override
    public StreamableTSeq<ListX<T>> grouped(int groupSize) {

        return (StreamableTSeq<ListX<T>>) StreamableT.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> StreamableTSeq<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {

        return (StreamableTSeq) StreamableT.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#grouped(java.util.function.Function)
     */
    @Override
    public <K> StreamableTSeq<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {

        return (StreamableTSeq) StreamableT.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#distinct()
     */
    @Override
    public StreamableTSeq<T> distinct() {

        return (StreamableTSeq<T>) StreamableT.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public StreamableTSeq<T> scanLeft(Monoid<T> monoid) {

        return (StreamableTSeq<T>) StreamableT.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> StreamableTSeq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {

        return (StreamableTSeq<U>) StreamableT.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public StreamableTSeq<T> scanRight(Monoid<T> monoid) {

        return (StreamableTSeq<T>) StreamableT.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> StreamableTSeq<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (StreamableTSeq<U>) StreamableT.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#sorted()
     */
    @Override
    public StreamableTSeq<T> sorted() {

        return (StreamableTSeq<T>) StreamableT.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#sorted(java.util.Comparator)
     */
    @Override
    public StreamableTSeq<T> sorted(Comparator<? super T> c) {

        return (StreamableTSeq<T>) StreamableT.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#takeWhile(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<T> takeWhile(Predicate<? super T> p) {

        return (StreamableTSeq<T>) StreamableT.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#dropWhile(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<T> dropWhile(Predicate<? super T> p) {

        return (StreamableTSeq<T>) StreamableT.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#takeUntil(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<T> takeUntil(Predicate<? super T> p) {

        return (StreamableTSeq<T>) StreamableT.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#dropUntil(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<T> dropUntil(Predicate<? super T> p) {

        return (StreamableTSeq<T>) StreamableT.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#dropRight(int)
     */
    @Override
    public StreamableTSeq<T> dropRight(int num) {

        return (StreamableTSeq<T>) StreamableT.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#takeRight(int)
     */
    @Override
    public StreamableTSeq<T> takeRight(int num) {

        return (StreamableTSeq<T>) StreamableT.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#skip(long)
     */
    @Override
    public StreamableTSeq<T> skip(long num) {

        return (StreamableTSeq<T>) StreamableT.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#skipWhile(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<T> skipWhile(Predicate<? super T> p) {

        return (StreamableTSeq<T>) StreamableT.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#skipUntil(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<T> skipUntil(Predicate<? super T> p) {

        return (StreamableTSeq<T>) StreamableT.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#limit(long)
     */
    @Override
    public StreamableTSeq<T> limit(long num) {

        return (StreamableTSeq<T>) StreamableT.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#limitWhile(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<T> limitWhile(Predicate<? super T> p) {

        return (StreamableTSeq<T>) StreamableT.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#limitUntil(java.util.function.Predicate)
     */
    @Override
    public StreamableTSeq<T> limitUntil(Predicate<? super T> p) {

        return (StreamableTSeq<T>) StreamableT.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#intersperse(java.lang.Object)
     */
    @Override
    public StreamableTSeq<T> intersperse(T value) {

        return (StreamableTSeq<T>) StreamableT.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#reverse()
     */
    @Override
    public StreamableTSeq<T> reverse() {

        return (StreamableTSeq<T>) StreamableT.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#shuffle()
     */
    @Override
    public StreamableTSeq<T> shuffle() {

        return (StreamableTSeq<T>) StreamableT.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#skipLast(int)
     */
    @Override
    public StreamableTSeq<T> skipLast(int num) {

        return (StreamableTSeq<T>) StreamableT.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#limitLast(int)
     */
    @Override
    public StreamableTSeq<T> limitLast(int num) {

        return (StreamableTSeq<T>) StreamableT.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#onEmpty(java.lang.Object)
     */
    @Override
    public StreamableTSeq<T> onEmpty(T value) {

        return (StreamableTSeq<T>) StreamableT.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public StreamableTSeq<T> onEmptyGet(Supplier<? extends T> supplier) {

        return (StreamableTSeq<T>) StreamableT.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> StreamableTSeq<T> onEmptyThrow(Supplier<? extends X> supplier) {

        return (StreamableTSeq<T>) StreamableT.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#shuffle(java.util.Random)
     */
    @Override
    public StreamableTSeq<T> shuffle(Random random) {

        return (StreamableTSeq<T>) StreamableT.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#slice(long, long)
     */
    @Override
    public StreamableTSeq<T> slice(long from, long to) {

        return (StreamableTSeq<T>) StreamableT.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.StreamableT#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> StreamableTSeq<T> sorted(Function<? super T, ? extends U> function) {
        return (StreamableTSeq) StreamableT.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StreamableTSeq) {
            return run.equals(((StreamableTSeq) o).run);
        }
        return false;
    }

}
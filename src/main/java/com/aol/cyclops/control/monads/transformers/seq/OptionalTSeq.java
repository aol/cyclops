package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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

import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.control.monads.transformers.values.ValueTransformerSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad transformer for JDK Optional nested within Sequential or non-scalar data types (e.g. Lists, Streams etc)
 * 
 * OptionalT allows the deeply wrapped Optional to be manipulating within it's nested /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the Optional within
 */
public class OptionalTSeq<T>
        implements OptionalT<T>, ValueTransformerSeq<T>, IterableFoldable<T>, ConvertableSequence<T>, CyclopsCollectable<T>, Sequential<T> {

    private final AnyMSeq<Optional<T>> run;

    private OptionalTSeq(final AnyMSeq<Optional<T>> run) {
        this.run = run;
    }

    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyMSeq<Optional<T>> unwrap() {
        return run;
    }

    @Override
    public <T> OptionalTSeq<T> unitStream(final ReactiveSeq<T> traversable) {
        return OptionalT.fromStream(traversable.map(Optional::of));

    }

    @Override
    public <T> OptionalTSeq<T> unitAnyM(final AnyM<Traversable<T>> traversable) {

        return of((AnyMSeq) traversable.map(t -> Maybe.fromIterable(t)
                                                      .toOptional()));
    }

    @Override
    public AnyMSeq<? extends Traversable<T>> transformerStream() {

        return run.map(e -> Maybe.fromOptional(e)
                                 .toListX());
    }

    /**
     * Peek at the current value of the Optional
     * <pre>
     * {@code 
     *    OptionalT.of(AnyM.fromStream(Optional.of(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of Optional
     * @return OptionalT with peek call
     */
    @Override
    public OptionalTSeq<T> peek(final Consumer<? super T> peek) {
        return of(run.peek(opt -> opt.map(a -> {
            peek.accept(a);
            return a;
        })));
    }

    /**
     * Filter the wrapped Optional
     * <pre>
     * {@code 
     *    OptionalT.of(AnyM.fromStream(Optional.of(10))
     *             .filter(t->t!=10);
     *             
     *     //OptionalT<AnyMSeq<Stream<Optional.empty>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped Optional
     * @return OptionalT that applies the provided filter
     */
    @Override
    public OptionalTSeq<T> filter(final Predicate<? super T> test) {
        return of(run.map(opt -> opt.filter(test)));
    }

    /**
     * Map the wrapped Optional
     * 
     * <pre>
     * {@code 
     *  OptionalT.of(AnyM.fromStream(Optional.of(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //OptionalT<AnyMSeq<Stream<Optional[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Optional
     * @return OptionalT that applies the map function to the wrapped Optional
     */
    @Override
    public <B> OptionalTSeq<B> map(final Function<? super T, ? extends B> f) {
        return new OptionalTSeq<B>(
                                   run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Optional
      * <pre>
     * {@code 
     *  OptionalT.of(AnyM.fromStream(Optional.of(10))
     *             .flatMap(t->Optional.empty();
     *  
     *  
     *  //OptionalT<AnyMSeq<Stream<Optional.empty>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return OptionalT that applies the flatMap function to the wrapped Optional
     */
    public <B> OptionalTSeq<B> flatMapT(final Function<? super T, OptionalTSeq<? extends B>> f) {

        return of(run.bind(opt -> {
            if (opt.isPresent())
                return f.apply(opt.get()).run.unwrap();
            return run.unit(Optional.<B> empty())
                      .unwrap();
        }));

    }

    private static <B> AnyMSeq<Optional<B>> narrow(final AnyMSeq<Optional<? extends B>> run) {
        return (AnyMSeq) run;
    }

    @Override
    public <B> OptionalTSeq<B> flatMap(final Function<? super T, ? extends MonadicValue<? extends B>> f) {
        final AnyMSeq<Optional<? extends B>> mapped = run.map(o -> Maybe.fromOptional(o)
                                                                        .flatMap(f)
                                                                        .toOptional());
        return of(narrow(mapped));
    }

    /**
     * Lift a function into one that accepts and returns an OptionalT
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add null handling (via Optional) and iteration (via Stream) to an existing function
     * <pre>
     * {@code 
     *  Function<Integer,Integer> add2 = i -> i+2;
    	Function<OptionalT<Integer>, OptionalT<Integer>> optTAdd2 = OptionalT.lift(add2);
    	
    	Stream<Integer> withNulls = Stream.of(1,2,null);
    	AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
    	AnyMSeq<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
    	List<Integer> results = optTAdd2.apply(OptionalT.of(streamOpt))
    									.unwrap()
    									.<Stream<Optional<Integer>>>unwrap()
    									.filter(Optional::isPresent)
    									.map(Optional::get)
    									.collect(Collectors.toList());
    	
    	//Arrays.asList(3,4);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from Optional and another monad type
     * @return Function that accepts and returns an OptionalT
     */
    public static <U, R> Function<OptionalTSeq<U>, OptionalTSeq<R>> lift(final Function<U, R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  OptionalTs
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add null handling (via Optional), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
     * to an existing function
     * 
     * <pre>
     * {@code 
     * BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<OptionalT<Integer>,OptionalT<Integer>, OptionalT<Integer>> optTAdd2 = OptionalT.lift2(add);
    	
    	Stream<Integer> withNulls = Stream.of(1,2,null);
    	AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
    	AnyMSeq<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
    	
    	CompletableFuture<Optional<Integer>> two = CompletableFuture.supplyAsync(()->Optional.of(2));
    	AnyMSeq<Optional<Integer>> future=  AnyM.ofMonad(two);
    	List<Integer> results = optTAdd2.apply(OptionalT.of(streamOpt),OptionalT.of(future))
    									.unwrap()
    									.<Stream<Optional<Integer>>>unwrap()
    									.filter(Optional::isPresent)
    									.map(Optional::get)
    									.collect(Collectors.toList());
    		//Arrays.asList(3,4);							
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Optional and another monad type
     * @return Function that accepts and returns an OptionalT
     */
    public static <U1, U2, R> BiFunction<OptionalTSeq<U1>, OptionalTSeq<U2>, OptionalTSeq<R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an OptionalT from an AnyM that contains a monad type that contains type other than Optional
     * The values in the underlying monad will be mapped to Optional<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Optional
     * @return OptionalT
     */
    public static <A> OptionalTSeq<A> fromAnyM(final AnyMSeq<A> anyM) {
        return of(anyM.map(Optional::ofNullable));
    }

    /**
     * Construct an OptionalT from an AnyM that wraps a monad containing  Optionals
     * 
     * @param monads AnyM that contains a monad wrapping an Optional
     * @return OptionalT
     */
    public static <A> OptionalTSeq<A> of(final AnyMSeq<Optional<A>> monads) {
        return new OptionalTSeq<>(
                                  monads);
    }

    public static <A> OptionalTSeq<A> of(final Optional<A> monads) {
        return OptionalT.fromIterable(ListX.of(monads));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("OptionalTSeq[%s]", run);
    }

    public boolean isPresent() {
        return run.anyMatch(m -> m.isPresent());
    }

    @Override
    public <R> OptionalTSeq<R> unit(final R value) {
        return of(run.unit(Optional.of(value)));
    }

    @Override
    public <R> OptionalTSeq<R> empty() {
        return of(run.unit(Optional.empty()));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream()
                  .flatMapIterable(e -> Maybe.fromOptional(e));
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    public <R> OptionalTSeq<R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Optional.of(i)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<T> collectable() {
        return stream();
    }

    @Override
    public boolean isSeqPresent() {
        return !run.isEmpty();
    }

    public static <T> OptionalTSeq<T> emptyList() {
        return OptionalT.fromIterable(ListX.of());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public OptionalTSeq<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(int)
     */
    @Override
    public OptionalTSeq<T> cycle(final int times) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public OptionalTSeq<T> cycle(final Monoid<T> m, final int times) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<T> cycleWhile(final Predicate<? super T> predicate) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<T> cycleUntil(final Predicate<? super T> predicate) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> OptionalTSeq<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (OptionalTSeq<R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    @Override
    public <U, R> OptionalTSeq<R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (OptionalTSeq<R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    @Override
    public <U, R> OptionalTSeq<R> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (OptionalTSeq<R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> OptionalTSeq<Tuple2<T, U>> zip(final Stream<? extends U> other) {

        return (OptionalTSeq) ValueTransformerSeq.super.zip(other);
    }

    @Override
    public <U> OptionalTSeq<Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (OptionalTSeq) ValueTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> OptionalTSeq<Tuple2<T, U>> zip(final Seq<? extends U> other) {

        return (OptionalTSeq) ValueTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> OptionalTSeq<Tuple3<T, S, U>> zip3(final Stream<? extends S> second, final Stream<? extends U> third) {

        return (OptionalTSeq) ValueTransformerSeq.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> OptionalTSeq<Tuple4<T, T2, T3, T4>> zip4(final Stream<? extends T2> second, final Stream<? extends T3> third,
            final Stream<? extends T4> fourth) {

        return (OptionalTSeq) ValueTransformerSeq.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipWithIndex()
     */
    @Override
    public OptionalTSeq<Tuple2<T, Long>> zipWithIndex() {

        return (OptionalTSeq<Tuple2<T, Long>>) ValueTransformerSeq.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int)
     */
    @Override
    public OptionalTSeq<ListX<T>> sliding(final int windowSize) {

        return (OptionalTSeq<ListX<T>>) ValueTransformerSeq.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int, int)
     */
    @Override
    public OptionalTSeq<ListX<T>> sliding(final int windowSize, final int increment) {

        return (OptionalTSeq<ListX<T>>) ValueTransformerSeq.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> OptionalTSeq<C> grouped(final int size, final Supplier<C> supplier) {

        return (OptionalTSeq<C>) ValueTransformerSeq.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (OptionalTSeq<ListX<T>>) ValueTransformerSeq.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    public OptionalTSeq<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (OptionalTSeq<ListX<T>>) ValueTransformerSeq.super.groupedStatefullyUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (OptionalTSeq<ListX<T>>) ValueTransformerSeq.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> OptionalTSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (OptionalTSeq<C>) ValueTransformerSeq.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> OptionalTSeq<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (OptionalTSeq<C>) ValueTransformerSeq.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int)
     */
    @Override
    public OptionalTSeq<ListX<T>> grouped(final int groupSize) {

        return (OptionalTSeq<ListX<T>>) ValueTransformerSeq.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> OptionalTSeq<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier,
            final Collector<? super T, A, D> downstream) {

        return (OptionalTSeq) ValueTransformerSeq.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function)
     */
    @Override
    public <K> OptionalTSeq<Tuple2<K, Seq<T>>> grouped(final Function<? super T, ? extends K> classifier) {

        return (OptionalTSeq) ValueTransformerSeq.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#distinct()
     */
    @Override
    public OptionalTSeq<T> distinct() {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public OptionalTSeq<T> scanLeft(final Monoid<T> monoid) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> OptionalTSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return (OptionalTSeq<U>) ValueTransformerSeq.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public OptionalTSeq<T> scanRight(final Monoid<T> monoid) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> OptionalTSeq<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (OptionalTSeq<U>) ValueTransformerSeq.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted()
     */
    @Override
    public OptionalTSeq<T> sorted() {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.Comparator)
     */
    @Override
    public OptionalTSeq<T> sorted(final Comparator<? super T> c) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<T> takeWhile(final Predicate<? super T> p) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<T> dropWhile(final Predicate<? super T> p) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<T> takeUntil(final Predicate<? super T> p) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<T> dropUntil(final Predicate<? super T> p) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropRight(int)
     */
    @Override
    public OptionalTSeq<T> dropRight(final int num) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeRight(int)
     */
    @Override
    public OptionalTSeq<T> takeRight(final int num) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skip(long)
     */
    @Override
    public OptionalTSeq<T> skip(final long num) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<T> skipWhile(final Predicate<? super T> p) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<T> skipUntil(final Predicate<? super T> p) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limit(long)
     */
    @Override
    public OptionalTSeq<T> limit(final long num) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<T> limitWhile(final Predicate<? super T> p) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    public OptionalTSeq<T> limitUntil(final Predicate<? super T> p) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#intersperse(java.lang.Object)
     */
    @Override
    public OptionalTSeq<T> intersperse(final T value) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#reverse()
     */
    @Override
    public OptionalTSeq<T> reverse() {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle()
     */
    @Override
    public OptionalTSeq<T> shuffle() {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipLast(int)
     */
    @Override
    public OptionalTSeq<T> skipLast(final int num) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitLast(int)
     */
    @Override
    public OptionalTSeq<T> limitLast(final int num) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    public OptionalTSeq<T> onEmpty(final T value) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public OptionalTSeq<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> OptionalTSeq<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle(java.util.Random)
     */
    @Override
    public OptionalTSeq<T> shuffle(final Random random) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#slice(long, long)
     */
    @Override
    public OptionalTSeq<T> slice(final long from, final long to) {

        return (OptionalTSeq<T>) ValueTransformerSeq.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> OptionalTSeq<T> sorted(final Function<? super T, ? extends U> function) {
        return (OptionalTSeq) ValueTransformerSeq.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof OptionalTSeq) {
            return run.equals(((OptionalTSeq) o).run);
        }
        return false;
    }
}

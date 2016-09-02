package com.aol.cyclops.control.monads.transformers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.seq.SetTSeq;
import com.aol.cyclops.control.monads.transformers.values.FoldableTransformerSeq;
import com.aol.cyclops.control.monads.transformers.values.SetTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;

/**
 * Monad Transformer for Java Sets
 * 
 * SetT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Set
 * 
 * <pre>
 * {@code 
 * SetT<AnyM<*SOME_MONAD_TYPE*<Set<T>>>>
 * }</pre>
 * 
 * SetT allows the deeply wrapped Set to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public interface SetT<T> extends FoldableTransformerSeq<T> {

    public <R> SetT<R> unitIterator(Iterator<R> it);

    public <R> SetT<R> unit(R t);

    public <R> SetT<R> empty();

    /**
     * @return The wrapped AnyM
     */
    AnyM<SetX<T>> unwrap();

    public <B> SetT<B> flatMap(Function<? super T, ? extends Iterable<? extends B>> f);

    /**
     * Peek at the current value of the Set
     * <pre>
     * {@code 
     *    SetT.of(AnyM.fromStream(Arrays.asSet(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek  Consumer to accept current value of Set
     * @return SetT with peek call
     */
    public SetT<T> peek(Consumer<? super T> peek);

    /**
     * Filter the wrapped Set
     * <pre>
     * {@code 
     *    SetT.of(AnyM.fromStream(Arrays.asSet(10,11))
     *             .filter(t->t!=10);
     *             
     *     //SetT<AnyM<Stream<Set[11]>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped Set
     * @return SetT that applies the provided filter
     */
    public SetT<T> filter(Predicate<? super T> test);

    /**
     * Map the wrapped Set
     * 
     * <pre>
     * {@code 
     *  SetT.of(AnyM.fromStream(Arrays.asSet(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //SetT<AnyM<Stream<Set[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Set
     * @return SetT that applies the map function to the wrapped Set
     */
    public <B> SetT<B> map(Function<? super T, ? extends B> f);

    /**
     * Flat Map the wrapped Set
      * <pre>
     * {@code 
     *  SetT.of(AnyM.fromStream(Arrays.asSet(10))
     *             .flatMap(t->Set.empty();
     *  
     *  
     *  //SetT<AnyM<Stream<Set.empty>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return SetT that applies the flatMap function to the wrapped Set
     */
    default <B> SetT<B> bind(Function<? super T, SetT<B>> f) {

        return of(unwrap().map(stream -> ReactiveSeq.fromIterable(stream)
                                                    .flatMap(a -> f.apply(a)
                                                                   .unwrap()
                                                                   .stream())
                                                    .flatMap(a -> a.stream())
                                                    .toSet()));
    }

    /**
     * Lift a function into one that accepts and returns an SetT
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling (via Set) and iteration (via Stream) to an existing function
     * <pre>
     * {@code 
     * Function<Integer,Integer> add2 = i -> i+2;
    	Function<SetT<Integer>, SetT<Integer>> optTAdd2 = SetT.lift(add2);
    	
    	Stream<Integer> nums = Stream.of(1,2);
    	AnyM<Stream<Integer>> stream = AnyM.ofMonad(asSet(nums));
    	
    	Set<Integer> results = optTAdd2.apply(SetT.fromStream(stream))
    									.unwrap()
    									.<Optional<Set<Integer>>>unwrap().get();
    	
    	
    	//asSet(3,4);
     * 
     * 
     * }</pre>
     * 
     * 
     * @param fn Function to enhance with functionality from Set and another monad type
     * @return Function that accepts and returns an SetT
     */
    public static <U, R> Function<SetT<U>, SetT<R>> lift(Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns  SetTs
     * This allows multiple monad types to add functionality to existing functions and methods
     * 
     * e.g. to add list handling (via Set), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
     * to an existing function
     * 
     * <pre>
     * {@code 
     *BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
    	BiFunction<SetT<Integer>,SetT<Integer>, SetT<Integer>> optTAdd2 = SetT.lift2(add);
    	
    	Streamable<Integer> threeValues = Streamable.of(1,2,3);
    	AnyM<Integer> stream = AnyM.fromStreamable(threeValues);
    	AnyM<Set<Integer>> streamOpt = stream.map(this::asSet);
    	
    	CompletableFuture<Set<Integer>> two = CompletableFuture.completedFuture(asSet(2));
    	AnyM<Set<Integer>> future=  AnyM.fromCompletableFuture(two);
    	Set<Integer> results = optTAdd2.apply(SetT.of(streamOpt),SetT.of(future))
    									.unwrap()
    									.<Stream<Set<Integer>>>unwrap()
    									.flatMap(i->i.stream())
    									.collect(Collectors.toSet());
    		//asSet(3,4);							
      }
      </pre>
     * @param fn BiFunction to enhance with functionality from Set and another monad type
     * @return Function that accepts and returns an SetT
     */
    public static <U1, U2, R> BiFunction<SetT<U1>, SetT<U2>, SetT<R>> lift2(BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an SetT from an AnyM that contains a monad type that contains type other than Set
     * The values in the underlying monad will be mapped to Set<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Set
     * @return SetT
     */
    public static <A> SetT<A> fromAnyM(AnyM<A> anyM) {
        return of(anyM.map(SetT::asSet));
    }

    static <T> Set<T> asSet(T... elements) {
        return new HashSet<T>(
                              Arrays.asList(elements));
    }

    /**
     * Construct an SetT from an AnyM that wraps a monad containing  Sets
     * 
     * @param monads AnyM that contains a monad wrapping an Set
     * @return SetT
     */
    public static <A> SetT<A> of(AnyM<Set<A>> monads) {
        return Matchables.anyM(monads)
                         .visit(v -> SetTValue.of(v), s -> SetTSeq.of(s));
    }

    /**
     * Create a SetT from an AnyM that wraps a monad containing a Stream
     * 
     * @param monads
     * @return
     */
    public static <A> SetT<A> fromStream(AnyM<Stream<A>> monads) {
        return of(monads.map(s -> s.collect(Collectors.toSet())));
    }

    public static <A> SetTValue<A> fromAnyMValue(AnyMValue<A> anyM) {
        return SetTValue.fromAnyM(anyM);
    }

    public static <A> SetTSeq<A> fromAnyMSeq(AnyMSeq<A> anyM) {
        return SetTSeq.fromAnyM(anyM);
    }

    public static <A> SetTSeq<A> fromIterable(Iterable<? extends Set<A>> iterableOfSets) {
        return SetTSeq.of(AnyM.fromIterable(iterableOfSets));
    }

    public static <A> SetTSeq<A> fromStream(Stream<? extends Set<A>> streamOfSets) {
        return SetTSeq.of(AnyM.fromStream(streamOfSets));
    }

    public static <A> SetTSeq<A> fromPublisher(Publisher<? extends Set<A>> publisherOfSets) {
        return SetTSeq.of(AnyM.fromPublisher(publisherOfSets));
    }

    public static <A, V extends MonadicValue<? extends Set<A>>> SetTValue<A> fromValue(V monadicValue) {
        return SetTValue.fromValue(monadicValue);
    }

    public static <A> SetTValue<A> fromOptional(Optional<? extends Set<A>> optional) {
        return SetTValue.of(AnyM.fromOptional(optional));
    }

    public static <A> SetTValue<A> fromFuture(CompletableFuture<Set<A>> future) {
        return SetTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A> SetTValue<A> fromIterableValue(Iterable<? extends Set<A>> iterableOfSets) {
        return SetTValue.of(AnyM.fromIterableValue(iterableOfSets));
    }

    public static <T> SetTSeq<T> emptySet() {
        return SetTSeq.emptySet();
    }

    /* (non-Javadoc)
    * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
    */
    @Override
    default <U> SetT<U> cast(Class<? extends U> type) {
        return (SetT<U>) FoldableTransformerSeq.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> SetT<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (SetT<R>) FoldableTransformerSeq.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> SetT<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, Supplier<? extends R> otherwise) {
        return (SetT<R>) FoldableTransformerSeq.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> SetT<U> ofType(Class<? extends U> type) {

        return (SetT<U>) FoldableTransformerSeq.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default SetT<T> filterNot(Predicate<? super T> fn) {

        return (SetT<T>) FoldableTransformerSeq.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    default SetT<T> notNull() {

        return (SetT<T>) FoldableTransformerSeq.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default SetT<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {

        return (SetT<T>) FoldableTransformerSeq.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycle(int)
     */
    @Override
    default SetT<T> cycle(int times) {

        return (SetT<T>) FoldableTransformerSeq.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    default SetT<T> cycle(Monoid<T> m, int times) {

        return (SetT<T>) FoldableTransformerSeq.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default SetT<T> cycleWhile(Predicate<? super T> predicate) {

        return (SetT<T>) FoldableTransformerSeq.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default SetT<T> cycleUntil(Predicate<? super T> predicate) {

        return (SetT<T>) FoldableTransformerSeq.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> SetT<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (SetT<R>) FoldableTransformerSeq.super.zip(other, zipper);
    }

    @Override
    default <U, R> SetT<R> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (SetT<R>) FoldableTransformerSeq.super.zip(other, zipper);
    }

    @Override
    default <U, R> SetT<R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (SetT<R>) FoldableTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zipStream(java.util.stream.Stream)
     */
    @Override
    default <U> SetT<Tuple2<T, U>> zip(Stream<? extends U> other) {

        return (SetT) FoldableTransformerSeq.super.zip(other);
    }

    @Override
    default <U> SetT<Tuple2<T, U>> zip(Iterable<? extends U> other) {

        return (SetT) FoldableTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip(org.jooq.lambda.Seq)
     */
    @Override
    default <U> SetT<Tuple2<T, U>> zip(Seq<? extends U> other) {

        return (SetT) FoldableTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> SetT<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {

        return (SetT) FoldableTransformerSeq.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> SetT<Tuple4<T, T2, T3, T4>> zip4(Stream<? extends T2> second, Stream<? extends T3> third, Stream<? extends T4> fourth) {

        return (SetT) FoldableTransformerSeq.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#zipWithIndex()
     */
    @Override
    default SetT<Tuple2<T, Long>> zipWithIndex() {

        return (SetT<Tuple2<T, Long>>) FoldableTransformerSeq.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sliding(int)
     */
    @Override
    default SetT<ListX<T>> sliding(int windowSize) {

        return (SetT<ListX<T>>) FoldableTransformerSeq.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sliding(int, int)
     */
    @Override
    default SetT<ListX<T>> sliding(int windowSize, int increment) {

        return (SetT<ListX<T>>) FoldableTransformerSeq.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> SetT<C> grouped(int size, Supplier<C> supplier) {

        return (SetT<C>) FoldableTransformerSeq.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default SetT<ListX<T>> groupedUntil(Predicate<? super T> predicate) {

        return (SetT<ListX<T>>) FoldableTransformerSeq.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    default SetT<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (SetT<ListX<T>>) FoldableTransformerSeq.super.groupedStatefullyWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default SetT<ListX<T>> groupedWhile(Predicate<? super T> predicate) {

        return (SetT<ListX<T>>) FoldableTransformerSeq.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> SetT<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {

        return (SetT<C>) FoldableTransformerSeq.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> SetT<C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {

        return (SetT<C>) FoldableTransformerSeq.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(int)
     */
    @Override
    default SetT<ListX<T>> grouped(int groupSize) {

        return (SetT<ListX<T>>) FoldableTransformerSeq.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    default <K, A, D> SetT<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {

        return (SetT) FoldableTransformerSeq.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#grouped(java.util.function.Function)
     */
    @Override
    default <K> SetT<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {

        return (SetT) FoldableTransformerSeq.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#distinct()
     */
    @Override
    default SetT<T> distinct() {

        return (SetT<T>) FoldableTransformerSeq.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    default SetT<T> scanLeft(Monoid<T> monoid) {

        return (SetT<T>) FoldableTransformerSeq.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> SetT<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {

        return (SetT<U>) FoldableTransformerSeq.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    default SetT<T> scanRight(Monoid<T> monoid) {

        return (SetT<T>) FoldableTransformerSeq.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> SetT<U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (SetT<U>) FoldableTransformerSeq.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sorted()
     */
    @Override
    default SetT<T> sorted() {

        return (SetT<T>) FoldableTransformerSeq.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sorted(java.util.Comparator)
     */
    @Override
    default SetT<T> sorted(Comparator<? super T> c) {

        return (SetT<T>) FoldableTransformerSeq.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#takeWhile(java.util.function.Predicate)
     */
    @Override
    default SetT<T> takeWhile(Predicate<? super T> p) {

        return (SetT<T>) FoldableTransformerSeq.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#dropWhile(java.util.function.Predicate)
     */
    @Override
    default SetT<T> dropWhile(Predicate<? super T> p) {

        return (SetT<T>) FoldableTransformerSeq.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#takeUntil(java.util.function.Predicate)
     */
    @Override
    default SetT<T> takeUntil(Predicate<? super T> p) {

        return (SetT<T>) FoldableTransformerSeq.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#dropUntil(java.util.function.Predicate)
     */
    @Override
    default SetT<T> dropUntil(Predicate<? super T> p) {

        return (SetT<T>) FoldableTransformerSeq.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#dropRight(int)
     */
    @Override
    default SetT<T> dropRight(int num) {

        return (SetT<T>) FoldableTransformerSeq.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#takeRight(int)
     */
    @Override
    default SetT<T> takeRight(int num) {

        return (SetT<T>) FoldableTransformerSeq.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skip(long)
     */
    @Override
    default SetT<T> skip(long num) {

        return (SetT<T>) FoldableTransformerSeq.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skipWhile(java.util.function.Predicate)
     */
    @Override
    default SetT<T> skipWhile(Predicate<? super T> p) {

        return (SetT<T>) FoldableTransformerSeq.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skipUntil(java.util.function.Predicate)
     */
    @Override
    default SetT<T> skipUntil(Predicate<? super T> p) {

        return (SetT<T>) FoldableTransformerSeq.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limit(long)
     */
    @Override
    default SetT<T> limit(long num) {

        return (SetT<T>) FoldableTransformerSeq.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limitWhile(java.util.function.Predicate)
     */
    @Override
    default SetT<T> limitWhile(Predicate<? super T> p) {

        return (SetT<T>) FoldableTransformerSeq.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limitUntil(java.util.function.Predicate)
     */
    @Override
    default SetT<T> limitUntil(Predicate<? super T> p) {

        return (SetT<T>) FoldableTransformerSeq.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#intersperse(java.lang.Object)
     */
    @Override
    default SetT<T> intersperse(T value) {

        return (SetT<T>) FoldableTransformerSeq.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#reverse()
     */
    @Override
    default SetT<T> reverse() {

        return (SetT<T>) FoldableTransformerSeq.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#shuffle()
     */
    @Override
    default SetT<T> shuffle() {

        return (SetT<T>) FoldableTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#skipLast(int)
     */
    @Override
    default SetT<T> skipLast(int num) {

        return (SetT<T>) FoldableTransformerSeq.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#limitLast(int)
     */
    @Override
    default SetT<T> limitLast(int num) {

        return (SetT<T>) FoldableTransformerSeq.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#onEmpty(java.lang.Object)
     */
    @Override
    default SetT<T> onEmpty(T value) {

        return (SetT<T>) FoldableTransformerSeq.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default SetT<T> onEmptyGet(Supplier<? extends T> supplier) {

        return (SetT<T>) FoldableTransformerSeq.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> SetT<T> onEmptyThrow(Supplier<? extends X> supplier) {

        return (SetT<T>) FoldableTransformerSeq.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#shuffle(java.util.Random)
     */
    @Override
    default SetT<T> shuffle(Random random) {

        return (SetT<T>) FoldableTransformerSeq.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#slice(long, long)
     */
    @Override
    default SetT<T> slice(long from, long to) {

        return (SetT<T>) FoldableTransformerSeq.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> SetT<T> sorted(Function<? super T, ? extends U> function) {
        return (SetT) FoldableTransformerSeq.super.sorted(function);
    }

}
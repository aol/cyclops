package com.aol.cyclops.control;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.util.function.*;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;
import org.reactivestreams.Publisher;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.monads.AnyMSeqImpl;
import com.aol.cyclops.internal.monads.AnyMValueImpl;

import com.aol.cyclops.types.EmptyUnit;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.To;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.Unwrapable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.anyM.Witness.completableFuture;
import com.aol.cyclops.types.anyM.Witness.eval;
import com.aol.cyclops.types.anyM.Witness.ior;
import com.aol.cyclops.types.anyM.Witness.list;
import com.aol.cyclops.types.anyM.Witness.maybe;
import com.aol.cyclops.types.anyM.Witness.optional;
import com.aol.cyclops.types.anyM.Witness.set;
import com.aol.cyclops.types.anyM.Witness.stream;
import com.aol.cyclops.types.anyM.Witness.streamable;
import com.aol.cyclops.types.anyM.Witness.tryType;
import com.aol.cyclops.types.anyM.Witness.xor;
import com.aol.cyclops.types.anyM.Witness.future;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.extensability.FunctionalAdapter;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.stream.ToStream;
import com.aol.cyclops.util.Optionals;

/**
 * 
 * Wrapper for Any Monad type
 * 
 * There are two subsclass of AnyM - @see {@link AnyMValue} and  @see {@link AnyMSeq}. 
 * AnyMValue is used to represent Monads that wrap a single value such as {@link Optional}, {@link CompletableFuture}, {@link Maybe}, {@link Eval}, {@link Xor}, {@link Try}, {@link Ior}, {@link FeatureToggle}
 * AnyMSeq is used to represent Monads that wrap an aggregation of values such as {@link Stream}, {@link LazyFutureStream}, {@link List}, {@link Set}, {@link Streamable}
 * 
 * Use AnyM to create your monad wrapper.
 * AnyM.fromXXXX methods can create the appropriate AnyM type for a range of known monad types.
 * 
 * <pre>
 * {@code 
 *    AnyMValue<String> monad1 = AnyM.fromOptional(Optional.of("hello"));
 *    
 *    AnyMSeq<String> monad2 = AnyM.fromStream(Stream.of("hello","world"));
 *  
 * }
 * </pre>
 * 
 * Wrapped monads can be unwrapped via the unwrap method, or converted to the desired type via toXXXX methods
 * 
 *
 * 
 * @author johnmcclean
 *
 * @param <T> type data wrapped by the underlying monad
 */
public interface AnyM<W extends WitnessType<W>,T> extends   Unwrapable,
                                                            To<AnyM<W,T>>,
                                                            EmptyUnit<T>,
                                                            Unit<T>,
                                                            Foldable<T>,
                                                            Functor<T>,
                                                            ToStream<T>,
                                                            Publisher<T> {
   
    
    default <U> AnyMSeq<W,U> unitIterator(Iterator<U> U){
        return (AnyMSeq<W,U>)adapter().unitIterator(U);
    }

    <R> AnyM<W,R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn);
    <R> AnyM<W,R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn);
    <R> AnyM<W,R> flatMapS(Function<? super T, ? extends Stream<? extends R>> fn);
    default <R> AnyM<W,R> flatMapA(Function<? super T, ? extends AnyM<W,? extends R>> fn){
        return adapter().flatMap(this, fn);
    }
    default <R> AnyM<W,R> map(Function<? super T,? extends R> fn){
        return adapter().map(this, fn);
    }
    default <T> AnyM<W,T> fromIterable(Iterable<T> t){
        return  (AnyM<W,T>)adapter().unitIterator(t.iterator());
    }
    /**
     * Construct a new instanceof AnyM using the type of the underlying wrapped monad
     * 
     * <pre>
     * {@code
     *   AnyM<Integer> ints = AnyM.fromList(Arrays.asList(1,2,3);
     *   AnyM<String> string = ints.unit("hello");
     * }
     * </pre>
     * 
     * @param t to embed inside the monad wrapped by AnyM
     * @return Newly instantated AnyM
     */
    @Override
    default <T> AnyM<W,T> unit(T t){
        return adapter().unit(t);
    }
    
    /**
     * Applicative 'ap' method to use fluently
     * 
     * <pre>
     * {@code 
     *    AnyM<optional,Function<Integer,Integer>> add = AnyM.fromNullable(this::add2);
     *    add.to(AnyM::ap)
     *       .apply(AnyM.ofNullable(10));
     *   
     *    //AnyM[12] //add 2
     * 
     * }
     * </pre>
     * 
     * @param fn Function inside an Applicative
     * @return Function to apply an Applicative's value to function
     */
    public static <W extends WitnessType<W>,T,R> Function<AnyM<W,T>,AnyM<W,R>> ap(AnyM<W, Function<T,R>> fn){
        return apply->apply.adapter().ap(fn,apply);
    }
    /**
     * Applicative ap2 method to use fluently to apply to a curried function
     * <pre>
     * {@code 
     *    AnyM<optional,Function<Integer,Function<Integer,Integer>>> add = AnyM.fromNullable(Curry.curry2(this::add));
     *    add.to(AnyM::ap2)
     *       .apply(AnyM.ofNullable(10),AnyM.ofNullable(20));
     *   
     *    //AnyM[30] //add together
     * 
     * }
     * </pre>
     * @param fn Curried function inside an Applicative
     * @return Function to apply two Applicative's values to a function
     */
    public static <W extends WitnessType<W>,T,T2,R> BiFunction<AnyM<W,T>,AnyM<W,T2>,AnyM<W,R>> ap2(AnyM<W, Function<T,Function<T2,R>>> fn){
        return (apply1,apply2)->apply1.adapter().ap2(fn,apply1,apply2);
    }

    /**
     * Perform a filter operation on the wrapped monad instance e.g.
     * 
     * <pre>
     * {@code
     *   AnyM.fromOptional(Optional.of(10)).filter(i->i<10);
     * 
     *   //AnyM[Optional.empty()]
     *   
     *   AnyM.fromStream(Stream.of(5,10)).filter(i->i<10);
     *   
     *   //AnyM[Stream[5]]
     * }
     * 
     * 
     * </pre>
     * 
     * @param fn Filtering predicate
     * @return Filtered AnyM
     */
    default  AnyM<W,T> filter(Predicate<? super T> fn){
        return adapter().filter(this, fn);
    }
   /**
    1. remove filterable
    2. create filterableAnyM subclass with filter operations
    3. remove AnyMValue / AnyMseq ?
    4. Add combine iterable / zip
    5. traverse / sequence methods
    6. remove bind method
    
    Monad transformers
    1. 1 type only AnyM
    2. map / filter (if filterable) / flatMap / flatMapT / zip - combine / fold - reduce operations
       on nested data structures (reduce etc all via map)
    3.  **/
    default <R> AnyM<W,R> coflatMapA(final Function<? super AnyM<W,T>, R> mapper) {
        return unit(Lambda.λ(()->mapper.apply(this))).map(Supplier::get);
    }
    
    
    default AnyM<W,AnyM<W,T>> nestA() {
        return unit(this);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.EmptyUnit#emptyUnit()
     */
    @Override
    default <T> Unit<T> emptyUnit(){
        return adapter().empty();
    }

    /**
     * Tests for equivalency between two AnyM types
     * 
     * <pre>
     * {@code
     *    boolean eqv = AnyM.fromOptional(Optional.of(1)).eqv(AnyM.fromStream(Stream.of(1)));
     *    //true
     *     boolean eqv = AnyM.fromOptional(Optional.of(1)).eqv(AnyM.fromStream(Stream.of(1,2)));
     *    //false
     * }
     * </pre>
     * 
     * @param t AnyM to check for equivalence with this AnyM
     * @return true if monads are equivalent
     */
    default boolean eqv(final AnyM<?,T> t) {
        return Predicates.eqvIterable(t)
                         .test(this);
    }

    /**
     * Allows structural matching on the value / seq nature of this AnyM.
     * If this AnyM can only store a single value an Xor.secondary with type AnyMValue is returned
     * If this AnyM can  store one or many values an Xor.primary with type AnyMSeq is returned
     * 
     * <pre>
     * {@code
     *    AnyM<String> monad;
     *    
     *    monad.matchable().visit(v->handleValue(v.get()),s->handleSequence(s.toList()));
     * }
     * </pre>
     * 
     * 
     * @return An Xor for pattern matching either an AnyMValue or AnyMSeq
     */
    Xor<AnyMValue<W,T>, AnyMSeq<W,T>> matchable();




    /* 
     * Convert this AnyM to an extended Stream (ReactiveSeq)
     * 
     * <pre>
     * {@code 
     *    AnyM<Integer> monad =  AnyM.fromOptional(Optional.of(10));
     *    
     *    Stream<Integer> stream = monad.stream();
     *    //ReactiveSeq[10]
     * }
     * </pre>
     * 
     */
    @Override
    default ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(this);
    }

    

    /**
     * Perform a peek operation on the wrapped monad e.g.
     * 
     * <pre>
     * {@code 
     *   AnyM.fromCompletableFuture(CompletableFuture.supplyAsync(()->loadData())
     *       .peek(System.out::println)
     * }
     * </pre>
     * 
     * @param c Consumer to accept current data
     * @return AnyM after peek operation
     */
    @Override
    default AnyM<W,T> peek(Consumer<? super T> c){
        return (AnyM<W, T>) Functor.super.peek(c);
    }



    /**
     * join / flatten one level of a nested hierarchy
     * 
     * @return Flattened / joined one level
     */ 
    static <W extends WitnessType<W>,T1> AnyM<W,T1> flatten(AnyM<W,AnyM<W,T1>> nested){
        return nested.flatMapA(Function.identity());
    }

   
    /**
     * Aggregate the contents of this Monad and the supplied Monad 
     * 
     * <pre>{@code 
     * 
     * AnyM.fromStream(Stream.of(1,2,3,4))
     * 							.aggregate(anyM(Optional.of(5)))
     * 
     * AnyM[Stream[List[1,2,3,4,5]]
     * 
     * List<Integer> result = AnyM.fromStream(Stream.of(1,2,3,4))
     * 							.aggregate(anyM(Optional.of(5)))
     * 							.toSequence()
     *                          .flatten()
     * 							.toList();
    	
    	assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
    	}</pre>
     * 
     * @param next Monad to aggregate content with
     * @return Aggregated Monad
     */
    default AnyM<W,List<T>> aggregate(AnyM<W,T> next){
        return unit(Stream.concat(matchable().visit(value -> value.stream(), seq -> seq.stream()), next.matchable()
                                  .visit(value -> value.stream(),
                                         seq -> seq.stream()))
                    .collect(Collectors.toList()));
    }

   
    

    /**
     * Construct an AnyM wrapping a new empty instance of the wrapped type 
     * 
     * e.g.
     * <pre>
     * {@code 
     * Any<Integer> ints = AnyM.fromStream(Stream.of(1,2,3));
     * AnyM<Integer> empty=ints.empty();
     * }
     * </pre>
     * @return Empty AnyM
     */
    default <T> AnyM<W,T> empty(){
        return adapter().empty();
    }

    
    /**
     * @return String representation of this AnyM
     */
    @Override
    public String toString();

    /**
     * Construct an AnyM instance that wraps a range from start (inclusive) to end (exclusive) provided
     * 
     * The AnyM will contain a SequenceM over the spefied range
     * 
     * @param start Inclusive start of the range
     * @param end Exclusive end of the range
     * @return AnyM range
     */
    public static AnyMSeq<stream,Integer> fromRange(final int start, final int end) {

        return AnyM.fromStream(ReactiveSeq.range(start, end));
    }

    /**
     * Construct an AnyM instance that wraps a range from start (inclusive) to end (exclusive) provided
     * 
     * The AnyM will contain a SequenceM over the spefied range
     * 
     * @param start Inclusive start of the range
     * @param end Exclusive end of the range
     * @return AnyM range
     */
    public static AnyMSeq<stream,Long> fromRangeLong(final long start, final long end) {

        return AnyM.fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Wrap a Streamable inside an AnyM
     * 
     * @param streamable wrap
     * @return AnyMSeq generated from a ToStream type
     */
    public static <T> AnyMSeq<streamable,T> fromStreamable(final ToStream<T> streamable) {
        Objects.requireNonNull(streamable);
        
        return AnyMFactory.instance.seq(Streamable.fromIterable(streamable),Witness.streamable.INSTANCE);
    }

    /**
     * Create an AnyM from a List
     * 
     * This AnyM will convert the List to a Stream under the covers, but will rematerialize the Stream as List
     * if wrap() is called
     * 
     * 
     * @param list to wrap inside an AnyM
     * @return AnyM wrapping a list
     */
    public static <T> AnyMSeq<list,T> fromList(final List<T> list) {
        Objects.requireNonNull(list);
        return AnyMFactory.instance.seq(list,Witness.list.INSTANCE);
    }
    public static <W extends Witness.CollectionXWitness<W>,T> AnyMSeq<W,T> fromCollectionX(final CollectionX<T> collection, W witness) {
        Objects.requireNonNull(collection);
        return AnyMFactory.instance.seq(collection,witness);

    }

    /**
     * Create an AnyM from a Set
     * 
     * This AnyM will convert the Set to a Stream under the covers, but will rematerialize the Stream as Set
     * if wrap() is called
     * 
     * 
     * @param set to wrap inside an AnyM
     * @return AnyM wrapping a Set
     */
    public static <T> AnyMSeq<set,T> fromSet(final Set<T> set) {
        Objects.requireNonNull(set);
        return AnyMFactory.instance.seq(set, Witness.set.INSTANCE);
    }

    /**
     * Create an AnyM wrapping a Stream of the supplied data
     * 
     * @param streamData values to populate a Stream
     * @return AnyMSeq wrapping a Stream that encompasses the supplied Array
     */
    public static <T> AnyMSeq<stream,T> fromArray(final T... streamData) {
        return AnyMFactory.instance.seq(Stream.of(streamData),Witness.stream.INSTANCE);
    }

    /**
     * Create an AnyM wrapping a Stream of the supplied data
     * 
     * Identical to fromArray, exists as it may appear functionally more obvious to users than fromArray (which fits the convention)
     * 
     * @param streamData values to populate a Stream
     * @return  AnyMSeq wrapping a Stream that encompasses the supplied Array
     */
    public static <T> AnyMSeq<stream,T> streamOf(final T... streamData) {
        return AnyMFactory.instance.seq(Stream.of(streamData),Witness.streamable.INSTANCE);
    }

    /**
     * Construct an AnyM that wraps a reactive-streams Publisher. If there is no registered Comprehender for the supplied Publisher, this method
     *  will attempt to convert the Publisher to a type that cyclops-react can understand.
     *  
     *  <pre>
     *  {@code 
     *       AnyMSeq<Integer> flux = AnyM.fromPublisher(Flux.just(10,20,30));
     *       
     *       //with cyclops-reactor
     *       //AnyM[Flux[Integer]]]
     *       
     *       //without cyclops-reactor
     *       //AnyM[ReactiveSeq[Integer]]]
     *  }
     *  </pre>
     *  It is generally safer to define a Comprehender and use a non-converting call to generate the wrapped AnyM
     *       (e.g. Reactor.Flux in cyclops-reactor for Pivotal Reactor Publishers)
     * 
     * @param publisher Publisher to wrap inside an AnyM
     * @return AnyMSeq that wraps a Publisher
     */
    public static <T> AnyMSeq<stream,T> fromPublisher(final Publisher<T> publisher) {
        return AnyMFactory.instance.seq(ReactiveSeq.fromPublisher(publisher),Witness.stream.INSTANCE);
    }

    /**
     * Create an AnyM instance that wraps a Stream
     * 
     * @param stream Stream to wrap
     * @return AnyM that wraps the provided Stream
     */
    public static <T> AnyMSeq<stream,T> fromStream(final Stream<T> stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream,Witness.stream.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an IntStream
     * 
     * @param stream IntStream to wrap
     * @return AnyM that wraps the provided IntStream
     */
    public static AnyMSeq<stream,Integer> fromIntStream(final IntStream stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream.boxed(),Witness.stream.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an DoubleStream
     * 
     * @param stream DoubleStream to wrap
     * @return AnyM that wraps the provided DoubleStream
     */
    public static AnyMSeq<stream,Double> fromDoubleStream(final DoubleStream stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream.boxed(),Witness.stream.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an LongStream
     * 
     * @param stream LongStream to wrap
     * @return AnyM that wraps the provided LongStream
     */
    public static AnyMSeq<stream,Long> fromLongStream(final LongStream stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream.boxed(),Witness.stream.INSTANCE);
    }

    /**
     * Create an AnyM instance that wraps an Optional
     * 
     * @param opt Optional to wrap
     * @return AnyM that wraps the provided Optonal
     */
    public static <T> AnyMValue<optional,T> fromOptional(final Optional<T> opt) {
        Objects.requireNonNull(opt);
        return AnyMFactory.instance.value(opt, Witness.optional.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an OptionalDouble
     * 
     * @param optional Optional to wrap
     * @return AnyM that wraps the provided OptonalDouble
     */
    public static AnyMValue<optional,Double> fromOptionalDouble(final OptionalDouble optional) {
        Objects.requireNonNull(optional);
        return AnyMFactory.instance.value(Optionals.optional(optional), Witness.optional.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an OptionalLong
     * 
     * @param optional OptionalLong to wrap
     * @return AnyM that wraps the provided OptonalLong
     */
    public static AnyMValue<optional,Long> fromOptionalLong(final OptionalLong optional) {
        Objects.requireNonNull(optional);
        return AnyMFactory.instance.value(Optionals.optional(optional), Witness.optional.INSTANCE);

    }

    /**
     * Create an AnyM instance that wraps an OptionalInt
     * 
     * @param optional OptionalInt to wrap
     * @return AnyM that wraps the provided OptonalInt
     */
    public static AnyMValue<optional,Integer> fromOptionalInt(final OptionalInt optional) {
        Objects.requireNonNull(optional);
        return AnyMFactory.instance.value(Optionals.optional(optional), Witness.optional.INSTANCE);
    }

    /**
     * Create an AnyM instance that wraps a CompletableFuture
     * 
     * @param future CompletableFuture to wrap
     * @return AnyM that wraps the provided CompletableFuture
     */
    public static <T> AnyMValue<completableFuture,T> fromCompletableFuture(final CompletableFuture<T> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future, Witness.completableFuture.INSTANCE);

    }

    /**
     * Create an AnyMValue instance that wraps an Xor
     * 
     * @param xor Xor to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Xor
     */
    public static <T> AnyMValue<xor,T> fromXor(final Xor<?, T> xor) {
        Objects.requireNonNull(xor);
        return AnyMFactory.instance.value(xor,Witness.xor.INSTANCE);
    }



    /**
     * Create an AnyMValue instance that wraps a Try
     * 
     * @param trySomething to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Try
     */
    public static <T> AnyMValue<tryType,T> fromTry(final Try<T, ?> trySomething) {
        Objects.requireNonNull(trySomething);
        return AnyMFactory.instance.value(trySomething, Witness.tryType.INSTANCE);
    }

    /**
     *  Create an AnyMValue instance that wraps an Ior
     * 
     * @param ior to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Ior
     */
    public static <T> AnyMValue<ior,T> fromIor(final Ior<?, T> ior) {
        Objects.requireNonNull(ior);
        return AnyMFactory.instance.value(ior, Witness.ior.INSTANCE);
    }

    /**
     * Create an AnyMValue instance that wraps an Eval
     * 
     * @param eval to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Eval
     */
    public static <T> AnyMValue<eval,T> fromEval(final Eval<T> eval) {
        Objects.requireNonNull(eval);
        return AnyMFactory.instance.value(eval, Witness.eval.INSTANCE);
    }
    public static <W extends Witness.MonadicValueWitness<W>,T> AnyMValue<W,T> fromMonadicValue(final MonadicValue<T> eval,W witness) {
        Objects.requireNonNull(eval);
        return AnyMFactory.instance.value(eval, witness);

    }

    /**
     * Create an AnyMValue instance that wraps a FutureW
     * 
     * @param future to wrap inside an AnyM
     * @return AnyM instance that wraps the provided future
     */
    public static <T> AnyMValue<future,T> fromFutureW(final FutureW<T> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future, Witness.future.INSTANCE);
    }

    /**
     * Create an AnyMValue instance that wraps a {@link Maybe}
     * 
     * @param maybe to wrap inside an AnyM
     * @return instance that wraps the provided Maybe
     */
    public static <T> AnyMValue<maybe,T> fromMaybe(final Maybe<T> maybe) {
        Objects.requireNonNull(maybe);
        return AnyMFactory.instance.value(maybe, Witness.maybe.INSTANCE);
    }


    /**
     * Create an AnyMValue that wraps the untyped monad
     * 
     * @param monad to wrap inside an AnyM
     * @return AnyMValue that wraps the supplied monad
    */
    public static <W extends WitnessType<W>,T> AnyMValue<W,T> ofValue(final Object monad, W witness) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.value(monad,witness);
    } 
    public static <W extends WitnessType<W>,T> AnyMValue<W,T> ofValue(final Object monad,FunctionalAdapter<?> adapter) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.value(monad,adapter);
    } 

    /**
     * Create an AnyMSeq that wraps the untyped monad
     * 
     * @param monad to wrap inside an AnyM
     * @return AnyMSeq that wraps the supplied monad
     */
    public static <W extends WitnessType<W>,T> AnyMSeq<W,T> ofSeq(final Object monad, W witness) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.seq(monad,witness);
    }

    /**
     * Generate an AnyM that wraps an Optional from the provided nullable object
     * 
     * @param nullable - Nullable object to generate an optional from
     * @return AnyM wrapping an Optional created with the supplied nullable
     */
    public static <T> AnyMValue<optional,T> ofNullable(final Object nullable) {
        return AnyMFactory.instance.value(Optional.ofNullable(nullable),Witness.optional.INSTANCE);
    }

    /**
     * Take an iterable containing Streamables and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(Streamable.of(1,2,3),Streamable.of(10,20,30));
     *     
     *     //List[AnyM[Streamable[1,2,3],Streamable[10,20,30]]]
     * }
     * 
     * @param anyM Iterable containing Streamables
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMSeq<streamable,T>> listFromStreamable(final Iterable<Streamable<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromStreamable(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Streams and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStream(Arrays.asList(Stream.of(1,2,3),Stream.of(10,20,30));
     *     
     *     //List[AnyM[Stream[1,2,3],Stream[10,20,30]]]
     * }
     * 
     * @param anyM Iterable containing Streams
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMSeq<stream,T>> listFromStream(final Iterable<Stream<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromStream(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Optionals and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(Optional.of(1),Optional.of(10));
     *     
     *     //List[AnyM[Optional[1],Optional[10]]]
     * }
     * 
     * @param anyM Iterable containing Optional
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMValue<optional,T>> listFromOptional(final Iterable<Optional<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromOptional(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing CompletableFutures and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(CompletableFuture.completedFuture(1),CompletableFuture.supplyAsync(()->10));
     *     
     *     //List[AnyM[CompletableFuture[1],CompleteableFuture[10]]]
     * }
     * 
     * @param anyM Iterable containing CompletableFuture
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMValue<completableFuture,T>> listFromCompletableFuture(final Iterable<CompletableFuture<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromCompletableFuture(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Streamables and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(Streamable.of(1,2,3),Streamable.of(10,20,30));
     *     
     *     //List[AnyM[Streamable[1,2,3],Streamable[10,20,30]]]
     * }
     * 
     * @param anyM Iterable containing Streamables
     * @return List of AnyMs
     
    public static <T> ListX<AnyMSeq<T>> listFromIterable(final Iterable<Iterable<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromIterable(i))
                            .collect(ListX.listXCollector());
    }
*/
    /**
     * Take an iterable containing Xors and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromXor(Arrays.asList(Xor.primary(1),Xor.secondary(10));
     *     
     *     //List[AnyM[Xor:primary[1],Xor:secondaary[10]]]
     * }
     * 
     * @param anyM Iterable containing Xors
     * @return List of AnyMs
     */
    public static <ST, T> ListX<AnyMValue<xor,T>> listFromXor(final Iterable<Xor<ST, T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromXor(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Iors and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromXor(Arrays.asList(Ior.primary(1),Ior.secondary(10));
     *     
     *     //List[AnyM[Ior:primary[1],Ior:secondaary[10]]]
     * }
     * 
     * @param anyM Iterable containing Iors
     * @return List of AnyMs
     */
    public static <ST, T> ListX<AnyMValue<ior,T>> listFromIor(final Iterable<Ior<ST, T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromIor(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Maybes and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromXor(Arrays.asList(Maybe.just(1),Maybe.just(10));
     *     
     *     //List[AnyM[Maybe[1],Maybe[10]]]
     * }
     * 
     * @param anyM Iterable containing Maybes
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMValue<maybe,T>> listFromMaybe(final Iterable<Maybe<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromMaybe(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Evals and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromXor(Arrays.asList(Eval.now(1),Eval.now(10));
     *     
     *     //List[AnyM[Eval[1],Eval[10]]]
     * }
     * 
     * @param anyM Iterable containing Maybes
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMValue<eval,T>> listFromEval(final Iterable<Eval<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromEval(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing FutureW and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromXor(Arrays.asList(FutureW.ofResult(1),FutureW.ofResult(10));
     *     
     *     //List[AnyM[FutureW[1],FutureW[10]]]
     * }
     * 
     * @param anyM Iterable containing Maybes
     * @return List of AnyMs
     */
    public static <T> ListX<AnyMValue<future,T>> listFromFutureW(final Iterable<FutureW<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromFutureW(i))
                            .collect(ListX.listXCollector());
    }

    /**
     * Take an iterable containing Streamables and convert them into a List of AnyMs
     * e.g.
     * {@code 
     *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(Arrays.asList(1,2,3).iterator(),Arrays.asList(10,20,30)).iterator();
     *     
     *     //List[AnyM[Stream[1,2,3],Stream[10,20,30]]]
     * }
     * 
     * @param anyM Iterable containing Iterators
     * @return List of AnyMs
     
    public static <T> ListX<AnyMSeq<T>> listFromIterator(final Iterable<Iterator<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromIterable(() -> i))
                            .collect(ListX.listXCollector());
    }*/

    /**
     * Convert a Collection of Monads to a Monad with a List
     * 
     * <pre>
     * {@code
        List<CompletableFuture<Integer>> futures = createFutures();
        AnyM<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));
    
       //where AnyM wraps  CompletableFuture<List<Integer>>
      }</pre>
     * 
     * 
     * @param seq Collection of monads to convert
     * @return Monad with a List
     */
    public static <W extends WitnessType<W>,T1> AnyM<W,ListX<T1>> sequence(final Collection<? extends AnyM<W,T1>> seq,W w) {
        return sequence(seq.stream(),w).map(ListX::fromStreamS);
    }

    /**
     * Convert a Collection of Monads to a Monad with a List applying the supplied function in the process
     * 
     * <pre>
     * {@code 
       List<CompletableFuture<Integer>> futures = createFutures();
       AnyM<List<String>> futureList = AnyMonads.traverse(AsAnyMList.anyMList(futures), (Integer i) -> "hello" +i);
        }
        </pre>
     * 
     * @param seq Collection of Monads
     * @param fn Function to apply 
     * @return Monad with a list
     */
    public static <W extends WitnessType<W>,T, R> AnyM<W,ListX<R>> traverse(final Collection<? extends AnyM<W,T>> seq, final Function<? super T, ? extends R> fn,W w) {
        return sequence(seq,w).map(l->l.map(fn));
    }

    

    static class AnyMFactory {
        static AnyMFactory instance = new AnyMFactory();

        public <W extends WitnessType<W>,T> AnyMValue<W,T> value(final Object o,FunctionalAdapter<?> adapter) {
            if (o instanceof AnyMValue)
                return (AnyMValue<W,T>) o;
            
            return new AnyMValueImpl<W,T>(
                                        o,(FunctionalAdapter)adapter);
        }

        /**
         * Non-type safe way to wrap a supported monad type in an AnyMValue
         * 
         * @param o Monad to wrap
         * @return AnyMValue wrapping supplied monad
         */
        public <W extends WitnessType<W>,T> AnyMValue<W,T> value(final Object o,W comp) {
            if (o instanceof AnyMValue)
                return (AnyMValue<W,T>) o;
            
            return new AnyMValueImpl<W,T>(
                                      o,comp.adapter());
        }

        /**
         * Non-type safe way to wrap a supported monad type in an AnyMSeq
         * 
         * @param o Monad to wrap
         * @return AnyMValue wrapping supplied monad
         */
        public <W extends WitnessType<W>,T> AnyMSeq<W,T> seq(final Object o, WitnessType comp) {
            if (o instanceof AnyMSeq)
                return (AnyMSeq<W,T>) o;
            return new AnyMSeqImpl<W,T>(o,comp.adapter());
        }

    }
    public static  <W extends WitnessType<W>,T> AnyM<W,Stream<T>> sequence(Stream<? extends AnyM<W,T>> stream, W witness) {
        FunctionalAdapter<W> c = witness.adapter();
        AnyM<W,Stream<T>> identity = c.unit(Stream.empty());
       
        BiFunction<AnyM<W,Stream<T>>,AnyM<W,T>,AnyM<W,Stream<T>>> combineToStream = (acc,next) -> c.ap2(c.unit(Lambda.l2((Stream<T> a)->(T b)->Stream.concat(a,Stream.of(b)))),acc,next);

        BinaryOperator<AnyM<W,Stream<T>>> combineStreams = (a,b)->a;//a.apply(b, (s1,s2)->s1);  

        return stream.reduce(identity,combineToStream,combineStreams);
    }
    public static  <W extends WitnessType<W>,T,R> AnyM<W,Stream<R>> traverse(Function<T,R> fn,Stream<AnyM<W,T>> stream, W witness) {
       return sequence(stream.map(h->h.map(fn)),witness);
    }
    FunctionalAdapter<W> adapter();

    public static <W extends WitnessType<W>,T> AnyM<W, T> narrow(AnyM<W, ? extends T> anyM){
        return (AnyM<W,T>)anyM;
    }
    
    /**
   * Lift a function so it accepts an AnyM and returns an AnyM (any monad)
   * AnyM view simplifies type related challenges.
   * 
   * @param fn
   * @return
   */
  public static <W extends WitnessType<W>,U, R> MFunc1<W,U,R> liftF(final Function<? super U, ? extends R> fn) {
      return u -> u.map(input -> fn.apply(input));
  }

  /**
   * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
   * AnyM view simplifies type related challenges. The actual native type is not specified here.
   * 
   * e.g.
   * 
   * <pre>{@code
   *  BiFunction<AnyM<Integer>,AnyM<Integer>,AnyM<Integer>> add = Monads.liftF2(this::add);
   *   
   *  Optional<Integer> result = add.apply(getBase(),getIncrease());
   *  
   *   private Integer add(Integer a, Integer b){
              return a+b;
      }
   * }</pre>
   * The add method has no null handling, but we can lift the method to Monadic form, and use Optionals to automatically handle null / empty value cases.
   * 
   * 
   * @param fn BiFunction to lift
   * @return Lifted BiFunction
   */
  public static <W extends WitnessType<W>,U1, U2, R> MFunc2<W,U1,U2,R> liftF2(
          final BiFunction<? super U1, ? super U2, ? extends R> fn) {

      return (u1, u2) -> u1.flatMapA(input1 -> u2.map(input2 -> fn.apply(input1, input2)));
  }


  /**
   * Lift a TriFunction into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
   * 
   * <pre>
   * {@code
   * TriFunction<AnyM<Double>,AnyM<Entity>,AnyM<W,String>,AnyM<Integer>> fn = liftF3(this::myMethod);
   *    
   * }
   * </pre>
   * 
   * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
   * 
   * @param fn Function to lift
   * @return Lifted function
   */
  public static <W extends WitnessType<W>,U1, U2, U3, R> F3<AnyM<W,U1>, AnyM<W,U2>, AnyM<W,U3>, AnyM<W,R>> liftF3(
          final Function3<? super U1, ? super U2, ? super U3, ? extends R> fn) {
      return (u1, u2, u3) -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3))));
  }


  /**
   * Lift a QuadFunction into Monadic form.
   * 
   * @param fn Quad funciton to lift
   * @return Lifted Quad function
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, R> F4<AnyM<W,U1>, AnyM<W,U2>, AnyM<W,U3>, AnyM<W,U4>, AnyM<W,R>> liftF4(
          final Function4<? super U1, ? super U2, ? super U3, ? super U4, ? extends R> fn) {

      return (u1, u2, u3, u4) -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.map(input4 -> fn.apply(input1, input2, input3, input4)))));
  }

  /**
   * Lift a  jOOλ Function5 (5 parameters) into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted Function
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, U5, R> F5<AnyM<W,U1>, AnyM<W,U2>, AnyM<W,U3>, AnyM<W,U4>, AnyM<W,U5>, AnyM<W,R>> liftF5(
          final Function5<? super U1, ? super U2, ? super U3, ? super U4, ? super U5, ? extends R> fn) {

      return (u1, u2, u3, u4,
              u5) -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.flatMapA(input4 -> u5.map(input5 -> fn.apply(input1, input2, input3,
                                                                                                                       input4, input5))))));
  }

  

  /**
   * Lift a Curried Function {@code(2 levels a->b->fn.apply(a,b) )} into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, AnyM<W,R>>> liftF2(final Function<U1, Function<U2, R>> fn) {
      return u1 -> u2 -> u1.flatMapA(input1 -> u2.map(input2 -> fn.apply(input1)
                                                              .apply(input2)));

  }

  /**
   * Lift a Curried Function {@code(3 levels a->b->c->fn.apply(a,b,c) )} into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, U3, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, Function<AnyM<W,U3>, AnyM<W,R>>>> liftF3(
          final Function<? super U1, Function<? super U2, Function<? super U3, ? extends R>>> fn) {
      return u1 -> u2 -> u3 -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.map(input3 -> fn.apply(input1)
                                                                                      .apply(input2)
                                                                                      .apply(input3))));
  }

  /**
   * Lift a Curried Function {@code(4 levels a->b->c->d->fn.apply(a,b,c,d) )} into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, Function<AnyM<W,U3>, Function<AnyM<W,U4>, AnyM<W,R>>>>> liftF4(
          final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, ? extends R>>>> fn) {

      return u1 -> u2 -> u3 -> u4 -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.map(input4 -> fn.apply(input1)
                                                                                                              .apply(input2)
                                                                                                              .apply(input3)
                                                                                                              .apply(input4)))));
  }

  /**
   * Lift a Curried Function {@code (5 levels a->b->c->d->e->fn.apply(a,b,c,d,e) ) }into Monadic form
   * 
   * @param fn Function to lift
   * @return Lifted function 
   */
  public static <W extends WitnessType<W>,U1, U2, U3, U4, U5, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, Function<AnyM<W,U3>, Function<AnyM<W,U4>, Function<AnyM<W,U5>, AnyM<W,R>>>>>> liftF5(
          final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, Function<? super U5, ? extends R>>>>> fn) {

      return u1 -> u2 -> u3 -> u4 -> u5 -> u1.flatMapA(input1 -> u2.flatMapA(input2 -> u3.flatMapA(input3 -> u4.flatMapA(input4 -> u5.map(input5 -> fn.apply(input1)
                                                                                                                                      .apply(input2)
                                                                                                                                      .apply(input3)
                                                                                                                                      .apply(input4)
                                                                                                                                      .apply(input5))))));
  }

}
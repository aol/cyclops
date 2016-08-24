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

import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.monads.transformers.seq.CompletableFutureTSeq;
import com.aol.cyclops.control.monads.transformers.seq.EvalTSeq;
import com.aol.cyclops.control.monads.transformers.seq.FutureWTSeq;
import com.aol.cyclops.control.monads.transformers.seq.MaybeTSeq;
import com.aol.cyclops.control.monads.transformers.seq.OptionalTSeq;
import com.aol.cyclops.control.monads.transformers.seq.TryTSeq;
import com.aol.cyclops.control.monads.transformers.seq.XorTSeq;
import com.aol.cyclops.control.monads.transformers.values.CompletableFutureTValue;
import com.aol.cyclops.control.monads.transformers.values.EvalTValue;
import com.aol.cyclops.control.monads.transformers.values.FutureWTValue;
import com.aol.cyclops.control.monads.transformers.values.MaybeTValue;
import com.aol.cyclops.control.monads.transformers.values.OptionalTValue;
import com.aol.cyclops.control.monads.transformers.values.TryTValue;
import com.aol.cyclops.control.monads.transformers.values.XorTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.internal.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.internal.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.internal.monads.AnyMonads;
import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.types.EmptyUnit;
import com.aol.cyclops.types.FlatMap;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.Unwrapable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.stream.ToStream;
import com.aol.cyclops.util.function.Predicates;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;
import com.aol.cyclops.util.stream.Streamable;

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
public interface AnyM<T> extends Unwrapable, EmptyUnit<T>, Unit<T>, Foldable<T>, Functor<T>, FlatMap<T>, ToStream<T> {
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
    default boolean eqv(AnyM<T> t) {
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
    Xor<AnyMValue<T>, AnyMSeq<T>> matchable();

    /**
     * Perform a flatMap operation that will only work as normal for AnyMSeq types, but for AnyMValue (which can only hold a single value) 
     * will take the first value from the Iterable returned.
     * 
     * @param fn FlatMaping function
     * @return AnyM with flattening transformation
     */
    <R> AnyM<R> flatMapFirst(Function<? super T, ? extends Iterable<? extends R>> fn);

    /**
     * Perform a flatMap operation that will only work as normal for AnyMSeq types, but for AnyMValue (which can only hold a single value) 
     * will take the first value from the Publisher returned.
     * 
     * @param fn FlatMaping function
     * @return AnyM with flattening transformation
     */
    <R> AnyM<R> flatMapFirstPublisher(Function<? super T, ? extends Publisher<? extends R>> fn);
     

    /**
     * Collect the contents of the monad wrapped by this AnyM into supplied collector
     * A mutable reduction operation equivalent to Stream#collect
     * 
     * <pre>
     * {@code 
     *      AnyM<Integer> monad1 = AnyM.fromStream(Stream.of(1,2,3));
     *      AnyM<Integer> monad2 = AnyM.fromOptional(Optional.of(1));
     *      
     *      List<Integer> list1 = monad1.collect(Collectors.toList());
     *      List<Integer> list2 = monad2.collect(Collectors.toList());
     *      
     * }
     * </pre>
     * 
     * 
     * @param collector JDK collector to perform mutable reduction
     * @return Reduced value
     */
    <R, A> R collect(Collector<? super T, A, R> collector);

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
    public ReactiveSeq<T> stream();

    /**
     * Sequence the contents of a Monad.  e.g.
     * Turn an <pre>
     *  {@code Optional<List<Integer>>  into Stream<Integer> }</pre>
     * 
     * <pre>{@code
     * List<Integer> list = AnyM.fromOptional(Optional.of(Arrays.asList(1,2,3,4,5,6)))
                                            .<Integer>toSequence(c->c.stream())
                                            .collect(Collectors.toList());
        
        
        assertThat(list,hasItems(1,2,3,4,5,6));
        
     * 
     * }</pre>
     * @param fn Function used to convert contents to Stream
     * 
     * @return A Sequence that wraps a Stream
     */
    <NT> ReactiveSeq<NT> toReactiveSeq(Function<? super T, ? extends Stream<? extends NT>> fn);

    /* 
     * Unwraps the wrapped monad
     * 
     * <pre>
     * {@code 
     *    AnyM<Integer> monad = AnyM.fromStream(Stream.of(1,2,3));
     *    
     *    Stream<Integer> stream = monad.unwrap();
     * }
     * </pre>
     * 
     * (non-Javadoc)
    * @see com.aol.cyclops.sequence.Unwrapable#unwrap()
    */
    @Override
    <R> R unwrap();

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
     * @param p Filtering predicate
     * @return Filtered AnyM
     */
    AnyM<T> filter(Predicate<? super T> p);

    /**
     * Perform a map operation on the wrapped monad instance e.g. 
     * 
     * <pre>
     * {@code 
     *   AnyM.fromIterable(Try.runWithCatch(this::loadData))
     *   	 .map(data->transform(data))		
     *   
     *   AnyM.fromStream(Stream.of(1,2,3))
     *       .map(i->i+2);
     *   
     *   //AnyM[Stream[3,4,5]]
     * }
     * </pre>
     * @param fn Mapping function
     * @return AnyM transformed by the mapping function
     */
    <R> AnyM<R> map(Function<? super T, ? extends R> fn);

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
    AnyM<T> peek(Consumer<? super T> c);

    /**
     * Perform a looser typed flatMap / bind operation
     * The return type can be another type other than the host type
     * 
     * Note the modified javaslang monad laws are not applied during the looser typed bind operation
     * The modification being used to work around the limits of the Java type system.
     * 
     * <pre>
     * {@code 
     * AnyM<List<Integer>> m  = AnyM.fromStream(Stream.of(Arrays.asList(1,2,3),Arrays.asList(1,2,3)));
       AnyM<Integer> intM = m.bind(Collection::stream);
     * }
     * </pre>
     * 
     * @param fn flatMap function
     * @return flatMapped monad
    */
    <R> AnyM<R> bind(Function<? super T, ?> fn);


    /**
     * join / flatten one level of a nested hierarchy
     * 
     * @return Flattened / joined one level
     */
    <T1> AnyM<T1> flatten();

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
    AnyM<List<T>> aggregate(AnyM<T> next);

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
     * @param value to embed inside the monad wrapped by AnyM
     * @return Newly instantated AnyM
     */
    public <T> AnyM<T> unit(T value);

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
    public <T> AnyM<T> empty();

    /**
     * Perform a monadic reduction using a Monoid that combines AnyM types.
     * 
     * 
     * e.g. 
     * <pre>{@code 
     *   Monoid<AnyMValue<Integer>> optionalAdd = Monoid.of(AnyM.fromOptional(Optional.of(0)), (a,b)-> AnyM.fromOptional(Optional.of(a.get()+b.get())));
    	
    	AnyM.fromStream(Stream.of(2,8,3,1)).reduceM(optionalAdd);
    	
    	//AnyM[Optional(14)];
    	  
    	 	 
    	}</pre>
    * 
    * 
    * 
    * @param reducer An identity value (approx. a seed) and BiFunction with a single type to reduce this anyM
    * @return Reduced AnyM
    */
    AnyMValue<T> reduceMValue(Monoid<AnyMValue<T>> reducer);
    /**
     * Perform a monadic reduction using a Monoid that combines AnyM types.
     * 
     * 
     * e.g. 
     * <pre>{@code 
     *   Monoid<AnyMSeq<Integer>> listAddFirst = Monoid.of(AnyM.fromList(Arrays.asList(0)), (a,b)-> AnyM.fromList(Arrays.asList(a.get(0)+b.get(0))));
        
        AnyM.fromStream(Stream.of(2,8,3,1)).reduceM(listAddFirst);
        
        //AnyM[Optional(14)];
          
             
        }</pre>
    * 
    * 
    * 
    * @param reducer An identity value (approx. a seed) and BiFunction with a single type to reduce this anyM
    * @return Reduced AnyM
    */
    AnyMSeq<T> reduceMSeq(Monoid<AnyMSeq<T>> reducer);

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
    public static AnyMSeq<Integer> fromRange(int start, int end) {

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
    public static AnyMSeq<Long> fromRangeLong(long start, long end) {

        return AnyM.fromStream(ReactiveSeq.rangeLong(start, end));
    }

    /**
     * Wrap a Streamable inside an AnyM
     * 
     * @param streamable wrap
     * @return AnyMSeq generated from a ToStream type
     */
    public static <T> AnyMSeq<T> fromStreamable(ToStream<T> streamable) {
        Objects.requireNonNull(streamable);
        return AnyMFactory.instance.convertSeq(streamable);
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
    public static <T> AnyMSeq<T> fromList(List<T> list) {
        Objects.requireNonNull(list);
        return AnyMFactory.instance.seq(list);
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
    public static <T> AnyMSeq<T> fromSet(Set<T> set) {
        Objects.requireNonNull(set);
        return AnyMFactory.instance.seq(set);
    }

    /**
     * Create an AnyM wrapping a Stream of the supplied data
     * 
     * @param streamData values to populate a Stream
     * @return AnyMSeq wrapping a Stream that encompasses the supplied Array
     */
    public static <T> AnyMSeq<T> fromArray(T... streamData) {
        return AnyMFactory.instance.seq(Stream.of(streamData));
    }

    /**
     * Create an AnyM wrapping a Stream of the supplied data
     * 
     * Identical to fromArray, exists as it may appear functionally more obvious to users than fromArray (which fits the convention)
     * 
     * @param streamData values to populate a Stream
     * @return  AnyMSeq wrapping a Stream that encompasses the supplied Array
     */
    public static <T> AnyMSeq<T> streamOf(T... streamData) {
        return AnyMFactory.instance.seq(Stream.of(streamData));
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
    public static <T> AnyMSeq<T> fromPublisher(Publisher<T> publisher) {
        return AnyMFactory.instance.convertSeq(publisher);
    }

    /**
     * Create an AnyM instance that wraps a Stream
     * 
     * @param stream Stream to wrap
     * @return AnyM that wraps the provided Stream
     */
    public static <T> AnyMSeq<T> fromStream(Stream<T> stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream);
    }

    /**
     * Create an AnyM instance that wraps an IntStream
     * 
     * @param stream IntStream to wrap
     * @return AnyM that wraps the provided IntStream
     */
    public static AnyMSeq<Integer> fromIntStream(IntStream stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream.boxed());
    }

    /**
     * Create an AnyM instance that wraps an DoubleStream
     * 
     * @param stream DoubleStream to wrap
     * @return AnyM that wraps the provided DoubleStream
     */
    public static AnyMSeq<Double> fromDoubleStream(DoubleStream stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream.boxed());
    }

    /**
     * Create an AnyM instance that wraps an LongStream
     * 
     * @param stream LongStream to wrap
     * @return AnyM that wraps the provided LongStream
     */
    public static AnyMSeq<Long> fromLongStream(LongStream stream) {
        Objects.requireNonNull(stream);
        return AnyMFactory.instance.seq(stream.boxed());
    }

    /**
     * Create an AnyM instance that wraps an Optional
     * 
     * @param optional Optional to wrap
     * @return AnyM that wraps the provided Optonal
     */
    public static <T> AnyMValue<T> fromOptional(Optional<T> optional) {
        Objects.requireNonNull(optional);
        return AnyMFactory.instance.value(optional);
    }

    /**
     * Create an AnyM instance that wraps an OptionalDouble
     * 
     * @param optional Optional to wrap
     * @return AnyM that wraps the provided OptonalDouble
     */
    public static AnyMValue<Double> fromOptionalDouble(OptionalDouble optional) {
        Objects.requireNonNull(optional);
        return AnyMFactory.instance.convertValue(optional);
    }

    /**
     * Create an AnyM instance that wraps an OptionalLong
     * 
     * @param optional OptionalLong to wrap
     * @return AnyM that wraps the provided OptonalLong
     */
    public static AnyMValue<Long> fromOptionalLong(OptionalLong optional) {
        Objects.requireNonNull(optional);
        return AnyMFactory.instance.convertValue(optional);
    }

    /**
     * Create an AnyM instance that wraps an OptionalInt
     * 
     * @param optional OptionalInt to wrap
     * @return AnyM that wraps the provided OptonalInt
     */
    public static AnyMValue<Integer> fromOptionalInt(OptionalInt optional) {
        Objects.requireNonNull(optional);
        return AnyMFactory.instance.convertValue(optional);
    }

    /**
     * Create an AnyM instance that wraps a CompletableFuture
     * 
     * @param future CompletableFuture to wrap
     * @return AnyM that wraps the provided CompletableFuture
     */
    public static <T> AnyMValue<T> fromCompletableFuture(CompletableFuture<T> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future);
    }

    /**
     * Create an AnyMValue instance that wraps an Xor
     * 
     * @param xor Xor to wrap inside an AnyM
     * @return AnyM instance that wraps the provided Xor
     */
    public static <T> AnyMValue<T> fromXor(Xor<?, T> xor) {
        Objects.requireNonNull(xor);
        return AnyMFactory.instance.value(xor);
    }

    /**
     * Create an AnyMValue instance that wraps an FeatureToggle
     * 
     * @param featureToggle to wrap inside an AnyM
     * @return AnyM instance that wraps the provided FeatureToggle
     */
    public static <T> AnyMValue<T> fromFeatureToggle(FeatureToggle<T> featureToggle) {
        Objects.requireNonNull(featureToggle);
        return AnyMFactory.instance.value(featureToggle);
    }

    public static <T> AnyMValue<T> fromTry(Try<T, ?> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future);
    }

    public static <T> AnyMValue<T> fromIor(Ior<?, T> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future);
    }

    public static <T> AnyMValue<T> fromEval(Eval<T> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future);
    }

    public static <T> AnyMValue<T> fromFutureW(FutureW<T> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future);
    }

    public static <T> AnyMValue<T> fromMaybe(Maybe<T> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future);
    }

    public static <T> AnyMValue<T> fromEvalTValue(EvalTValue<T> evalT) {
        Objects.requireNonNull(evalT);
        return AnyMFactory.instance.value(evalT);
    }

    public static <T> AnyMValue<T> fromMaybeTValue(MaybeTValue<T> maybeT) {
        Objects.requireNonNull(maybeT);
        return AnyMFactory.instance.value(maybeT);
    }

    public static <T> AnyMValue<T> fromOptionalTValue(OptionalTValue<T> optionalT) {
        Objects.requireNonNull(optionalT);
        return AnyMFactory.instance.value(optionalT);
    }

    public static <T> AnyMValue<T> fromCompletableFutureTValue(CompletableFutureTValue<T> futureT) {
        Objects.requireNonNull(futureT);
        return AnyMFactory.instance.value(futureT);
    }

    public static <ST, PT> AnyMValue<PT> fromXorTValue(XorTValue<ST, PT> xorT) {
        Objects.requireNonNull(xorT);
        return AnyMFactory.instance.value(xorT);
    }

    public static <T, X extends Throwable> AnyMValue<T> fromTryTValue(TryTValue<T, X> tryT) {
        Objects.requireNonNull(tryT);
        return AnyMFactory.instance.value(tryT);
    }

    public static <ST, PT> AnyMSeq<PT> fromXorTSeq(XorTSeq<ST, PT> xorT) {
        Objects.requireNonNull(xorT);
        return AnyMFactory.instance.seq(xorT);
    }

    public static <T, X extends Throwable> AnyMSeq<T> fromTryTSeq(TryTSeq<T, X> tryT) {
        Objects.requireNonNull(tryT);
        return AnyMFactory.instance.seq(tryT);
    }

    public static <T> AnyMSeq<T> fromEvalTSeq(EvalTSeq<T> evalT) {
        Objects.requireNonNull(evalT);
        return AnyMFactory.instance.seq(evalT);
    }

    public static <T> AnyMSeq<T> fromMaybeTSeq(MaybeTSeq<T> maybeT) {
        Objects.requireNonNull(maybeT);
        return AnyMFactory.instance.seq(maybeT);
    }

    public static <T> AnyMSeq<T> fromOptionalTSeq(OptionalTSeq<T> optionalT) {
        Objects.requireNonNull(optionalT);
        return AnyMFactory.instance.seq(optionalT);
    }

    public static <T> AnyMSeq<T> fromCompletableFutureTSeq(CompletableFutureTSeq<T> futureT) {
        Objects.requireNonNull(futureT);
        return AnyMFactory.instance.seq(futureT);
    }

    public static <T> AnyMValue<T> fromFutureWTValue(FutureWTValue<T> futureT) {
        Objects.requireNonNull(futureT);
        return AnyMFactory.instance.value(futureT);
    }

    public static <T> AnyMSeq<T> fromFutureWTSeq(FutureWTSeq<T> futureT) {
        Objects.requireNonNull(futureT);
        return AnyMFactory.instance.seq(futureT);
    }

    public static <T> AnyMSeq<T> fromListT(ListT<T> listT) {
        Objects.requireNonNull(listT);
        return AnyMFactory.instance.seq(listT);
    }

    public static <T> AnyMSeq<T> fromStreamT(StreamT<T> streamT) {
        Objects.requireNonNull(streamT);
        return AnyMFactory.instance.seq(streamT);
    }

    public static <T> AnyMSeq<T> fromStreamableT(StreamableT<T> streamT) {
        Objects.requireNonNull(streamT);
        return AnyMFactory.instance.seq(streamT);
    }

    public static <T> AnyMSeq<T> fromSetT(SetT<T> setT) {
        Objects.requireNonNull(setT);
        return AnyMFactory.instance.seq(setT);
    }

    /**
     * Create an AnyM instance that wraps an Iterable
     * 
     * @param iterable Iterable to wrap
     * @return AnyM that wraps the provided Iterable
     */
    public static <T> AnyMSeq<T> fromIterable(Iterable<T> iterable) {
        Objects.requireNonNull(iterable);
        if (iterable instanceof AnyMSeq)
            return (AnyMSeq<T>) iterable;
        if (iterable instanceof List)
            iterable = ListX.fromIterable(iterable);
        if (iterable instanceof Set)
            iterable = SetX.fromIterable(iterable);
        return AnyMFactory.instance.convertSeq(iterable);
    }

    /**
     * Use this method to create an AnyMValue from an Iterable.
     * This exists as many monadic value types in Java libraries implement iterable (such 
     * as Optional in Javaslang or FunctionalJava).
     * 
     * @param iterable To generate AnyMValue from
     * @return AnyMValue wrapping the supplied Iterable
     */
    public static <T> AnyMValue<T> fromIterableValue(Iterable<T> iterable) {
        Objects.requireNonNull(iterable);
        return AnyMFactory.instance.value(iterable);
    }

    /**
     * Take the supplied object and always attempt to convert it to a Monad type
     * 
     * @param monad Monad to convert to a supported type and wrap inside an AnyMValue
     * @return AnyMValue that wraps the supplied converted
     */
    public static <T> AnyMValue<T> ofConvertableValue(Object monad) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.convertValue(monad);
    }

    public static <T> AnyMSeq<T> ofConvertableSeq(Object monad) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.convertSeq(monad);
    }

    public static <T> AnyMValue<T> ofValue(Object monad) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.value(monad);
    }

    public static <T> AnyMSeq<T> ofSeq(Object monad) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.seq(monad);
    }

    /**
     * Generate an AnyM that wraps an Optional from the provided nullable object
     * 
     * @param nullable - Nullable object to generate an optional from
     * @return AnyM wrapping an Optional created with the supplied nullable
     */
    public static <T> AnyMValue<T> ofNullable(Object nullable) {
        return AnyMFactory.instance.value(Optional.ofNullable(nullable));
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
    public static <T> List<AnyMSeq<T>> listFromStreamable(Iterable<Streamable<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromStreamable(i))
                            .collect(Collectors.toList());
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
    public static <T> List<AnyMSeq<T>> listFromStream(Iterable<Stream<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromStream(i))
                            .collect(Collectors.toList());
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
    public static <T> List<AnyMValue<T>> listFromOptional(Iterable<Optional<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromOptional(i))
                            .collect(Collectors.toList());
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
    public static <T> List<AnyMValue<T>> listFromCompletableFuture(Iterable<CompletableFuture<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromCompletableFuture(i))
                            .collect(Collectors.toList());
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
    public static <T> List<AnyMSeq<T>> listFromIterable(Iterable<Iterable<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromIterable(i))
                            .collect(Collectors.toList());
    }

    public static <ST, T> List<AnyMValue<T>> listFromXor(Iterable<Xor<ST, T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromXor(i))
                            .collect(Collectors.toList());
    }

    public static <ST, T> List<AnyMValue<T>> listFromIor(Iterable<Ior<ST, T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromIor(i))
                            .collect(Collectors.toList());
    }

    public static <T> List<AnyMValue<T>> listFromMaybe(Iterable<Maybe<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromMaybe(i))
                            .collect(Collectors.toList());
    }

    public static <T> List<AnyMValue<T>> listFromEval(Iterable<Eval<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromEval(i))
                            .collect(Collectors.toList());
    }

    public static <T> List<AnyMValue<T>> listFromFutureW(Iterable<FutureW<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromFutureW(i))
                            .collect(Collectors.toList());
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
     */
    public static <T> List<AnyMSeq<T>> listFromIterator(Iterable<Iterator<T>> anyM) {
        return StreamSupport.stream(anyM.spliterator(), false)
                            .map(i -> AnyM.fromIterable(() -> i))
                            .collect(Collectors.toList());
    }

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
    public static <T1> AnyMValue<ListX<T1>> sequence(Collection<? extends AnyM<T1>> seq) {
        return new AnyMonads().sequence(seq);
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
    public static <T, R> AnyMValue<ListX<R>> traverse(Collection<? extends AnyM<T>> seq, Function<? super T, ? extends R> fn) {
        return new AnyMonads().traverse(seq, fn);
    }

    /**
     * Convert a Stream of Monads to a Monad with a Stream applying the supplied function in the process
     * 
     * @param source  Stream to traverse
     * @param unitEmpty  Supplier to generate an AnyM with an Empty Stream
     * @param fn Mapping function
     * @return AnyM wrapping a Monad with a Stream transformed by the supplied mapping function
     *
     */
    public static <T, R> AnyM<Stream<R>> traverse(Stream<AnyM<T>> source, Supplier<AnyM<Stream<T>>> unitEmpty, Function<? super T, ? extends R> fn) {
        return sequence(source, unitEmpty).map(s -> s.map(fn));
    }

    /**
     * Convert a Stream of Monads to a Monad with a Stream
     * 
     * <pre>
     * {@code 
     * 
     *  
        Supplier<AnyM<Stream<Integer>>> unitEmpty = ()->AnyM.fromOptional(Optional.of(Stream.<Integer>empty()));
        
        AnyMValue<Integer> just = AnyM.fromOptional(Optional.of(10));
        Stream<AnyM<Integer>> streamOfOptionals = ReactiveSeq.of(just,AnyM.fromOptional(Optional.of(1)));
        
        AnyM<Stream<Integer>> optionalWithAStream  =AnyM.sequence(streamOfOptionals, unitEmpty);
        Optional<Stream<Integer>> optional = optionalWithAStream.unwrap();
       
     * }
     * </pre>
     * @param source Stream of monads to sequence
     * @param unitEmpty Supplier to generate an AnyM with an Empty Stream
     * @return  AnyM wrapping a Monad with a Stream
     */
    public static <T> AnyM<Stream<T>> sequence(Stream<? extends AnyM<T>> source, Supplier<? extends AnyM<Stream<T>>> unitEmpty) {
        Stream<AnyM<T>> narrowed = (Stream<AnyM<T>>) source;
        return narrowed.reduce((AnyM<Stream<T>>) unitEmpty.get(), (fl, fo) -> fl.flatMapFirst(a -> {
            Streamable<T> streamable = Streamable.fromStream(a);
            return fo.map(b -> Stream.concat(streamable.stream(), Stream.of(b)));
        }), (fa, fb) -> fa.flatMapFirst(a -> fb.map(b -> Stream.concat(a, b))));
    }

    /**
     * Lift a function so it accepts an AnyM and returns an AnyM (any monad)
     * 
     * 
     * @param fn Function to Lift into monadic form
     * @return A monadic function
     */
    public static <U, R> Function<AnyM<U>, AnyM<R>> liftM(Function<? super U, ? extends R> fn) {
        return u -> u.map(input -> fn.apply(input));
    }

    /**
     * Lift a function so it accepts an AnyM wrapped Monad and returns an AnyMwrapped Monad.
     * 
     * e.g.
     * 
     * <pre>
     * {@code
     * 	BiFunction<AnyM<Integer>,AnyM<Integer>,AnyM<Integer>> add = Monads.liftM2(this::add);
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
    public static <U1, U2, R> BiFunction<AnyM<U1>, AnyM<U2>, AnyM<R>> liftM2(BiFunction<? super U1, ? super U2, ? extends R> fn) {

        return (u1, u2) -> u1.bind(input1 -> u2.map(input2 -> fn.apply(input1, input2))
                                               .unwrap());
    }

    /**
     * Lift a TriFunction  into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
     * 
     * <pre>
     * {@code
     * TriFunction<AnyM<Double>,AnyM<Entity>,AnyM<String>,AnyM<Integer>> fn = liftM3(this::myMethod);
     *    
     * }
     * </pre>
     * 
     * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
     * 
     * @param fn Function to lift
     * @return Lifted function
     */
    public static <U1, U2, U3, R> TriFunction<AnyM<U1>, AnyM<U2>, AnyM<U3>, AnyM<R>> liftM3(
            TriFunction<? super U1, ? super U2, ? super U3, ? extends R> fn) {
        return (u1, u2, u3) -> u1.bind(input1 -> u2.bind(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3)))
                                                   .unwrap());
    }

   
    

    /**
     * Lift a QuadFunction into Monadic form.
     * 
     * @param fn Quad funciton to lift
     * @return Lifted Quad function
     */
    public static <U1, U2, U3, U4, R> QuadFunction<AnyM<U1>, AnyM<U2>, AnyM<U3>, AnyM<U4>, AnyM<R>> liftM4(
            QuadFunction<? super U1, ? super U2, ? super U3, ? super U4, ? extends R> fn) {

        return (u1, u2, u3, u4) -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.map(input4 -> fn.apply(input1, input2, input3, input4))
                                                                                           .unwrap())
                                                                         .unwrap())
                                                       .unwrap());
    }

    
    /**
     * Lift a QuintFunction (5 parameters) into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted Function
     */
    public static <U1, U2, U3, U4, U5, R> QuintFunction<AnyM<U1>, AnyM<U2>, AnyM<U3>, AnyM<U4>, AnyM<U5>, AnyM<R>> liftM5(
            QuintFunction<? super U1, ? super U2, ? super U3, ? super U4, ? super U5, ? extends R> fn) {

        return (u1, u2, u3, u4,
                u5) -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.bind(input4 -> u5.map(input5 -> fn.apply(input1, input2, input3,
                                                                                                                         input4, input5))
                                                                                                 .unwrap())
                                                                               .unwrap())
                                                             .unwrap())
                                           .unwrap());
    }

    /**
     * Lift a Curried Function {@code(2 levels a->b->fn.apply(a,b) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, R> Function<AnyM<U1>, Function<AnyM<U2>, AnyM<R>>> liftM2(Function<U1, Function<U2, R>> fn) {
        return u1 -> u2 -> u1.bind(input1 -> u2.map(input2 -> fn.apply(input1)
                                                                .apply(input2))
                                               .unwrap());

    }

    /**
     * Lift a Curried Function {@code(3 levels a->b->c->fn.apply(a,b,c) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, U3, R> Function<AnyM<U1>, Function<AnyM<U2>, Function<AnyM<U3>, AnyM<R>>>> liftM3(
            Function<? super U1, Function<? super U2, Function<? super U3, ? extends R>>> fn) {
        return u1 -> u2 -> u3 -> u1.bind(input1 -> u2.bind(input2 -> u3.map(input3 -> fn.apply(input1)
                                                                                        .apply(input2)
                                                                                        .apply(input3)))
                                                     .unwrap());
    }

    /**
     * Lift a Curried Function {@code(4 levels a->b->c->d->fn.apply(a,b,c,d) )} into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, U3, U4, R> Function<AnyM<U1>, Function<AnyM<U2>, Function<AnyM<U3>, Function<AnyM<U4>, AnyM<R>>>>> liftM4(
            Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, ? extends R>>>> fn) {

        return u1 -> u2 -> u3 -> u4 -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.map(input4 -> fn.apply(input1)
                                                                                                                .apply(input2)
                                                                                                                .apply(input3)
                                                                                                                .apply(input4))))
                                                           .unwrap());
    }

    /**
     * Lift a Curried Function {@code (5 levels a->b->c->d->e->fn.apply(a,b,c,d,e) ) }into Monadic form
     * 
     * @param fn Function to lift
     * @return Lifted function 
     */
    public static <U1, U2, U3, U4, U5, R> Function<AnyM<U1>, Function<AnyM<U2>, Function<AnyM<U3>, Function<AnyM<U4>, Function<AnyM<U5>, AnyM<R>>>>>> liftM5(
            Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, Function<? super U5, ? extends R>>>>> fn) {

        return u1 -> u2 -> u3 -> u4 -> u5 -> u1.bind(input1 -> u2.bind(input2 -> u3.bind(input3 -> u4.bind(input4 -> u5.map(input5 -> fn.apply(input1)
                                                                                                                                        .apply(input2)
                                                                                                                                        .apply(input3)
                                                                                                                                        .apply(input4)
                                                                                                                                        .apply(input5)))))
                                                                 .unwrap());
    }

    static class AnyMFactory {
        static AnyMFactory instance = new AnyMFactory();

     
        /**
         * Convert an object to an AnyMValue type if possible. If a registered monad comprehender exists the supplied object will
         * be wrapped as is, otherwise it will be converted into a support type (if possible). 
         * 
         * <pre>
         * {@code
         *     //Wrapped as is 
         *      
         *     AnyMValue<Integer> maybe = factory.convertValue(Maybe.just(10));
         *     //AnyMValue[Maybe[Integer]]]
         *     
         *     //Converted 
         *     
         *     AnyMValue<Integer> maybe = factory.convertValue(null);
         *     
         *     //AnyMValue[Optional[Integer]]
         *     
         * }
         * </pre>
         * 
         * 
         * @param o Object to convert
         * @return AnyMValue wrapping supplied Object
         */
        public <T> AnyMValue<T> convertValue(Object o) {

            if (new ComprehenderSelector().selectComprehender(o) instanceof InvokeDynamicComprehender)
                return new MonadWrapper<>(
                                          new MonadicConverters().convertToMonadicForm(o)).anyMValue();
            return new MonadWrapper<>(
                                      o).anyMValue();
        }

        /**
         * Convert an object to an AnyMSeq type if possible. If a registered monad comprehender exists the supplied object will
         * be wrapped as is, otherwise it will be converted into a support type (if possible). 
         * 
         * <pre>
         * {@code
         *     //Wrapped as is 
         *      
         *     AnyMSeq<Integer> maybe = factory.convertSeq(Stream.of(10));
         *     //AnyMValue[Stream[Integer]]]
         *     
         *     //Converted 
         *     BufferedReader reader;
         *     AnyMSeq<Integer> maybe = factory.convertSeq(reader);
         *     
         *     //AnyMSeq[ReactiveSeq[String]]
         *     
         * }
         * </pre>
         * 
         * 
         * @param o Object to convert
         * @return AnyMSeq wrapping supplied Object
         */
        public <T> AnyMSeq<T> convertSeq(Object o) {

            if (new ComprehenderSelector().selectComprehender(o) instanceof InvokeDynamicComprehender)
                return new MonadWrapper<>(
                                          new MonadicConverters().convertToMonadicForm(o)).anyMSeq();
            return new MonadWrapper<>(
                                      o).anyMSeq();
        }

        
        /**
         * Non-type safe way to wrap a supported monad type in an AnyMValue
         * 
         * @param o Monad to wrap
         * @return AnyMValue wrapping supplied monad
         */
        public <T> AnyMValue<T> value(Object o) {
            if (o instanceof AnyMValue)
                return (AnyMValue<T>) o;
            return new MonadWrapper<>(
                                      o).anyMValue();
        }

        /**
         * Non-type safe way to wrap a supported monad type in an AnyMSeq
         * 
         * @param o Monad to wrap
         * @return AnyMValue wrapping supplied monad
         */
        public <T> AnyMSeq<T> seq(Object o) {
            if (o instanceof AnyMSeq)
                return (AnyMSeq<T>) o;
            return new MonadWrapper<>(
                                      o).anyMSeq();
        }

    }

}
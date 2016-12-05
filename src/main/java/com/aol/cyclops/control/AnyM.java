package com.aol.cyclops.control;

import static com.aol.cyclops.Matchables.listT;
import static com.aol.cyclops.Matchables.streamT;
import static com.aol.cyclops.types.anyM.Witness.*;
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
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.monads.transformers.seq.CompletableFutureTSeq;
import com.aol.cyclops.control.monads.transformers.seq.EvalTSeq;
import com.aol.cyclops.control.monads.transformers.seq.FutureWTSeq;
import com.aol.cyclops.control.monads.transformers.seq.ListTSeq;
import com.aol.cyclops.control.monads.transformers.seq.MaybeTSeq;
import com.aol.cyclops.control.monads.transformers.seq.OptionalTSeq;
import com.aol.cyclops.control.monads.transformers.seq.SetTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamTSeq;
import com.aol.cyclops.control.monads.transformers.seq.StreamableTSeq;
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
import com.aol.cyclops.internal.comprehensions.comprehenders.CompletableFutureComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.EvalComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.FeatureToggleComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.FutureFunctorComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.IorComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.ListComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.MaybeComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.OptionalComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.SetComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.StreamComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.TryComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.XorComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.CompletableFutureTValueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.EvalTValueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.ListTValueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.MaybeTValueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.OptionalTValueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.SetTValueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.StreamTValueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.StreamableTValueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.TryTValueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.XorTValueComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.CompletableFutureTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.EvalTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.FutureWTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.ListTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.MaybeTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.OptionalTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.SetTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.StreamTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.StreamableTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.TryTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.comprehenders.transformers.seq.XorTSeqComprehender;
import com.aol.cyclops.internal.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.internal.monads.AnyMonads;
import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.types.EmptyUnit;
import com.aol.cyclops.types.FlatMap;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.To;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.Unwrapable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.anyM.Witness.optional;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.extensability.Comprehender;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.stream.ToStream;
import com.aol.cyclops.util.Optionals;
import com.aol.cyclops.util.function.Predicates;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;

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
public interface AnyM<W extends WitnessType,T> extends Unwrapable, To<AnyM<W,T>>, EmptyUnit<T>, Unit<T>, Foldable<T>, Functor<T>, FlatMap<T>, ToStream<T>,Publisher<T> {
   
    
    
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.EmptyUnit#emptyUnit()
     */
    @Override
    <T> Unit<T> emptyUnit() ;

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

    /**
     * Perform a flatMap operation that will only work as normal for AnyMSeq types, but for AnyMValue (which can only hold a single value) 
     * will take the first value from the Iterable returned.
     * 
     * @param fn FlatMaping function
     * @return AnyM with flattening transformation
     */
    <R> AnyM<W,R> flatMapFirst(Function<? super T, ? extends Iterable<? extends R>> fn);

    /**
     * Perform a flatMap operation that will only work as normal for AnyMSeq types, but for AnyMValue (which can only hold a single value) 
     * will take the first value from the Publisher returned.
     * 
     * @param fn FlatMaping function
     * @return AnyM with flattening transformation
     */
    <R> AnyM<W,R> flatMapFirstPublisher(Function<? super T, ? extends Publisher<? extends R>> fn);

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
    @Override
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
    AnyM<W,T> filter(Predicate<? super T> p);

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
    @Override
    <R> AnyM<W,R> map(Function<? super T, ? extends R> fn);

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
    AnyM<W,T> peek(Consumer<? super T> c);

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
    <R> AnyM<W,R> bind(Function<? super T, ?> fn);

    /**
     * join / flatten one level of a nested hierarchy
     * 
     * @return Flattened / joined one level
     */
    @Override
    <T1> AnyM<W,T1> flatten();

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
    AnyM<W,List<T>> aggregate(AnyM<W,T> next);

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
    @Override
    public <T> AnyM<W,T> unit(T value);

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
    public <T> AnyM<W,T> empty();

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
    AnyMValue<W,T> reduceMValue(Monoid<AnyMValue<W,T>> reducer);

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
    AnyMSeq<W,T> reduceMSeq(Monoid<AnyMSeq<W,T>> reducer);

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
     * @param optional Optional to wrap
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
     * Create an AnyMValue instance that wraps an FeatureToggle
     * 
     * @param featureToggle to wrap inside an AnyM
     * @return AnyM instance that wraps the provided FeatureToggle
     */
    public static <T> AnyMValue<featureToggle,T> fromFeatureToggle(final FeatureToggle<T> featureToggle) {
        Objects.requireNonNull(featureToggle);
        return AnyMFactory.instance.value(featureToggle,Witness.featureToggle.INSTANCE);
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

    /**
     * Create an AnyMValue instance that wraps a FutureW
     * 
     * @param future to wrap inside an AnyM
     * @return AnyM instance that wraps the provided future
     */
    public static <T> AnyMValue<futureW,T> fromFutureW(final FutureW<T> future) {
        Objects.requireNonNull(future);
        return AnyMFactory.instance.value(future, Witness.futureW.INSTANCE);
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
     * Create an AnyMValue instance that wraps an EvalTransformer {@link EvalTValue}
     * 
     * @param evalT  to wrap inside an AnyM
     * @return instance that wraps the provided EvalTransformer
    
    public static <T> AnyMValue<eval,T> fromEvalTValue(final EvalTValue<T> evalT) {
        Objects.requireNonNull(evalT);
        return AnyMFactory.instance.value(evalT, Witness.eval.INSTANCE);
    }
 */
    /**
     *  Create an AnyMValue instance that wraps an MaybeTransformer {@link MaybeTValue}
     * 
     * @param maybeT  to wrap inside an AnyM
     * @return instance that wraps the provided MaybeTransformer
     
    public static <T> AnyMValue<T> fromMaybeTValue(final MaybeTValue<T> maybeT) {
        Objects.requireNonNull(maybeT);
        return AnyMFactory.instance.value(maybeT,new MaybeTValueComprehender());
    }*/

    /**
     * Create an AnyMValue instance that wraps an OptionalTransformer {@link OptionalTValue}
     * 
     * @param optionalT to wrap inside an AnyM
     * @return instance that wraps the provided OptionalTransformer
    
    public static <T> AnyMValue<T> fromOptionalTValue(final OptionalTValue<T> optionalT) {
        Objects.requireNonNull(optionalT);
        return AnyMFactory.instance.value(optionalT,new OptionalTValueComprehender());
    }
 */
    /**
     * Create an AnyMValue instance that wraps an CompletableFutureTransformer {@link CompletableFutureTValue}
     * 
     * @param futureT  to wrap inside an AnyM
     * @return instance that wraps the provided CompletableFutureTransformer
     
    public static <T> AnyMValue<T> fromCompletableFutureTValue(final CompletableFutureTValue<T> futureT) {
        Objects.requireNonNull(futureT);
        return AnyMFactory.instance.value(futureT,new CompletableFutureTValueComprehender());
    }*/

    /**
     *  Create an AnyMValue instance that wraps an XorTransformer {@link CompletableFutureTValue}
     * 
     * @param xorT to wrap inside an AnyM
     * @return instance that wraps the provided XorTransformer
    
    public static <ST, PT> AnyMValue<PT> fromXorTValue(final XorTValue<ST, PT> xorT) {
        Objects.requireNonNull(xorT);
        return AnyMFactory.instance.value(xorT,new XorTValueComprehender());
    } */

    /**
     * Create an AnyMValue instance that wraps an TryTransformer {@link TryTValue}
     * 
     * @param tryT to wrap inside an AnyM
     * @return instance that wraps the provided TryTransformer
    
    public static <T, X extends Throwable> AnyMValue<T> fromTryTValue(final TryTValue<T, X> tryT) {
        Objects.requireNonNull(tryT);
        return AnyMFactory.instance.value(tryT,new TryTValueComprehender());
    } */

    /**
     * Create an AnyMSeq instance that wraps an XorTransformer {@link XorTSeq}
     * 
     * @param xorT to wrap inside an AnyM
     * @return instance that wraps the provided XorTransformer
     
    public static <ST, PT> AnyMSeq<PT> fromXorTSeq(final XorTSeq<ST, PT> xorT) {
        Objects.requireNonNull(xorT);
        return AnyMFactory.instance.seq(xorT, new XorTSeqComprehender());
    }*/

    /**
     * Create an AnyMSeq instance that wraps an TryTransformer {@link TryTSeq}
     * 
     * @param tryT to wrap inside an AnyM
     * @return instance that wraps the provided TryTransformer
     
    public static <T, X extends Throwable> AnyMSeq<T> fromTryTSeq(final TryTSeq<T, X> tryT) {
        Objects.requireNonNull(tryT);
        return AnyMFactory.instance.seq(tryT,new TryTSeqComprehender());
    }*/

    /**
     * Create an AnyMSeq instance that wraps an EvalTransformer {@link EvalTSeq}
     * 
     * @param evalT to wrap inside an AnyM
     * @return instance that wraps the provided EvalTransformer
     
    public static <T> AnyMSeq<T> fromEvalTSeq(final EvalTSeq<T> evalT) {
        Objects.requireNonNull(evalT);
        return AnyMFactory.instance.seq(evalT,new EvalTSeqComprehender());
    }
*/
    /**
     * Create an AnyMSeq instance that wraps an MaybeTransformer {@link MaybeTSeq}
     * 
     * @param maybeT to wrap inside an AnyM
     * @return instance that wraps the provided MaybeTransformer
     
    public static <T> AnyMSeq<T> fromMaybeTSeq(final MaybeTSeq<T> maybeT) {
        Objects.requireNonNull(maybeT);
        return AnyMFactory.instance.seq(maybeT,new MaybeTSeqComprehender());
    }
*/
    /**
     * Create an AnyMSeq instance that wraps an OptionalTransformer {@link OptionalTSeq}
     * 
     * @param optionalT to wrap inside an AnyM
     * @return instance that wraps the provided OptionalTransformer
     
    public static <T> AnyMSeq<T> fromOptionalTSeq(final OptionalTSeq<T> optionalT) {
        Objects.requireNonNull(optionalT);
        return AnyMFactory.instance.seq(optionalT,new OptionalTSeqComprehender());
    }*/

    /**
     *  Create an AnyMSeq instance that wraps an CompletableFutureTransformer {@link CompletableFutureTSeq}
     * 
     * @param futureT to wrap inside an AnyM
     * @return instance that wraps the provided CompletableFutureTransformer
    
    public static <T> AnyMSeq<T> fromCompletableFutureTSeq(final CompletableFutureTSeq<T> futureT) {
        Objects.requireNonNull(futureT);
        return AnyMFactory.instance.seq(futureT,new CompletableFutureTSeqComprehender());
    } */

    /**
     * Create an AnyMValue instance that wraps an FutureWTransformer {@link FutureWTSeq}
     * 
     * @param futureT to wrap inside an AnyM
     * @return  instance that wraps the provided FutureWTransformer
     
    public static <T> AnyMValue<T> fromFutureWTValue(final FutureWTValue<T> futureT) {
        Objects.requireNonNull(futureT);
        return AnyMFactory.instance.value(futureT,new FutureWTSeqComprehender());
    }*/

    /**
     * Create an AnyMSeq instance that wraps an FutureWTransformer {@link FutureWTSeq}
     * 
     * @param futureT to wrap inside an AnyM
     * @return instance that wraps the provided FutureWTransformer
     
    public static <T> AnyMSeq<T> fromFutureWTSeq(final FutureWTSeq<T> futureT) {
        Objects.requireNonNull(futureT);
        return AnyMFactory.instance.seq(futureT);
    }*/

    /**
     * Create an AnyMSeq instance that wraps an ListTransformer {@link ListTSeq}
     * 
     * @param listT to wrap inside an AnyM
     * @return instance that wraps the provided ListTransformer
     
    public static <T> AnyMSeq<T> fromListT(final ListT<T> listT) {
        Objects.requireNonNull(listT);
        return AnyMFactory.instance.seq(listT,listT(listT).visit(v->new ListTValueComprehender(),
                                                                            s->new ListTSeqComprehender()));
    }*/

    /**
     * Create an AnyMSeq instance that wraps an StreamTransformer {@link StreamTSeq}
     * 
     * @param streamT to wrap inside an AnyM
     * @return instance that wraps the provided StreamTransformer
     
    public static <T> AnyMSeq<T> fromStreamT(final StreamT<T> streamT) {
        Objects.requireNonNull(streamT);
        return AnyMFactory.instance.seq(streamT,streamT(streamT).visit(v->new StreamTValueComprehender(),
                                                                 s->new StreamTSeqComprehender()));
       
    }*/

    /**
     * Create an AnyMSeq instance that wraps an StreamableTransformer {@link StreamableTSeq}
     * 
     * @param streamT  to wrap inside an AnyM
     * @return instance that wraps the provided StreamableTransformer
    
    public static <T> AnyMSeq<T> fromStreamableT(final StreamableT<T> streamT) {
        Objects.requireNonNull(streamT);
        return AnyMFactory.instance.seq(streamT,Matchables.streamableT(streamT).visit(v->new StreamableTValueComprehender(),
                                                                          s->new StreamableTSeqComprehender()));
    } */

    /**
     * Create an AnyMSeq instance that wraps an SetTransformer {@link SetTSeq}
     * 
     * @param setT to wrap inside an AnyM
     * @return instance that wraps the provided SetTransformer
     
    public static <T> AnyMSeq<T> fromSetT(final SetT<T> setT) {
        Objects.requireNonNull(setT);
        return AnyMFactory.instance.seq(setT,Matchables.setT(setT).visit(v->new SetTValueComprehender(),
                                                                       s->new SetTSeqComprehender()));
    }*/

    /**
     * Create an AnyM instance that wraps an Iterable
     * 
     * @param iterable Iterable to wrap
     * @return AnyM that wraps the provided Iterable
     
    public static <T> AnyMSeq<T> fromIterable(Iterable<T> iterable) {
        Objects.requireNonNull(iterable);
        if (iterable instanceof AnyMSeq)
            return (AnyMSeq<T>) iterable;
        if (iterable instanceof List)
            iterable = ListX.fromIterable(iterable);
        if (iterable instanceof Set)
            iterable = SetX.fromIterable(iterable);
        return AnyMFactory.instance.convertSeq(iterable);
    }*/

    /**
     * Use this method to create an AnyMValue from an Iterable.
     * This exists as many monadic value types in Java libraries implement iterable (such 
     * as Optional in Javaslang or FunctionalJava).
     * 
     * @param iterable To generate AnyMValue from
     * @return AnyMValue wrapping the supplied Iterable
    
    public static <T> AnyMValue<T> fromIterableValue(final Iterable<T> iterable) {
        Objects.requireNonNull(iterable);
        return AnyMFactory.instance.value(iterable);
    } */

    /**
     * Take the supplied object and attempt to convert it to a supported Monad type
     * 
     * @param monad Monad to convert to a supported type and wrap inside an AnyMValue
     * @return AnyMValue that wraps the supplied converted monad
     
    public static <T> AnyMValue<T> ofConvertableValue(final Object monad) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.convertValue(monad);
    }*/

    /**
     * Take the supplied object and attempt to convert it to a supported Monad type
     * 
     * @param monad Monad to convert to a supported type and wrap inside an AnyMValue
     * @return AnyMSeq that wraps the supplied converted
    
    public static <T> AnyMSeq<T> ofConvertableSeq(final Object monad) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.convertSeq(monad);
    } */

    /**
     * Create an AnyMValue that wraps the untyped monad
     * 
     * @param monad to wrap inside an AnyM
     * @return AnyMValue that wraps the supplied monad
    */
    public static <W extends WitnessType,T> AnyMValue<W,T> ofValue(final Object monad, W witness) {
        Objects.requireNonNull(monad);
        return AnyMFactory.instance.value(monad,witness);
    } 

    /**
     * Create an AnyMSeq that wraps the untyped monad
     * 
     * @param monad to wrap inside an AnyM
     * @return AnyMSeq that wraps the supplied monad
     */
    public static <W extends WitnessType,T> AnyMSeq<W,T> ofSeq(final Object monad, W witness) {
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
    public static <T> ListX<AnyMValue<futureW,T>> listFromFutureW(final Iterable<FutureW<T>> anyM) {
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
    public static <W extends WitnessType,T1> AnyMValue<W,ListX<T1>> sequence(final Collection<? extends AnyM<W,T1>> seq) {
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
    public static <W extends WitnessType,T, R> AnyMValue<W,ListX<R>> traverse(final Collection<? extends AnyM<W,T>> seq, final Function<? super T, ? extends R> fn) {
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
    public static <W extends WitnessType, T, R> AnyM<W,Stream<R>> traverse(final Stream<AnyM<W,T>> source, final Supplier<AnyM<W,Stream<T>>> unitEmpty,
            final Function<? super T, ? extends R> fn) {
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
    public static <W extends WitnessType,T> AnyM<W,Stream<T>> sequence(final Stream<? extends AnyM<W,T>> source, final Supplier<? extends AnyM<W,Stream<T>>> unitEmpty) {
        final Stream<AnyM<W,T>> narrowed = (Stream<AnyM<W,T>>) source;
        return narrowed.reduce((AnyM<W,Stream<T>>) unitEmpty.get(), (fl, fo) -> fl.flatMapFirst(a -> {
            final Streamable<T> streamable = Streamable.fromStream(a);
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
    public static <W extends WitnessType,U, R> Function<AnyM<W,U>, AnyM<W,R>> liftM(final Function<? super U, ? extends R> fn) {
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
    public static <W extends WitnessType,U1, U2, R> BiFunction<AnyM<W,U1>, AnyM<W,U2>, AnyM<W,R>> liftM2(final BiFunction<? super U1, ? super U2, ? extends R> fn) {

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
    public static <W extends WitnessType,U1, U2, U3, R> TriFunction<AnyM<W,U1>, AnyM<W,U2>, AnyM<W,U3>, AnyM<W,R>> liftM3(
            final TriFunction<? super U1, ? super U2, ? super U3, ? extends R> fn) {
        return (u1, u2, u3) -> u1.bind(input1 -> u2.bind(input2 -> u3.map(input3 -> fn.apply(input1, input2, input3)))
                                                   .unwrap());
    }

    /**
     * Lift a QuadFunction into Monadic form.
     * 
     * @param fn Quad funciton to lift
     * @return Lifted Quad function
     */
    public static <W extends WitnessType,U1, U2, U3, U4, R> QuadFunction<AnyM<W,U1>, AnyM<W,U2>, AnyM<W,U3>, AnyM<W,U4>, AnyM<W,R>> liftM4(
            final QuadFunction<? super U1, ? super U2, ? super U3, ? super U4, ? extends R> fn) {

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
    public static <W extends WitnessType,U1, U2, U3, U4, U5, R> QuintFunction<AnyM<W,U1>, AnyM<W,U2>, AnyM<W,U3>, AnyM<W,U4>, AnyM<W,U5>, AnyM<W,R>> liftM5(
            final QuintFunction<? super U1, ? super U2, ? super U3, ? super U4, ? super U5, ? extends R> fn) {

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
    public static <W extends WitnessType,U1, U2, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, AnyM<W,R>>> liftM2(final Function<U1, Function<U2, R>> fn) {
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
    public static <W extends WitnessType,U1, U2, U3, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, Function<AnyM<W,U3>, AnyM<W,R>>>> liftM3(
            final Function<? super U1, Function<? super U2, Function<? super U3, ? extends R>>> fn) {
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
    public static <W extends WitnessType,U1, U2, U3, U4, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, Function<AnyM<W,U3>, Function<AnyM<W,U4>, AnyM<W,R>>>>> liftM4(
            final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, ? extends R>>>> fn) {

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
    public static <W extends WitnessType,U1, U2, U3, U4, U5, R> Function<AnyM<W,U1>, Function<AnyM<W,U2>, Function<AnyM<W,U3>, Function<AnyM<W,U4>, Function<AnyM<W,U5>, AnyM<W,R>>>>>> liftM5(
            final Function<? super U1, Function<? super U2, Function<? super U3, Function<? super U4, Function<? super U5, ? extends R>>>>> fn) {

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
         
        public <T> AnyMValue<T> convertValue(final Object o) {

            if (new ComprehenderSelector().selectComprehender(o) instanceof InvokeDynamicComprehender)
                return new MonadWrapper<>(
                                          new MonadicConverters().convertToMonadicForm(o)).anyMValue();
            return new MonadWrapper<>(
                                      o).anyMValue();
        }*/

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
         
        public <T> AnyMSeq<T> convertSeq(final Object o) {

            if (new ComprehenderSelector().selectComprehender(o) instanceof InvokeDynamicComprehender)
                return new MonadWrapper<>(
                                          new MonadicConverters().convertToMonadicForm(o)).anyMSeq();
            return new MonadWrapper<>(
                                      o).anyMSeq();
        }
*/
        /**
         * Non-type safe way to wrap a supported monad type in an AnyMValue
         * 
         * @param o Monad to wrap
         * @return AnyMValue wrapping supplied monad
         */
        public <W extends WitnessType,T> AnyMValue<W,T> value(final Object o,WitnessType comp) {
            if (o instanceof AnyMValue)
                return (AnyMValue<W,T>) o;
            
            return new MonadWrapper<>(
                                      o,comp.adapter()).anyMValue();
        }

        /**
         * Non-type safe way to wrap a supported monad type in an AnyMSeq
         * 
         * @param o Monad to wrap
         * @return AnyMValue wrapping supplied monad
         */
        public <W extends WitnessType,T> AnyMSeq<W,T> seq(final Object o, WitnessType comp) {
            if (o instanceof AnyMSeq)
                return (AnyMSeq<W,T>) o;
            return new MonadWrapper<>(
                                      o,comp.adapter()).anyMSeq();
        }

    }

}
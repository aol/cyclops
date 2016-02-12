package com.aol.cyclops.control;



import java.io.File;
import java.net.URL;
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
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.internal.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.internal.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.internal.monads.AnyMFunctions;
import com.aol.cyclops.internal.monads.AnyMSeqImpl;
import com.aol.cyclops.internal.monads.AnyMValueImpl;
import com.aol.cyclops.internal.monads.AnyMonads;
import com.aol.cyclops.internal.monads.ComprehenderSelector;
import com.aol.cyclops.internal.monads.MonadWrapper;
import com.aol.cyclops.control.SequenceM;
import com.aol.cyclops.types.EmptyUnit;
import com.aol.cyclops.types.FlatMap;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.Unwrapable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.ReduceM;
import com.aol.cyclops.types.stream.ToStream;
import com.aol.cyclops.util.function.QuadFunction;
import com.aol.cyclops.util.function.QuintFunction;
import com.aol.cyclops.util.function.TriFunction;
import com.aol.cyclops.util.stream.Streamable;

/**
 * 
 * Wrapper for Any Monad type
 *
 * 
 * @author johnmcclean
 *
 * @param <T> type data wrapped by the underlying monad
 */

public interface AnyM<T> extends Unwrapable,EmptyUnit<T>, Unit<T>,Foldable<T>,Functor<T>,
									FlatMap<T>,
									ToStream<T>,
									ReduceM<T>{
	
	/* Convert this AnyM to a Stream (SequenceM)
	 * Chooses the most appropriate of asSequence() and toSequence()
	 * 
	 * (non-Javadoc)
	 * @see com.aol.cyclops.sequence.streamable.ToStream#stream()
	 * @see com.aol.cyclops.monad.asSequence()
	 * @see com.aol.cyclops.monad.toSequence()
	 */
	public SequenceM<T> stream();
	

	<R, A> R collect(Collector<? super T, A, R> collector);
	
	
	 /* 
	  * Unwraps the wrapped monad, in it's current state.
	  * i.e. Lists or Sets may be Streams
	  * (non-Javadoc)
	 * @see com.aol.cyclops.sequence.Unwrapable#unwrap()
	 */
	<R> R unwrap();
	
	
	
	

	 <X extends Object> X monad();
	
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
	AnyM<T>  filter(Predicate<? super T> p);
	
	
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
	 *   AnyM[Stream[3,4,5]]
	 * }
	 * </pre>
	 * @param fn
	 * @return
	 */
	<R> AnyM<R> map(Function<? super T,? extends R> fn);
	
	
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
	AnyM<T>  peek(Consumer<? super T> c) ;
	
	
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
	 <R> AnyM<R> bind(Function<? super T,?> fn);
	/**
	 * Perform a bind operation (@see #bind) but also lift the return value into a Monad using configured
	 * MonadicConverters
	 
	 * Note the modified javaslang monad laws are not applied during the looser typed bind operation
	 * The modification being used to work around the limits of the Java type system.
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	 */
	 <R> AnyM<R> liftAndBind(Function<? super T,?> fn);

	
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
	 * Sequence the contents of a Monad.  e.g.
	 * Turn an <pre>
	 * 	{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
	 * 
	 * <pre>{@code
	 * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Integer>toSequence(c->c.stream())
											.collect(Collectors.toList());
		
		
		assertThat(list,hasItems(1,2,3,4,5,6));
		
	 * 
	 * }</pre>
	 * 
	 * @return A Sequence that wraps a Stream
	 */
	 <NT> SequenceM<NT> toSequence(Function<? super T,? extends Stream<? extends NT>> fn);
	/**
	 *  <pre>
	 *  {@code Optional<List<Integer>>  into Stream<Integer> }
	 *  </pre>
	 * Less type safe equivalent, but may be more accessible than toSequence(fn) i.e. 
	 * <pre>
	 * {@code 
	 *    toSequence(Function<T,Stream<NT>> fn)
	 *   }
	 *   </pre>
	 *  <pre>{@code
	 * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Integer>toSequence()
											.collect(Collectors.toList());
		
		
		
	 * 
	 * }</pre>
	
	 * @return A Sequence that wraps a Stream
	 */
	 <T> SequenceM<T> toSequence();
	
	
	/**
	 * Wrap this Monad's contents as a Sequence without disaggreating it. .e.
	 *  <pre>{@code Optional<List<Integer>>  into Stream<List<Integer>> }</pre>
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	 SequenceM<T> asSequence();
	
	
		
	

	
	 
	
	  
	  
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
	 * Construct an AnyM wrapping an empty instance of the wrapped type 
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
	 * 
	 * Replicate given Monad
	 * 
	 * <pre>{@code 
	 * 	
	 *   AnyM<Optional<Integer>> applied =AnyM.fromOptional(Optional.of(2)).replicateM(5);
	 *   
		 //AnyM[Optional[List(2,2,2,2,2)]]
		 
		 }</pre>
	 * 
	 * 
	 * @param times number of times to replicate
	 * @return Replicated Monad
	 */
	 AnyM<List<T>> replicateM(int times);
	
	 /**
		 * Perform a reduction where NT is a (native) Monad type
		 * e.g. 
		 * <pre>{@code 
		 *   Monoid<Optional<Integer>> optionalAdd = Monoid.of(AnyM.fromOptional(Optional.of(0)), (a,b)-> AnyM.fromOptional(Optional.of(a.get()+b.get())));
			
			AnyM.fromStream(Stream.of(2,8,3,1)).reduceM(optionalAdd);
			
			//AnyM[Optional(14)];
			}</pre>
		 * 
		 * 
		 * @param reducer An identity value (approx. a seed) and BiFunction with a single type to reduce this anyM
		 * @return Reduced AnyM
		 */
	  AnyM<T> reduceM(Monoid<AnyM<T>> reducer);
	
	 
	 
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
	public static AnyMSeq<Integer> fromRange(int start, int end){
		
		return AnyM.fromStream(SequenceM.range(start, end));
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
	public static AnyMSeq<Long> fromRangeLong(long start, long end){
		
		return AnyM.fromStream(SequenceM.rangeLong(start, end));
	}
	/**
	 * Wrap a Streamable inside an AnyM
	 * 
	 * @param streamable wrap
	 * @return
	 */
	public static <T> AnyMSeq<T> fromStreamable(ToStream<T> streamable){
		 Objects.requireNonNull(streamable);
		return new AnyMSeqImpl<>(AnyMFactory.instance.monad(streamable));
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
	public static <T> AnyMSeq<T> fromList(List<T> list){
		 Objects.requireNonNull(list);
		return new AnyMSeqImpl<>(AnyMFactory.instance.monad(list));
	}
	/**
	 * Create an AnyM from a Set
	 * 
	 * This AnyM will convert the Set to a Stream under the covers, but will rematerialize the Stream as Set
	 * if wrap() is called
	 * 
	 * 
	 * @param list to wrap inside an AnyM
	 * @return AnyM wrapping a Set
	 */
	public static <T> AnyMSeq<T> fromSet(Set<T> set){
		 Objects.requireNonNull(set);
		return new AnyMSeqImpl<>(AnyMFactory.instance.monad(set));
	}
	
	
	/**
	 * Create an AnyM wrapping a Stream of the supplied data
	 * 
	 * @param streamData values to populate a Stream
	 * @return
	 */
	public static <T> AnyMSeq<T> fromArray(T... streamData){
		return new AnyMSeqImpl<>(AnyMFactory.instance.monad(Stream.of(streamData)));
	}
	/**
	 * Create an AnyM wrapping a Stream of the supplied data
	 * 
	 * Identical to fromArray, exists as it may appear functionally more obvious to users than fromArray (which fits the convention)
	 * 
	 * @param streamData values to populate a Stream
	 * @return
	 */
	public static <T> AnyMSeq<T> streamOf(T... streamData){
		return new AnyMSeqImpl<>(AnyMFactory.instance.monad(Stream.of(streamData)));
	}
	
	/**
	 * Create an AnyM instance that wraps a Stream
	 * 
	 * @param stream Stream to wrap
	 * @return AnyM that wraps the provided Stream
	 */
	public static <T> AnyMSeq<T> fromStream(Stream<T> stream){
		Objects.requireNonNull(stream);
		return new AnyMSeqImpl<T>(AnyMFactory.instance.monad(stream));
	}
	/**
	 * Create an AnyM instance that wraps an IntStream
	 * 
	 * @param stream IntStream to wrap
	 * @return AnyM that wraps the provided IntStream
	 */
	public static AnyMSeq<Integer> fromIntStream(IntStream stream){
		Objects.requireNonNull(stream);
		return new AnyMSeqImpl<>(AnyMFactory.instance.monad(stream.boxed()));
	}
	/**
	 * Create an AnyM instance that wraps an DoubleStream
	 * 
	 * @param stream DoubleStream to wrap
	 * @return AnyM that wraps the provided DoubleStream
	 */
	public static AnyMSeq<Double> fromDoubleStream(DoubleStream stream){
		Objects.requireNonNull(stream);
		return new AnyMSeqImpl<>(AnyMFactory.instance.monad(stream.boxed()));
	}
	/**
	 * Create an AnyM instance that wraps an LongStream
	 * 
	 * @param stream LongStream to wrap
	 * @return AnyM that wraps the provided LongStream
	 */
	public static AnyMSeq<Long> fromLongStream(LongStream stream){
		Objects.requireNonNull(stream);
		return new AnyMSeqImpl<>(AnyMFactory.instance.monad(stream.boxed()));
	}
	/**
	 * Create an AnyM instance that wraps an Optional
	 * 
	 * @param stream Optional to wrap
	 * @return AnyM that wraps the provided Optonal
	 */
	public static <T> AnyMValue<T> fromOptional(Optional<T> optional){
		 Objects.requireNonNull(optional);
		return new AnyMValueImpl<>(AnyMFactory.instance.monad(optional));
	}
	/**
	 * Create an AnyM instance that wraps an OptionalDouble
	 * 
	 * @param stream Optional to wrap
	 * @return AnyM that wraps the provided OptonalDouble
	 */
	public static  AnyMValue<Double> fromOptionalDouble(OptionalDouble optional){
		Objects.requireNonNull(optional);
		return new AnyMValueImpl<>(AnyMFactory.instance.of(optional));
	}
	/**
	 * Create an AnyM instance that wraps an OptionalLong
	 * 
	 * @param stream OptionalLong to wrap
	 * @return AnyM that wraps the provided OptonalLong
	 */
	public static  AnyMValue<Long> fromOptionalLong(OptionalLong optional){
		Objects.requireNonNull(optional);
		return new AnyMValueImpl<>(AnyMFactory.instance.of(optional));
	}
	/**
	 * Create an AnyM instance that wraps an OptionalInt
	 * 
	 * @param stream OptionalInt to wrap
	 * @return AnyM that wraps the provided OptonalInt
	 */
	public static  AnyMValue<Integer> fromOptionalInt(OptionalInt optional){
		Objects.requireNonNull(optional);
		return new AnyMValueImpl<>(AnyMFactory.instance.of(optional));
	}
	/**
	 * Create an AnyM instance that wraps a CompletableFuture
	 * 
	 * @param stream CompletableFuture to wrap
	 * @return AnyM that wraps the provided CompletableFuture
	 */
	public static <T> AnyMValue<T> fromCompletableFuture(CompletableFuture<T> future){
		Objects.requireNonNull(future);
		return new AnyMValueImpl<>(AnyMFactory.instance.monad(future));
	}
	public static <T> AnyMValue<T> fromXor(Xor<?,T> future){
		Objects.requireNonNull(future);
		return new AnyMValueImpl<>(AnyMFactory.instance.monad(future));
	}
	public static <T> AnyMValue<T> fromIor(Ior<?,T> future){
		Objects.requireNonNull(future);
		return new AnyMValueImpl<>(AnyMFactory.instance.monad(future));
	}
	public static <T> AnyMValue<T> fromEval(Eval<T> future){
		Objects.requireNonNull(future);
		return new AnyMValueImpl<>(AnyMFactory.instance.monad(future));
	}
	public static <T> AnyMValue<T> fromFutureW(FutureW<T> future){
		Objects.requireNonNull(future);
		return new AnyMValueImpl<>(AnyMFactory.instance.monad(future));
	}
	public static <T> AnyMValue<T> fromMaybe(Maybe<T> future){
		Objects.requireNonNull(future);
		return new AnyMValueImpl<>(AnyMFactory.instance.monad(future));
	}
	/**
	 * Create an AnyM instance that wraps a Collection
	 * 
	 * @param stream Collection to wrap
	 * @return AnyM that wraps the provided Collection
	 */
	public static <T> AnyMSeq<T> fromCollection(Collection<T> collection){
		Objects.requireNonNull(collection);
		return new AnyMSeqImpl<>(AnyMFactory.instance.of(collection));
	}
	/**
	 * Create an AnyM instance that wraps an Iterable
	 * 
	 * @param stream Iterable to wrap
	 * @return AnyM that wraps the provided Iterable
	 */
	public static <T> AnyMSeq<T> fromIterable(Iterable<T> iterable){
		Objects.requireNonNull(iterable);
		
		return new AnyMSeqImpl<>(AnyMFactory.instance.of(iterable));
	}
	/**
	 * Create an AnyM instance that wraps an textual Stream from a file
	 * 
	 * @param stream File to generate text / line Stream from, and to wrap
	 * @return AnyM that wraps the Stream generated from the provided file
	 */
	public static AnyMSeq<String> fromFile(File file){
		Objects.requireNonNull(file);
		return new AnyMSeqImpl<>(AnyMFactory.instance.of(file));
	}
	/**
	 * Create an AnyM instance that wraps an textual Stream from a URL
	 * 
	 * @param stream URL to generate text / line Stream from, and to wrap
	 * @return AnyM that wraps the Stream generated from the provided url
	 */
	public static AnyMSeq<String> fromURL(URL url){
		Objects.requireNonNull(url);
		return new AnyMSeqImpl<>(AnyMFactory.instance.of(url));
	}
	
	/**
	 * Take the supplied object and always attempt to convert it to a Monad type
	 * 
	 * @param monad
	 * @return
	 */
	public static <T> AnyM<T> ofConvertable(Object monad){
		Objects.requireNonNull(monad);
		return AnyMFactory.instance.of(monad);
	}
	/**
	 * Take the supplied object and wrap it inside an AnyM - must be a supported monad type already
	 * 
	 * @param monad to wrap
	 * @return Wrapped Monad
	 */
	public static <T> AnyM<T> ofMonad(Object monad){
		Objects.requireNonNull(monad);
		return AnyMFactory.instance.monad(monad);
	}
	/**
	 * Generate an AnyM that wraps an Optional from the provided nullable object
	 * 
	 * @param nullable - Nullable object to generate an optional from
	 * @return AnyM wrapping an Optional created with the supplied nullable
	 */
	public static <T> AnyMValue<T> ofNullable(Object nullable){
		return new AnyMValueImpl<T>(AnyMFactory.instance.monad(Optional.ofNullable(nullable)));
	}
	
	/**
	 * Take an iterable containing monads and convert it into a List of AnyMs
	 * Uses ofMonad to take the supplied object and wrap it inside an AnyM - must be a supported monad type already
	 * 
	 * @param anyM Iterable containing Monads
	 * @return List of AnyMs
	 */
	public static <T> List<AnyM<T>> ofMonadList(Iterable<Object> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> (AnyM<T>)AnyM.ofMonad(i)).collect(Collectors.toList());
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
	public static <T> List<AnyMSeq<T>> listFromStreamable(Iterable<Streamable<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromStreamable(i)).collect(Collectors.toList());
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
	public static <T> List<AnyMSeq<T>> listFromStream(Iterable<Stream<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromStream(i)).collect(Collectors.toList());
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
	public static <T> List<AnyMValue<T>> listFromOptional(Iterable<Optional<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromOptional(i)).collect(Collectors.toList());
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
	public static <T> List<AnyMValue<T>> listFromCompletableFuture(Iterable<CompletableFuture<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromCompletableFuture(i)).collect(Collectors.toList());
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
	public static <T> List<AnyMSeq<T>> listFromIterable(Iterable<Iterable<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromIterable(i)).collect(Collectors.toList());
	}
	/**
	 * Take an iterable containing Streamables and convert them into a List of AnyMs
	 * e.g.
	 * {@code 
	 *     List<AnyM<Integer>> anyMs = AnyM.listFromStreamable(Arrays.asList(Arrays.asList(1,2,3),Arrays.asList(10,20,30));
	 *     
	 *     //List[AnyM[List[1,2,3],List[10,20,30]]]
	 * }
	 * 
	 * @param anyM Iterable containing Collections
	 * @return List of AnyMs
	 */
	public static <T> List<AnyMSeq<T>> listFromCollection(Iterable<Collection<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromCollection(i)).collect(Collectors.toList());
	}
	
	public static  <ST,T> List<AnyMValue<T>> listFromXor(Iterable<Xor<ST,T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromXor(i)).collect(Collectors.toList());
	}
	public static  <ST,T> List<AnyMValue<T>> listFromIor(Iterable<Ior<ST,T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromIor(i)).collect(Collectors.toList());
	}
	public static  <T> List<AnyMValue<T>> listFromMaybe(Iterable<Maybe<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromMaybe(i)).collect(Collectors.toList());
	}
	public static  <T> List<AnyMValue<T>> listFromEval(Iterable<Eval<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromEval(i)).collect(Collectors.toList());
	}
	public static  <T> List<AnyMValue<T>> listFromFutureW(Iterable<FutureW<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromFutureW(i)).collect(Collectors.toList());
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
	public static <T> List<AnyMSeq<T>> listFromIterator(Iterable<Iterator<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromIterable(()->i)).collect(Collectors.toList());
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
	public static <T,R> AnyM<ListX<R>> traverse(Collection<? extends AnyM<T>> seq, Function<? super T,? extends R> fn){
		return new AnyMonads().traverse(seq,fn);
	}
	/**
	 * Convert a Stream of Monads to a Monad with a List applying the supplied function in the process
	 * 
	<pre>{@code 
       Stream<CompletableFuture<Integer>> futures = createFutures();
       AnyM<List<String>> futureList = AnyMonads.traverse(AsAnyMList.anyMList(futures), (Integer i) -> "hello" +i);
        }
		</pre>
	 * 
	 * @param seq Stream of Monads
	 * @param fn Function to apply 
	 * @return Monad with a list
	 */
	public static <T,R> AnyM<ListX<R>> traverse(Stream<? extends AnyM<T>> seq, Function<? super T,? extends R> fn){
		
		return new AnyMonads().traverse(seq,fn);
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
	 * @see com.aol.cyclops.monad.AsAnyMList for helper methods to convert a List of Monads / Collections to List of AnyM
	 * @param seq Collection of monads to convert
	 * @return Monad with a List
	 */ 
	public static <T1>  AnyM<ListX<T1>> sequence(Collection<? extends AnyM<T1>> seq){
		return new AnyMonads().sequence(seq);
	}
	/**
	 * Convert a Stream of Monads to a Monad with a List
	 * 
	 * <pre>{@code
		Stream<CompletableFuture<Integer>> futures = createFutures();
		AnyM<List<Integer>> futureList = AnyMonads.sequence(AsAnyMList.anyMList(futures));

	   //where AnyM wraps  CompletableFuture<List<Integer>>
	  }</pre>
	 * 
	 * @see com.aol.cyclops.monad.AsAnyMList for helper methods to convert a List of Monads / Collections to List of AnyM
	 * @param seq Stream of monads to convert
	 * @return Monad with a List
	 */
	public static <T1>  AnyM<SequenceM<T1>> sequence(Stream<? extends AnyM<T1>> seq){
		return new AnyMonads().sequence(seq);
	}
	/**
	 * Lift a function so it accepts an AnyM and returns an AnyM (any monad)
	 * AnyM view simplifies type related challenges.
	 * 
	 * @param fn
	 * @return
	 */
	public static <U,R> Function<AnyM<U>,AnyM<R>> liftM(Function<? super U,? extends R> fn){
		return u -> u.map( input -> fn.apply(input)  );
	}
	
	
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * AnyM view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * e.g.
	 * 
	 * <pre>{@code
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
	public static <U1,U2,R> BiFunction<AnyM<U1>,AnyM<U2>,AnyM<R>> liftM2(BiFunction<? super U1,? super U2,? extends R> fn){
		
		return (u1,u2) -> u1.bind( input1 -> u2.map(input2 -> fn.apply(input1,input2)  ).unwrap());
	}
	/**
	 * Lift a jOOλ Function3  into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
	 * 
	 * <pre>
	 * {@code
	 * Function3 <AnyM<Double>,AnyM<Entity>,AnyM<String>,AnyM<Integer>> fn = liftM3(this::myMethod);
	 *    
	 * }
	 * </pre>
	 * 
	 * Now we can execute the Method with Streams, Optional, Futures, Try's etc to transparently inject iteration, null handling, async execution and / or error handling
	 * 
	 * @param fn Function to lift
	 * @return Lifted function
	 */
	public static <U1,U2,U3,R> Function3<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<R>> liftM3(Function3<? super U1,? super U2,? super U3,? extends R> fn){
		return (u1,u2,u3) -> u1.bind( input1 -> 
									u2.bind(input2 -> 
										u3.map(input3->fn.apply(input1,input2,input3)  )).unwrap());
	}
	/**
	 * Lift a TriFunction into Monadic form. A good use case it to take an existing method and lift it so it can accept and return monads
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
	public static <U1,U2,U3,R> TriFunction<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<R>> liftM3Cyclops(TriFunction<? super U1,? super U2,? super U3,? extends R> fn){
		return (u1,u2,u3) -> u1.bind( input1 -> 
									u2.bind(input2 -> 
										u3.map(input3->fn.apply(input1,input2,input3)  ).unwrap()).unwrap());
	}
	/**
	 * Lift a  jOOλ Function4 into Monadic form.
	 * 
	 * @param fn Quad funciton to lift
	 * @return Lifted Quad function
	 */
	public static <U1,U2,U3,U4,R> Function4<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<U4>,AnyM<R>> liftM4(Function4<? super U1,? super U2,? super U3,? super U4,? extends R> fn){
		
		return (u1,u2,u3,u4) -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.map(input4->fn.apply(input1,input2,input3,input4)  ))).unwrap());
	}
	/**
	 * Lift a QuadFunction into Monadic form.
	 * 
	 * @param fn Quad funciton to lift
	 * @return Lifted Quad function
	 */
	public static <U1,U2,U3,U4,R> QuadFunction<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<U4>,AnyM<R>> liftM4Cyclops(QuadFunction<? super U1,? super U2,? super U3,? super U4,? extends R> fn){
		
		return (u1,u2,u3,u4) -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.map(input4->fn.apply(input1,input2,input3,input4)  ).unwrap()).unwrap()).unwrap());
	}
	/**
	 * Lift a  jOOλ Function5 (5 parameters) into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted Function
	 */
	public static <U1,U2,U3,U4,U5,R> Function5<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<U4>,AnyM<U5>,AnyM<R>> liftM5(Function5<? super U1,? super U2,? super U3,? super U4,? super U5,? extends R> fn){
		
		return (u1,u2,u3,u4,u5) -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.bind(input4->
															u5.map(input5->fn.apply(input1,input2,input3,input4,input5)  )))).unwrap());
	}
	/**
	 * Lift a QuintFunction (5 parameters) into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted Function
	 */
	public static <U1,U2,U3,U4,U5,R> QuintFunction<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<U4>,AnyM<U5>,AnyM<R>> liftM5Cyclops(QuintFunction<? super U1,? super U2,? super U3,? super U4,? super U5,? extends R> fn){
		
		return (u1,u2,u3,u4,u5) -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.bind(input4->
															u5.map(input5->fn.apply(input1,input2,input3,input4,input5)  ).unwrap()).unwrap()).unwrap()).unwrap());
	}
	/**
	 * Lift a Curried Function {@code(2 levels a->b->fn.apply(a,b) )} into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,R> Function<AnyM<U1>,Function<AnyM<U2>,AnyM<R>>> liftM2(Function<U1,Function<U2,R>> fn){
		return u1 -> u2 -> u1.bind( input1 -> u2.map(input2 -> fn.apply(input1).apply(input2)  ).unwrap());

	}
	/**
	 * Lift a Curried Function {@code(3 levels a->b->c->fn.apply(a,b,c) )} into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,U3,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,AnyM<R>>>> liftM3(Function<? super U1,Function<? super U2,Function<? super U3,? extends R>>> fn){
		return u1 -> u2 ->u3 -> u1.bind( input1 -> 
									u2.bind(input2 -> 
										u3.map(input3->fn.apply(input1).apply(input2).apply(input3)  )).unwrap());
	}
	
	/**
	 * Lift a Curried Function {@code(4 levels a->b->c->d->fn.apply(a,b,c,d) )} into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,U3,U4,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,Function<AnyM<U4>,AnyM<R>>>>> liftM4(Function<? super U1,Function<? super U2,Function<? super U3,Function<? super U4,? extends R>>>> fn){
		
		return u1->u2->u3->u4 -> u1.bind( input1 -> 
										u2.bind(input2 -> 
												u3.bind(input3->
														u4.map(input4->fn.apply(input1).apply(input2).apply(input3).apply(input4)  ))).unwrap());
	}
	/**
	 * Lift a Curried Function {@code (5 levels a->b->c->d->e->fn.apply(a,b,c,d,e) ) }into Monadic form
	 * 
	 * @param fn Function to lift
	 * @return Lifted function 
	 */
	public static <U1,U2,U3,U4,U5,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,Function<AnyM<U4>,Function<AnyM<U5>,AnyM<R>>>>>> liftM5(Function<? super U1,Function<? super U2,Function<? super U3,Function<? super U4,Function<? super U5,? extends R>>>>> fn){
		
		return u1 ->u2 ->u3 ->u4 ->u5  -> u1.bind( input1 -> 
										   u2.bind(input2 -> 
												u3.bind(input3->
														u4.bind(input4->
															u5.map(input5->fn.apply(input1).apply(input2).apply(input3).apply(input4).apply(input5)  )))).unwrap());
	}
	
	

static class AnyMFactory {
static AnyMFactory instance = new AnyMFactory();
	/* 
	 * This will convert the supplied Object if possible into a supported Monad type (or more efficient type)
	 * (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyMFactory#of(java.lang.Object)
	 */
	public <T> AnyM<T> of(Object o) {
		
		if(new ComprehenderSelector().selectComprehender(
				o) instanceof InvokeDynamicComprehender)
			return new MonadWrapper<>(new MonadicConverters().convertToMonadicForm(o)).anyM();
		return new MonadWrapper<>(o).anyM();
	}
	/* This will accept the supplied monad as is
	 * 
	 * (non-Javadoc)
	 * @see com.aol.cyclops.monad.AnyMFactory#monad(java.lang.Object)
	 */
	public <T> AnyM<T> monad(Object o) {
		return new MonadWrapper<>(o).anyM();
	}
	public AnyMFunctions anyMonads() {
		return new AnyMonads();
	}
}
	
}
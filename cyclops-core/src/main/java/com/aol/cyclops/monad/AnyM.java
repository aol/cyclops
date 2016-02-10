package com.aol.cyclops.monad;



import static com.aol.cyclops.monad.Utils.firstOrNull;

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

import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Ior;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.lambda.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.lambda.monads.EmptyUnit;
import com.aol.cyclops.lambda.monads.FlatMap;
import com.aol.cyclops.lambda.monads.Foldable;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.Unit;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.Unwrapable;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.sequence.streamable.ToStream;
import com.aol.cyclops.sequence.traits.ConvertableSequence;
import com.aol.cyclops.value.Value;

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
									FlatMap<T>,ToStream<T>, ApplyM<T>,FlatMapM<T>,ReduceM<T>,
									ConvertableSequence<T>, ZippingApplicativable<T>{
	
	/* Convert this AnyM to a Stream (SequenceM)
	 * Chooses the most appropriate of asSequence() and toSequence()
	 * 
	 * (non-Javadoc)
	 * @see com.aol.cyclops.sequence.streamable.ToStream#stream()
	 * @see com.aol.cyclops.monad.asSequence()
	 * @see com.aol.cyclops.monad.toSequence()
	 */
	public SequenceM<T> stream();
	
	
	default Value<T> toFirstValue(){
		return ()-> firstOrNull(toList());
	}
	
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
		 * Perform a two level nested internal iteration over this Stream and the supplied monad (allowing null handling, exception handling
		 * etc to be injected, for example)
		 * 
		 * <pre>
		 * {@code 
		 * AnyM.fromArray(1,2,3)
							.forEachAnyM2(a->AnyM.fromIntStream(IntStream.range(10,13)),
										a->b->a+b);
										
		 * 
		 *  //AnyM[11,14,12,15,13,16]
		 * }
		 * </pre>
		 * 
		 * 
		 * @param monad Nested Monad to iterate over
		 * @param yieldingFunction Function with pointers to the current element from both Streams that generates the new elements
		 * @return LazyFutureStream with elements generated via nested iteration
		 */
		 <R1, R> AnyM<R> forEach2(Function<? super T, ? extends AnyM<R1>> monad, Function<? super T, Function<? super R1, ? extends R>> yieldingFunction);
			
			
	
	    /**
		 * Perform a two level nested internal iteration over this Stream and the supplied monad (allowing null handling, exception handling
		 * etc to be injected, for example)
		 * 
		 * <pre>
		 * {@code 
		 * AnyM.fromArray(1,2,3)
							.forEach2(a->AnyM.fromIntStream(IntStream.range(10,13)),
							            a->b-> a<3 && b>10,
										a->b->a+b);
										
		 * 
		 *  //AnyM[14,15]
		 * }
		 * </pre>
		 * @param monad Nested Monad to iterate over
		 * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
		 * @param yieldingFunction Function with pointers to the current element from both monads that generates the new elements
		 * @return
		 */
		 <R1,R> AnyM<R> forEach2(Function<? super T,? extends AnyM<R1>> monad, 
					Function<? super T, Function<? super R1, Boolean>> filterFunction,
						Function<? super T,Function<? super R1,? extends R>> yieldingFunction );
	 	/** 
		 * Perform a three level nested internal iteration over this Stream and the supplied streams
		  *<pre>
		 * {@code 
		 * AnyM.fromArray(1,2)
							.forEach2(a->AnyM.fromIntStream(IntStream.range(10,13)),
							(a->b->AnyM.fromArray(""+(a+b),"hello world"),
										a->b->c->c+":"a+":"+b);
										
		 * 
		 *  //AnyM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
		 * }
		 * </pre> 
		 * @param monad1 Nested monad to flatMap over
		 * @param stream2 Nested monad to flatMap over
		 * @param yieldingFunction Function with pointers to the current element from both monads that generates the new elements
		 * @return AnyM with elements generated via nested iteration
		 */
		 <R1, R2, R> AnyM<R> forEach3(Function<? super T, ? extends AnyM<R1>> monad1, 	
					Function<? super T,Function<? super R1,? extends AnyM<R2>>> monad2,
			Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction, 
				Function<? super T, Function<? super R1, Function<? super R2,? extends R>>> yieldingFunction);
		
		


		
		
		/**
		 * Perform a three level nested internal iteration over this AnyM and the supplied monads
		 *<pre>
		 * {@code 
		 * AnyM.fromArray(1,2,3)
						.forEach3(a->AnyM.fromStream(IntStream.range(10,13)),
							 a->b->AnyM.fromArray(""+(a+b),"hello world"),
						         a->b->c-> c!=3,
									a->b->c->c+":"a+":"+b);
									
		 * 
		 *  //SequenceM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
		 * }
	 * </pre> 
		 * 
		 * @param monad1 Nested Stream to iterate over
		 * @param monad2 Nested Stream to iterate over
		 * @param filterFunction Filter to apply over elements before passing non-filtered values to the yielding function
		 * @param yieldingFunction Function with pointers to the current element from both Monads that generates the new elements
		 * @return AnyM with elements generated via nested iteration
		 */
		<R1, R2, R> AnyM<R> forEach3( Function<? super T, ? extends AnyM<R1>> monad1, 	
						Function<? super T,Function<? super R1,? extends AnyM<R2>>> monad2,
						Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction);
	/**
	 * flatMap operation
	  * 
	 * AnyM follows the javaslang modified 'monad' laws https://gist.github.com/danieldietrich/71be006b355d6fbc0584
	 * In particular left-identity becomes
	 * Left identity: unit(a).flatMap(f) ≡ select(f.apply(a))
	 * Or in plain English, if your flatMap function returns multiple values (such as flatMap by Stream) but the current Monad only can only hold one value,
	 * only the first value is accepted.
	 * 
	 * Example 1 : multi-values are supported (AnyM wraps a Stream, List, Set etc)
	 * <pre>
	 * {@code 
	 *   AnyM<Integer> anyM = AnyM.fromStream(Stream.of(1,2,3)).flatMap(i->AnyM.fromArray(i+1,i+2));
	 *   
	 *   //AnyM[Stream[2,3,3,4,4,5]]
	 * }
	 * </pre>
	 * Example 2 : multi-values are not supported (AnyM wraps a Stream, List, Set etc)
	 * <pre>
	 * {@code 
	 *   AnyM<Integer> anyM = AnyM.fromOptional(Optional.of(1)).flatMap(i->AnyM.fromArray(i+1,i+2));
	 *   
	 *   //AnyM[Optional[2]]
	 * }
	 * </pre>
	 * @param fn flatMap function
	 * @return  flatMapped AnyM
	 */
	 <R> AnyM<R> flatMap(Function<? super T,? extends AnyM<? extends R>> fn) ;
	
	
	
	
	
	
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
	 * Apply function/s inside supplied Monad to data in current Monad
	 * 
	 * e.g. with Streams
	 * <pre>{@code 
	 * 
	 * AnyM<Integer> applied =AnyM.fromStream(Stream.of(1,2,3))
	 * 								.applyM(AnyM.fromStreamable(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2)));
	
	 	assertThat(applied.toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 }</pre>
	 * 
	 * with Optionals 
	 * <pre>{@code
	 * 
	 *  Any<Integer> applied =AnyM.fromOptional(Optional.of(2)).applyM(AnyM.fromOptional(Optional.of( (Integer a)->a+1)) );
		assertThat(applied.toList(),equalTo(Arrays.asList(3)));}
		</pre>
	 * 
	 * @param fn
	 * @return
	 */
	<R> AnyM<R> applyM(AnyM<Function<? super T,? extends R>> fn);
	 
	
	  
	  
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
	 * @return Convert this AnyM to an Optional
	 */
	default Optional<ListX<T>> toOptional() {

		return this.<T> toSequence().toOptional();
	}
	/**
	 * @return Convert this AnyM to a Maybe
	 */
	default Maybe<ListX<T>> toMaybe() {

		return this.<T> toSequence().toMaybe();
	}
	/**
	 * @return Convert this AnyM to an Ior
	 */
	default Xor<?,ListX<T>> toXor() {

		return this.<T> toSequence().toXor();
	}
	/**
	 * @return Convert this AnyM to an Eval
	 */
	default Eval<ListX<T>> toEvalAlways() {

		return Eval.always( ()->this.<T> toSequence().toEvalAlways()).flatMap(i->i);
	}
	/**
	 * @return Convert this AnyM to an Eval
	 */
	default Eval<ListX<T>> toEvalLater() {

		return Eval.always( ()->this.<T> toSequence().toEvalLater()).flatMap(i->i);
	}
	/**
	 * @return Convert this AnyM to an Eval
	 */
	default Eval<ListX<T>> toEvalNow() {

		return this.<T> toSequence().toEvalNow();
	}

	/**
	 * @return Convert this AnyM to a CompletableFuture
	 */
	default CompletableFuture<ListX<T>> toCompletableFuture() {
		return this.<T> toSequence().toCompletableFuture();
	}
	/**
	 * @return Convert this AnyM to a CompletableFuture
	 */
	default FutureW<ListX<T>> toFutureW() {
		return this.<T> toSequence().toFutureW();
	}

	/**
	 * Convert this monad into a List
	 * <pre>
	 * @{code 
	 * 
	 * Stream<Integer> becomes List<Integer>
	 * Optional<Integer> becomes List<Integer>
	 * Set<Integer> becomes List<Integer>
	 * }
	 * </pre>
	 * 
	 * @return AnyM as a List
	 */
	public List<T> toList();
	/**
	 * Convert this monad into a Set
	 * <pre>
	 * @{code 
	 * 
	 * Stream<Integer> becomes Set<Integer>
	 * Optional<Integer> becomes Set<Integer>
	 * List<Integer> becomes Set<Integer>
	 * 
	 * }
	 * </pre>
	 * 
	 * @return AnyM as a Set
	 */
	public Set<T> toSet();
	
	/**
	 * Collect the contents of the monad wrapped by this AnyM into supplied collector
	 */
	public <R, A> R collect(Collector<? super T, A, R> collector);
	
	
	




	/**
	 * Construct an AnyM instance that wraps a range from start (inclusive) to end (exclusive) provided
	 * 
	 * The AnyM will contain a SequenceM over the spefied range
	 * 
	 * @param start Inclusive start of the range
	 * @param end Exclusive end of the range
	 * @return AnyM range
	 */
	public static AnyM<Integer> fromRange(int start, int end){
		
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
	public static AnyM<Long> fromRangeLong(long start, long end){
		
		return AnyM.fromStream(SequenceM.rangeLong(start, end));
	}
	/**
	 * Wrap a Streamable inside an AnyM
	 * 
	 * @param streamable wrap
	 * @return
	 */
	public static <T> AnyM<T> fromStreamable(ToStream<T> streamable){
		 Objects.requireNonNull(streamable);
		return AnyMFactory.instance.monad(streamable);
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
	public static <T> AnyM<T> fromList(List<T> list){
		 Objects.requireNonNull(list);
		return AnyMFactory.instance.monad(list);
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
	public static <T> AnyM<T> fromSet(Set<T> set){
		 Objects.requireNonNull(set);
		return AnyMFactory.instance.monad(set);
	}
	
	
	/**
	 * Create an AnyM wrapping a Stream of the supplied data
	 * 
	 * @param streamData values to populate a Stream
	 * @return
	 */
	public static <T> AnyM<T> fromArray(T... streamData){
		return AnyMFactory.instance.monad(Stream.of(streamData));
	}
	/**
	 * Create an AnyM wrapping a Stream of the supplied data
	 * 
	 * Identical to fromArray, exists as it may appear functionally more obvious to users than fromArray (which fits the convention)
	 * 
	 * @param streamData values to populate a Stream
	 * @return
	 */
	public static <T> AnyM<T> streamOf(T... streamData){
		return AnyMFactory.instance.monad(Stream.of(streamData));
	}
	
	/**
	 * Create an AnyM instance that wraps a Stream
	 * 
	 * @param stream Stream to wrap
	 * @return AnyM that wraps the provided Stream
	 */
	public static <T> AnyM<T> fromStream(Stream<T> stream){
		Objects.requireNonNull(stream);
		return AnyMFactory.instance.monad(stream);
	}
	/**
	 * Create an AnyM instance that wraps an IntStream
	 * 
	 * @param stream IntStream to wrap
	 * @return AnyM that wraps the provided IntStream
	 */
	public static AnyM<Integer> fromIntStream(IntStream stream){
		Objects.requireNonNull(stream);
		return AnyMFactory.instance.monad(stream.boxed());
	}
	/**
	 * Create an AnyM instance that wraps an DoubleStream
	 * 
	 * @param stream DoubleStream to wrap
	 * @return AnyM that wraps the provided DoubleStream
	 */
	public static AnyM<Double> fromDoubleStream(DoubleStream stream){
		Objects.requireNonNull(stream);
		return AnyMFactory.instance.monad(stream.boxed());
	}
	/**
	 * Create an AnyM instance that wraps an LongStream
	 * 
	 * @param stream LongStream to wrap
	 * @return AnyM that wraps the provided LongStream
	 */
	public static AnyM<Long> fromLongStream(LongStream stream){
		Objects.requireNonNull(stream);
		return AnyMFactory.instance.monad(stream.boxed());
	}
	/**
	 * Create an AnyM instance that wraps an Optional
	 * 
	 * @param stream Optional to wrap
	 * @return AnyM that wraps the provided Optonal
	 */
	public static <T> AnyM<T> fromOptional(Optional<T> optional){
		 Objects.requireNonNull(optional);
		return AnyMFactory.instance.monad(optional);
	}
	/**
	 * Create an AnyM instance that wraps an OptionalDouble
	 * 
	 * @param stream Optional to wrap
	 * @return AnyM that wraps the provided OptonalDouble
	 */
	public static  AnyM<Double> fromOptionalDouble(OptionalDouble optional){
		Objects.requireNonNull(optional);
		return AnyMFactory.instance.of(optional);
	}
	/**
	 * Create an AnyM instance that wraps an OptionalLong
	 * 
	 * @param stream OptionalLong to wrap
	 * @return AnyM that wraps the provided OptonalLong
	 */
	public static  AnyM<Long> fromOptionalLong(OptionalLong optional){
		Objects.requireNonNull(optional);
		return AnyMFactory.instance.of(optional);
	}
	/**
	 * Create an AnyM instance that wraps an OptionalInt
	 * 
	 * @param stream OptionalInt to wrap
	 * @return AnyM that wraps the provided OptonalInt
	 */
	public static  AnyM<Integer> fromOptionalInt(OptionalInt optional){
		Objects.requireNonNull(optional);
		return AnyMFactory.instance.of(optional);
	}
	/**
	 * Create an AnyM instance that wraps a CompletableFuture
	 * 
	 * @param stream CompletableFuture to wrap
	 * @return AnyM that wraps the provided CompletableFuture
	 */
	public static <T> AnyM<T> fromCompletableFuture(CompletableFuture<T> future){
		Objects.requireNonNull(future);
		return AnyMFactory.instance.monad(future);
	}
	public static <T> AnyM<T> fromXor(Xor<?,T> future){
		Objects.requireNonNull(future);
		return AnyMFactory.instance.monad(future);
	}
	public static <T> AnyM<T> fromIor(Ior<?,T> future){
		Objects.requireNonNull(future);
		return AnyMFactory.instance.monad(future);
	}
	public static <T> AnyM<T> fromEval(Eval<T> future){
		Objects.requireNonNull(future);
		return AnyMFactory.instance.monad(future);
	}
	public static <T> AnyM<T> fromFutureW(FutureW<T> future){
		Objects.requireNonNull(future);
		return AnyMFactory.instance.monad(future);
	}
	public static <T> AnyM<T> fromMaybe(Maybe<T> future){
		Objects.requireNonNull(future);
		return AnyMFactory.instance.monad(future);
	}
	/**
	 * Create an AnyM instance that wraps a Collection
	 * 
	 * @param stream Collection to wrap
	 * @return AnyM that wraps the provided Collection
	 */
	public static <T> AnyM<T> fromCollection(Collection<T> collection){
		Objects.requireNonNull(collection);
		return AnyMFactory.instance.of(collection);
	}
	/**
	 * Create an AnyM instance that wraps an Iterable
	 * 
	 * @param stream Iterable to wrap
	 * @return AnyM that wraps the provided Iterable
	 */
	public static <T> AnyM<T> fromIterable(Iterable<T> iterable){
		Objects.requireNonNull(iterable);
		
		return AnyMFactory.instance.of(iterable);
	}
	/**
	 * Create an AnyM instance that wraps an textual Stream from a file
	 * 
	 * @param stream File to generate text / line Stream from, and to wrap
	 * @return AnyM that wraps the Stream generated from the provided file
	 */
	public static AnyM<String> fromFile(File file){
		Objects.requireNonNull(file);
		return AnyMFactory.instance.of(file);
	}
	/**
	 * Create an AnyM instance that wraps an textual Stream from a URL
	 * 
	 * @param stream URL to generate text / line Stream from, and to wrap
	 * @return AnyM that wraps the Stream generated from the provided url
	 */
	public static AnyM<String> fromURL(URL url){
		Objects.requireNonNull(url);
		return AnyMFactory.instance.of(url);
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
	public static <T> AnyM<T> ofNullable(Object nullable){
		return AnyMFactory.instance.monad(Optional.ofNullable(nullable));
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
	public static <T> List<AnyM<T>> listFromStreamable(Iterable<Streamable<T>> anyM){
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
	public static <T> List<AnyM<T>> listFromStream(Iterable<Stream<T>> anyM){
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
	public static <T> List<AnyM<T>> listFromOptional(Iterable<Optional<T>> anyM){
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
	public static <T> List<AnyM<T>> listFromCompletableFuture(Iterable<CompletableFuture<T>> anyM){
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
	public static <T> List<AnyM<T>> listFromIterable(Iterable<Iterable<T>> anyM){
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
	public static <T> List<AnyM<T>> listFromCollection(Iterable<Collection<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromCollection(i)).collect(Collectors.toList());
	}
	
	public static  <ST,T> List<AnyM<T>> listFromXor(Iterable<Xor<ST,T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromXor(i)).collect(Collectors.toList());
	}
	public static  <ST,T> List<AnyM<T>> listFromIor(Iterable<Ior<ST,T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromIor(i)).collect(Collectors.toList());
	}
	public static  <T> List<AnyM<T>> listFromMaybe(Iterable<Maybe<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromMaybe(i)).collect(Collectors.toList());
	}
	public static  <T> List<AnyM<T>> listFromEval(Iterable<Eval<T>> anyM){
		return StreamSupport.stream(anyM.spliterator(),false).map(i-> AnyM.fromEval(i)).collect(Collectors.toList());
	}
	public static  <T> List<AnyM<T>> listFromFutureW(Iterable<FutureW<T>> anyM){
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
	public static <T> List<AnyM<T>> listFromIterator(Iterable<Iterator<T>> anyM){
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
	public static <T,R> AnyM<ListX<R>> traverse(Collection<AnyM<T>> seq, Function<? super T,? extends R> fn){
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
	public static <T,R> AnyM<ListX<R>> traverse(Stream<AnyM<T>> seq, Function<? super T,? extends R> fn){
		
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
	public static <T1>  AnyM<ListX<T1>> sequence(Collection<AnyM<T1>> seq){
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
	public static <T1>  AnyM<SequenceM<T1>> sequence(Stream<AnyM<T1>> seq){
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
	
	
	
	
}
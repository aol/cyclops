package com.aol.cyclops.monad;



import java.io.BufferedReader;
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
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;

import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.Unwrapable;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.sequence.streamable.ToStream;

/**
 * 
 * Wrapper for Any Monad type
 * @see AnyMonads companion class for static helper methods
 * 
 * @author johnmcclean
 *
 * @param <T> type data wrapped by the underlying monad
 */

public interface AnyM<T> extends Unwrapable, ToStream<T>{
	public SequenceM<T> stream();
	public static <T> List<AnyM<T>> notTypeSafeAnyMList(Collection<Object> anyM){
		return anyM.stream().map(i-> (AnyM<T>)AnyM.ofMonad(i)).collect(Collectors.toList());
	}
	
	public static <T> List<AnyM<T>> streamableToAnyMList(Collection<Streamable<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromStreamable(i)).collect(Collectors.toList());
	}
	
	public static <T> List<AnyM<T>> streamToAnyMList(Collection<Stream<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromStream(i)).collect(Collectors.toList());
	}
	
	public static <T> List<AnyM<T>> optionalToAnyMList(Collection<Optional<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromOptional(i)).collect(Collectors.toList());
	}
	
	public static <T> List<AnyM<T>> completableFutureToAnyMList(Collection<CompletableFuture<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromCompletableFuture(i)).collect(Collectors.toList());
	}
	public static <T> List<AnyM<T>> iterableToAnyMList(Collection<Iterable<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromIterable(i)).collect(Collectors.toList());
	}
	public static <T> List<AnyM<T>> collectionToAnyMList(Collection<Collection<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromCollection(i)).collect(Collectors.toList());
	}
	public static <T> List<AnyM<T>> iteratorToAnyMList(Collection<Iterator<T>> anyM){
		return anyM.stream().map(i-> AnyM.fromIterable(()->i)).collect(Collectors.toList());
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
	public static <T,R> AnyM<List<R>> traverse(Collection<AnyM<T>> seq, Function<T,R> fn){
		return AnyMFactory.instance.anyMonads().traverse(seq,fn);
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
	public static <T,R> AnyM<List<R>> traverse(Stream<AnyM<T>> seq, Function<T,R> fn){
		
		return AnyMFactory.instance.anyMonads().traverse(seq,fn);
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
	public static <T1>  AnyM<Stream<T1>> sequence(Collection<AnyM<T1>> seq){
		return AnyMFactory.instance.anyMonads().sequence(seq);
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
	public static <T1>  AnyM<Stream<T1>> sequence(Stream<AnyM<T1>> seq){
		return AnyMFactory.instance.anyMonads().sequence(seq);
	}
	/**
	 * Lift a function so it accepts an AnyM and returns an AnyM (any monad)
	 * AnyM view simplifies type related challenges.
	 * 
	 * @param fn
	 * @return
	 */
	public static <U,R> Function<AnyM<U>,AnyM<R>> liftM(Function<U,R> fn){
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
	public static <U1,U2,R> BiFunction<AnyM<U1>,AnyM<U2>,AnyM<R>> liftM2(BiFunction<U1,U2,R> fn){
		
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
	public static <U1,U2,U3,R> Function3<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<R>> liftM3(Function3<U1,U2,U3,R> fn){
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
	public static <U1,U2,U3,U4,R> Function4<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<U4>,AnyM<R>> liftM4(Function4<U1,U2,U3,U4,R> fn){
		
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
	public static <U1,U2,U3,U4,U5,R> Function5<AnyM<U1>,AnyM<U2>,AnyM<U3>,AnyM<U4>,AnyM<U5>,AnyM<R>> liftM5(Function5<U1,U2,U3,U4,U5,R> fn){
		
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
	public static <U1,U2,U3,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,AnyM<R>>>> liftM3(Function<U1,Function<U2,Function<U3,R>>> fn){
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
	public static <U1,U2,U3,U4,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,Function<AnyM<U4>,AnyM<R>>>>> liftM4(Function<U1,Function<U2,Function<U3,Function<U4,R>>>> fn){
		
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
	public static <U1,U2,U3,U4,U5,R> Function<AnyM<U1>,Function<AnyM<U2>,Function<AnyM<U3>,Function<AnyM<U4>,Function<AnyM<U5>,AnyM<R>>>>>> liftM5(Function<U1,Function<U2,Function<U3,Function<U4,Function<U5,R>>>>> fn){
		
		return u1 ->u2 ->u3 ->u4 ->u5  -> u1.bind( input1 -> 
										   u2.bind(input2 -> 
												u3.bind(input3->
														u4.bind(input4->
															u5.map(input5->fn.apply(input1).apply(input2).apply(input3).apply(input4).apply(input5)  )))).unwrap());
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
	public static AnyM<Integer> fromRange(int start, int end){
		
		return AnyM.fromStream(SequenceM.range(start, end));
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
		return AnyMFactory.instance.monad(stream);
	}
	/**
	 * Create an AnyM instance that wraps an DoubleStream
	 * 
	 * @param stream DoubleStream to wrap
	 * @return AnyM that wraps the provided DoubleStream
	 */
	public static AnyM<Double> fromDoubleStream(DoubleStream stream){
		Objects.requireNonNull(stream);
		return AnyMFactory.instance.monad(stream);
	}
	/**
	 * Create an AnyM instance that wraps an LongStream
	 * 
	 * @param stream LongStream to wrap
	 * @return AnyM that wraps the provided LongStream
	 */
	public static AnyM<Long> fromLongStream(LongStream stream){
		Objects.requireNonNull(stream);
		return AnyMFactory.instance.monad(stream);
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
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	 */
	 <R> AnyM<R> liftAndBind(Function<? super T,?> fn);
	/**
	 * Perform a flatMap operation where the result will be a flattened stream of Characters
	 * from the CharSequence returned by the supplied function.
	 * 
	 * <pre>
	 * {@code
	 * List<Character> result = anyM("input.file")
								.liftAndBindCharSequence(i->"hello world")
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	 * 
	 * }</pre>
	 * 
	 * 
	 * 
	 * @param fn
	 * @return
	 */
	  AnyM<Character> flatMapCharSequence(Function<? super T,CharSequence> fn);
	/**
	 *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied files.
	 * 
	 * <pre>
	 * {@code
	 * 		List<String> result = anyM("input.file")
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								.liftAndBindFile(File::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	  AnyM<String> flatMapFile(Function<? super T,File> fn);
	/**
	 *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied URLs 
	 * <pre>
	 * {@code 
	 * List<String> result = anyM("input.file")
								.liftAndBindURL(getClass().getClassLoader()::getResource)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * }
	 * 
	 * </pre>
	 * 
	 * 
	 * @param fn
	 * @return
	 */
	  AnyM<String> flatMapURL(Function<? super T, URL> fn) ;
	/**
	  *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied BufferedReaders
	 * 
	 * <pre>
	 * {@code
	 * List<String> result = anyM("input.file")
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								.liftAndBindBufferedReader(BufferedReader::new)
								.asSequence()
								.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * }
	 * 
	 * 
	 * 
	 * @param fn
	 * @return
	 */
	  AnyM<String> flatMapBufferedReader(Function<? super T,BufferedReader> fn) ;
	
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
	 * flatMap operation
	 * 
	 * @param fn
	 * @return 
	 */
	 <R> AnyM<R> flatMap(Function<? super T,AnyM<? extends R>> fn) ;
	
	/**
	 * Convenience method to allow method reference support, when flatMap return type is a Stream
	 * 
	 * @param fn
	 * @return
	 */
	 <R> AnyM<R> flatMapStream(Function<? super T,BaseStream<? extends R,?>> fn);
	/**
	 * Convenience method to allow method reference support, when flatMap return type is a Streamable
	 * 
	 * @param fn
	 * @return
	 */
	 <R> AnyM<R> flatMapStreamable(Function<? super T,Streamable<R>> fn) ;
	/**
	 * flatMapping to a Stream will result in the Stream being converted to a List, if the host Monad
	 * type is not a Stream (or Stream like type). (i.e.
	 *  <pre>
	 *  {@code  
	 *   AnyM<Integer> opt = anyM(Optional.of(20));
	 *   Optional<List<Integer>> optionalList = opt.flatMap( i -> anyM(Stream.of(1,2,i))).unwrap();  
	 *   
	 *   //Optional [1,2,20]
	 *  }</pre>
	 *  
	 *  In such cases using Arrays.asList would be more performant
	 *  <pre>
	 *  {@code  
	 *   AnyM<Integer> opt = anyM(Optional.of(20));
	 *   Optional<List<Integer>> optionalList = opt.flatMapCollection( i -> asList(1,2,i))).unwrap();  
	 *   
	 *   //Optional [1,2,20]
	 *  }</pre>
	 * @param fn
	 * @return
	 */
	 <R> AnyM<R> flatMapCollection(Function<? super T,Collection<? extends R>> fn);
	/**
	 * Convenience method to allow method reference support, when flatMap return type is a Optional
	 * 
	 * @param fn
	 * @return
	 */
	 <R> AnyM<R> flatMapOptional(Function<? super T,Optional<? extends R>> fn) ;
	 <R> AnyM<R> flatMapCompletableFuture(Function<? super T,CompletableFuture<? extends R>> fn);

	 <R> AnyM<R> flatMapSequenceM(Function<? super T,SequenceM<? extends R>> fn);
	
	
	
	
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
	 <NT> SequenceM<NT> toSequence(Function<T,Stream<NT>> fn);
	/**
	 *  <pre>{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
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
	 * AnyM<Integer> applied =anyM(Stream.of(1,2,3)).applyM(AnyM.fromStreamable(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2)));
	
	 	assertThat(applied.toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 }</pre>
	 * 
	 * with Optionals 
	 * <pre>{@code
	 * 
	 *  Any<Integer> applied =anyM(Optional.of(2)).applyM(AnyM.fromOptional(Optional.of( (Integer a)->a+1)) );
		assertThat(applied.toList(),equalTo(Arrays.asList(3)));}
		</pre>
	 * 
	 * @param fn
	 * @return
	 */
	 <R> AnyM<R> applyM(AnyM<Function<? super T,? extends R>> fn);
	/**
	 * Filter current monad by each element in supplied Monad
	 * 
	 * e.g.
	 * 
	 * <pre>{@code
	 *  AnyM<AnyM<Integer>> applied = AnyM.fromStream(Stream.of(1,2,3))
	 *    									.filterM(AnyM.fromStreamable(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3)));
	 *    								
	 * 
	 *  //results in AnyM((AnyM(1),AnyM(2),AnyM(())
	 * //or in terms of the underlying monad as Stream.of(Stream.of(1),Stream.of(2),Stream.of(())
	 * }</pre>
	 * 
	 * @param fn
	 * @return
	 */
	   AnyM<AnyM<T>> simpleFilter(AnyM<Predicate<? super T>> fn);
	   AnyM<Stream<T>> simpleFilter(Stream<Predicate<? super T>> fn);
	   AnyM<Stream<T>> simpleFilter(Streamable<Predicate<? super T>> fn);
	   AnyM<Optional<T>> simpleFilter(Optional<Predicate<? super T>> fn);
	   AnyM<CompletableFuture<T>> simpleFilter(CompletableFuture<Predicate<? super T>> fn);
	  
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
	 *   Monoid<Optional<Integer>> optionalAdd = Monoid.of(Optional.of(0), (a,b)-> Optional.of(a.get()+b.get()));
		
		AnyM.fromStream(Stream.of(2,8,3,1)).reduceM(optionalAdd);
		
		//AnyM[Optional(14)];
		}</pre>
	 * 
	 * 
	 * @param reducer
	 * @return
	 */
	 AnyM<T> reduceMOptional(Monoid<Optional<T>> reducer);
	 AnyM<T> reduceMStream(Monoid<Stream<T>> reducer);
	 AnyM<T> reduceMStreamable(Monoid<Streamable<T>> reducer);
	 AnyM<T> reduceMCompletableFuture(Monoid<CompletableFuture<T>> reducer);
	  
	 AnyM<T> reduceM(Monoid<AnyM<T>> reducer);
	
	
	 
	
	/**
	 * @return String representation of this AnyM
	 */
	@Override
    public String toString() ;
	/**
	 * @return Convert this AnyM to an Optional
	 */
	default Optional<List<T>> toOptional(){
		
		return this.<T>toSequence().toOptional();
	}
	/**
	 * @return Convert this AnyM to a CompletableFuture
	 */
	default CompletableFuture<List<T>> toCompletableFuture(){
		return this.<T>toSequence().toCompletableFuture();
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
	
	
	 
	
}
package com.aol.cyclops.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import sun.security.pkcs11.wrapper.Functions;

import com.aol.cyclops.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.comprehensions.donotation.Do;
import com.aol.cyclops.comprehensions.donotation.Do.DoComp1;
import com.aol.cyclops.dynamic.As;
import com.aol.cyclops.lambda.api.Decomposable;
import com.aol.cyclops.lambda.api.Mappable;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Reducers;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.Monad;
import com.aol.cyclops.lambda.monads.MonadWrapper;
import com.aol.cyclops.lambda.monads.Monads;
import com.aol.cyclops.matcher.Cases;
import com.aol.cyclops.matcher.Extractors;
import com.aol.cyclops.matcher.Matchable;
import com.aol.cyclops.matcher.Predicates;
import com.aol.cyclops.matcher.builders.ADTPredicateBuilder;
import com.aol.cyclops.matcher.builders.CheckTypeAndValues;
import com.aol.cyclops.matcher.builders.ElementCase;
import com.aol.cyclops.matcher.builders.IterableCase;
import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.builders.MatchingInstance;
import com.aol.cyclops.matcher.builders.StreamCase;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.trampoline.Trampoline;
import com.aol.cyclops.value.StreamableValue;
import com.aol.cyclops.value.ValueObject;

public class Core extends Functions {
	/**
	 * Wrap the object as a replayable Stream
	 * 
	 * @param toCoerce Object to wrap as a replayable Stream
	 * @return Replayable Stream
	 */
	public static <T> Streamable<T> asStreamable(Object toCoerce){
		return As.asStreamable(toCoerce);
	}
	/**
	 * Wrap the stream as a replayable Stream
	 * 
	 * @param toCoerce Stream to wrap as a replayable Stream
	 * @return Replayable Stream
	 */
	public static <T> Streamable<T> asStreamable(Stream<T> toCoerce){
		return As.asStreamable(toCoerce);
	}

	/**
	 * Coerce / wrap an Object as a StreamableValue instance
	 * Adds pattern matching and decomposability
	 * As well as the ability to convert the fields of the supplied
	 * Object into a Stream
	 * 
	 * @param toCoerce Object to making into a StreamableValue
	 * @return StreamableValue that adds functionality to the supplied object
	 */
	public static <T> StreamableValue<T> asStreamableValue(Object toCoerce){
		return As.asStreamableValue(toCoerce);
	}
	/**
	 * Coerce an Object to implement the ValueObject interface
	 * Adds pattern matching and decomposability functionality
	 * 
	 * @param toCoerce Object to coerce
	 * @return ValueObject that adds functionality to the supplied object
	 */
	public static ValueObject asValue(Object toCoerce){
		return As.asValue(toCoerce);
	}
	/**
	 * Coerce / wrap an Object as a Decomposable instance
	 * This adds an unapply method that returns an interable over the supplied
	 * objects fields.
	 * 
	 * Can be useful for pattern matching against object fields
	 * 
	 * 
	 * @param toCoerce Object to convert into a Decomposable
	 * @return Decomposable  that adds functionality to the supplied object
	 */
	public static  Decomposable asDecomposable(Object toCoerce){
		return As.asDecomposable(toCoerce);
	}
	
	/**
	 * Convert supplied object to a Mappable instance.
	 * Mappable will convert the (non-static) fields of the supplied object into a map
	 * 
	 * 
	 * @param toCoerce Object to convert to a Mappable
	 * @return  Mappable instance
	 */
	public static  Mappable asMappable(Object toCoerce){
		return As.asMappable(toCoerce);
	}
	
	/**
	 * Coerce / wrap an Object as a Matchable instance
	 * This adds match / _match methods for pattern matching against the object
	 * 
	 * @param toCoerce Object to convert into a Matchable
	 * @return Matchable that adds functionality to the supplied object
	 */
	public static  Matchable asMatchable(Object toCoerce){
		return As.asMatchable(toCoerce);
	}

	
	/**
	 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> asAnyM(Object monad){
		return As.asAnyM(monad);
	}
	/**
	 * Create a Duck typed functor. Wrapped class should have a method
	 * 
	 * map(F f)
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.
	 * 
	 * @param o functor to wrap
	 * @return Duck typed functor
	 */
	public static <T> Functor<T> asFunctor(Object o){
		return As.asFunctor(o);
	}
	/**
	 * Create a Duck typing  based Supplier
	 * 
	 * 
	 * 
	 * @param toCoerce Object to convert into a Supplier, 
	 * 		must have a non-void get() method
	 * @return Supplier that delegates to the supplied object
	 */
	public static <T>  Supplier<T> asSupplier(Object toCoerce){
		return As.asSupplier(toCoerce);
	}
	
	/**
	 * Create a Duck typing  based Supplier
	 * That returns the result of a call to the supplied method name
	 * 
	 * @param toCoerce Object to convert into a supplier
	 * @param method Method to call when Supplier.get() called
	 * @return Supplier that delegates to supplied object
	 */
	public static <T>  Supplier<T> asSupplier(Object toCoerce, String method){
		return As.asSupplier(toCoerce,method);
	}
	/**
	 * Wrap supplied Monoid object in the cylops Monoid interface
	 * 
	 * Will look for sum(a,b) or combine(a,b) methods for combiner
	 * and zero() method for zero
	 * 
	 * @param o Monoid type to wrap
	 * @return Cyclopse Monoid
	 */
	public static <A> Monoid<A> asMonoid(Object o){
		return As.asMonoid(o);
	}
	
	/**
	 * Create a Trampoline that is completed
	 * 
	 * @param t Result value
	 * @return Completed Trampoline
	 */
	public static <T> Trampoline<T> asDone(T t){
		return As.asDone(t);
	}
	/**
	 * Create a Trampoline with more work to do
	 * 
	 * <pre>
	 * {@code
	 * 		return As.asMore(()->loop(times-1,sum+times));
	 * }
	 * 
	 * @param trampoline Next stage in computation
	 * @return In progress Trampoline
	 */
	public static <T> Trampoline<T> asMore(Trampoline<Trampoline<T>> trampoline){
		return As.asMore(trampoline);
	}
	
	/**
	 * Reverse a Stream
	 * 
	 * @param stream Stream to reverse
	 * @return Reversed stream
	 */
	public static <U> Stream<U> reverse(Stream<U> stream){
		return StreamUtils.reverse(stream);
	}
	/**
	 * Create a reversed Stream from a List
	 * 
	 * @param list List to create a reversed Stream from
	 * @return Reversed Stream
	 */
	public static <U> Stream<U> reversedStream(List<U> list){
		return StreamUtils.reversedStream(list);
	}
	/**
	 * Create a new Stream that infiniteable cycles the provided Stream
	 * @param s Stream to cycle
	 * @return New cycling stream
	 */
	public static <U> Stream<U> cycle(Stream<U> s){
		return StreamUtils.cycle(s);
	}
	/**
	 * Create a Stream that infiniteable cycles the provided Streamable
	 * @param s Streamable to cycle
	 * @return New cycling stream
	 */
	public static <U> Stream<U> cycle(Streamable<U> s){
		return StreamUtils.cycle(s);
	}
	
	/**
	 * Create a Stream that infiniteable cycles the provided Streamable
	 * @param s Streamable to cycle
	 * @return New cycling stream
	 */
	public static <U> Stream<U> cycle(int times,Streamable<U> s){
		return StreamUtils.cycle(times, s);
	}
	
	/**
	 * Create a stream from an iterable
	 * 
	 * @param it Iterable to convert to a Stream
	 * @return Stream from iterable
	 */
	public static <U> Stream<U> stream(Iterable<U> it){
		return StreamUtils.stream(it);
	}
	/**
	 * Create a stream from an iterator
	 * 
	 * @param it Iterator to convert to a Stream
	 * @return Stream from iterator
	 */
	public static <U> Stream<U> stream(Iterator<U> it){
		return StreamUtils.stream(it);
	}
	
	
	/**
	 * Concat an Object and a Stream
	 * If the Object is a Stream, Streamable or Iterable will be converted (or left) in Stream form and concatonated
	 * Otherwise a new Stream.of(o) is created
	 * 
	 * @param o Object to concat
	 * @param stream  Stream to concat
	 * @return Concatonated Stream
	 */
	public static <U> Stream<U> concat(Object o, Stream<U> stream){
		return StreamUtils.concat(o, stream);		
	}
	/**
	 * Create a stream from a map
	 * 
	 * @param it Iterator to convert to a Stream
	 * @return Stream from a map
	 */
	public static <K,V> Stream<Map.Entry<K, V>> stream(Map<K,V> it){
		return StreamUtils.stream(it);
	}
	/**
	 * Simultanously reduce a stream with multiple reducers
	 * 
	 * <pre>{@code
	 * 
	 *  Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
		val result = StreamUtils.reduce(Stream.of(1,2,3,4),Arrays.asList(sum,mult));
				
		 
		assertThat(result,equalTo(Arrays.asList(10,24)));
		}</pre>
	 * 
	 * @param stream Stream to reduce
	 * @param reducers Reducers to reduce Stream
	 * @return Reduced Stream values as List entries
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	public static <R> List<R> reduce(Stream<R> stream,Iterable<Monoid<R>> reducers){
	
		return StreamUtils.reduce(stream, reducers);
	}
	/**
	 * Simultanously reduce a stream with multiple reducers
	 * 
	 *  @param stream Stream to reduce
	 * @param reducers Reducers to reduce Stream
	 * @return Reduced Stream values as List entries
	 */
	@SuppressWarnings({"rawtypes","unchecked"})
	public static <R> List<R> reduce(Stream<R> stream,Stream<Monoid<R>> reducers){
		return StreamUtils.reduce(stream, reducers);
		
	}
	
	/**
	 *  Apply multiple Collectors, simultaneously to a Stream
	 * 
	 * @param stream Stream to collect
	 * @param collectors Collectors to apply
	 * @return Result as a list
	 */
	public static <T,A,R> List<R> collect(Stream<T> stream, Stream<Collector> collectors){
		return StreamUtils.collect(stream, collectors);
	}
	/**
	 *  Apply multiple Collectors, simultaneously to a Stream
	 * 
	 * @param stream Stream to collect
	 * @param collectors Collectors to apply
	 * @return Result as a list
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T,A,R> List<R> collect(Stream<T> stream, Iterable<Collector> collectors){
		return StreamUtils.collect(stream, collectors);
	}
	/**
	 * Apply multiple Collectors, simultaneously to a Stream
	 * 
	 * @param stream Stream to collect
	 * @param collectors  Collectors to apply
	 * @return Result as a list
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> List collect(Stream<T> stream, Streamable<Collector> collectors){
		return StreamUtils.collect(stream, collectors);
	}
	

	/**
	 *
	 *Create a Pattern Matcher Builder from supplied Cases
	 * 
	 * @param cases to match on
	 * @return Pattern Mather Builder
	 */
	public static final <T,X> MatchingInstance<T,X> matchOf(Cases<T,X,? extends Function<T,X>> cases){
		return Matching.of(cases);
	}
	
	/**
	 * Create a builder for Matching on Case classes. This is the closest builder
	 * for Scala / ML style pattern matching.
	 * 
	 * Case classes can be constructed succintly in Java with Lombok or jADT
	 * e.g.
	 * <pre>{@code
	 * \@Value final class CaseClass implements Decomposable { int field1; String field2;}
	 * }
	 * 
	 * Use with static imports from the Predicates class to get wildcards via '__' or ANY()
	 * And to apply nested / recursive matching via Predicates.type(  ).with (   )
	 * 
	 * Match disaggregated elements by type, value, JDK 8 Predicate or Hamcrest Matcher
	 * 
	 * @return Case Class style Pattern Matching Builder
	 */
	public static final<USER_VALUE> CheckTypeAndValues<USER_VALUE> matchWhenValues(){
		return Matching.whenValues();
	}
	/**
	 * Create a builder for Matching against a provided Object as is (i.e. the Steps this builder provide assume you don't wish to disaggregate it and
	 * match on it's decomposed parts separately).
	 * 
	 * Allows matching by type, value, JDK 8 Predicate, or Hamcrest Matcher
	 * 
	 * @return Simplex Element based Pattern Matching Builder
	 */
	public static final<X> ElementCase<X> matchWhen(){
		return Matching.when();
	}
	
	/**
	 * Create a builder for matching on the disaggregated elements of a collection.
	 * 
	 * Allows matching by type, value, JDK 8 Predicate, or Hamcrest Matcher per element
	 * 
	 * @return Iterable / Collection based Pattern Matching Builder
	 */
	public static final<USER_VALUE> IterableCase<USER_VALUE> matchWhenIterable(){
		return Matching.whenIterable();
	}
	
	/**
	 * Create a builder that builds Pattern Matching Cases from Streams of data.
	 * 
	 * 
	 * @return Stream based Pattern Matching Builder
	 */
	public static final  StreamCase matchWhenFromStream(){
		return Matching.whenFromStream();
	}
	
	
	
	
	/**
	 * Start a for comprehension from a Supplier
	 * 
	 * Supplier#get will be called immediately
	 * 
	 *  If  supplied type is a Monad Cyclops knows about (@see com.aol.cyclops.lambda.api.Comprehender) it will be used directly
	 *  Otherwise an attempt will be made to lift the type to a Monadic form (@see com.aol.cyclops.lambda.api.MonadicConverter)
	 *
	 * 
	 * @param o Supplier that generates Object to use
	 * @return Next stage in the step builder
	 */
	public static  DoComp1 doWith(Supplier<Object> o){
		return  Do.with(o);
	}
	/**
	 * Build a for comprehension from supplied type
	 * If type is a Monad Cyclops knows about (@see com.aol.cyclops.lambda.api.Comprehender) it will be used directly
	 * Otherwise an attempt will be made to lift the type to a Monadic form (@see com.aol.cyclops.lambda.api.MonadicConverter)
	 * 
	 * @param o Object to use
	 * @return Next stage in for comprehension step builder
	 */
	public static  DoComp1 doWith(Object o){
		return Do.with(o);
	}
	
	
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (native / unwrapped in Monad wrapper interface)
	 * 
	 * @param fn
	 * @return
	 */
	public static <U1,R1,U2,R2> Function<U2,R2> liftMNative(Function<U1,R1> fn){
		return Monads.liftMNative(fn);
	}
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * Simplex view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * @param fn
	 * @return
	 */
	public static <U,R> Function<AnyM<U>,AnyM<R>> liftM(Function<U,R> fn){
		return Monads.liftM(fn);
	}
	public static <MONAD1,U,MONAD2,R> Function<Monad<MONAD1,U>,Monad<MONAD2,R>> liftMonad(Function<U,R> fn){
		return Monads.liftMonad(fn);
	}
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (native / unwrapped in Monad wrapper interface)
	 * 
	 * @param fn
	 * @return
	 */
	public static <U1,R1,U2,R2,U3,U4> BiFunction<U2,U3,R2> liftMNative2(BiFunction<U1,U4,R1> fn){
		return Monads.liftMNative2(fn);
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
		return Monads.liftM2(fn);
	}
	/**
	 *  Lift a function so it accepts Monads and returns a Monads. Allows Monad functionality to be 'injected' into plain methods or code.
	 *  
	 *  e.g. Add both looping error handling to a basic divide method 
	 *  <pre>
	 *  {@code 
	 *  
	 *  	val divide = Monads.liftM2(this::divide);
		
			AnyM<Integer> result = divide.apply(monad(Try.of(20, ArithmeticException.class)), monad(Stream.of(4,0,2,3)));
		
			assertThat(result.<Try<Integer,ArithmeticException>>unwrapMonad().isFailure(),equalTo(true));
	 *  
	 *  
	 *  	private Integer divide(Integer a, Integer b){
				return a/b;
			}
		}
	 *  </pre>
	 * 
	 * @param fn BiFunction to lift
	 * @return Lifted BiFunction
	 */
	public static <MONAD1,U1,MONAD2,U2,MONAD3,R> BiFunction<Monad<MONAD1,U1>,Monad<MONAD2,U2>,Monad<MONAD3,R>> liftMonad2(BiFunction<U1,U2,R> fn){
		return Monads.liftMonad2(fn);
	}
	
	
	
	/**
	 * Convert a list of Monads to a Monad with a List
	 * 
	 * <pre>{@code
	 * List<CompletableFuture<Integer>> futures;

        
        CompletableFuture<List<Integer>> futureList = Monads.sequence(CompletableFuture.class, futures);

	  
	  }
	 * </pre>
	 * @param c The type of Monad to convert
	 * @param seq List of monads to convert
	 * @return Monad with a List
	 */ 	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <MONAD,MONAD_LIST> MONAD_LIST sequenceNative(Class c,List<MONAD> seq){
		return Monads.sequenceNative(c, seq);
	}
	/**
	 * Convert a list of Monads to a Monad with a List applying the supplied function in the process
	 * 
	 * <pre>{@code 
	 *    List<CompletableFuture<Integer>> futures;

        
        CompletableFuture<List<String>> futureList = Monads.traverse(CompletableFuture.class, futures, (Integer i) -> "hello" +i);
        }
        </pre>

	 * 
	 * @param c Monad type to traverse
	 * @param seq List of Monads
	 * @param fn Function to apply 
	 * @return Monad with a list
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <MONAD,MONAD_LIST,R> MONAD_LIST traverseNative(Class c,List<MONAD> seq, Function<?,R> fn){
		return Monads.traverseNative(c, seq, fn);
	}
	
	/**
	 * Convert a list of Monads to a Monad with a List applying the supplied function in the process
	 * 
	 * <pre>{@code 
	 *    List<CompletableFuture<Integer>> futures;

        
        AnyM<List<String>> futureList = Monads.traverse(CompletableFuture.class, futures, (Integer i) -> "hello" +i).anyM();
        }
		</pre>
	 * 
	 * @param c Monad type to traverse
	 * @param seq List of Monads
	 * @param fn Function to apply 
	 * @return Monad with a list
	 */
	public static <MONAD,R> Monad<MONAD,List<R>> traverse(Class c,List<?> seq, Function<?,R> fn){
		return Monads.traverse(c, seq, fn);
	}

	
	/**
	 * Convert a list of Monads to a Monad with a List
	 * 
	 * <pre>{@code
	 * List<CompletableFuture<Integer>> futures;

       
        AnyM<List<Integer>> futureList = Monads.sequence(CompletableFuture.class, futures).anyM();

	   //where Simplex wraps  CompletableFuture<List<Integer>>
	  }</pre>
	 * 
	 * @param c The type of Monad to convert
	 * @param seq List of monads to convert
	 * @return Monad with a List
	 */ 
	public static <MONAD,T>  Monad<MONAD,T> sequence(Class c, List<?> seq){
		return Monads.sequence(c, seq);
	}
	
	
	/**
	 * Create a Monad wrapper from a Streamable
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Streamable<T> anyM){
		return Monads.anyM(anyM);
	}
	/**
	 * Create a Monad wrapper from a Stream
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Stream<T> anyM){
		return Monads.anyM(anyM);
	}
	/**
	 * Create a Monad wrapper from an Optional
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Optional<Integer>,Integer> opt;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> opt;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Optional<T> anyM){
		return Monads.anyM(anyM);
	}
	/**
	 * Create a Monad wrapper from a CompletableFuture
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<CompletableFuture<Integer>,Integer> future;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> future;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)  -- thenApply/Async
	 * 
	 * flatMap(F<x,MONAD> fm) -- thenCompose/Async
	 * 
	 * and optionally 
	 * 
	 * filter(P p)  -- not present for CompletableFutures
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(CompletableFuture<T> anyM){
		return Monads.anyM(anyM);
	}
	/**
	 * Create a Monad wrapper from a Collection
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Collection<T> anyM){
		return Monads.anyM(anyM);
	}
	/**
	 * Create a Monad wrapper from an Iterable
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Iterable<T> anyM){
		return Monads.anyM(anyM);
	}
	/**
	 * Create a Monad wrapper from an Iterator
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(Iterator<T> anyM){
		return Monads.anyM(anyM);
	}
	/**
	 * Create a Monad wrapper from an array of values
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> anyM(T... values){
		return Monads.anyM(values);
	}
	/**
	 * Create a Monad wrapper from an Object
		 * Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> toAnyM(Object anyM){
		return Monads.toAnyM(anyM);
	}
	/**
	 * Create a Monad wrapper from an Object that will be converted to Monadic form if neccessary by the registered
	 * MonadicConverters. You can register your own MonadicConverter instances and / or change the priorities of currently registered converters.
	 * 
	* Create a duck typed Monad wrapper. Using AnyM we focus only on the underlying type
	 * e.g. instead of 
	 * <pre>
	 * {@code 
	 *  Monad<Stream<Integer>,Integer> stream;
	 * 
	 * we can write
	 * 
	 *   AnyM<Integer> stream;
	 * }
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> convertToAnyM(Object anyM){
		
		return Monads.convertToAnyM(anyM);
	}
	
	/**
	 * Create a duck typed Monad wrapper. 
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <MONAD,T> Monad<MONAD,T> asMonad(Object monad){
		return Monads.asMonad(monad);
	}

	/**
	 * Create a Monad wrapper from a Streamable Create a duck typed Monad
	 * wrapper.
	 * 
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad
	 * Types. Cyclops will attempt to manage any Monad type (via the
	 * InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single
	 * parameter and returns a result. Where P is a Functional Interface of any
	 * type that takes a single parameter and returns a boolean
	 * 
	 * flatMap operations on the duck typed Monad can return any Monad type
	 * 
	 * 
	 * @param anyM
	 *            to wrap
	 * @return Duck typed Monad
	 */
	public static <T> Monad<Stream<T>, T> monad(Streamable<T> monad) {
		return Monads.monad(monad);
	}
	
	/**
	 * Create a Monad wrapper from a Stream
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */	
	public static <T> Monad<Stream<T>,T> monad(Stream<T> monad){
		return Monads.monad(monad);
	}
	/**
	 * Create a Monad wrapper from an Optional
	 *
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> Monad<Optional<T>,T> monad(Optional<T> monad){
		return Monads.monad(monad);
	}
	/**
	 * Create a Monad wrapper from a CompletableFuture
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)  -- thenApply/Async
	 * 
	 * flatMap(F<x,MONAD> fm) -- thenCompose/Async
	 * 
	 * and optionally 
	 * 
	 * filter(P p)  -- not present for CompletableFutures
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	
	public static <T> Monad<CompletableFuture<T>,T> monad(CompletableFuture<T> monad){
		return Monads.monad(monad);
	}
	/**
	 * Create a Monad wrapper from a Collection
		
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> Monad<Stream<T>,T> monad(Collection<T> monad){
		return Monads.monad(monad);
	}
	/**
	 * Create a Monad wrapper from an Iterable
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> Monad<Stream<T>,T> monad(Iterable<T> monad){
		return Monads.monad(monad);
	}
	/**
	 * Create a Monad wrapper from an Iterator
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> Monad<Stream<T>,T> monad(Iterator<T> monad){
		return Monads.monad(monad);
	}
	/**
	 * Create a Monad wrapper from an array of values
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> Monad<Stream<T>,T> monad(T... values){
		return Monads.monad(values);
	}
	/**
	 * Create a Monad wrapper from an Object
	 *  
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T> Monad<?,T> toMonad(Object monad){
		return Monads.toMonad(monad);
	}
	/**
	 * Create a Monad wrapper from an Object that will be converted to Monadic form if neccessary by the registered
	 * MonadicConverters. You can register your own MonadicConverter instances and / or change the priorities of currently registered converters.
	 * 
	* Create a duck typed Monad wrapper. 
	 * The wrapped Monaad should have equivalent methods for
	 * 
	 * <pre>
	 * {@code 
	 * map(F f)
	 * 
	 * flatMap(F<x,MONAD> fm)
	 * 
	 * and optionally 
	 * 
	 * filter(P p)
	 * }
	 * </pre>
	 * 
	 * A Comprehender instance can be created and registered for new Monad Types. Cyclops will attempt
	 * to manage any Monad type (via the InvokeDynamicComprehender) althouh behaviour is best guaranteed with
	 * customised Comprehenders.
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 *  
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <T,MONAD> Monad<T,MONAD> convertToMonad(Object monad){
		return Monads.convertToMonad(monad);
	}
	
	
	/** 
	 * Reducers should be accessed Statically - this helper method is here to help you
	 * navigate the API. Most IDEs will support refactoring from Object / instance level access to static Access
	 * 
	 * 
	 * @return Reducers (access them statically)
	 */
	public static Reducers reducers(){
		return new Reducers();
	}
	/** 
	 * Extractors should be accessed Statically - this helper method is here to help you
	 * navigate the API. Most IDEs will support refactoring from Object / instance level access to static Access
	 * 
	 * 
	 * @return Extractors (access them statically)
	 */
	public static Extractors extractors(){
		return new Extractors();
	}
	/** 
	 * Predicates should be accessed Statically - this helper method is here to help you
	 * navigate the API. Most IDEs will support refactoring from Object / instance level access to static Access
	 * 
	 * 
	 * @return Predicates (access them statically)
	 */
	public static Predicates  predicates(){
		return new Predicates();
	}
	/**
	 * wildcard predicate
	 * 
	 */
	public static final Predicate __ = test ->true;
	
	/**
	 * Recursively decompose and match against case classes of specified type.
	 * 
	 * <pre>
	 * {@code
	 *  return Matching.<Expression>whenValues().isType( (Add<Const,Mult> a)-> new Const(1))
									.with(__,type(Mult.class).with(__,new Const(0)))
				.whenValues().isType( (Add<Mult,Const> a)-> new Const(0)).with(type(Mult.class).with(__,new Const(0)),__)
				.whenValues().isType( (Add<Add,Const> a)-> new Const(-100)).with(with(__,new Const(2)),__)
				
				
				.apply(e).orElse(new Const(-1));
	 * 
	 * }
	 * </pre>
	 * 
	 * 
	 * @param type Classs type to decompose
	 * @return Predicate builder that can decompose classes of specified type
	 */
	public	static<T> ADTPredicateBuilder<T> type(Class<T> type){
			return Predicates.type(type);
	}
	/**
	 * Recursively compose an Object without specifying a type
	 * 
	 * <pre>
	 * {@code 
	 * return Matching.<Expression>whenValues().isType( (Add<Const,Mult> a)-> new Const(1))
									.with(__,type(Mult.class).with(__,new Const(0)))
				.whenValues().isType( (Add<Mult,Const> a)-> new Const(0)).with(type(Mult.class).with(__,new Const(0)),__)
				.whenValues().isType( (Add<Add,Const> a)-> new Const(-100)).with(with(__,new Const(2)),__)
				
				
				.apply(e).orElse(new Const(-1));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param values To match against
	 * @return Predicate builder that can decompose Case class and match against specified values
	 */
	public	static<V> Predicate with(V... values){
		return Predicates.with(values);
	}
	
	
}

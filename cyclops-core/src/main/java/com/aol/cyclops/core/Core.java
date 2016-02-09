package com.aol.cyclops.core;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.Reducer;
import com.aol.cyclops.closures.immutable.LazyImmutable;
import com.aol.cyclops.closures.mutable.Mutable;
import com.aol.cyclops.dynamic.As;
import com.aol.cyclops.functions.Functions;
import com.aol.cyclops.lambda.api.Mappable;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.Cases;
import com.aol.cyclops.matcher.CollectionMatcher;
import com.aol.cyclops.matcher.Extractors;
import com.aol.cyclops.matcher.Predicates;
import com.aol.cyclops.matcher.builders.ADTPredicateBuilder;
import com.aol.cyclops.matcher.builders.CheckTypeAndValues;
import com.aol.cyclops.matcher.builders.ElementCase;
import com.aol.cyclops.matcher.builders.IterableCase;
import com.aol.cyclops.matcher.builders.Matching;
import com.aol.cyclops.matcher.builders.MatchingInstance;
import com.aol.cyclops.matcher.builders.StreamCase;
import com.aol.cyclops.matcher.recursive.Matchable;
import com.aol.cyclops.matcher.recursive.RecursiveMatcher;
import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.objects.Decomposable;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.trampoline.Trampoline;
import com.aol.cyclops.value.StreamableValue;
import com.aol.cyclops.value.ValueObject;

@Deprecated
public class Core extends Functions {
	/**
	 * Construct an instance of Case from supplied predicate and action
	 * 
	 * @param predicate That will be used to match
	 * @param action Function that is executed on succesful match
	 * @return New Case instance
	 */
	public static <T,R,X extends Function<T,R>> Case<T,R,X> caseOf(Predicate<T> predicate,X action){
		return Case.of(predicate,action);
	}
	/**
	 * Construct a Cases instance from an array Pattern Matching Cases
	 * Will execute sequentially when Match is called.
	 * 
	 * @param cases Array of cases to build Cases instance from 
	 * @return New Cases instance (sequential)
	 */
	public static <T,R,X extends Function<T,R>>  Cases<T,R,X> casesOf(Case<T,R,X>... cases){
		return Cases.of(cases);
	}
	
	/**
	 * @return an unitialised LazyImmutable
	 */
	public static <T> LazyImmutable<T> lazyImmutable(){
		return LazyImmutable.def();
	}
	/**
	 * Create a Mutable variable, which can be mutated inside a Closure 
	 * 
	 * e.g.
	 * <pre>{@code
	 *   Mutable<Integer> num = Mutable.of(20);
	 *   
	 *   Stream.of(1,2,3,4).map(i->i*10).peek(i-> num.mutate(n->n+i)).foreach(System.out::println);
	 *   
	 *   System.out.println(num.get());
	 *   //prints 120
	 * } </pre>
	 * 
	 * @param var Initial value of Mutable
	 * @return New Mutable instance
	 */
	public static <T> Mutable<T> mutable(T var){
		return Mutable.of(var);
	}
 	/**
	 * Wrap the object as a replayable Stream
	 * 
	 * @param toCoerce Object to wrap as a replayable Stream
	 * @return Replayable Stream
	 */
	public static <T> Streamable<T> asStreamable(Object toCoerce){
		return As.asStreamableFromObject(toCoerce);
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
	 * }</pre>
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
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> ofMonad(Object monad){
		return AnyM.ofMonad(monad);
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
	 * </pre>
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
	public static <R> List<R> reduce(Stream<R> stream,Iterable<Reducer<R>> reducers){
	
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
	 * }</pre>
	 * 
	 * Use with static imports from the Predicates class to get wildcards via '__' or ANY()
	 * And to apply nested / recursive matching via Predicates.type(  ).with (   )
	 * 
	 * Match disaggregated elements by type, value, JDK 8 Predicate or Hamcrest Matcher
	 * 
	 * @return Case Class style Pattern Matching Builder
	 */
	public static final<USER_VALUE> CheckTypeAndValues<USER_VALUE> matchWhenValues(){
		return RecursiveMatcher.when();
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
		return CollectionMatcher.whenIterable();
	}
	
	/**
	 * Create a builder that builds Pattern Matching Cases from Streams of data.
	 * 
	 * 
	 * @return Stream based Pattern Matching Builder
	 */
	public static final  StreamCase matchWhenFromStream(){
		return CollectionMatcher.whenFromStream();
	}
	
	
	
	

	
	/**
	 * Lift a function so it accepts a Monad and returns a Monad (simplex view of a wrapped Monad)
	 * Simplex view simplifies type related challenges. The actual native type is not specified here.
	 * 
	 * @param fn
	 * @return
	 */
	public static <U,R> Function<AnyM<U>,AnyM<R>> liftM(Function<U,R> fn){
		return AnyM.liftM(fn);
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
		return AnyM.liftM2(fn);
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
		return Predicates.hasValues(values);
	}
	
	
}

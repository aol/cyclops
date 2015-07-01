package com.aol.cyclops.lambda.monads;

import static com.aol.cyclops.lambda.api.AsGenericMonad.asMonad;
import static com.aol.cyclops.lambda.api.AsGenericMonad.monad;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.api.AsStreamable;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.lambda.api.Unwrapable;
import com.aol.cyclops.streams.Pair;
import com.aol.cyclops.streams.StreamUtils;
import com.nurkiewicz.lazyseq.LazySeq;

@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class AnyM<T> implements Unwrapable{
	
	private final Monad<Object,T> monad;
	
	
	public final <R> R unwrap(){
		return (R)monad.unwrap();
	}
	public final <MONAD> Monad<MONAD,T> monad(){
		return (Monad)monad;
	}
	
	public final   AnyM<T>  filter(Predicate<? super T> fn){
		return monad.filter(fn).anyM();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	public final  <R> AnyM<R> map(Function<? super T,? extends R> fn){
		return monad.map(fn).anyM();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	public final   AnyM<T>  peek(Consumer<? super T> c) {
		return monad.peek(c).anyM();
	}
	
	
	/**
	 * Perform a looser typed flatMap / bind operation
	 * The return type can be another type other than the host type
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	*/
	public final <R> AnyM<R> bind(Function<? super T,?> fn){
		return monad.bind(fn).anyM();
	
	} 
	/**
	 * Perform a bind operation (@see #bind) but also lift the return value into a Monad using configured
	 * MonadicConverters
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	 */
	public final <R> AnyM<R> liftAndBind(Function<? super T,?> fn){
		return monad.liftAndBind(fn).anyM();
	
	}
	
	/**
	 * join / flatten one level of a nested hierarchy
	 * 
	 * @return Flattened / joined one level
	 */
	public final <T1> AnyM<T1> flatten(){
		return monad.flatten().anyM();
		
	}
	
	/**
	 * Aggregate the contents of this Monad and the supplied Monad 
	 * 
	 * <pre>{@code 
	 * 
	 * List<Integer> result = monad(Stream.of(1,2,3,4)).<Integer>aggregate(monad(Optional.of(5))).toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
		}</pre>
	 * 
	 * @param next Monad to aggregate content with
	 * @return Aggregated Monad
	 */
	public final <R> AnyM<R> aggregate(AnyM<?> next){
		return monad.aggregate(next.monad).anyM();
	}
	
	/**
	 * flatMap operation
	 * 
	 * @param fn
	 * @return
	 */
	public final <R> AnyM<R> flatMap(Function<? super T,AnyM<? extends R>> fn) {
		return monad.flatMap(in -> fn.apply(in).unwrap()).anyM();
	}
	
	
	
	
	/**
	 * Sequence the contents of a Monad.  e.g.
	 * Turn an <pre>{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
	 * 
	 * <pre>{@code
	 * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.traversable(c->c.stream())
											.collect(Collectors.toList());
		
		
		assertThat(list,hasItems(1,2,3,4,5,6));
		
	 * 
	 * }</pre>
	 * 
	 * @return A Monad that wraps a Stream
	 */
	public final <NT> TraversableM<NT> traversable(Function<T,Stream<NT>> fn){
		return monad.flatMapToStream((Function)fn)
					.sequence();
	}
	/**
	 *  <pre>{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
	 * Less type safe equivalent, but may be more accessible equivalent to  
	 * <pre>
	 * {@code 
	 *    sequence(Function<T,Stream<NT>> fn)
	 *   }
	 *   </pre>
	 *  <pre>{@code
	 * List<Integer> list = anyM(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.traversable()
											.collect(Collectors.toList());
		
		
		
	 * 
	 * }</pre>
	
	 * @return A Monad that wraps a Stream
	 */
	public final <T> TraversableM<T> traversable(){
		return monad.streamedMonad().sequence();
	}
	
	/**
	 * Wrap this Monad's contents as a Sequence without disaggreating it. .e.
	 *  <pre>{@code Optional<List<Integer>>  into Stream<List<Integer>> }</pre>
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	public final TraversableM<T> toTraversable(){
		return monad.sequence();
		
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
	 * @param o to wrap
	 * @return Duck typed Monad
	 */
	public static <T> AnyM<T> of(Object o){
		return AsAnyM.notTypeSafeAnyM(o);
	}
	
	
		
	

	/**
	 * Apply function/s inside supplied Monad to data in current Monad
	 * 
	 * e.g. with Streams
	 * <pre>{@code 
	 * 
	 * Simplex<Integer> applied =monad(Stream.of(1,2,3)).applyM(monad(Streamable.of( (Integer a)->a+1 ,(Integer a) -> a*2))).simplex();
	
	 	assertThat(applied.toList(),equalTo(Arrays.asList(2, 2, 3, 4, 4, 6)));
	 }</pre>
	 * 
	 * with Optionals 
	 * <pre>{@code
	 * 
	 *  Simplex<Integer> applied =monad(Optional.of(2)).applyM(monad(Optional.of( (Integer a)->a+1)) ).simplex();
		assertThat(applied.toList(),equalTo(Arrays.asList(3)));}</pre>
	 * 
	 * @param fn
	 * @return
	 */
	public final <R> AnyM<R> applyM(AnyM<Function<? super T,? extends R>> fn){
		return monad.applyM(fn.monad).anyM();
		
	}
	/**
	 * Filter current monad by each element in supplied Monad
	 * 
	 * e.g.
	 * 
	 * <pre>{@code
	 *  Simplex<Stream<Integer>> applied = monad(Stream.of(1,2,3))
	 *    									.filterM(monad(Streamable.of( (Integer a)->a>5 ,(Integer a) -> a<3)))
	 *    									.simplex();
	 * 
	 * //results in Stream.of(Stream.of(1),Stream.of(2),Stream.of(())
	 * }</pre>
	 * 
	 * @param fn
	 * @return
	 */
	public final <NT,R> Monad<NT,R> simpleFilter(AnyM<Predicate<? super T>> fn){
		return  monad.simpleFilter(fn.monad);
			
	
	//	filterM((a: Int) => List(a > 2, a % 2 == 0), List(1, 2, 3), ListMonad),
	//List(List(3), Nil, List(2, 3), List(2), List(3),
	//	  Nil, List(2, 3), List(2))												
	}
	/**
	 * 
	 * Replicate given Monad
	 * 
	 * <pre>{@code 
	 * 	
	 *   Simplex<Optional<Integer>> applied =monad(Optional.of(2)).replicateM(5).simplex();
		 assertThat(applied.unwrap(),equalTo(Optional.of(Arrays.asList(2,2,2,2,2))));
		 
		 }</pre>
	 * 
	 * 
	 * @param times number of times to replicate
	 * @return Replicated Monad
	 */
	public final <NT,R> Monad<NT,R> replicateM(int times){
		
		return monad.replicateM(times);		
	}
	/**
	 * Perform a reduction where NT is a (native) Monad type
	 * e.g. 
	 * <pre>{@code 
	 * Monoid<Optional<Integer>> optionalAdd = Monoid.of(Optional.of(0), (a,b)-> Optional.of(a.get()+b.get()));
		
		assertThat(monad(Stream.of(2,8,3,1)).reduceM(optionalAdd).unwrap(),equalTo(Optional.of(14)));
		}</pre>
	 * 
	 * 
	 * @param reducer
	 * @return
	 */
	public final <NT,R> Monad<NT,R> reduceM(Monoid<NT> reducer){
	//	List(2, 8, 3, 1).foldLeftM(0) {binSmalls} -> Optional(14)
	//	convert to list Optionals
		
		return monad.reduceM(reducer);		
	}
	
	
	
	@Override
    public String toString() {
        return String.format("AnyM(%s)", monad );
    }
	
}
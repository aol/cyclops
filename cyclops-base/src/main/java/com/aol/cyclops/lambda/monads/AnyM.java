package com.aol.cyclops.lambda.monads;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Unwrapable;
import com.nurkiewicz.lazyseq.LazySeq;

/**
 * 
 * Wrapper for Any Monad type
 * @see AnyMonads companion class for static helper methods
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@AllArgsConstructor(access=AccessLevel.PROTECTED)
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
	public final  AnyM<Character> liftAndBindCharSequence(Function<? super T,CharSequence> fn) {
		return monad.liftAndBind(fn).anyM();
	}
	public final  AnyM<String> liftAndBindFile(Function<? super T,File> fn) {
		return monad.liftAndBind(fn).anyM();
	}
	public final  AnyM<String> liftAndBindURL(Function<? super T, URL> fn) {
		return monad.liftAndBind(fn).anyM();
	}
	public final  AnyM<String> liftAndBindBufferedReader(Function<? super T,BufferedReader> fn) {
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
	 * List<Integer> result = anyM(Stream.of(1,2,3,4))
	 * 							.aggregate(anyM(Optional.of(5)))
	 * 							.asSequence()
	 * 							.toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
		}</pre>
	 * 
	 * @param next Monad to aggregate content with
	 * @return Aggregated Monad
	 */
	public final  AnyM<T> aggregate(AnyM<T> next){
		return monad.aggregate(next.monad).anyM();
	}
	public final  <R> AnyM<List<R>> aggregateUntyped(AnyM<?> next){
		return monad.aggregate(next.monad).anyM();
	}
	public void forEach(Consumer<? super T> action) {
		monad.forEach(action);
		
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
	
	public final <R> AnyM<R> flatMapStream(Function<? super T,BaseStream<? extends R,?>> fn) {
		return monad.flatMap(in -> fn.apply(in)).anyM();
	}
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
	public final <R> AnyM<R> flatMapCollection(Function<? super T,Collection<? extends R>> fn) {
		return monad.flatMap(in -> fn.apply(in)).anyM();
	}
	public final <R> AnyM<R> flatMapOptional(Function<? super T,Optional<? extends R>> fn) {
		return monad.flatMap(in -> fn.apply(in)).anyM();
	}
	public final <R> AnyM<R> flatMapCompletableFuture(Function<? super T,CompletableFuture<? extends R>> fn) {
		return monad.flatMap(in -> fn.apply(in)).anyM();
	}
	public final <R> AnyM<R> flatMapLazySeq(Function<? super T,LazySeq<? extends R>> fn) {
		return monad.flatMap(in -> fn.apply(in)).anyM();
	}
	public final <R> AnyM<R> flatMapSequenceM(Function<? super T,SequenceM<? extends R>> fn) {
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
	public final <NT> SequenceM<NT> toSequence(Function<T,Stream<NT>> fn){
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
	public final <T> SequenceM<T> toSequence(){
		return monad.streamedMonad().sequence();
	}
	
	/**
	 * Wrap this Monad's contents as a Sequence without disaggreating it. .e.
	 *  <pre>{@code Optional<List<Integer>>  into Stream<List<Integer>> }</pre>
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	public final SequenceM<T> asSequence(){
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
	 
	public static <T> AnyM<T> of(Object o){
		return AsAnyM.notTypeSafeAnyM(o);
	}*/
	
	
		
	

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
	public final <R> AnyM<R> simpleFilter(AnyM<Predicate<? super T>> fn){
		return  monad.simpleFilter(fn.monad).anyM();
			
	
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
	public final <R> AnyM<R> replicateM(int times){
		
		return monad.replicateM(times).anyM();		
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
	public final <R> AnyM<R> reduceM(Monoid<R> reducer){
	//	List(2, 8, 3, 1).foldLeftM(0) {binSmalls} -> Optional(14)
	//	convert to list Optionals
		
		return monad.reduceM(reducer).anyM();		
	}
	
	
	
	@Override
    public String toString() {
        return String.format("AnyM(%s)", monad );
    }
	
}
package com.aol.cyclops.lambda.monads;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.AsGenericMonad;
import com.aol.cyclops.lambda.api.AsStreamable;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.streams.StreamUtils;
import com.nurkiewicz.lazyseq.LazySeq;

public interface AnyM<T> extends Monad<Object,T>{
	default <R> R unwrapMonad(){
		return (R)unwrap();
	}
	default <MONAD> Monad<MONAD,T> monad(){
		return AsGenericMonad.asMonad(unwrapMonad());
	}
	
	default   AnyM<T>  filter(Predicate<T> fn){
		return (AnyM)Monad.super.filter(fn).anyM();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	default  <R> AnyM<R> map(Function<T,R> fn){
		return (AnyM)Monad.super.map(fn).anyM();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	default   AnyM<T>  peek(Consumer<T> c) {
		return (AnyM)Monad.super.peek(c).anyM();
	}
	
	
	/**
	 * Perform a looser typed flatMap / bind operation
	 * The return type can be another type other than the host type
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	 */
	default <R> AnyM<T> bind(Function<T,R> fn){
		return (AnyM)Monad.super.bind(fn).anyM();
	
	}
	/**
	 * Perform a bind operation (@see #bind) but also lift the return value into a Monad using configured
	 * MonadicConverters
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	 */
	default <R> AnyM<R> liftAndBindAnyM(Function<T,?> fn){
		return (AnyM)Monad.super.liftAndBind(fn).anyM();
	
	}
	
	/**
	 * join / flatten one level of a nested hierarchy
	 * 
	 * @return Flattened / joined one level
	 */
	default <T1> AnyM<T1> flattenAnyM(){
		return (AnyM)Monad.super.flatten().anyM();
		
	}
	
	
	
	
	
	
	/**
	 * Convert to a Stream with the values repeated specified times
	 * 
	 * @param times Times values should be repeated within a Stream
	 * @return Stream with values repeated
	 */
	default AnyM<T> cycleAnyM(int times){
		return Monad.super.cycle(times).anyM();
		
	}
	
	/**
	 * Convert to a Stream with the result of a reduction operation repeated specified times
	 * 
	 * {@code 
	  		List<Integer> list = AsGenericMonad,asMonad(Stream.of(1,2,2))
											.cycle(Reducers.toCountInt(),3)
											.collect(Collectors.toList());
		//is asList(3,3,3);
	  }
	 * 
	 * @param m Monoid to be used in reduction
	 * @param times Number of times value should be repeated
	 * @return Stream with reduced values repeated
	 */
	default AnyM<T> cycleAnyM(Monoid<T> m,int times){
		return Monad.super.cycle(m,times).anyM();
	}
	
	
	/**
	 * 
	 * Convert to a Stream, repeating the resulting structure specified times and
	 * lifting all values to the specified Monad type
	 * 
	 * {@code
	 * 
	 *  List<Optional<Integer>> list  = monad(Stream.of(1,2))
											.cycle(Optional.class,2)
											.toList();
											
	    //is asList(Optional.of(1),Optional.of(2),Optional.of(1),Optional.of(2)	));
	
	 * 
	 * }
	 * 
	 * 
	 * 
	 * @param monad
	 * @param times
	 * @return
	 */
	default <R> AnyM<R> cycleAnyM(Class<R> monad,int times){
		return Monad.super.cycle(monad,times).anyM();
	}

	/**
	 * Repeat in a Stream while specified predicate holds
	 * 
	 * @param predicate repeat while true
	 * @return Repeating Stream
	 */
	default  AnyM<T> cycleWhileAnyM(Predicate<T> predicate){
		return Monad.super.cycleWhile(predicate).anyM();
	}
	/**
	 * Repeat in a Stream until specified predicate holds
	 * 
	 * @param predicate repeat while true
	 * @return Repeating Stream
	 */
	default  AnyM<T> cycleUntilAnyM(Predicate<T> predicate){
		return Monad.super.cycleUntil(predicate).anyM();
	}
	/**
	 * Generic zip function. E.g. Zipping a Stream and an Optional
	 * 
	 * {@code
	 * Stream<List<Integer>> zipped = asMonad(Stream.of(1,2,3)).zip(asMonad(Optional.of(2)), 
													(a,b) -> Arrays.asList(a,b));
	 * // [[1,2]]
	 * }
	 * 
	 * @param second Monad to zip with
	 * @param zipper Zipping function
	 * @return Stream zipping two Monads
	 */
	default <S,R> AnyM<R> zipAnyM(AnyM<? extends S> second, BiFunction<? super T, ? super S, ? extends R> zipper){
		return Monad.super.zip(second, zipper).anyM();
	}
	
	/**
	 * Zip this Monad with a Stream
	 * 
	 * {@code 
	 * Stream<List<Integer>> zipped = asMonad(Stream.of(1,2,3)).zip(Stream.of(2,3,4), 
													(a,b) -> Arrays.asList(a,b));
													
		//[[1,2][2,3][3,4]]											
	 * }
	 * 
	 * @param second Stream to zip with
	 * @param zipper  Zip funciton
	 * @return This monad zipped with a Stream
	 */
	default <S,R> AnyM<R> zipAnyM(Stream<? extends S> second, BiFunction<? super T, ? super S, ? extends R> zipper){
		return Monad.super.zip(second, zipper).anyM();
	}
	/**
	 * Create a sliding view over this monad
	 * 
	 * @param windowSize Size of sliding window
	 * @return Stream with sliding view over monad
	 */
	default AnyM<List<T>> slidingAnyM(int windowSize){
		return Monad.super.sliding(windowSize).anyM();
	}
	
	/**
	 * Group elements in a Monad into a Stream
	 * 
	 * {@code
	 * 
	 * List<List<Integer>> list = monad(Stream.of(1,2,3,4,5,6))
	 * 									.grouped(3)
	 * 									.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
		
		}
	 * 
	 * @param groupSize Size of each Group
	 * @return Stream with elements grouped by size
	 */
	default AnyM<List<T>> groupedAnyM(int groupSize){
		return Monad.super.grouped(groupSize).anyM();
	}
	
	
	
	/*
	 * Return the distinct Stream of elements
	 * 
	 * {@code
	 * 	List<Integer> list = monad(Optional.of(Arrays.asList(1,2,2,2,5,6)))
											.<Stream<Integer>,Integer>streamedMonad()
											.distinct()
											.collect(Collectors.toList());
		}
	 */
	default AnyM<T> distinctAnyM(){
		return Monad.super.distinct().anyM();
	}
	/**
	 * Scan left using supplied Monoid
	 * 
	 * {@code  
	 * 
	 * 	assertEquals(asList("", "a", "ab", "abc"),monad(Stream.of("a", "b", "c")).scanLeft(Reducers.toString("")).toList());
            
            }
	 * 
	 * @param monoid
	 * @return
	 */
	default AnyM<T> scanLeftAnyM(Monoid<T> monoid){
		return Monad.super.scanLeft(monoid).anyM();
	}
	
	/**
	 * @return Monad converted to Stream via stream() and sorted - to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 *  e.g.
	 *  
	 *  {@code 
	 *    monad(Optional.of(Arrays.asList(1,2,3))).sorted()  // Monad[Stream[List[1,2,3]]]
	 *    
	 *     monad(Optional.of(Arrays.asList(1,2,3))).streamedMonad().sorted() // Monad[Stream[1,2,3]]
	 *  }
	 * 
	 *  {@code assertThat(monad(Stream.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }
	 * 
	 */
	default AnyM<T> sortedAnyM(){
		return Monad.super.sorted().anyM();
	}
	/**
	 *	 
	 *  Monad converted to Stream via stream() and sorted - to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 *  e.g.
	 *  
	 *  {@code 
	 *    monad(Optional.of(Arrays.asList(1,2,3))).sorted( (a,b)->b-a)  // Monad[Stream[List[1,2,3]]]
	 *    
	 *     monad(Optional.of(Arrays.asList(1,2,3))).streamedMonad().sorted( (a,b)->b-a) // Monad[Stream[3,2,1]]
	 *  }
	 * 

	 * 
	 * @param c Compartor to sort with
	 * @return Sorted Monad
	 */
	default AnyM<T> sortedAnyM(Comparator<T > c){
		return Monad.super.sorted(c).anyM();   
	}
	/**
	 * {@code assertThat(monad(Stream.of(4,3,6,7)).skip(2).toList(),equalTo(Arrays.asList(6,7))); }
	 * 
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * @param num  Number of elemenets to skip
	 * @return Monad converted to Stream with specified number of elements skipped
	 */
	default AnyM<T> skipAnyM(int num){
		return Monad.super.skip(num).anyM();
	}
	/**
	 * 
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * {@code
	 * assertThat(monad(Stream.of(4,3,6,7)).sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
	 * }
	 * 
	 * @param p Predicate to skip while true
	 * @return Monad converted to Stream with elements skipped while predicate holds
	 */
	default AnyM<T> skipWhileAnyM(Predicate<T> p){
		return Monad.super.skipWhile(p).anyM();
	}
	/**
	 * 
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * {@code assertThat(monad(Stream.of(4,3,6,7)).skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));}
	 * 
	 * 
	 * @param p Predicate to skip until true
	 * @return Monad converted to Stream with elements skipped until predicate holds
	 */
	default AnyM<T> skipUntilAnyM(Predicate<T> p){
		return Monad.super.skipUntil(p).anyM();
	}
	/**
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * {@code assertThat(monad(Stream.of(4,3,6,7)).limit(2).toList(),equalTo(Arrays.asList(4,3)));}
	 * 
	 * @param num Limit element size to num
	 * @return Monad converted to Stream with elements up to num
	 */
	default AnyM<T> limitAnyM(int num){
		return Monad.super.limit(num).anyM();
	}
	/**
	 *  NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * {@code assertThat(monad(Stream.of(4,3,6,7)).sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));}
	 * 
	 * @param p Limit while predicate is true
	 * @return Monad converted to Stream with limited elements
	 */
	default AnyM<T> limitWhileAnyM(Predicate<T> p){
		return Monad.super.limitWhile(p).anyM();
	}
	/**
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * {@code assertThat(monad(Stream.of(4,3,6,7)).limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3))); }
	 * 
	 * @param p Limit until predicate is true
	 * @return Monad converted to Stream with limited elements
	 */
	default AnyM<T> limitUntilAnyM(Predicate<T> p){
		return Monad.super.limitUntil(p).anyM();
	}
	

	
	
	
	/**
	 * Aggregate the contents of this Monad and the supplied Monad 
	 * 
	 * {@code 
	 * 
	 * List<Integer> result = monad(Stream.of(1,2,3,4)).<Integer>aggregate(monad(Optional.of(5))).toList();
		
		assertThat(result,equalTo(Arrays.asList(1,2,3,4,5)));
		}
	 * 
	 * @param next Monad to aggregate content with
	 * @return Aggregated Monad
	 */
	default <R> AnyM<R> aggregate(AnyM<?> next){
		return Monad.super.aggregate(next).anyM();
	}
	
	/**
	 * flatMap operation
	 * 
	 * @param fn
	 * @return
	 */
	default <NT> AnyM<NT> flatMapAnyM(Function<T,?> fn) {
		return Monad.super.flatMap(fn).anyM();
	}
	
	
	/**
	 * @return this monad converted to a Parallel Stream, via streamedMonad() wraped in Monad interface
	 */
	default <NT> AnyM<NT> parallelAnyM(){
		return Monad.super.parallel().anyM();
	}
	
	/**
	 * Transform the contents of a Monad into a Monad wrapping a Stream e.g.
	 * Turn an {@code Optional<List<Integer>>  into Stream<Integer> }
	 * 
	 * {@code
	 * List<List<Integer>> list = monad(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Stream<Integer>,Integer>streamedMonad()
											.grouped(3)
											.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
	 * 
	 * }
	 * 
	 * 
	 * @return A Monad that wraps a Stream
	 */
	default <NT> AnyM<NT> streamedMonadAnyM(){
		return Monad.super.streamedMonad().anyM();
	}
}
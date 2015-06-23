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
import com.aol.cyclops.streams.Pair;
import com.aol.cyclops.streams.StreamUtils;
import com.nurkiewicz.lazyseq.LazySeq;

@AllArgsConstructor(access=AccessLevel.PACKAGE)
public class AnyM<T>{
	
	private final Monad<Object,T> monad;
	
	
	public final <R> R unwrap(){
		return (R)monad.unwrap();
	}
	public final <MONAD> Monad<MONAD,T> monad(){
		return (Monad)monad;
	}
	
	public final   AnyM<T>  filter(Predicate<T> fn){
		return monad.filter(fn).anyM();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	public final  <R> AnyM<R> map(Function<T,R> fn){
		return monad.map(fn).anyM();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#peek(java.util.function.Consumer)
	 */
	public final   AnyM<T>  peek(Consumer<T> c) {
		return monad.peek(c).anyM();
	}
	
	
	/**
	 * Perform a looser typed flatMap / bind operation
	 * The return type can be another type other than the host type
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	 */
	public final <R> AnyM<R> bind(Function<T,R> fn){
		return monad.bind(fn).anyM();
	
	}
	/**
	 * Perform a bind operation (@see #bind) but also lift the return value into a Monad using configured
	 * MonadicConverters
	 * 
	 * @param fn flatMap function
	 * @return flatMapped monad
	 */
	public final <R> AnyM<R> liftAndBind(Function<T,?> fn){
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
	 * Convert to a Stream with the values repeated specified times
	 * 
	 * @param times Times values should be repeated within a Stream
	 * @return Stream with values repeated
	 */
	public final AnyM<T> cycle(int times){
		return monad.cycle(times).anyM();
		
	}
	
	/**
	 * Convert to a Stream with the result of a reduction operation repeated specified times
	 * <pre>{@code 
	  		List<Integer> list = AsGenericMonad,asMonad(Stream.of(1,2,2))
											.cycle(Reducers.toCountInt(),3)
											.collect(Collectors.toList());
		//is asList(3,3,3);
	  }
	  </pre>
	 * 
	 * @param m Monoid to be used in reduction
	 * @param times Number of times value should be repeated
	 * @return Stream with reduced values repeated
	 */
	public final AnyM<T> cycle(Monoid<T> m,int times){
		return monad.cycle(m,times).anyM();
	}
	
	
	/**
	 * 
	 * Convert to a Stream, repeating the resulting structure specified times and
	 * lifting all values to the specified Monad type
	 * 
	 * <pre>{@code
	 * 
	 *  List<Optional<Integer>> list  = monad(Stream.of(1,2))
											.cycle(Optional.class,2)
											.toList();
											
	    //is asList(Optional.of(1),Optional.of(2),Optional.of(1),Optional.of(2)	));
	
	 * 
	 * }</pre>
	 * 
	 * 
	 * 
	 * @param monadC class type
	 * @param times
	 * @return
	 */
	public final <R> AnyM<R> cycle(Class<R> monadC,int times){
		return monad.cycle(monadC,times).anyM();
	}

	/**
	 * Repeat in a Stream while specified predicate holds
	 * 
	 * @param predicate repeat while true
	 * @return Repeating Stream
	 */
	public final  AnyM<T> cycleWhile(Predicate<T> predicate){
		return monad.cycleWhile(predicate).anyM();
	}
	/**
	 * Repeat in a Stream until specified predicate holds
	 * 
	 * @param predicate repeat while true
	 * @return Repeating Stream
	 */
	public final  AnyM<T> cycleUntil(Predicate<T> predicate){
		return monad.cycleUntil(predicate).anyM();
	}
	/**
	 * Generic zip function. E.g. Zipping a Stream and an Optional
	 * 
	 * <pre>{@code
	 * Stream<List<Integer>> zipped = asMonad(Stream.of(1,2,3)).zip(asMonad(Optional.of(2)), 
													(a,b) -> Arrays.asList(a,b));
	 * // [[1,2]]
	 * }</pre>
	 * 
	 * @param second Monad to zip with
	 * @param zipper Zipping function
	 * @return Stream zipping two Monads
	 */
	public final <S,R> AnyM<R> zip(AnyM<? extends S> second, BiFunction<? super T, ? super S, ? extends R> zipper){
		return monad.zip(second.monad, zipper).anyM();
	}
	
	/**
	 * Zip this Monad with a Stream
	 * 
	 * <pre>{@code 
	 * Stream<List<Integer>> zipped = asMonad(Stream.of(1,2,3)).zip(Stream.of(2,3,4), 
													(a,b) -> Arrays.asList(a,b));
													
		//[[1,2][2,3][3,4]]											
	 * }</pre>
	 * 
	 * @param second Stream to zip with
	 * @param zipper  Zip funciton
	 * @return This monad zipped with a Stream
	 */
	public final <S,R> AnyM<R> zip(Stream<? extends S> second, BiFunction<? super T, ? super S, ? extends R> zipper){
		return monad.zip(second, zipper).anyM();
	}
	/**
	 * Create a sliding view over this monad
	 * 
	 * @param windowSize Size of sliding window
	 * @return Stream with sliding view over monad
	 */
	public final AnyM<List<T>> sliding(int windowSize){
		return monad.sliding(windowSize).anyM();
	}
	
	/**
	 * Group elements in a Monad into a Stream
	 * 
	 * <pre>{@code
	 * 
	 * List<List<Integer>> list = monad(Stream.of(1,2,3,4,5,6))
	 * 									.grouped(3)
	 * 									.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
		
		}</pre>
	 * 
	 * @param groupSize Size of each Group
	 * @return Stream with elements grouped by size
	 */
	public final AnyM<List<T>> grouped(int groupSize){
		return monad.grouped(groupSize).anyM();
	}
	
	
	
	/*
	 * Return the distinct Stream of elements
	 * 
	 * <pre>{@code
	 * 	List<Integer> list = monad(Optional.of(Arrays.asList(1,2,2,2,5,6)))
											.<Stream<Integer>,Integer>streamedMonad()
											.distinct()
											.collect(Collectors.toList());
		}</pre>
	 */
	public final AnyM<T> distinct(){
		return monad.distinct().anyM();
	}
	/**
	 * Scan left using supplied Monoid
	 * 
	 * <pre>{@code  
	 * 
	 * 	assertEquals(asList("", "a", "ab", "abc"),monad(Stream.of("a", "b", "c")).scanLeft(Reducers.toString("")).toList());
            
            }</pre>
	 * 
	 * @param monoid
	 * @return
	 */
	public final AnyM<T> scanLeft(Monoid<T> monoid){
		return monad.scanLeft(monoid).anyM();
	}
	
	/**
	 * @return Monad converted to Stream via stream() and sorted - to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 *  e.g.
	 *  
	 *  <pre>{@code 
	 *    monad(Optional.of(Arrays.asList(1,2,3))).sorted()  // Monad[Stream[List[1,2,3]]]
	 *    
	 *     monad(Optional.of(Arrays.asList(1,2,3))).streamedMonad().sorted() // Monad[Stream[1,2,3]]
	 *  }</pre>
	 * 
	 *  <pre>{@code assertThat(monad(Stream.of(4,3,6,7)).sorted().toList(),equalTo(Arrays.asList(3,4,6,7))); }</pre>
	 * 
	 */
	public final AnyM<T> sorted(){
		return monad.sorted().anyM();
	}
	/**
	 *	 
	 *  Monad converted to Stream via stream() and sorted - to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 *  e.g.
	 *  
	 *  <pre>{@code 
	 *    monad(Optional.of(Arrays.asList(1,2,3))).sorted( (a,b)->b-a)  // Monad[Stream[List[1,2,3]]]
	 *    
	 *     monad(Optional.of(Arrays.asList(1,2,3))).streamedMonad().sorted( (a,b)->b-a) // Monad[Stream[3,2,1]]
	 *  }</pre>
	 * 

	 * 
	 * @param c Compartor to sort with
	 * @return Sorted Monad
	 */
	public final AnyM<T> sorted(Comparator<T > c){
		return monad.sorted(c).anyM();   
	}
	/**
	 * <pre>{@code assertThat(monad(Stream.of(4,3,6,7)).skip(2).toList(),equalTo(Arrays.asList(6,7))); }</pre>
	 * 
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * @param num  Number of elemenets to skip
	 * @return Monad converted to Stream with specified number of elements skipped
	 */
	public final AnyM<T> skip(int num){
		return monad.skip(num).anyM();
	}
	/**
	 * 
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * <pre>{@code
	 * assertThat(monad(Stream.of(4,3,6,7)).sorted().skipWhile(i->i<6).toList(),equalTo(Arrays.asList(6,7)));
	 * }</pre>
	 * 
	 * @param p Predicate to skip while true
	 * @return Monad converted to Stream with elements skipped while predicate holds
	 */
	public final AnyM<T> skipWhile(Predicate<T> p){
		return monad.skipWhile(p).anyM();
	}
	/**
	 * 
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * <pre>{@code assertThat(monad(Stream.of(4,3,6,7)).skipUntil(i->i==6).toList(),equalTo(Arrays.asList(6,7)));}</pre>
	 * 
	 * 
	 * @param p Predicate to skip until true
	 * @return Monad converted to Stream with elements skipped until predicate holds
	 */
	public final AnyM<T> skipUntil(Predicate<T> p){
		return monad.skipUntil(p).anyM();
	}
	/**
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * <pre>{@code assertThat(monad(Stream.of(4,3,6,7)).limit(2).toList(),equalTo(Arrays.asList(4,3)));}</pre>
	 * 
	 * @param num Limit element size to num
	 * @return Monad converted to Stream with elements up to num
	 */
	public final AnyM<T> limit(int num){
		return monad.limit(num).anyM();
	}
	/**
	 *  NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * <pre>{@code assertThat(monad(Stream.of(4,3,6,7)).sorted().limitWhile(i->i<6).toList(),equalTo(Arrays.asList(3,4)));}</pre>
	 * 
	 * @param p Limit while predicate is true
	 * @return Monad converted to Stream with limited elements
	 */
	public final AnyM<T> limitWhile(Predicate<T> p){
		return monad.limitWhile(p).anyM();
	}
	/**
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * <pre>{@code assertThat(monad(Stream.of(4,3,6,7)).limitUntil(i->i==6).toList(),equalTo(Arrays.asList(4,3))); }</pre>
	 * 
	 * @param p Limit until predicate is true
	 * @return Monad converted to Stream with limited elements
	 */
	public final AnyM<T> limitUntil(Predicate<T> p){
		return monad.limitUntil(p).anyM();
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
	public final <NT> AnyM<NT> flatMap(Function<T,AnyM<NT>> fn) {
		return monad.flatMap(fn).anyM();
	}
	
	
	/**
	 * @return this monad converted to a Parallel Stream, via streamedMonad() wraped in Monad interface
	 */
	public final <NT> AnyM<NT> parallel(){
		return monad.parallel().anyM();
	}
	
	/**
	 * Transform the contents of a Monad into a Monad wrapping a Stream e.g.
	 * Turn an <pre>{@code Optional<List<Integer>>  into Stream<Integer> }</pre>
	 * 
	 * <pre>{@code
	 * List<List<Integer>> list = monad(Optional.of(Arrays.asList(1,2,3,4,5,6)))
											.<Stream<Integer>,Integer>streamedMonad()
											.grouped(3)
											.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
	 * 
	 * }</pre>
	 * 
	 * 
	 * @return A Monad that wraps a Stream
	 */
	public final <NT> AnyM<NT> streamedMonad(){
		return monad.streamedMonad().anyM();
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
		return AsAnyM.asAnyM(o);
	}
	
	
	/**
	 * True if predicate matches all elements when Monad converted to a Stream
	 * 
	 * @param c Predicate to check if all match
	 */
	public final  void  allMatch(Predicate<T> c) {
		stream().allMatch(c);
	}
	/**
	 * True if a single element matches when Monad converted to a Stream
	 * 
	 * @param c Predicate to check if any match
	 */
	public final  void  anyMatch(Predicate<T> c) {
		stream().anyMatch(c);
	}
	/**
	 * @return First matching element in sequential order
	 * 
	 * (deterministic)
	 * 
	 */
	public final  Optional<T>  findFirst() {
		return stream().findFirst();
	}
	/**
	 * @return first matching element,  but order is not guaranteed
	 * 
	 * (non-deterministic) 
	 */
	public final  Optional<T>  findAny() {
		return stream().findAny();
	}
	
	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final  <R> R mapReduce(Monoid<R> reducer){
		return reducer.mapReduce(stream());
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid, using supplied function
	 *  Then use Monoid to reduce values
	 *  
	 * @param mapper Function to map Monad type
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final  <R> R mapReduce(Function<T,R> mapper, Monoid<R> reducer){
		return reducer.reduce(stream().map(mapper));
	}
	
	/**
	 * Mutable reduction / collection over this Monad converted to a Stream
	 * 
	 * @param collector Collection operation definition
	 * @return Collected result
	 */
	public final <R, A> R collect(Collector<T,A,R> collector){
		return stream().collect(collector);
	}
	/**
	 * Apply multiple collectors Simulataneously to this Monad
	 * 
	 * <pre>{@code
	  	List result = monad(Stream.of(1,2,3)).collect(Stream.of(Collectors.toList(),
	  															Collectors.summingInt(Integer::intValue),
	  															Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
		}</pre>
		
		 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().collect(collectors);
	 * 
	 * @param collectors Stream of Collectors to apply
	 * @return  List of results
	 */
	public final  List collect(Stream<Collector> collectors){
		return StreamUtils.collect(stream(),collectors);
	}
	
	/**
	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values
	 * @return reduced values
	 */
	public final  T reduce(Monoid<T> reducer){
		return reducer.reduce(stream());
	}
	
	/**
     * Reduce with multiple reducers in parallel
	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * @param reducers
	 * @return
	 */
	public final List<T> reduce(Stream<Monoid<T>> reducers){
		return StreamUtils.reduce(stream(), reducers);
	}
	/**
     * Reduce with multiple reducers in parallel
	 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * @param reducers
	 * @return
	 */
	public final List<T> reduce(Iterable<Monoid<T>> reducers){
		return StreamUtils.reduce(stream(), reducers);
	}
	
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldLeft
	 * @return Reduced result
	 */
	public final T foldLeft(Monoid<T> reducer){
		return reduce(reducer);
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final <T> T foldLeftMapToType(Monoid<T> reducer){
		return reducer.mapReduce(stream());
	}
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	public final T foldRight(Monoid<T> reducer){
		return reducer.reduce(StreamUtils.reverse(stream()));
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final <T> T foldRightMapToType(Monoid<T> reducer){
		return reducer.mapReduce(StreamUtils.reverse(stream()));
	}
	/**
	 * @return Underlying monad converted to a Streamable instance
	 */
	public final Streamable<T> toStreamable(){
		return  AsStreamable.asStreamable(stream());
	}
	/**
	 * @return This monad converted to a set
	 */
	public final Set<T> toSet(){
		return (Set)stream().collect(Collectors.toSet());
	}
	/**
	 * @return this monad converted to a list
	 */
	public final List<T> toList(){
		return (List)stream().collect(Collectors.toList());
	}
	/**
	 * @return  calls to stream() but more flexible on type for inferencing purposes.
	 */
	public final <T> Stream<T> toStream(){
		return (Stream)stream();
	}
	/**
	 * Unwrap this Monad into a Stream.
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	public final Stream<T> stream(){
		return monad.stream();
		
	}

	/**
	 * 
	 * <pre>{@code 
	 * assertTrue(monad(Stream.of(1,2,3,4)).startsWith(Arrays.asList(1,2,3)));
	 * }</pre>
	 * 
	 * @param iterable
	 * @return True if Monad starts with Iterable sequence of data
	 */
	public final boolean startsWith(Iterable<T> iterable){
		return LazySeq.of(stream().iterator()).startsWith(iterable);
		
	}
	/**
	 * 	<pre>{@code assertTrue(monad(Stream.of(1,2,3,4)).startsWith(Arrays.asList(1,2,3).iterator())) }</pre>

	 * @param iterator
	 * @return True if Monad starts with Iterators sequence of data
	 */
	public final boolean startsWith(Iterator<T> iterator){
		return LazySeq.of(stream().iterator()).startsWith(iterator);
		
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
	public final <R> AnyM<R> applyM(AnyM<Function<T,R>> fn){
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
	public final <NT,R> Monad<NT,R> simpleFilter(AnyM<Predicate<T>> fn){
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
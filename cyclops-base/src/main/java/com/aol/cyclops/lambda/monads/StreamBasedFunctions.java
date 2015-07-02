package com.aol.cyclops.lambda.monads;
import static com.aol.cyclops.internal.AsGenericMonad.asMonad;
import static com.aol.cyclops.internal.AsGenericMonad.monad;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.internal.Monad;
import com.aol.cyclops.lambda.api.AsStreamable;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.streams.StreamUtils;
import com.nurkiewicz.lazyseq.LazySeq;

public interface StreamBasedFunctions<MONAD,T> extends Streamable<T>  {
	 MONAD unwrap();
	 public <MONAD,T> Monad<MONAD,T> withMonad(Object invoke);
	/**
	 * True if predicate matches all elements when Monad converted to a Stream
	 * 
	 * @param c Predicate to check if all match
	 */
	default  void  allMatch(Predicate<T> c) {
		stream().allMatch(c);
	}
	/**
	 * True if a single element matches when Monad converted to a Stream
	 * 
	 * @param c Predicate to check if any match
	 */
	default  void  anyMatch(Predicate<T> c) {
		stream().anyMatch(c);
	}
	/**
	 * @return First matching element in sequential order
	 * 
	 * (deterministic)
	 * 
	 */
	default  Optional<T>  findFirst() {
		return stream().findFirst();
	}
	/**
	 * @return first matching element,  but order is not guaranteed
	 * 
	 * (non-deterministic) 
	 */
	default  Optional<T>  findAny() {
		return stream().findAny();
	}
	
	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	default  <R> R mapReduce(Monoid<R> reducer){
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
	default  <R> R mapReduce(Function<T,R> mapper, Monoid<R> reducer){
		return reducer.reduce(stream().map(mapper));
	}
	
	/**
	 * Mutable reduction / collection over this Monad converted to a Stream
	 * 
	 * @param collector Collection operation definition
	 * @return Collected result
	 */
	default <R, A> R collect(Collector<T,A,R> collector){
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
		}
		</pre>
		
		 * NB if this Monad is an Optional [Arrays.asList(1,2,3)]  reduce will operate on the Optional as if the list was one value
	 * To reduce over the values on the list, called streamedMonad() first. I.e. streamedMonad().collect(collectors);
	 * 
	 * @param collectors Stream of Collectors to apply
	 * @return  List of results
	 */
	default  List collect(Stream<Collector> collectors){
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
	default  T reduce(Monoid<T> reducer){
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
	default List<T> reduce(Stream<Monoid<T>> reducers){
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
	default List<T> reduce(Iterable<Monoid<T>> reducers){
		return StreamUtils.reduce(stream(), reducers);
	}
	
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldLeft
	 * @return Reduced result
	 */
	default T foldLeft(Monoid<T> reducer){
		return reduce(reducer);
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	default <T> T foldLeftMapToType(Monoid<T> reducer){
		return reducer.mapReduce(stream());
	}
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	default T foldRight(Monoid<T> reducer){
		return reducer.reduce(StreamUtils.reverse(stream()));
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	default <T> T foldRightMapToType(Monoid<T> reducer){
		return reducer.mapReduce(StreamUtils.reverse(stream()));
	}
	/**
	 * @return Underlying monad converted to a Streamable instance
	 */
	default Streamable<T> toStreamable(){
		return  AsStreamable.asStreamable(stream());
	}
	/**
	 * @return This monad converted to a set
	 */
	default Set<T> toSet(){
		return (Set)stream().collect(Collectors.toSet());
	}
	/**
	 * @return this monad converted to a list
	 */
	default List<T> toList(){
		return (List)stream().collect(Collectors.toList());
	}
	/**
	 * @return  calls to stream() but more flexible on type for inferencing purposes.
	 */
	default <T> Stream<T> toStream(){
		return (Stream)stream();
	}
	/**
	 * Unwrap this Monad into a Stream.
	 * If the underlying monad is a Stream it is returned
	 * Otherwise we flatMap the underlying monad to a Stream type
	 */
	default Stream<T> stream(){
		if(unwrap() instanceof Stream)
			return (Stream)unwrap();
		if(unwrap() instanceof Iterable)
			return StreamSupport.stream(((Iterable)unwrap()).spliterator(), false);
		Stream stream = Stream.of(1);
		return (Stream)withMonad((Stream)new ComprehenderSelector().selectComprehender(
				stream).executeflatMap(stream, i-> unwrap())).flatMap(Function.identity()).unwrap();
		
	}
	/**
	 * Convert to a Stream with the values repeated specified times
	 * 
	 * @param times Times values should be repeated within a Stream
	 * @return Stream with values repeated
	 */
	default Monad<Stream<T>,T> cycle(int times){
		
		return monad(StreamUtils.cycle(times,AsStreamable.asStreamable(stream())));
		
	}
	
	/**
	 * Convert to a Stream with the result of a reduction operation repeated specified times
	 * 
	 * <pre>{@code 
	  		List<Integer> list = AsGenericMonad,asMonad(Stream.of(1,2,2))
											.cycle(Reducers.toCountInt(),3)
											.collect(Collectors.toList());
		//is asList(3,3,3);
	  }</pre>
	 * 
	 * @param m Monoid to be used in reduction
	 * @param times Number of times value should be repeated
	 * @return Stream with reduced values repeated
	 */
	default Monad<Stream<T>,T> cycle(Monoid<T> m,int times){
		return monad(StreamUtils.cycle(times,AsStreamable.asStreamable(m.reduce(stream()))));
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
	 * @param monad
	 * @param times
	 * @return
	 */
	default <R> Monad<Stream<R>,R> cycle(Class<R> monad,int times){
		return (Monad)cycle(times).map(r -> new ComprehenderSelector().selectComprehender(monad).of(r));	
	}

	/**
	 * Repeat in a Stream while specified predicate holds
	 * 
	 * @param predicate repeat while true
	 * @return Repeating Stream
	 */
	default  Monad<Stream<T>,T> cycleWhile(Predicate<? super T> predicate){
		return monad(LazySeq.of(StreamUtils.cycle(stream()).iterator()).takeWhile(predicate).stream());
	}
	/**
	 * Repeat in a Stream until specified predicate holds
	 * 
	 * @param predicate repeat while true
	 * @return Repeating Stream
	 */
	default  Monad<Stream<T>,T> cycleUntil(Predicate<? super T> predicate){
		return monad(LazySeq.of(StreamUtils.cycle(stream()).iterator()).takeWhile(predicate.negate()).stream());
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
	default <MONAD2,S,R> Monad<Stream<R>,R> zip(Monad<MONAD2,? extends S> second, BiFunction<? super T, ? super S, ? extends R> zipper){
		return monad((Stream)LazySeq.of(stream().iterator()).zip(LazySeq.of(second.stream().iterator()), zipper).stream());
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
	default <S,R> Monad<Stream<R>,R> zip(Stream<? extends S> second, BiFunction<? super T, ? super S, ? extends R> zipper){
		return monad((Stream)LazySeq.of(stream().iterator()).zip(LazySeq.of(second.iterator()), zipper).stream());
	}
	/**
	 * Create a sliding view over this monad's contents
	 * 
	 * @param windowSize Size of sliding window
	 * @return Stream with sliding view over monad
	 */
	default Monad<Stream<List<T>>,List<T>> sliding(int windowSize){
		return monad((Stream)LazySeq.of(stream().iterator()).sliding(windowSize).stream());
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
	default Monad<Stream<List<T>>,List<T>> grouped(int groupSize){
		return monad(LazySeq.of(stream().iterator()).grouped(groupSize).stream());
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
	default boolean startsWith(Iterable<T> iterable){
		return LazySeq.of(stream().iterator()).startsWith(iterable);
		
	}
	/**
	 * 	<pre>{@code assertTrue(monad(Stream.of(1,2,3,4)).startsWith(Arrays.asList(1,2,3).iterator())) }</pre>
 		
	 * @param iterator
	 * @return True if Monad starts with Iterators sequence of data
	 */
	default boolean startsWith(Iterator<T> iterator){
		return LazySeq.of(stream().iterator()).startsWith(iterator);
		
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
	default Monad<Stream<T>,T> distinct(){
		return monad(LazySeq.of(stream().iterator()).distinct().stream());
	}
	/**
	 * Scan left using supplied Monoid
	 * 
	 * <pre>{@code  
	 * 
	 * 	assertEquals(asList("", "a", "ab", "abc"),monad(Stream.of("a", "b", "c")).scanLeft(Reducers.toString("")).toList());
            
            }
	 * </pre>
	 * @param monoid
	 * @return
	 */
	default Monad<Stream<T>,T> scanLeft(Monoid<T> monoid){
		return monad(LazySeq.of(stream().iterator()).scan(monoid.zero(), monoid.reducer()).stream());
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
	default Monad<Stream<T>,T> sorted(){
		return monad(stream().sorted());
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
	default Monad<Stream<T>,T> sorted(Comparator<? super T > c){
		return monad(stream().sorted(c));   
	}
	/**
	 * <pre>{@code assertThat(monad(Stream.of(4,3,6,7)).skip(2).toList(),equalTo(Arrays.asList(6,7))); }</pre>
	 * 
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * @param num  Number of elemenets to skip
	 * @return Monad converted to Stream with specified number of elements skipped
	 */
	default Monad<Stream<T>,T> skip(int num){
		return monad(stream()
					.skip(num));
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
	default Monad<Stream<T>,T> skipWhile(Predicate<? super T> p){
		return monad(LazySeq.of(stream().iterator())
				.dropWhile(p)
				.stream());
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
	default Monad<Stream<T>,T> skipUntil(Predicate<? super T> p){
		return monad(LazySeq.of(stream().iterator())
				.dropWhile(p.negate())
				.stream());
	}
	/**
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * <pre>{@code assertThat(monad(Stream.of(4,3,6,7)).limit(2).toList(),equalTo(Arrays.asList(4,3)));}</pre>
	 * 
	 * @param num Limit element size to num
	 * @return Monad converted to Stream with elements up to num
	 */
	default Monad<Stream<T>,T> limit(int num){
		return monad(stream()
				.limit(num));
	}
	/**
	 *  NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * <pre>{@code assertThat(monad(Stream.of(4,3,6,7))
	 * 										.sorted().limitWhile(i->i<6).toList(),
	 * 								equalTo(Arrays.asList(3,4)));
	 * }</pre>
	 * 
	 * @param p Limit while predicate is true
	 * @return Monad converted to Stream with limited elements
	 */
	default Monad<Stream<T>,T> limitWhile(Predicate<? super T> p){
		return monad(LazySeq.of(stream().iterator())
					.takeWhile(p)
					.stream());
	}
	/**
	 * NB to access nested collections in non-Stream monads as a stream use streamedMonad() first
	 * 
	 * <pre>{@code assertThat(monad(Stream.of(4,3,6,7))
	 * 									.limitUntil(i->i==6).toList(),
	 * 									equalTo(Arrays.asList(4,3))); 
	 * }
	 * </pre>
	 * 
	 * @param p Limit until predicate is true
	 * @return Monad converted to Stream with limited elements
	 */
	default Monad<Stream<T>,T> limitUntil(Predicate<? super T> p){
		return monad(LazySeq.of(stream().iterator())
					.takeWhile(p.negate()).stream());
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
	default <R,NT> Monad<R,NT> streamedMonad(){
		Stream stream = Stream.of(1);
		 Monad r = this.<Stream,T>withMonad((Stream)new ComprehenderSelector().selectComprehender(
				stream).executeflatMap(stream, i-> unwrap()));
		 return r.flatMap(e->e);
	}
	default <R,NT> Monad<R,NT> lazySeqMonad(){
		LazySeq stream = LazySeq.of(1);
		 Monad r = this.<LazySeq,T>withMonad((LazySeq)new ComprehenderSelector().selectComprehender(
				stream).executeflatMap(stream, i-> unwrap()));
		 return r.flatMap(e->e);
	}
	
	default <R> LazySeq<R> lazySeq(){
		
		LazySeq stream = LazySeq.of(1);
		return (LazySeq)withMonad((LazySeq)new ComprehenderSelector().selectComprehender(
				stream).executeflatMap(stream, i-> unwrap())).flatMap(Function.identity()).unwrap();
	}
	/**
	 * @return this monad converted to a Parallel Stream, via streamedMonad() wraped in Monad interface
	 */
	default <R,NT> Monad<R,NT> parallel(){
		return (Monad)monad(streamedMonad().stream().parallel());
	}

}

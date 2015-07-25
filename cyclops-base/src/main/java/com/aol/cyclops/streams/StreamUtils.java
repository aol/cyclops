package com.aol.cyclops.streams;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.lambda.api.AsAnyM;
import com.aol.cyclops.lambda.api.AsStreamable;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.lambda.monads.AnyM;
import com.aol.cyclops.lambda.monads.SequenceM;
import com.nurkiewicz.lazyseq.LazySeq;

public class StreamUtils{
	
	/**
	 * Convert to a Stream with the result of a reduction operation repeated
	 * specified times
	 * 
	 * <pre>
	 * {@code 
	 *   		List<Integer> list = StreamUtils.cycle(Stream.of(1,2,2),Reducers.toCountInt(),3)
	 * 										.
	 * 										.collect(Collectors.toList());
	 * 	//is asList(3,3,3);
	 *   }
	 * </pre>
	 * 
	 * @param m
	 *            Monoid to be used in reduction
	 * @param times
	 *            Number of times value should be repeated
	 * @return Stream with reduced values repeated
	 */
	public final static <T> Stream<T> cycle(Stream<T> stream,Monoid<T> m, int times) {
		return StreamUtils.cycle(times,AsStreamable.asStreamable(m.reduce(stream)));
		
	}
	
	/**
	 * 
	 * @return
	 */
	public final  static <T> HeadAndTail<T> headAndTail(Stream<T> stream){
		Iterator<T> it = stream.iterator();
		return new HeadAndTail(it.next(),AsAnyM.anyM(stream(it)).asSequence());
	}
	public final  static <T> Optional<HeadAndTail<T>> headAndTailOptional(Stream<T> stream){
		Iterator<T> it = stream.iterator();
		if(it.hasNext())
			return Optional.empty();
		return Optional.of(new HeadAndTail(it.next(),AsAnyM.anyM(stream(it)).asSequence()));
	}
	
	/**
	 * skip elements in Stream until Predicate holds true
	 * 	<pre>
	 * {@code  StreamUtils.skipUntil(Stream.of(4,3,6,7),i->i==6).collect(Collectors.toList())
	 *  // [6,7]
	 *  }</pre>

	 * @param stream Stream to skip elements from 
	 * @param predicate to apply
	 * @return Stream with elements skipped
	 */
	public static <U> Stream<U> skipUntil(Stream<U> stream,Predicate<? super U> predicate){
		return LazySeq.of(stream.iterator()).dropWhile(predicate.negate()).stream();
	}
	/**
	 * skip elements in a Stream while Predicate holds true
	 * 
	 * <pre>
	 * 
	 * {@code  StreamUtils.skipWhile(Stream.of(4,3,6,7).sorted(),i->i<6).collect(Collectors.toList())
	 *  // [6,7]
	 *  }</pre>
	 * @param stream
	 * @param predicate
	 * @return
	 */
	public static <U> Stream<U> skipWhile(Stream<U> stream,Predicate<? super U> predicate){
		return LazySeq.of(stream.iterator()).dropWhile(predicate).stream();
	}
	/**
	 * Take elements from a stream while the predicates hold
	 * <pre>
	 * {@code StreamUtils.limitWhile(Stream.of(4,3,6,7).sorted(),i->i<6).collect(Collectors.toList());
	 * //[4,3]
	 * }
	 * </pre>
	 * @param stream
	 * @param predicate
	 * @return
	 */
	public static <U> Stream<U> limitWhile(Stream<U> stream,Predicate<? super U> predicate){
		return LazySeq.of(stream.iterator()).takeWhile(predicate).stream();
	}
	/**
	 * Take elements from a Stream until the predicate holds
	 *  <pre>
	 * {@code StreamUtils.limitUntil(Stream.of(4,3,6,7),i->i==6).collect(Collectors.toList());
	 * //[4,3]
	 * }
	 * </pre>
	 * @param stream
	 * @param predicate
	 * @return
	 */
	public static <U> Stream<U> limitUntil(Stream<U> stream,Predicate<? super U> predicate){
		return LazySeq.of(stream.iterator()).takeWhile(predicate.negate()).stream();
	}
	/**
	 * Reverse a Stream
	 * 
	 * @param stream Stream to reverse
	 * @return Reversed stream
	 */
	public static <U> Stream<U> reverse(Stream<U> stream){
		return reversedStream(stream.collect(Collectors.toList()));
	}
	/**
	 * Create a reversed Stream from a List
	 * 
	 * @param list List to create a reversed Stream from
	 * @return Reversed Stream
	 */
	public static <U> Stream<U> reversedStream(List<U> list){
		return new ReversedIterator<>(list).stream();
	}
	/**
	 * Create a new Stream that infiniteable cycles the provided Stream
	 * @param s Stream to cycle
	 * @return New cycling stream
	 */
	public static <U> Stream<U> cycle(Stream<U> s){
		return cycle(AsStreamable.asStreamable(s));
	}
	/**
	 * Create a Stream that infiniteable cycles the provided Streamable
	 * @param s Streamable to cycle
	 * @return New cycling stream
	 */
	public static <U> Stream<U> cycle(Streamable<U> s){
		return Stream.iterate(s.stream(),s1-> s.stream()).flatMap(Function.identity());
	}
	
	/**
	 * Create a Stream that infiniteable cycles the provided Streamable
	 * @param s Streamable to cycle
	 * @return New cycling stream
	 */
	public static <U> Stream<U> cycle(int times,Streamable<U> s){
		return Stream.iterate(s.stream(),s1-> s.stream()).limit(times).flatMap(Function.identity());
	}
	
	/**
	 * Create a stream from an iterable
	 * 
	 * @param it Iterable to convert to a Stream
	 * @return Stream from iterable
	 */
	public static <U> Stream<U> stream(Iterable<U> it){
		return StreamSupport.stream(it.spliterator(),
					false);
	}
	/**
	 * Create a stream from an iterator
	 * 
	 * @param it Iterator to convert to a Stream
	 * @return Stream from iterator
	 */
	public static <U> Stream<U> stream(Iterator<U> it){
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED),
					false);
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
		Stream<U> first = null;
		if(o instanceof Stream){
			first = (Stream)o;
		}else if(o instanceof Iterable){
			first = stream( (Iterable)o);
		}
		else if(o instanceof Streamable){
			first = ((Streamable)o).stream();
		}
		else{
			first = Stream.of((U)o);
		}
		return Stream.concat(first, stream);
		
	}
	/**
	 * Create a stream from a map
	 * 
	 * @param it Iterator to convert to a Stream
	 * @return Stream from a map
	 */
	public static <K,V> Stream<Map.Entry<K, V>> stream(Map<K,V> it){
		return it.entrySet().stream();
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
	
		Monoid<R> m = new Monoid(){
			public List zero(){
				return stream(reducers).map(r->r.zero()).collect(Collectors.toList());
			}
			public BiFunction<List,List,List> combiner(){
				return (c1,c2) -> { 
					List l= new ArrayList<>();
					int i =0;
					for(Monoid next : reducers){
						l.add(next.combiner().apply(c1.get(i),c2.get(0)));
						i++;
					}
					
					
					return l;
				};
			}
			
		
			public Stream mapToType(Stream stream){
				return (Stream) stream.map(value->Arrays.asList(value));
			}
		};
		return (List)m.mapReduce(stream);
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
		return (List)reduce(stream, (List)reducers.collect(Collectors.toList()));
		
	}
	
	/**
	 *  Apply multiple Collectors, simultaneously to a Stream
	 * 
	 * @param stream Stream to collect
	 * @param collectors Collectors to apply
	 * @return Result as a list
	 */
	public static <T,A,R> List<R> collect(Stream<T> stream, Stream<Collector> collectors){
		return collect(stream, AsStreamable.<Collector>asStreamable(collectors));
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
		return collect(stream, AsStreamable.<Collector>asStreamable(collectors));
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
		
		
		final Supplier supplier =  ()-> collectors.stream().map(c->c.supplier().get()).collect(Collectors.toList());
		final BiConsumer accumulator = (acc,next) -> {  LazySeq.of(collectors.stream().iterator()).<Object,Pair<Collector,Object>>zip(LazySeq.of((List)acc),(a,b)->new Pair<Collector,Object>(a,b))
													
													.forEach( t -> t._1().accumulator().accept(t._2(),next));
		};
		final BinaryOperator combiner = (t1,t2)->  {
			Iterator t1It = ((Iterable)t1).iterator();
			Iterator t2It =  ((Iterable)t2).iterator();
			return collectors.stream().map(c->c.combiner().apply(t1It.next(),t2It.next())).collect(Collectors.toList());
		};
		Function finisher = t1 -> {
			Iterator t1It = ((Iterable)t1).iterator();
			return collectors.stream().map(c->c.finisher().apply(t1It.next())).collect(Collectors.toList());
		};
		 Collector col = Collector.of( supplier,accumulator , combiner,finisher);
			
		 return (List)stream.collect(col);
	}
	
	/**
	 * Repeat in a Stream while specified predicate holds
	 * 
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	public final static <T> Stream<T> cycleWhile(Stream<T>  stream,Predicate<? super T> predicate) {
		return LazySeq.of(StreamUtils.cycle(stream).iterator()).takeWhile(predicate).stream();
	}

	/**
	 * Repeat in a Stream until specified predicate holds
	 * 
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	public final static <T> Stream<T> cycleUntil(Stream<T> stream,Predicate<? super T> predicate) {
		return LazySeq.of(StreamUtils.cycle(stream).iterator()).takeWhile(predicate.negate()).stream();
	}

	/**
	 * Generic zip function. E.g. Zipping a Stream and an Optional
	 * 
	 * 
	 * @param second
	 *            Monad to zip with
	 * @param zipper
	 *            Zipping function
	 * @return Stream zipping two Monads
	 */
	public final static <T,S, R> Stream<R> zip(Stream<T> stream,SequenceM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return (Stream)LazySeq.of(stream.iterator()).zip(LazySeq.of(second.stream().iterator()), zipper).stream();
		
	}
	public final static <T,S, R> Stream<R> zip(Stream<T> stream,AnyM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return zip(stream,second.toSequence(), zipper);
	}

	/**
	 * Zip this Monad with a Stream
	 * 
	 
	 * 
	 * @param second
	 *            Stream to zip with
	 * @param zipper
	 *            Zip funciton
	 * @return This monad zipped with a Stream
	 */
	public final static <T,S,R> Stream<R> zip(Stream<T> stream,Stream<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return (Stream)steamToLazySeq(stream).zip(LazySeq.of(second.iterator()), zipper).stream();
	}

	/**
	 * Create a sliding view over this monad
	 * 
	 * @param windowSize
	 *            Size of sliding window
	 * @return Stream with sliding view over monad
	 */
	public final static <T> Stream<List<T>> sliding(Stream<T> stream,int windowSize) {
		return steamToLazySeq(stream).sliding(windowSize).stream();
	}

	/**
	 * Group elements in a Monad into a Stream
	 * 
	
	 * 
	 * @param groupSize
	 *            Size of each Group
	 * @return Stream with elements grouped by size
	 */
	public final static<T> Stream<List<T>> grouped(Stream<T> stream,int groupSize) {
		return LazySeq.of(stream.iterator()).grouped(groupSize).stream();
	}



	/**
	 * Scan left using supplied Monoid
	 * 
	 * <pre>
	 * {@code  
	 * 
	 * 	assertEquals(asList("", "a", "ab", "abc"),StreamUtils.scanLeft(Stream.of("a", "b", "c"),Reducers.toString("")).collect(Collectors.toList());
	 *         
	 *         }
	 * </pre>
	 * 
	 * @param monoid
	 * @return
	 */
	public final static <T> Stream<T> scanLeft(Stream<T> stream,Monoid<T> monoid) {
		return steamToLazySeq(stream).scan(monoid.zero(), monoid.reducer()).stream();
	}
	public  static <T> LazySeq<T> steamToLazySeq(Stream<T> stream) {
		return LazySeq.of(stream.iterator());
	}

	
	
	
	public  static <T> boolean xMatch(Stream<T> stream,int num, Predicate<? super T> c) {
		return stream.map(t -> c.test(t)) .collect(Collectors.counting()) == num;
	}
	public final static <T>  boolean  noneMatch(Stream<T> stream,Predicate<? super T> c) {
		return stream.allMatch(c.negate());
	}
	public final static  <T> String mkString(Stream<T> stream,String sep){
		return steamToLazySeq(stream).mkString(sep);
	}
	public final  static<T> String mkString(Stream<T> stream,String start, String sep,String end){
		return steamToLazySeq(stream).mkString(start, sep, end);
	}
	public final  static<T> String mkString(Stream<T> stream,String start, String sep,String end,boolean lazy){
		return steamToLazySeq(stream).mkString(start, sep, end, lazy);
		
	}
	
	public final static   <T,C extends Comparable<? super C>> Optional<T> minBy(Stream<T> stream,Function<T,C> f){
		return steamToLazySeq(stream).minBy(f);
	}
	public final  static<T> Optional<T> min(Stream<T> stream,Comparator<? super T> comparator){
		return steamToLazySeq(stream).min(comparator);
	}
	public final static <T,C extends Comparable<? super C>> Optional<T> maxBy(Stream<T> stream,Function<T,C> f){
		
		return steamToLazySeq(stream).maxBy(f);
	}
	public final static<T> Optional<T> max(Stream<T> stream,Comparator<? super T> comparator){
		return steamToLazySeq(stream).max(comparator);
	}
	
	
	
	
	
	
	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final static<T,R> R mapReduce(Stream<T> stream,Monoid<R> reducer){
		return reducer.mapReduce(stream);
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid, using supplied function
	 *  Then use Monoid to reduce values
	 *  
	 * @param mapper Function to map Monad type
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final static <T,R> R mapReduce(Stream<T> stream,Function<? super T,? extends R> mapper, Monoid<R> reducer){
		return reducer.reduce(stream.map(mapper));
	}
	
	
	
	
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldLeft
	 * @return Reduced result
	 */
	public final static <T> T foldLeft(Stream<T> stream,Monoid<T> reducer){
		return reducer.reduce(stream);
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final static <T> T foldLeftMapToType(Stream<T> stream,Monoid<T> reducer){
		return reducer.mapReduce(stream);
	}
	/**
	 * 
	 * 
	 * @param reducer Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	public final static <T> T foldRight(Stream<T> stream,Monoid<T> reducer){
		return reducer.reduce(StreamUtils.reverse(stream));
	}
	/**
	 *  Attempt to map this Monad to the same type as the supplied Monoid (using mapToType on the monoid interface)
	 * Then use Monoid to reduce values
	 * 
	 * @param reducer Monoid to reduce values
	 * @return Reduce result
	 */
	public final static <T> T foldRightMapToType(Stream<T> stream,Monoid<T> reducer){
		return reducer.mapReduce(StreamUtils.reverse(stream));
	}
	/**
	 * @return Underlying monad converted to a Streamable instance
	 */
	public final static <T> Streamable<T> toStreamable(Stream<T> stream){
		return  AsStreamable.asStreamable(stream);
	}
	/**
	 * @return This monad converted to a set
	 */
	public final static <T> Set<T> toSet(Stream<T> stream){
		return (Set)stream.collect(Collectors.toSet());
	}
	/**
	 * @return this monad converted to a list
	 */
	public final static <T> List<T> toList(Stream<T> stream){
	
		return (List)stream.collect(Collectors.toList());
	}
	
	
	
	/**
	 * 
	 * <pre>{@code 
	 * assertTrue(StreamUtils.startsWith(Stream.of(1,2,3,4),Arrays.asList(1,2,3)));
	 * }</pre>
	 * 
	 * @param iterable
	 * @return True if Monad starts with Iterable sequence of data
	 */
	public final static <T> boolean startsWith(Stream<T> stream,Iterable<T> iterable){
		return steamToLazySeq(stream).startsWith(iterable);
		
	}
	/**
	 * 	<pre>{@code assertTrueStreamUtils.startsWith(Stream.of(1,2,3,4),Arrays.asList(1,2,3).iterator())) }</pre>

	 * @param iterator
	 * @return True if Monad starts with Iterators sequence of data
	 */
	public final static <T> boolean startsWith(Stream<T> stream,Iterator<T> iterator){
		return steamToLazySeq(stream).startsWith(iterator);
		
	}
	
	public static<T> AnyM<T> anyM(Stream<T> monad){
		return AsAnyM.anyM(monad);
	}
	public static<T> SequenceM<T> sequenceM(Stream<T> monad){
		return AsAnyM.anyM(monad).asSequence();
	}
	
	/**
	 * flatMap operation
	 * 
	 * @param fn
	 * @return
	 */
	public final static <T,R> Stream<R> flatMapSequenceM(Stream<T> stream,Function<? super T,SequenceM<? extends R>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMap(fn).stream();
	}
	public final static <T,R> Stream<R> flatMapAnyM(Stream<T> stream,Function<? super T,AnyM<? extends R>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMapAnyM(fn).stream();
	}
	
	public final static <T,R> Stream<R> flatMapCollection(Stream<T> stream,Function<? super T,Collection<? extends R>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMapCollection(fn).stream();
		
	}
	public final static <T,R> Stream<R> flatMapStream(Stream<T> stream,Function<? super T,BaseStream<? extends R,?>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMapStream(fn).stream();
	}
	public final static <T,R> Stream<R> flatMapOptional(Stream<T> stream,Function<? super T,Optional<? extends R>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMapOptional(fn).stream();
		
	}
	public final static <T,R> Stream<R> flatMapCompletableFuture(Stream<T> stream,Function<? super T,CompletableFuture<? extends R>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMapCompletableFuture(fn).stream();
		
	}
	public final static <T,R> Stream<R>  flatMapLazySeq(Stream<T> stream,Function<? super T,LazySeq<? extends R>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMapLazySeq(fn).stream();
		
	}
	
	/**
	 * Perform a flatMap operation where the result will be a flattened stream of Characters
	 * from the CharSequence returned by the supplied function.
	 * 
	 * <pre>
	 * {@code 
	 *   List<Character> result = StreamUtils.liftAndBindCharSequence(Stream.of("input.file"),
									.i->"hello world")
									.toList();
		
		assertThat(result,equalTo(Arrays.asList('h','e','l','l','o',' ','w','o','r','l','d')));
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	public final static <T> Stream<Character> liftAndBindCharSequence(Stream<T> stream,Function<? super T,CharSequence> fn) {
		return AsAnyM.anyM(stream).asSequence().liftAndBindCharSequence(fn).stream();
		
	}
	/**
	 *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied files.
	 * 
	 * <pre>
	 * {@code
	 * 
		List<String> result = StreamUtils.liftAndBindFile(Stream.of("input.file")
								.map(getClass().getClassLoader()::getResource)
								.peek(System.out::println)
								.map(URL::getFile)
								,File::new)
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
	public final static <T> Stream<String> liftAndBindFile(Stream<T> stream,Function<? super T,File> fn) {
		return AsAnyM.anyM(stream).asSequence().liftAndBindFile(fn).stream();
	}
	/**
	 *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied URLs 
	 * 
	 * <pre>
	 * {@code 
	 * List<String> result = StreamUtils.liftAndBindURL(Stream.of("input.file")
								,getClass().getClassLoader()::getResource)
								.collect(Collectors.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param fn
	 * @return
	 */
	public final  static <T> Stream<String> liftAndBindURL(Stream<T> stream,Function<? super T, URL> fn) {
		return AsAnyM.anyM(stream).asSequence().liftAndBindURL(fn).stream();
				
	}
	/**
	  *  Perform a flatMap operation where the result will be a flattened stream of Strings
	 * from the text loaded from the supplied BufferedReaders
	 * 
	 * <pre>
	 * List<String> result = StreamUtils.liftAndBindBufferedReader(Stream.of("input.file")
								.map(getClass().getClassLoader()::getResourceAsStream)
								.map(InputStreamReader::new)
								,BufferedReader::new)
								.collect(Collectors.toList();
		
		assertThat(result,equalTo(Arrays.asList("hello","world")));
	 * 
	 * </pre>
	 * 
	 * 
	 * @param fn
	 * @return
	 */
	public final static <T> Stream<String> liftAndBindBufferedReader(Stream<T> stream,Function<? super T,BufferedReader> fn) {
		return AsAnyM.anyM(stream).asSequence().liftAndBindBufferedReader(fn).stream();
	}

	 /**
	   * Projects an immutable collection of this stream.
	   *
	   * @return An immutable collection of this stream.
	   */
	  public static final <A> Collection<A> toLazyCollection(Stream<A> stream) {
		  	return toLazyCollection(stream.iterator());
	  }	
		  public static final <A> Collection<A> toLazyCollection(Iterator<A> iterator) {
	    return new AbstractCollection<A>() {
	    	
	    @Override  
	    public boolean equals(Object o){
	    	  if(o==null)
	    		  return false;
	    	  if(! (o instanceof Collection))
	    		  return false;
	    	  Collection<A> c = (Collection)o;
	    	  Iterator<A> it1 = iterator();
	    	  Iterator<A> it2 = c.iterator();
	    	  while(it1.hasNext()){
	    		  if(!it2.hasNext())
	    			  return false;
	    		  if(!Objects.equals(it1.next(),it2.next()))
	    			  return false;
	    	  }
	    	  if(it2.hasNext())
	    		  return false;
	    	  return true;
	      }
	      @Override  
	      public int hashCode(){
	    	  Iterator<A> it1 = iterator();
	    	  List<A> arrayList= new ArrayList<>();
	    	  while(it1.hasNext()){
	    		  arrayList.add(it1.next());
	    	  }
	    	  return Objects.hashCode(arrayList.toArray());
	      }
	      List<A> data =new ArrayList<>();
	     
	      boolean complete=false;
	      public Iterator<A> iterator() {
	    	  if(complete)
	    		  return data.iterator();
	    	  return new Iterator<A>(){
	    		  int current = -1;
				@Override
				public boolean hasNext() {
					if(current==data.size()-1 && !complete){
						boolean result = iterator.hasNext();
						complete = !result;
						return result;
					}
					if(current<data.size())
						return true;
					return false;
				}

				@Override
				public A next() {
					if(current<data.size() &&!complete){
						data.add(iterator.next());
						
						return data.get(++current);
					}
					current++;
					return data.get(current);
						
					
					
				}
	    		  
	    	  };
	        
	      }

	      public int size() {
	    	  if(complete)
	    		  return data.size();
	    	  Iterator it = iterator();
	    	  while(it.hasNext())
	    		  it.next();
	    	  
	    	  return data.size();
	      }
	    };
	  }
		
}

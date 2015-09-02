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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
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
import com.aol.cyclops.closures.mutable.Mutable;
import lombok.AllArgsConstructor;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.SequenceMImpl;
import com.aol.cyclops.sequence.Streamable;



public class StreamUtils{
	
	public final static <T> Stream<T> optionalToStream(Optional<T> optional){
		if(optional.isPresent())
			return Stream.of(optional.get());
		return Stream.of();
	}
	public final static <T> Stream<T> completableFutureToStream(CompletableFuture<T> future){
		return Stream.of(future.join());
			
	}
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
	 * extract head and tail together
	 * 
	 * <pre>
	 * {@code 
	 *  Stream<String> helloWorld = Stream.of("hello","world","last");
		HeadAndTail<String> headAndTail = StreamUtils.headAndTail(helloWorld);
		 String head = headAndTail.head();
		 assertThat(head,equalTo("hello"));
		
		SequenceM<String> tail =  headAndTail.tail();
		assertThat(tail.headAndTail().head(),equalTo("world"));
	 * }
	 * </pre>
	 * 
	 * @return
	 */
	public final  static <T> HeadAndTail<T> headAndTail(Stream<T> stream){
		Iterator<T> it = stream.iterator();
		return new HeadAndTail(it.next(),sequenceM(stream(it)));
	}
	/**
	 * <pre>
	 * {@code 
	 *  Stream<String> helloWorld = Stream.of();
		Optional<HeadAndTail<String>> headAndTail = StreamUtils.headAndTailOptional(helloWorld);
		assertTrue(!headAndTail.isPresent());
	 * }
	 * </pre>
	 * @param stream to extract head and tail from
	 * @return
	 */
	public final  static <T> Optional<HeadAndTail<T>> headAndTailOptional(Stream<T> stream){
		Iterator<T> it = stream.iterator();
		if(!it.hasNext())
			return Optional.empty();
		return Optional.of(new HeadAndTail(it.next(),sequenceM(stream(it))));
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
		return skipWhile(stream,predicate.negate());
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
		Iterator<U> it = stream.iterator();
		return StreamUtils.stream(new Iterator<U>(){
			U next;
			boolean nextSet = false;
			boolean init =false;
			@Override
			public boolean hasNext() {
				if(init)
					return it.hasNext();
				try{
					while(it.hasNext()){
						
						next = it.next();
						nextSet = true;
						
						if(!predicate.test(next))
							return true;
					
					}
					return false;
				}finally{
					init =true;
				}
			}

			@Override
			public U next() {
				if(!init){
					hasNext();
				}
				if(nextSet){
					nextSet = false;
					return next;
				}
				return it.next();
			}
			
		});
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
		Iterator<U> it = stream.iterator();
		return StreamUtils.stream(new Iterator<U>(){
			U next;
			boolean nextSet = false;
			boolean stillGoing =true;
			@Override
			public boolean hasNext() {
				if(!stillGoing)
					return false;
				if(nextSet)
					return stillGoing;
				
				if (it.hasNext()) {
					next = it.next();
					nextSet = true;
					if (!predicate.test(next)) {
						stillGoing = false;
					}
					
				} else {
					stillGoing = false;
				}
				return stillGoing;
				
					
			}

			@Override
			public U next() {
				
				if(nextSet){
					nextSet = false;
					return next;
				}
				
				
				U local = it.next();
				if(stillGoing){
					stillGoing = !predicate.test(local);
				}
				return local;
			}
			
		});
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
		return limitWhile(stream,predicate.negate());
		
	}
	/**
	 * Reverse a Stream
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(StreamUtils.reverse(Stream.of(1,2,3)).collect(Collectors.toList())
				,equalTo(Arrays.asList(3,2,1)));
	 * }
	 * </pre>
	 * 
	 * @param stream Stream to reverse
	 * @return Reversed stream
	 */
	public static <U> Stream<U> reverse(Stream<U> stream){
		return reversedStream(stream.collect(Collectors.toList()));
	}
	/**
	 * Create a reversed Stream from a List
	 * <pre>
	 * {@code 
	 * StreamUtils.reversedStream(asList(1,2,3))
				.map(i->i*100)
				.forEach(System.out::println);
		
		
		assertThat(StreamUtils.reversedStream(Arrays.asList(1,2,3)).collect(Collectors.toList())
				,equalTo(Arrays.asList(3,2,1)));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param list List to create a reversed Stream from
	 * @return Reversed Stream
	 */
	public static <U> Stream<U> reversedStream(List<U> list){
		return new ReversedIterator<>(list).stream();
	}
	/**
	 * Create a new Stream that infiniteable cycles the provided Stream
	 * 
	 * <pre>
	 * {@code 		
	 * assertThat(StreamUtils.cycle(Stream.of(1,2,3))
	 * 						.limit(6)
	 * 						.collect(Collectors.toList()),
	 * 								equalTo(Arrays.asList(1,2,3,1,2,3)));
		}
	 * </pre>
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
	 * Create a Stream that finitely cycles the provided Streamable, provided number of times
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(StreamUtils.cycle(3,Streamable.of(1,2,2))
								.collect(Collectors.toList()),
									equalTo(Arrays.asList(1,2,2,1,2,2,1,2,2)));
	 * }
	 * </pre>
	 * @param s Streamable to cycle
	 * @return New cycling stream
	 */
	public static <U> Stream<U> cycle(int times,Streamable<U> s){
		return Stream.iterate(s.stream(),s1-> s.stream()).limit(times).flatMap(Function.identity());
	}
	
	/**
	 * Create a stream from an iterable
	 * <pre>
	 * {@code 
	 * 	assertThat(StreamUtils.stream(Arrays.asList(1,2,3))
	 * 								.collect(Collectors.toList()),
	 * 									equalTo(Arrays.asList(1,2,3)));

	 * 
	 * }
	 * </pre>
	 * @param it Iterable to convert to a Stream
	 * @return Stream from iterable
	 */
	public static <U> Stream<U> stream(Iterable<U> it){
		return StreamSupport.stream(it.spliterator(),
					false);
	}
	public static <U> Stream<U> stream(Spliterator<U> it){
		return StreamSupport.stream(it,
					false);
	}
	/**
	 * Create a stream from an iterator
	 * <pre>
	 * {@code 
	 * 	assertThat(StreamUtils.stream(Arrays.asList(1,2,3).iterator())	
	 * 							.collect(Collectors.toList()),
	 * 								equalTo(Arrays.asList(1,2,3)));

	 * }
	 * </pre>
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
	 * <pre>
	 * {@code 
	 * 	Map<String,String> map = new HashMap<>();
		map.put("hello","world");
		assertThat(StreamUtils.stream(map).collect(Collectors.toList()),equalTo(Arrays.asList(new AbstractMap.SimpleEntry("hello","world"))));

	 * }</pre>
	 * 
	 * 
	 * @param it Iterator to convert to a Stream
	 * @return Stream from a map
	 */
	public static <K,V> Stream<Map.Entry<K, V>> stream(Map<K,V> it){
		return it.entrySet().stream();
	}
	/**
	 * Simultaneously reduce a stream with multiple reducers
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
	 * <pre>
	 * {@code 
	 *  Monoid<String> concat = Monoid.of("",(a,b)->a+b);
		Monoid<String> join = Monoid.of("",(a,b)->a+","+b);
		assertThat(StreamUtils.reduce(Stream.of("hello", "world", "woo!"),Stream.of(concat,join))
		                 ,equalTo(Arrays.asList("helloworldwoo!",",hello,world,woo!")));
	 * }
	 * </pre>
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
	 * <pre>
	 * {@code 
	 * List result = StreamUtils.collect(Stream.of(1,2,3),
								Stream.of(Collectors.toList(),
								Collectors.summingInt(Integer::intValue),
								Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
	 * }
	 * </pre>
	 * @param stream Stream to collect
	 * @param collectors Collectors to apply
	 * @return Result as a list
	 */
	public static <T,A,R> List<R> collect(Stream<T> stream, Stream<Collector> collectors){
		return collect(stream, AsStreamable.<Collector>asStreamable(collectors));
	}
	/**
	 *  Apply multiple Collectors, simultaneously to a Stream
	 * <pre>
	 * {@code 
	 * List result = StreamUtils.collect(Stream.of(1,2,3),
								Arrays.asList(Collectors.toList(),
								Collectors.summingInt(Integer::intValue),
								Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
	 * }
	 * </pre>
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
	 * <pre>
	 * {@code
	 * List result = StreamUtils.collect(Stream.of(1,2,3),
								Streamable.<Collector>of(Collectors.toList(),
								Collectors.summingInt(Integer::intValue),
								Collectors.averagingInt(Integer::intValue)));
		
		assertThat(result.get(0),equalTo(Arrays.asList(1,2,3)));
		assertThat(result.get(1),equalTo(6));
		assertThat(result.get(2),equalTo(2.0));
	 * 
	 * }
	 * </pre>
	 * @param stream Stream to collect
	 * @param collectors  Collectors to apply
	 * @return Result as a list
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> List collect(Stream<T> stream, Streamable<Collector> collectors){
		
		
		final Supplier supplier =  ()-> collectors.stream().map(c->c.supplier().get()).collect(Collectors.toList());
		final BiConsumer accumulator = (acc,next) -> {  Seq.of(collectors.stream().iterator()).<Object,Tuple2<Collector,Object>>zip(
																Seq.of((List)acc),(a,b)->new Tuple2<Collector,Object>(a,b))
													
													.forEach( t -> t.v1().accumulator().accept(t.v2(),next));
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
	 * <pre>
	 * {@code 
	 *  int count =0;
	 *  
		assertThat(StreamUtils.cycleWhile(Stream.of(1,2,2)
											,next -> count++<6 )
											.collect(Collectors.toList()),equalTo(Arrays.asList(1,2,2,1,2,2)));
	 * }
	 * </pre>
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	public final static <T> Stream<T> cycleWhile(Stream<T>  stream,Predicate<? super T> predicate) {
		return StreamUtils.limitWhile(StreamUtils.cycle(stream),predicate);
	}

	/**
	 * Repeat in a Stream until specified predicate holds
	 * 
	 * <pre>
	 * {@code 
	 * 	count =0;
		assertThat(StreamUtils.cycleUntil(Stream.of(1,2,2,3)
											,next -> count++>10 )
											.collect(Collectors.toList()),equalTo(Arrays.asList(1, 2, 2, 3, 1, 2, 2, 3, 1, 2, 2)));

	 * }
	 * </pre>
	 * @param predicate
	 *            repeat while true
	 * @return Repeating Stream
	 */
	public final static <T> Stream<T> cycleUntil(Stream<T> stream,Predicate<? super T> predicate) {
		return StreamUtils.limitUntil(StreamUtils.cycle(stream),predicate);
	}

	/**
	 * Generic zip function. E.g. Zipping a Stream and an Optional
	 * 
	 * <pre>
	 * {@code 
	 * Stream<List<Integer>> zipped = StreamUtils.zip(Stream.of(1,2,3)
												,SequenceM.of(2,3,4), 
													(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(1);
		assertThat(zip.get(0),equalTo(2));
		assertThat(zip.get(1),equalTo(3));
	 * }
	 * </pre>
	 * @param second
	 *            Monad to zip with
	 * @param zipper
	 *            Zipping function
	 * @return Stream zipping two Monads
	 */
	public final static <T,S, R> Stream<R> zip(Stream<T> stream,SequenceMImpl<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		Iterator<T> left = stream.iterator();
		Iterator<? extends S> right = second.stream().iterator();
		return StreamUtils.stream(new Iterator<R>(){

			@Override
			public boolean hasNext() {
				return left.hasNext() && right.hasNext();
			}

			@Override
			public R next() {
				return zipper.apply(left.next(), right.next());
			}
			
		});
		
	}
	/**
	 *  Generic zip function. E.g. Zipping a Stream and an Optional
	 * 
	 * <pre>
	 * {@code
	 * Stream<List<Integer>> zipped = StreamUtils.zip(Stream.of(1,2,3)
										,anyM(Optional.of(2)), 
											(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(0);
		assertThat(zip.get(0),equalTo(1));
		assertThat(zip.get(1),equalTo(2));
	 * 
	 * }
	 * </pre>
	 
	 */
	public final static <T,S, R> Stream<R> zip(Stream<T> stream,AnyM<? extends S> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		return zip(stream,second.toSequence(), zipper);
	}

	/**
	 * Zip this Monad with a Stream
	 * 
	   <pre>
	   {@code 
	   Stream<List<Integer>> zipped = StreamUtils.zipStream(Stream.of(1,2,3)
												,Stream.of(2,3,4), 
													(a,b) -> Arrays.asList(a,b));
		
		
		List<Integer> zip = zipped.collect(Collectors.toList()).get(1);
		assertThat(zip.get(0),equalTo(2));
		assertThat(zip.get(1),equalTo(3));
	   }
	   </pre>
	 * 
	 * @param second
	 *            Stream to zip with
	 * @param zipper
	 *            Zip funciton
	 * @return This monad zipped with a Stream
	 */
	public final static <T,S,R> Stream<R> zipStream(Stream<T> stream,BaseStream<? extends S,? extends BaseStream<? extends S,?>> second,
			BiFunction<? super T, ? super S, ? extends R> zipper) {
		Iterator<T> left = stream.iterator();
		Iterator<? extends S> right = second.iterator();
		return StreamUtils.stream(new Iterator<R>(){

			@Override
			public boolean hasNext() {
				return left.hasNext() && right.hasNext();
			}

			@Override
			public R next() {
				return zipper.apply(left.next(), right.next());
			}
			
		});
				
	}

	/**
	 * Create a sliding view over this Stream
	 * <pre>
	 * {@code 
	 * List<List<Integer>> list = StreamUtils.sliding(Stream.of(1,2,3,4,5,6)
												,2,1)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	 * }
	 * </pre>
	 * @param windowSize
	 *            Size of sliding window
	 * @return Stream with sliding view over monad
	 */
	public final static <T> Stream<List<T>> sliding(Stream<T> stream,int windowSize,int increment) {
		Iterator<T> it = stream.iterator();
		Mutable<PStack<T>> list = Mutable.of(ConsPStack.empty());
		return StreamUtils.stream(new Iterator<List<T>>(){
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public List<T> next() {
				for(int i=0;i<increment && list.get().size()>0;i++)
					 list.mutate(var -> var.minus(0));
				for (int i = 0; list.get().size() < windowSize && it.hasNext(); i++) {
					if(it.hasNext()){
						list.mutate(var -> var.plus(Math.max(0,var.size()),it.next()));
					}
					
				}
				return list.get();
			}
			
		});
	}
	/**
	 * Create a sliding view over this Stream
	 * <pre>
	 * {@code 
	 * List<List<Integer>> list = StreamUtils.sliding(Stream.of(1,2,3,4,5,6)
												,2)
									.collect(Collectors.toList());
		
	
		assertThat(list.get(0),hasItems(1,2));
		assertThat(list.get(1),hasItems(2,3));
	 * }
	 * </pre> 
	 * 
	 * @param stream Stream to create sliding view on
	 * @param windowSize size of window
	 * @return
	 */
	public final static <T> Stream<List<T>> sliding(Stream<T> stream,int windowSize) {
		return sliding(stream,windowSize,1);
	}

	/**
	 * Group elements in a Monad into a Stream
	 * 
	   <pre>
	   {@code 
	 * 	List<List<Integer>> list = StreamUtils.grouped(Stream.of(1,2,3,4,5,6)
														,3)
													.collect(Collectors.toList());
		
		
		assertThat(list.get(0),hasItems(1,2,3));
		assertThat(list.get(1),hasItems(4,5,6));
		}
	 * </pre>
	 * @param groupSize
	 *            Size of each Group
	 * @return Stream with elements grouped by size
	 */
	public final static<T> Stream<List<T>> grouped(Stream<T> stream,int groupSize) {
		Iterator<T> it = stream.iterator();
		return StreamUtils.stream(new Iterator<List<T>>(){
			
			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public List<T> next() {
				List<T> list = new ArrayList<>();
				for (int i = 0; i < groupSize; i++) {
					if(it.hasNext())
						list.add(it.next());
					
				}
				return list;
			}
			
		});
		
		
	}


	
	/**
	 * Scan left using supplied Monoid
	 * 
	 * <pre>
	 * {@code  
	 * 
	 * 	assertEquals(asList("", "a", "ab", "abc"),
	 * 					StreamUtils.scanLeft(Stream.of("a", "b", "c"),Reducers.toString(""))
	 * 			.collect(Collectors.toList());
	 *         
	 *         }
	 * </pre>
	 * 
	 * @param monoid
	 * @return
	 */
	public final static <T> Stream<T> scanLeft(Stream<T> stream,Monoid<T> monoid) {
		Iterator<T> it = stream.iterator();
		return StreamUtils.stream(new Iterator<T>() {
				boolean init = false;
				T next = monoid.zero();

	            @Override
	            public boolean hasNext() {
	            	if(!init)
	            		return true;
	                return it.hasNext();
	            }

	            @Override
	            public T next() {
	                if (!init) {
	                    init = true;
	                    return monoid.zero();
	                } 
	                return next = monoid.combiner().apply(next, it.next());
	                
	                
	            }
	        
			
		});

	}


	
	
	
	/**
	 * Check that there are specified number of matches of predicate in the Stream
	 * 
	 * <pre>
	 * {@code 
	 *  assertTrue(StreamUtils.xMatch(Stream.of(1,2,3,5,6,7),3, i->i>4));
	 * }
	 * </pre>
	 * 
	 */
	public  static <T> boolean xMatch(Stream<T> stream,int num, Predicate<? super T> c) {
		
		return stream.filter(t -> c.test(t)) .collect(Collectors.counting()) == num;
	}
	/**
	 * <pre>
	 * {@code 
     * assertThat(StreamUtils.noneMatch(of(1,2,3,4,5),it-> it==5000),equalTo(true));
     * }
     * </pre>
     *
	 */
	public final static <T>  boolean  noneMatch(Stream<T> stream,Predicate<? super T> c) {
		return stream.allMatch(c.negate());
	}
	
	public final static  <T> String join(Stream<T> stream){
		return stream.map(t->t.toString()).collect(Collectors.joining());
	}
	public final static  <T> String join(Stream<T> stream,String sep){
		return stream.map(t->t.toString()).collect(Collectors.joining(sep));
	}
	public final  static<T> String join(Stream<T> stream, String sep,String start,String end){
		return stream.map(t->t.toString()).collect(Collectors.joining(sep,start,end));
	}
	
	
	public final static  <T,C extends Comparable<C>>  Optional<T> minBy(Stream<T> stream,Function<T,C> f){
		Optional<Tuple2<C,T>> o = stream.map(in->new Tuple2<C,T>(f.apply(in),in)).min(Comparator.comparing(n->n.v1(),Comparator.naturalOrder()));
		return	o.map(p->p.v2());
	}
	public final  static<T> Optional<T> min(Stream<T> stream,Comparator<? super T> comparator){
		return stream.collect(Collectors.minBy(comparator));
	}
	public final static <T,C extends Comparable<? super C>> Optional<T> maxBy(Stream<T> stream,Function<T,C> f){
		Optional<Tuple2<C,T>> o = stream.map(in->new Tuple2<C,T>(f.apply(in),in)).max(Comparator.comparing(n->n.v1(),Comparator.naturalOrder()));
		return	o.map(p->p.v2());
	}
	public final static<T> Optional<T> max(Stream<T> stream,Comparator<? super T> comparator){
		return stream.collect(Collectors.maxBy(comparator));
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
		return startsWith(stream,iterable.iterator());
		
	}
	public final static <T> boolean endsWith(Stream<T> stream,Iterable<T> iterable){
		Iterator<T> it = iterable.iterator();
		List<T> compare1 = new ArrayList<>();
		while(it.hasNext()){
			compare1.add(it.next());
		}
		LinkedList<T> list = new LinkedList<>();
		stream.forEach(v -> {
			list.add(v);
			if(list.size()>compare1.size())
				list.remove();
		});
		return startsWith(list.stream(),compare1.iterator());
		
	}
	/**
	 * 	<pre>
	 * {@code
	 * 		 assertTrue(StreamUtils.startsWith(Stream.of(1,2,3,4),Arrays.asList(1,2,3).iterator())) 
	 * }</pre>

	 * @param iterator
	 * @return True if Monad starts with Iterators sequence of data
	 */
	public final static <T> boolean startsWith(Stream<T> stream,Iterator<T> iterator){
		Iterator<T> it = stream.iterator();
		while(iterator.hasNext()){
			if(!it.hasNext())
				return false;
			if(!Objects.equals(it.next(), iterator.next()))
				return false;
		}
		return true;
		
		
	}
	
	
	public static<T> SequenceM<T> sequenceM(Stream<T> stream){
		if(stream instanceof SequenceM)
			return (SequenceM)stream;
		return new SequenceMImpl(stream);
	}
	
	/**
	 * Returns a stream with a given value interspersed between any two values
	 * of this stream.
	 * 
	 * <pre>
	 * {@code 
	 * assertThat(Arrays.asList(1, 0, 2, 0, 3, 0, 4),
	 * 			equalTo( StreamUtils.intersperse(Stream.of(1, 2, 3, 4),0));
	 * }
	 * </pre>
	 */
	public static <T> Stream<T> intersperse(Stream<T> stream, T value) {
		return stream.flatMap(t -> Stream.of(value, t)).skip(1);
	}
	/**
	 * Keep only those elements in a stream that are of a given type.
	 * 
	 * 
	 * assertThat(Arrays.asList(1, 2, 3), 
	 *      equalTo( StreamUtils.ofType(Stream.of(1, "a", 2, "b", 3,Integer.class));
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static <T, U> Stream<U> ofType(Stream<T> stream, Class<U> type) {
		return stream.filter(type::isInstance).map(t -> (U) t);
	}

	/**
	 * Cast all elements in a stream to a given type, possibly throwing a
	 * {@link ClassCastException}.
	 * 
	 * <pre>
	 * {@code
	 * StreamUtils.cast(Stream.of(1, "a", 2, "b", 3),Integer.class)
	 *  // throws ClassCastException
	 *  }
	 */
	public static <T, U> Stream<U> cast(Stream<T> stream, Class<U> type) {
		return stream.map(type::cast);
	}
	/**
	 * flatMap operation
	 * <pre>
	 * {@code 
	 * 		assertThat(StreamUtils.flatMapSequenceM(Stream.of(1,2,3),
	 * 							i->SequenceM.of(i+2)).collect(Collectors.toList()),
	 * 								equalTo(Arrays.asList(3,4,5)));
	 * }
	 * </pre>
	 * @param fn
	 * @return
	 */
	public final static <T,R> Stream<R> flatMapSequenceM(Stream<T> stream,Function<? super T,SequenceMImpl<? extends R>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMap(fn).stream();
	}
	public final static <T,R> Stream<R> flatMapAnyM(Stream<T> stream,Function<? super T,AnyM<? extends R>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMapAnyM(fn).stream();
	}
	
	/**
	 * flatMap operation that allows a Collection to be returned
	 * <pre>
	 * {@code 
	 * 	assertThat(StreamUtils.flatMapCollection(Stream.of(20),i->Arrays.asList(1,2,i))
	 * 								.collect(Collectors.toList()),
	 * 								equalTo(Arrays.asList(1,2,20)));

	 * }
	 * </pre>
	 *
	 */
	public final static <T,R> Stream<R> flatMapCollection(Stream<T> stream,Function<? super T,Collection<? extends R>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMapCollection(fn).stream();
		
	}
	/**
	 * <pre>
	 * {@code 
	 * 	assertThat(StreamUtils.flatMapStream(Stream.of(1,2,3),
	 * 							i->Stream.of(i)).collect(Collectors.toList()),
	 * 							equalTo(Arrays.asList(1,2,3)));

	 * 
	 * }
	 * </pre>
	 *
	 */
	public final static <T,R> Stream<R> flatMapStream(Stream<T> stream,Function<? super T,BaseStream<? extends R,?>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMapStream(fn).stream();
	}
	/**
	 * cross type flatMap, removes null entries
     * <pre>
     * {@code 
     * 	 assertThat(StreamUtils.flatMapOptional(Stream.of(1,2,3,null),
     * 										Optional::ofNullable)
     * 										.collect(Collectors.toList()),
     * 										equalTo(Arrays.asList(1,2,3)));

     * }
     * </pre>
	 */
	public final static <T,R> Stream<R> flatMapOptional(Stream<T> stream,Function<? super T,Optional<? extends R>> fn) {
		return AsAnyM.anyM(stream).asSequence().flatMapOptional(fn).stream();
		
	}
	/**
	 *<pre>
	 * {@code 
	 * 	assertThat(StreamUtils.flatMapCompletableFuture(Stream.of(1,2,3),
	 * 								i->CompletableFuture.completedFuture(i+2))
	 * 								.collect(Collectors.toList()),
	 * 								equalTo(Arrays.asList(3,4,5)));

	 * }
	 *</pre>
	 */
	public final static <T,R> Stream<R> flatMapCompletableFuture(Stream<T> stream,Function<? super T,CompletableFuture<? extends R>> fn) {
		return	stream.flatMap( in->StreamUtils.completableFutureToStream(fn.apply(in)));
		
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
		
		return AsGenericMonad.<Stream<T>,T>asStreamUtils.sequenceM(monad).liftAndBind(fn).sequence();
				
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
	   * Projects an immutable collection of this stream. Initial iteration over the collection is not thread safe 
	   * (can't be performed by multiple threads concurrently) subsequent iterations are.
	   *
	   * @return An immutable collection of this stream.
	   */
	  public static final <A> Collection<A> toLazyCollection(Stream<A> stream) {
		  	return toLazyCollection(stream.iterator());
	  }	
	  public static final <A> Collection<A> toLazyCollection(Iterator<A> iterator){
		  return toLazyCollection(iterator,false);
	  }
	  /**
	   * Lazily constructs a Collection from specified Stream. Collections iterator may be safely used
	   * concurrently by multiple threads.
	 * @param stream
	 * @return
	 */
	public static final <A> Collection<A> toConcurrentLazyCollection(Stream<A> stream) {
		  	return toConcurrentLazyCollection(stream.iterator());
	  }	
	  public static final <A> Collection<A> toConcurrentLazyCollection(Iterator<A> iterator){
		  return toLazyCollection(iterator,true);
	  }
	 private static final <A> Collection<A> toLazyCollection(Iterator<A> iterator,boolean concurrent) {
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
	     
	      volatile boolean complete=false;
	      
	      Object lock = new Object();
	      ReentrantLock rlock = new ReentrantLock();
	      public Iterator<A> iterator() {
	    	  if(complete)
	    		  return data.iterator();
	    	  return new Iterator<A>(){
	    		int current = -1;
				@Override
				public boolean hasNext() {
					
					if(concurrent){
						
						rlock.lock();
					}
					try{
						
						if(current==data.size()-1 && !complete){
							boolean result = iterator.hasNext();
							complete = !result;
							
							return result;
						}
						if(current+1<data.size()){
							
							return true;
						}
						return false;
					}finally{
						if(concurrent)
							rlock.unlock();
					}
				}

					@Override
					public A next() {
						
						if (concurrent) {

							rlock.lock();
						}
						try {
							if (current < data.size() && !complete) {
								if(iterator.hasNext())
									data.add(iterator.next());

								return data.get(++current);
							}
							current++;	
							return data.get(current);
						} finally {
							
							if (concurrent)
								rlock.unlock();
						}

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
	 public static final <A> Tuple2<Iterator<A>,Iterator<A>> toBufferingDuplicator(Iterator<A> iterator) {
		 return toBufferingDuplicator(iterator,Long.MAX_VALUE);
	 }
	 public static final <A> Tuple2<Iterator<A>,Iterator<A>> toBufferingDuplicator(Iterator<A> iterator,long pos) {
		 LinkedList<A> bufferTo = new LinkedList<A>();
		 LinkedList<A> bufferFrom = new LinkedList<A>();
		  return new Tuple2(new DuplicatingIterator(bufferTo,bufferFrom,iterator,Long.MAX_VALUE,0),
				  new DuplicatingIterator(bufferFrom,bufferTo,iterator,pos,0));
	 }
	 public static final <A> List<Iterator<A>> toBufferingCopier(Iterator<A> iterator,int copies) {
		List<Iterator<A>> result = new ArrayList<>();
		List<CopyingIterator<A>> leaderboard = new LinkedList<>();
		LinkedList<A> buffer = new LinkedList<>();
		 for(int i=0;i<copies;i++)
			 result.add(new CopyingIterator(iterator,leaderboard,buffer,copies));
		 return result;
	 }
	
	 @AllArgsConstructor
	 static class DuplicatingIterator<T> implements Iterator<T>{
		
		 LinkedList<T> bufferTo;
		 LinkedList<T> bufferFrom;
		 Iterator<T> it;
		 long otherLimit=Long.MAX_VALUE;
		 long counter=0;
		 

		@Override
		public boolean hasNext() {
			if(bufferFrom.size()>0 || it.hasNext())
				return true;
			return false;
		}

		@Override
		public T next() {
			try{
				if(bufferFrom.size()>0)
					return bufferFrom.remove(0);
				else{
					T next = it.next();
					if(counter<otherLimit)
						bufferTo.add(next);
					return next;
				}
			}finally{
				counter++;
			}
		}
		 
	 }
	
	 static class CopyingIterator<T> implements Iterator<T>{
		
		 
		 LinkedList<T> buffer;
		 Iterator<T> it;
		 List<CopyingIterator<T>> leaderboard = new LinkedList<>();
		 boolean added=  false;
		 int total = 0;
		 int counter=0;

		@Override
		public boolean hasNext() {
			
			if(isLeader())
				return it.hasNext();
			if(isLast())
				return buffer.size()>0 || it.hasNext();
			if(it.hasNext())
				return true;
			return counter < buffer.size();
		}

		private boolean isLeader() {
			return leaderboard.size()==0 || this==leaderboard.get(0);
		}
		private boolean isLast() {
			return leaderboard.size()==total && this==leaderboard.get(leaderboard.size()-1);
		}

		@Override
		public T next() {
			
			if(!added){
				
				this.leaderboard.add(this);
				added = true;
			}
			
			if(isLeader()){
				
				return handleLeader();
			}
			if(isLast()){
				
				if(buffer.size()>0)
					return buffer.poll();
				return it.next();
			}
			if(counter< buffer.size())	
				return buffer.get(counter++);
			return handleLeader(); //exceed buffer, now leading
				
		 
	 }

		private T handleLeader() {
			T next = it.next();
			buffer.offer(next);
			return next;
		}

		public CopyingIterator(Iterator<T> it,
				List<CopyingIterator<T>> leaderboard,LinkedList<T> buffer,int total) {
			
			this.it = it;
			this.leaderboard = leaderboard;
			this.buffer = buffer;
			this.total = total;
		}
	 }
}

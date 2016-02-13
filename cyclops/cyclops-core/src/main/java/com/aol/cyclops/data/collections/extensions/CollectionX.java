package com.aol.cyclops.data.collections.extensions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Do;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.aol.cyclops.internal.matcher2.CheckValues;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.ExtendedTraversable;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.IterableCollectable;
import com.aol.cyclops.types.IterableFilterable;
import com.aol.cyclops.types.IterableFunctor;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.stream.HeadAndTail;
import com.aol.cyclops.types.stream.SequenceMCollectable;
import com.aol.cyclops.types.stream.future.FutureOperations;

//pattern match, for comprehensions
public interface CollectionX<T> extends ExtendedTraversable<T>,
										IterableCollectable<T>,
										Iterable<T>,
										IterableFunctor<T>, 
										Foldable<T>,
										IterableFilterable<T>,
										ZippingApplicativable<T>,
										Unit<T>,
										Collection<T>,
										SequenceMCollectable<T>{
	
	static <T> CollectionX<T> fromCollection(Collection<T> col){
		return new CollectionXImpl(col);
	}
	
	
	@Override
	default ReactiveSeq<T> stream(){
		
		return ReactiveSeq.fromIterable(this);
	}
	@Override
	default SequenceMCollectable<T> collectable(){
		return stream();
	}
	
	default Optional<T> getAtIndex(int index){
		return stream().get(index);
	}
	
	default HeadAndTail<T> headAndTail(){
		return new HeadAndTail<>(iterator());
	}
	
	default T head(){
		return iterator().next();
	}
	<T1> CollectionX<T1> from(Collection<T1> c);
	CollectionX<T> reverse();
	/**
	 * <pre>
	 * {@code 
	 *    
	 *    //1
	 *    SequenceM.of(1).single(); 
	 *    
	 *    //UnsupportedOperationException
	 *    SequenceM.of().single();
	 *     
	 *     //UnsupportedOperationException
	 *    SequenceM.of(1,2,3).single();
	 * }
	 * </pre>
	 * 
	 * @return a single value or an UnsupportedOperationException if 0/1 values
	 *         in this Stream
	 */
	default T single() {
		
		Iterator<T> it = iterator();
		if (it.hasNext()) {
			T result = it.next();
			if (!it.hasNext())
				return result;
		}
		throw new UnsupportedOperationException("single only works for Streams with a single value");

	}

	default T single(Predicate<? super T> predicate) {
		return this.filter(predicate).single();

	}

	/**
	 * <pre>
	 * {@code 
	 *    
	 *    //Optional[1]
	 *    SequenceM.of(1).singleOptional(); 
	 *    
	 *    //Optional.empty
	 *    SequenceM.of().singleOpional();
	 *     
	 *     //Optional.empty
	 *    SequenceM.of(1,2,3).singleOptional();
	 * }
	 * </pre>
	 * 
	 * @return An Optional with single value if this Stream has exactly one
	 *         element, otherwise Optional Empty
	 */
	default Optional<T> singleOptional() {
		Iterator<T> it = iterator();
		if (it.hasNext()) {
			T result = it.next();
			if (!it.hasNext())
				return Optional.of(result);
		}
		return Optional.empty();

	}
	/**
	 * @return First matching element in sequential order
	 * 
	 *         <pre>
	 * {@code
	 * SequenceM.of(1,2,3,4,5).filter(it -> it <3).findFirst().get();
	 * 
	 * //3
	 * }
	 * </pre>
	 * 
	 *         (deterministic)
	 * 
	 */
	default Optional<T> findFirst(){
		return stream().findFirst();
	}

	/**
	 * @return first matching element, but order is not guaranteed
	 * 
	 *         <pre>
	 * {@code
	 * SequenceM.of(1,2,3,4,5).filter(it -> it <3).findAny().get();
	 * 
	 * //3
	 * }
	 * </pre>
	 * 
	 * 
	 *         (non-deterministic)
	 */
	default Optional<T> findAny(){
		return stream().findAny();
	}
	default <K> MapX<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
		return stream().groupBy(classifier);
	}
	CollectionX<T> filter(Predicate<? super T> pred);
	<R> CollectionX<R> map(Function<? super T, ? extends R> mapper);
	<R> CollectionX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);
	CollectionX<T> limit(long num);
	CollectionX<T> skip(long num);
	CollectionX<T> takeWhile(Predicate<? super T> p);
	CollectionX<T> dropWhile(Predicate<? super T> p);
	CollectionX<T> takeUntil(Predicate<? super T> p);
	CollectionX<T> dropUntil(Predicate<? super T> p);
	CollectionX<T> dropRight(int num);
	CollectionX<T> takeRight(int num);
	default CollectionX<T> peek(Consumer<? super T> c){
		return (CollectionX<T>)ZippingApplicativable.super.peek(c);
	}
	CollectionX<ListX<T>> grouped(int groupSize);
	<K, A, D> CollectionX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);
	<K> CollectionX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier);
	<U> CollectionX<Tuple2<T, U>> zip(Iterable<U> other);
	<U, R> CollectionX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper);
	<U> CollectionX<Tuple2<T, U>> zipStream(Stream<U> other);
	CollectionX<Tuple2<T, Long>> zipWithIndex();
	CollectionX<ListX<T>> sliding(int windowSize);
	CollectionX<ListX<T>> sliding(int windowSize, int increment);
	CollectionX<T> scanLeft(Monoid<T> monoid);
	<U> CollectionX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function);
	CollectionX<T> scanRight(Monoid<T> monoid);
	<U> CollectionX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner);

	/**
	 * <pre>
	 * {@code
	 *  assertEquals("123".length(),SequenceM.of(1, 2, 3).join().length());
	 * }
	 * </pre>
	 * 
	 * @return Stream as concatenated String
	 */
	default String join(){
		
		return stream().join();
	}

	/**
	 * <pre>
	 * {@code
	 * assertEquals("1, 2, 3".length(), SequenceM.of(1, 2, 3).join(", ").length());
	 * }
	 * </pre>
	 * 
	 * @return Stream as concatenated String
	 */
	default String join(String sep){
		return stream().join(sep);
	}

	/**
	 * <pre>
	 * {@code 
	 * assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
	 * }
	 * </pre>
	 * 
	 * @return Stream as concatenated String
	 */
	default String join(String sep, String start, String end){
		return stream().join(sep,start,end);
	}
	default boolean xMatch(int num, Predicate<? super T> c){
		return stream().xMatch(num, c);
	}
	default void print(PrintStream str){
		stream().print(str);
	}
	default void print(PrintWriter writer){
		stream().print(writer);
	}
	default void printOut(){
		stream().printOut();
	}
	default void printErr(){
		stream().printErr();
	}
	/**
	 * Access asynchronous terminal operations (each returns a Future)
	 * 
	 * @param exec
	 *            Executor to use for Stream execution
	 * @return Async Future Terminal Operations
	 */
	default FutureOperations<T> futureOperations(Executor exec){
		return stream().futureOperations(exec);
	}
	 /**
	  * Performs a map operation that can call a recursive method without running out of stack space
	  * <pre>
	  * {@code
	  * SequenceM.of(10,20,30,40)
				 .trampoline(i-> fibonacci(i))
				 .forEach(System.out::println); 
				 
		Trampoline<Long> fibonacci(int i){
			return fibonacci(i,1,0);
		}
		Trampoline<Long> fibonacci(int n, long a, long b) {
	    	return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
		}		 
				 
	  * 55
		6765
		832040
		102334155
	  * 
	  * 
	  * SequenceM.of(10_000,200_000,3_000_000,40_000_000)
				 .trampoline(i-> fibonacci(i))
				 .forEach(System.out::println);
				 
				 
	  * completes successfully
	  * }
	  * 
	 * @param mapper
	 * @return
	 */
	<R> CollectionX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper);

	
	/**
	 * Perform a three level nested internal iteration over this Stream and the
	 * supplied streams
	 *
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2)
	 * 						.forEach3(a->IntStream.range(10,13),
	 * 						        a->b->Stream.of(""+(a+b),"hello world"),
	 * 									a->b->c->c+":"a+":"+b);
	 * 									
	 * 
	 *  //SequenceM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
	 * }
	 * </pre>
	 * 
	 * @param stream1
	 *            Nested Stream to iterate over
	 * @param stream2
	 *            Nested Stream to iterate over
	 * @param yieldingFunction
	 *            Function with pointers to the current element from both
	 *            Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	default <R1, R2, R> ReactiveSeq<R> forEach3(Function<? super T, Iterable<R1>> stream1,
			Function<? super T, Function<? super R1, Iterable<R2>>> stream2,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction){
		return Do.add(stream())
				.withIterable(stream1)
				.withIterable(stream2)
				.yield(yieldingFunction)
				.unwrap();
		
	}


	/**
	 * Perform a three level nested internal iteration over this Stream and the
	 * supplied streams
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3)
	 * 						.forEach3(a->IntStream.range(10,13),
	 * 						      a->b->Stream.of(""+(a+b),"hello world"),
	 * 						         a->b->c-> c!=3,
	 * 									a->b->c->c+":"a+":"+b);
	 * 									
	 * 
	 *  //SequenceM[11:1:2,hello world:1:2,14:1:4,hello world:1:4,12:1:2,hello world:1:2,15:1:5,hello world:1:5]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param stream1
	 *            Nested Stream to iterate over
	 * @param stream2
	 *            Nested Stream to iterate over
	 * @param filterFunction
	 *            Filter to apply over elements before passing non-filtered
	 *            values to the yielding function
	 * @param yieldingFunction
	 *            Function with pointers to the current element from both
	 *            Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	default <R1, R2, R> ReactiveSeq<R> forEach3(Function<? super T, Iterable<R1>> stream1,
			Function<? super T, Function<? super R1, Iterable<R2>>> stream2,
			Function<? super T, Function<? super R1, Function<? super R2, Boolean>>> filterFunction,
			Function<? super T, Function<? super R1, Function<? super R2, ? extends R>>> yieldingFunction){
		return Do.add(stream())
				.withIterable(stream1)
				.withIterable(stream2)
				.filter(filterFunction)
				.yield(yieldingFunction)
				.unwrap();
	}

	/**
	 * Perform a two level nested internal iteration over this Stream and the
	 * supplied stream
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3)
	 * 						.forEach2(a->IntStream.range(10,13),
	 * 									a->b->a+b);
	 * 									
	 * 
	 *  //SequenceM[11,14,12,15,13,16]
	 * }
	 * </pre>
	 * 
	 * 
	 * @param stream1
	 *            Nested Stream to iterate over
	 * @param yieldingFunction
	 *            Function with pointers to the current element from both
	 *            Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	default <R1, R> ReactiveSeq<R> forEach2(Function<? super T, Iterable<R1>> stream1,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction){
		
		return Do.add(stream())
				.withIterable(stream1)
				.yield(yieldingFunction).unwrap();
	}

	/**
	 * Perform a two level nested internal iteration over this Stream and the
	 * supplied stream
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of(1,2,3)
	 * 						.forEach2(a->IntStream.range(10,13),
	 * 						            a->b-> a<3 && b>10,
	 * 									a->b->a+b);
	 * 									
	 * 
	 *  //SequenceM[14,15]
	 * }
	 * </pre>
	 * 
	 * @param stream1
	 *            Nested Stream to iterate over
	 * @param filterFunction
	 *            Filter to apply over elements before passing non-filtered
	 *            values to the yielding function
	 * @param yieldingFunction
	 *            Function with pointers to the current element from both
	 *            Streams that generates the new elements
	 * @return SequenceM with elements generated via nested iteration
	 */
	default <R1, R> ReactiveSeq<R> forEach2(Function<? super T, Iterable<R1>> stream1, 
			Function<? super T, Function<? super R1, Boolean>> filterFunction,
			Function<? super T, Function<? super R1, ? extends R>> yieldingFunction){
		return Do.add(stream())
				.withIterable(stream1)
				.filter(filterFunction)
				.yield(yieldingFunction).unwrap();
		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#slice(long, long)
	 */
	CollectionX<T> slice(long from, long to);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
	 */
	<U extends Comparable<? super U>> CollectionX<T> sorted(Function<? super T, ? extends U> function);

	/**
	 * emit x elements per time period
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	SimpleTimer timer = new SimpleTimer();
	 * 	assertThat(SequenceM.of(1, 2, 3, 4, 5, 6).xPer(6, 100000000, TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(), is(6));
	 * 
	 * }
	 * </pre>
	 * 
	 * @param x
	 *            number of elements to emit
	 * @param time
	 *            period
	 * @param t
	 *            Time unit
	 * @return SequenceM that emits x elements per time period
	 */
	default ReactiveSeq<T> xPer(int x, long time, TimeUnit t){
		return stream().xPer(x, time, t);
	}

	/**
	 * emit one element per time period
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.iterate("", last -> "next")
	 * 				.limit(100)
	 * 				.batchBySize(10)
	 * 				.onePer(1, TimeUnit.MICROSECONDS)
	 * 				.peek(batch -> System.out.println("batched : " + batch))
	 * 				.flatMap(Collection::stream)
	 * 				.peek(individual -> System.out.println("Flattened : "
	 * 						+ individual))
	 * 				.forEach(a->{});
	 * }
	 * @param time period
	 * @param t Time unit
	 * @return SequenceM that emits 1 element per time period
	 */
	default ReactiveSeq<T> onePer(long time, TimeUnit t){
		return stream().onePer(time, t);
	}


	/**
	 * emit elements after a fixed delay
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	SimpleTimer timer = new SimpleTimer();
	 * 	assertThat(SequenceM.of(1, 2, 3, 4, 5, 6).fixedDelay(10000, TimeUnit.NANOSECONDS).collect(Collectors.toList()).size(), is(6));
	 * 	assertThat(timer.getElapsedNanoseconds(), greaterThan(60000l));
	 * }
	 * </pre>
	 * 
	 * @param l
	 *            time length in nanos of the delay
	 * @param unit
	 *            for the delay
	 * @return SequenceM that emits each element after a fixed delay
	 */
	default ReactiveSeq<T> fixedDelay(long l, TimeUnit unit){
		return stream().fixedDelay(l, unit);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#permutations()
	 */
	@Override
	CollectionX<ReactiveSeq<T>> permutations();
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations(int)
	 */
	@Override
	CollectionX<ReactiveSeq<T>> combinations(int size);
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations()
	 */
	@Override
	CollectionX<ReactiveSeq<T>> combinations();
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> CollectionX<U> cast(Class<U> type) {
		
		return (CollectionX<U>)ZippingApplicativable.super.cast(type);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> CollectionX<R> patternMatch(R defaultValue,
			Function<CheckValues<? super T, R>, CheckValues<? super T, R>> case1) {
		
		return (CollectionX<R>)ZippingApplicativable.super.patternMatch(defaultValue, case1);
	}
	
	
    
    
   
	
}

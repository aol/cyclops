package com.aol.cyclops.collections.extensions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.pcollections.PCollection;

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.matcher.builders.CheckValues;
import com.aol.cyclops.matcher.recursive.Matchable;
import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.SequenceMCollectable;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.trampoline.Trampoline;

//pattern match, for comprehensions
public interface CollectionX<T> extends Iterable<T>, Collection<T>,SequenceMCollectable<T>{
	
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}
	@Override
	default Collectable<T> collectable(){
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
	CollectionX<T> filter(Predicate<? super T> pred);
	<R> CollectionX<R> map(Function<? super T, ? extends R> mapper);
	<R> CollectionX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);
	CollectionX<T> limit(long num);
	CollectionX<T> skip(long num);
	CollectionX<T> takeWhile(Predicate<? super T> p);
	CollectionX<T> dropWhile(Predicate<? super T> p);
	CollectionX<T> takeUntil(Predicate<? super T> p);
	CollectionX<T> dropUntil(Predicate<? super T> p);
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
	 * Attempt to map this Sequence to the same type as the supplied Monoid
	 * (Reducer) Then use Monoid to reduce values
	 * 
	 * <pre>
	 * {@code 
	 * SequenceM.of("hello","2","world","4").mapReduce(Reducers.toCountInt());
	 * 
	 * //4
	 * }
	 * </pre>
	 * 
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	default <R> R mapReduce(Monoid<R> reducer){
		return stream().mapReduce(reducer);
	}

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid, using
	 * supplied function Then use Monoid to reduce values
	 * 
	 * <pre>
	 *  {@code
	 *  SequenceM.of("one","two","three","four")
	 *           .mapReduce(this::toInt,Reducers.toTotalInt());
	 *  
	 *  //10
	 *  
	 *  int toInt(String s){
	 * 		if("one".equals(s))
	 * 			return 1;
	 * 		if("two".equals(s))
	 * 			return 2;
	 * 		if("three".equals(s))
	 * 			return 3;
	 * 		if("four".equals(s))
	 * 			return 4;
	 * 		return -1;
	 * 	   }
	 *  }
	 * </pre>
	 * 
	 * @param mapper
	 *            Function to map Monad type
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	default <R> R mapReduce(Function<? super T, ? extends R> mapper, Monoid<R> reducer){
		return stream().mapReduce(mapper,reducer);
	}
	/**
	 * <pre>
	 * {@code 
	 * SequenceM.of("hello","2","world","4").reduce(Reducers.toString(","));
	 * 
	 * //hello,2,world,4
	 * }
	 * </pre>
	 * 
	 * @param reducer
	 *            Use supplied Monoid to reduce values
	 * @return reduced values
	 */
	default T reduce(Monoid<T> reducer){
		return stream().reduce(reducer);
	}

	/*
	 * <pre> {@code assertThat(SequenceM.of(1,2,3,4,5).map(it -> it*100).reduce(
	 * (acc,next) -> acc+next).get(),equalTo(1500)); } </pre>
	 */
	default Optional<T> reduce(BinaryOperator<T> accumulator){
		return stream().reduce(accumulator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BinaryOperator)
	 */
	default T reduce(T identity, BinaryOperator<T> accumulator){
		return stream().reduce(identity, accumulator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.stream.Stream#reduce(java.lang.Object,
	 * java.util.function.BiFunction, java.util.function.BinaryOperator)
	 */
	default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner){
		return stream().reduce(identity,accumulator,combiner);
	}

	/**
	 * Reduce with multiple reducers in parallel NB if this Monad is an Optional
	 * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
	 * was one value To reduce over the values on the list, called
	 * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Monoid&lt;Integer&gt; sum = Monoid.of(0, (a, b) -&gt; a + b);
	 * 	Monoid&lt;Integer&gt; mult = Monoid.of(1, (a, b) -&gt; a * b);
	 * 	List&lt;Integer&gt; result = SequenceM.of(1, 2, 3, 4).reduce(Arrays.asList(sum, mult).stream());
	 * 
	 * 	assertThat(result, equalTo(Arrays.asList(10, 24)));
	 * 
	 * }
	 * </pre>
	 * 
	 * 
	 * @param reducers
	 * @return
	 */
	default List<T> reduce(Stream<? extends Monoid<T>> reducers){
		return stream().reduce(reducers);
	}

	/**
	 * Reduce with multiple reducers in parallel NB if this Monad is an Optional
	 * [Arrays.asList(1,2,3)] reduce will operate on the Optional as if the list
	 * was one value To reduce over the values on the list, called
	 * streamedMonad() first. I.e. streamedMonad().reduce(reducer)
	 * 
	 * <pre>
	 * {@code 
	 * Monoid<Integer> sum = Monoid.of(0,(a,b)->a+b);
	 * 		Monoid<Integer> mult = Monoid.of(1,(a,b)->a*b);
	 * 		List<Integer> result = SequenceM.of(1,2,3,4))
	 * 										.reduce(Arrays.asList(sum,mult) );
	 * 				
	 * 		 
	 * 		assertThat(result,equalTo(Arrays.asList(10,24)));
	 * 
	 * }
	 * 
	 * @param reducers
	 * @return
	 */
	default List<T> reduce(Iterable<Monoid<T>> reducers){
		return stream().reduce(reducers);
	}

	/**
	 * 
	 * 
	 <pre>
	 * 		{@code
	 * 		SequenceM.of("a","b","c").foldLeft(Reducers.toString(""));
	 *        
	 *         // "abc"
	 *         }
	 * </pre>
	 * 
	 * @param reducer
	 *            Use supplied Monoid to reduce values starting via foldLeft
	 * @return Reduced result
	 */
	default T foldLeft(Monoid<T> reducer){
		return stream().foldLeft(reducer);
	}

	/**
	 * foldLeft : immutable reduction from left to right
	 * 
	 * <pre>
	 * {@code 
	 * 
	 * assertTrue(SequenceM.of("a", "b", "c").foldLeft("", String::concat).equals("abc"));
	 * }
	 * </pre>
	 */
	default T foldLeft(T identity, BinaryOperator<T> accumulator){
		return stream().foldLeft(identity,accumulator);
	}

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using
	 * mapToType on the monoid interface) Then use Monoid to reduce values
	 * 
	 * <pre>
	 * 		{@code
	 * 		SequenceM.of(1,2,3).foldLeftMapToType(Reducers.toString(""));
	 *        
	 *         // "123"
	 *         }
	 * </pre>
	 * 
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	default <T> T foldLeftMapToType(Monoid<T> reducer){
		return stream().foldLeftMapToType(reducer);
	}

	/**
	 * 
	 * <pre>
	 * 		{@code
	 * 		SequenceM.of("a","b","c").foldRight(Reducers.toString(""));
	 *        
	 *         // "cab"
	 *         }
	 * </pre>
	 * 
	 * @param reducer
	 *            Use supplied Monoid to reduce values starting via foldRight
	 * @return Reduced result
	 */
	default T foldRight(Monoid<T> reducer){
		return stream().foldRight(reducer);
	}

	/**
	 * Immutable reduction from right to left
	 * 
	 * <pre>
	 * {@code 
	 *  assertTrue(SequenceM.of("a","b","c").foldRight("", String::concat).equals("cba"));
	 * }
	 * </pre>
	 * 
	 * @param identity
	 * @param accumulator
	 * @return
	 */
	default T foldRight(T identity, BinaryOperator<T> accumulator){
		return stream().foldRight(identity,accumulator);
	}

	/**
	 * Attempt to map this Monad to the same type as the supplied Monoid (using
	 * mapToType on the monoid interface) Then use Monoid to reduce values
	 * 
	 * <pre>
	 * 		{@code
	 * 		SequenceM.of(1,2,3).foldRightMapToType(Reducers.toString(""));
	 *        
	 *         // "321"
	 *         }
	 * </pre>
	 * 
	 * 
	 * @param reducer
	 *            Monoid to reduce values
	 * @return Reduce result
	 */
	default <T> T foldRightMapToType(Monoid<T> reducer){
		return stream().foldRightMapToType(reducer);
	}
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
	default <R1, R2, R> CollectionX<R> forEach3(Function<? super T, Iterable<R1>> stream1,
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
	default <R1, R2, R> SequenceM<R> forEach3(Function<? super T, Iterable<R1>> stream1,
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
	default <R1, R> SequenceM<R> forEach2(Function<? super T, Iterable<R1>> stream1,
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
	default <R1, R> SequenceM<R> forEach2(Function<? super T, Iterable<R1>> stream1, 
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
	Collection<T> slice(long from, long to);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
	 */
	<U extends Comparable<? super U>> Collection<T> sorted(Function<? super T, ? extends U> function);

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
	default SequenceM<T> xPer(int x, long time, TimeUnit t){
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
	default SequenceM<T> onePer(long time, TimeUnit t){
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
	default SequenceM<T> fixedDelay(long l, TimeUnit unit){
		return stream().fixedDelay(l, unit);
	}
	
	 /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     * List<String> result = CollectionX.of(1,2,3,4)
                                              .patternMatch(
                                                        c->c.hasValuesWhere( (Integer i)->i%2==0 ).then(i->"even")
                                                      )
     * }
     * // CollectionX["odd","even","odd","even"]
     * </pre>
     *
     *
     * @param defaultValue Value if supplied case doesn't match
     * @param case1 Function to generate a case (or chain of cases as a single case)
     * @return CollectionX where elements are transformed by pattern matching
     */
    default <R> CollectionX<R> patternMatch(R defaultValue,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> case1){

        return  map(u-> Matchable.of(u).mayMatch(case1).orElse(defaultValue));
    }
    /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     *
     * List<String> result = CollectionX.of(-2,01,2,3,4)
     *                                        .filter(i->i>0)
                                              .patternMatch("many",
                                                        c->c.hasValuesWhere( (Integer i)->i==1 ).then(i->"one"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"two")
                                                      );
         // CollectionX["one","two","many","many"]
     * }
     *
     * </pre>
     *
     * @param defaultValue Value if supplied cases don't match
     * @param case1 Function to generate a case (or chain of cases as a single case)
     * @param case2 Function to generate a case (or chain of cases as a single case)
     * @return  CollectionX where elements are transformed by pattern matching
     */
    default <R> CollectionX<R> patternMatch(R defaultValue,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> case1
                            ,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> case2){
        return map(u-> Matchable.of(u).mayMatch(case1,case2).orElse(defaultValue));
    }
    /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     *
     * List<String> result = CollectionX.of(-2,01,2,3,4)
     *                                        .filter(i->i>0)
                                              .patternMatch("many",
                                                        c->c.hasValuesWhere( (Integer i)->i==1 ).then(i->"one"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"two"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"three")
                                                      )
                                                 .map(opt -> opt.orElse("many"));
     * }
     * // CollectionX["one","two","three","many"]
     * </pre>
     * @param defaultValue Value if supplied cases don't match
     * @param fn1 Function to generate a case (or chain of cases as a single case)
     * @param fn2 Function to generate a case (or chain of cases as a single case)
     * @param fn3 Function to generate a case (or chain of cases as a single case)
     * @return CollectionX where elements are transformed by pattern matching
     */
    default <R> CollectionX<R> patternMatch(R defaultValue,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1,
                                                    Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2,
                                                    Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3){

        return map(u-> Matchable.of(u).mayMatch(fn1,fn2,fn3).orElse(defaultValue));
    }
    /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     * List<String> result = CollectionX.of(-2,01,2,3,4,5)
     *                                        .filter(i->i>0)
                                              .patternMatch("many",
                                                        c->c.hasValuesWhere( (Integer i)->i==1 ).then(i->"one"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"two"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"three"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"four")
                                                      )
     * }
     * // CollectionX["one","two","three","four","many"]
     * </pre>
     * @param defaultValue Value if supplied cases don't match
     * @param fn1  Function to generate a case (or chain of cases as a single case)
     * @param fn2  Function to generate a case (or chain of cases as a single case)
     * @param fn3  Function to generate a case (or chain of cases as a single case)
     * @param fn4  Function to generate a case (or chain of cases as a single case)
     * @return  CollectionX where elements are transformed by pattern matching
     */
    default <R> CollectionX<R> patternMatch(R defaultValue,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1, Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2,
                            Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn4){

        return map(u-> Matchable.of(u).mayMatch(fn1,fn2,fn3,fn4).orElse(defaultValue));
    }
    /**
     * Transform the elements of this Stream with a Pattern Matching case and default value
     *
     * <pre>
     * {@code
     * List<String> result = CollectionX.of(-2,01,2,3,4,5,6)
     *                                        .filter(i->i>0)
                                              .patternMatch("many",
                                                        c->c.hasValuesWhere( (Integer i)->i==1 ).then(i->"one"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"two"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"three"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"four"),
                                                        c->c.hasValuesWhere( (Integer i)->i==2 ).then(i->"five")
                                                      )
                                             .map(opt -> opt.orElse("many"));
     * }
     * // CollectionX["one","two","three","four","five","many"]
     * </pre>
     * @param defaultValue Value if supplied cases don't match
     * @param fn1 Function to generate a case (or chain of cases as a single case)
     * @param fn2 Function to generate a case (or chain of cases as a single case)
     * @param fn3 Function to generate a case (or chain of cases as a single case)
     * @param fn4 Function to generate a case (or chain of cases as a single case)
     * @param fn5 Function to generate a case (or chain of cases as a single case)
     * @return CollectionX where elements are transformed by pattern matching
     */
    default <R> CollectionX<R> patternMatch(R defaultValue,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn1, Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn2,
            Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn3,Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn4,
                            Function<CheckValues<? super T,R>,CheckValues<? super T,R>> fn5){

        return map(u-> Matchable.of(u).mayMatch(fn1,fn2,fn3,fn4,fn5).orElse(defaultValue));
    }

   
	
}

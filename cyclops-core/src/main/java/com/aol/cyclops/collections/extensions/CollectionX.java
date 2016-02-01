package com.aol.cyclops.collections.extensions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.PCollection;

import com.aol.cyclops.collections.extensions.persistent.PStackX;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.lambda.monads.Foldable;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.matcher.builders.CheckValues;
import com.aol.cyclops.matcher.recursive.Matchable;
import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.HotStream;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.SequenceMCollectable;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.trampoline.Trampoline;

//pattern match, for comprehensions
public interface CollectionX<T> extends Iterable<T>,Functor<T>, Foldable<T>,Collection<T>,SequenceMCollectable<T>{
	
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
	default <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
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
		forEach(c);
		return this;
	}
	CollectionX<ListX<T>> grouped(int groupSize);
	<K, A, D> CollectionX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);
	<K> CollectionX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier);
	<U> CollectionX<Tuple2<T, U>> zip(Iterable<U> other);
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
	 * Emit data from this Collection on a schedule
	 * 
	 * <pre>
	 * {@code
	 *  //run at 8PM every night
	 *  SequenceM.generate(()->"next job:"+formatDate(new Date()))
	 *            .map(this::processJob)
	 *            .schedule("0 20 * * *",Executors.newScheduledThreadPool(1));
	 * }
	 * </pre>
	 * 
	 * Connect to the Scheduled Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	HotStream&lt;Data&gt; dataStream = SequenceeM.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
	 * 			.schedule(&quot;0 20 * * *&quot;, Executors.newScheduledThreadPool(1));
	 * 
	 * 	data.connect().forEach(this::logToDB);
	 * }
	 * </pre>
	 * 
	 * 
	 * 
	 * @param cron
	 *            Expression that determines when each job will run
	 * @param ex
	 *            ScheduledExecutorService
	 * @return Connectable HotStream of output from scheduled Stream
	 */
	default HotStream<T> schedule(String cron, ScheduledExecutorService ex){
		return stream().schedule(cron, ex);
	}

	/**
	 * Emit data from this Collection on a schedule a schedule
	 * 
	 * <pre>
	 * {@code
	 *  //run every 60 seconds after last job completes
	 *  SequenceeM.generate(()->"next job:"+formatDate(new Date()))
	 *            .map(this::processJob)
	 *            .scheduleFixedDelay(60_000,Executors.newScheduledThreadPool(1));
	 * }
	 * </pre>
	 * 
	 * Connect to the Scheduled Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	HotStream&lt;Data&gt; dataStream = SequenceeM.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
	 * 			.scheduleFixedDelay(60_000, Executors.newScheduledThreadPool(1));
	 * 
	 * 	data.connect().forEach(this::logToDB);
	 * }
	 * </pre>
	 * 
	 * 
	 * @param delay
	 *            Between last element completes passing through the Stream
	 *            until the next one starts
	 * @param ex
	 *            ScheduledExecutorService
	 * @return Connectable HotStream of output from scheduled Stream
	 */
	default HotStream<T> scheduleFixedDelay(long delay, ScheduledExecutorService ex){
		return stream().scheduleFixedDelay(delay,ex);
	}

	/**
	 * Emit data from this Collection on a schedule
	 * 
	 * <pre>
	 * {@code
	 *  //run every 60 seconds
	 *  SequenceeM.generate(()->"next job:"+formatDate(new Date()))
	 *            .map(this::processJob)
	 *            .scheduleFixedRate(60_000,Executors.newScheduledThreadPool(1));
	 * }
	 * </pre>
	 * 
	 * Connect to the Scheduled Stream
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	HotStream&lt;Data&gt; dataStream = SequenceeM.generate(() -&gt; &quot;next job:&quot; + formatDate(new Date())).map(this::processJob)
	 * 			.scheduleFixedRate(60_000, Executors.newScheduledThreadPool(1));
	 * 
	 * 	data.connect().forEach(this::logToDB);
	 * }
	 * </pre>
	 * 
	 * @param rate
	 *            Time in millis between job runs
	 * @param ex
	 *            ScheduledExecutorService
	 * @return Connectable HotStream of output from scheduled Stream
	 */
	default HotStream<T> scheduleFixedRate(long rate, ScheduledExecutorService ex){
		return stream().scheduleFixedRate(rate,ex);
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

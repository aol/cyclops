package com.aol.cyclops.collections.extensions;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.SequenceMCollectable;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.trampoline.Trampoline;
//foldLeft, foldRight, reverse
//pattern match, for comprehensions
public interface CollectionX<T> extends Iterable<T>, Collection<T>,SequenceMCollectable<T>{
	
	SequenceM<T> stream();
	<T> Monoid<Collection<T>> monoid();
	<T1> CollectionX<T1> from(Collection<T1> c);
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
	default CollectionX<T> filter(Predicate<? super T> pred){
		return from(this.<T>monoid().mapReduce(stream().filter(pred)));
	}
	default <R> CollectionX<R> map(Function<? super T, ? extends R> mapper){
		return from(this.<R>monoid().mapReduce(stream().map(mapper)));
	}
	default <R> CollectionX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper){
		return from(this.<R>monoid().mapReduce(stream().flatMap(mapper)));
	}
	default CollectionX<T> limit(long num){
		return from(this.<T>monoid().mapReduce(stream().limit(num)));
	}
	default CollectionX<T> skip(long num){
		return from(this.<T>monoid().mapReduce(stream().skip(num)));
	}
	default CollectionX<T> takeWhile(Predicate<? super T> p){
		return from(this.<T>monoid().mapReduce(stream().limitWhile(p)));
	}
	default CollectionX<T> dropWhile(Predicate<? super T> p){
		return from(this.<T>monoid().mapReduce(stream().skipWhile(p)));
	}
	default CollectionX<T> takeUntil(Predicate<? super T> p){
		return from(this.<T>monoid().mapReduce(stream().limitUntil(p)));
	}
	default CollectionX<T> dropUntil(Predicate<? super T> p){
		return from(this.<T>monoid().mapReduce(stream().skipUntil(p)));
	}
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
	default <R> CollectionX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper){
		
		 return  from(this.<R>monoid().mapReduce(stream().trampoline(mapper)));	 
	}

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
}

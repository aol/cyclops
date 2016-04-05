package com.aol.cyclops.control.monads.transformers;


import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.monads.transformers.seq.StreamTSeq;
import com.aol.cyclops.control.monads.transformers.values.StreamTValue;


/**
 * Monad Transformer for Cyclops Streams
 * 
 * StreamT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Stream
 * 
 * StreamT<AnyM<*SOME_MONAD_TYPE*<Stream<T>>>>
 * 
 * StreamT allows the deeply wrapped Stream to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public interface StreamT<T> {
  
   
   public <B> StreamT<B> flatMap(Function<? super T, ? extends Stream<? extends B>> f);
   /**
	 * @return The wrapped AnyM
	 */
   public AnyM<Stream<T>> unwrap();
   /**
  	 * Peek at the current value of the Stream
  	 * <pre>
  	 * {@code 
  	 *    StreamT.of(AnyM.fromStream(Arrays.asStream(10))
  	 *             .peek(System.out::println);
  	 *             
  	 *     //prints 10        
  	 * }
  	 * </pre>
  	 * 
  	 * @param peek  Consumer to accept current value of Stream
  	 * @return StreamT with peek call
  	 */
   public StreamT<T> peek(Consumer<? super T> peek);
   /**
 	 * Filter the wrapped Stream
 	 * <pre>
 	 * {@code 
 	 *    StreamT.of(AnyM.fromStream(Arrays.asStream(10,11))
 	 *             .filter(t->t!=10);
 	 *             
 	 *     //StreamT<AnyM<Stream<Stream[11]>>>
 	 * }
 	 * </pre>
 	 * @param test Predicate to filter the wrapped Stream
 	 * @return StreamT that applies the provided filter
 	 */
   public StreamT<T> filter(Predicate<? super T> test);
   /**
	 * Map the wrapped Stream
	 * 
	 * <pre>
	 * {@code 
	 *  StreamT.of(AnyM.fromStream(Arrays.asStream(10))
	 *             .map(t->t=t+1);
	 *  
	 *  
	 *  //StreamT<AnyM<Stream<Stream[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped Stream
	 * @return StreamT that applies the map function to the wrapped Stream
	 */
   public <B> StreamT<B> map(Function<? super T,? extends B> f);
   /**
	 * Flat Map the wrapped Stream
	  * <pre>
	 * {@code 
	 *  StreamT.of(AnyM.fromStream(Arrays.asStream(10))
	 *             .flatMap(t->Stream.empty();
	 *  
	 *  
	 *  //StreamT<AnyM<Stream<Stream.empty>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return StreamT that applies the flatMap function to the wrapped Stream
	 */
   default <B> StreamT<B> bind(Function<? super T,StreamT<? extends B>> f){
	   return of(unwrap().map(stream-> stream.flatMap(a-> f.apply(a).unwrap().stream())
			   							.<B>flatMap(a->a)));
   }
   /**
 	 * Lift a function into one that accepts and returns an StreamT
 	 * This allows multiple monad types to add functionality to existing functions and methods
 	 * 
 	 * e.g. to add iteration handling (via Stream) and nullhandling (via Optional) to an existing function
 	 * <pre>
 	 * {@code 
 		Function<Integer,Integer> add2 = i -> i+2;
		Function<StreamT<Integer>, StreamT<Integer>> optTAdd2 = StreamT.lift(add2);
		
		Stream<Integer> nums = Stream.of(1,2);
		AnyM<Stream<Integer>> stream = AnyM.fromOptional(Optional.of(nums));
		
		List<Integer> results = optTAdd2.apply(StreamT.of(stream))
										.unwrap()
										.<Optional<Stream<Integer>>>unwrap()
										.get()
										.collect(Collectors.toList());
 		//Stream.of(3,4);
 	 * 
 	 * 
 	 * }</pre>
 	 * 
 	 * 
 	 * @param fn Function to enhance with functionality from Stream and another monad type
 	 * @return Function that accepts and returns an StreamT
 	 */
   public static <U, R> Function<StreamT<U>, StreamT<R>> lift(Function<? super U,? extends R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}
   /**
	 * Construct an StreamT from an AnyM that contains a monad type that contains type other than Stream
	 * The values in the underlying monad will be mapped to Stream<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Stream
	 * @return StreamT
	 */
   public static <A> StreamT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(Stream::of));
   }
   /**
	 * Create a StreamT from an AnyM that wraps a monad containing a Stream
	 * 
	 * @param monads
	 * @return
	 */
   public static <A> StreamT<A> of(AnyM<Stream<A>> monads){
       return Matchables.anyM(monads).visit(v-> StreamTValue.of(v), s->StreamTSeq.of(s));
   }
   
   
   
}
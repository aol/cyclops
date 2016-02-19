package com.aol.cyclops.control.monads.transformers;


import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.control.AnyM;

/**
 * Monad Transformer for Java  CompletableFutures
 * 
 * CompletableFutureT consists of an AnyM instance that in turns wraps anoter Monad type that contains an CompletableFuture
 * 
 * CompletableFutureT<AnyM<*SOME_MONAD_TYPE*<CompletableFuture<T>>>>
 * 
 * CompletableFutureT allows the deeply wrapped CompletableFuture to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public class CompletableFutureT<A> {
   
   private final AnyM<CompletableFuture<A>> run;
   /**
	 * @return The wrapped AnyM
	 */
   public AnyM<CompletableFuture<A>> unwrap(){
	   return run;
   }
   private CompletableFutureT(final AnyM<CompletableFuture<A>> run){
       this.run = run;
   }
   /**
	 * Peek at the current value of the CompletableFuture
	 * <pre>
	 * {@code 
	 *    CompletableFutureT.of(AnyM.fromStream(Arrays.asCompletableFuture(10))
	 *             .peek(System.out::println);
	 *             
	 *     //prints 10        
	 * }
	 * </pre>
	 * 
	 * @param peek  Consumer to accept current value of CompletableFuture
	 * @return CompletableFutureT with peek call
	 */
   public CompletableFutureT<A> peek(Consumer<? super A> peek){
	  
       return of(run.peek(future-> future.thenApply(a->{peek.accept(a); return a;})));
   }
   /**
	 * Map the wrapped CompletableFuture
	 * 
	 * <pre>
	 * {@code 
	 *  CompletableFutureT.of(AnyM.fromStream(Arrays.asCompletableFuture(10))
	 *             .map(t->t=t+1);
	 *  
	 *  
	 *  //CompletableFutureT<AnyM<Stream<CompletableFuture[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped CompletableFuture
	 * @return CompletableFutureT that applies the map function to the wrapped CompletableFuture
	 */   
   public <B> CompletableFutureT<B> map(Function<? super A,? extends B> f){
       return new CompletableFutureT<B>(run.map(o-> o.thenApply(f)));
   }
   /**
	 * Flat Map the wrapped CompletableFuture
	  * <pre>
	 * {@code 
	 *  CompletableFutureT.of(AnyM.fromStream(Arrays.asCompletableFuture(10))
	 *             .flatMap(t->CompletableFuture.completedFuture(20));
	 *  
	 *  
	 *  //CompletableFutureT<AnyM<Stream<CompletableFuture[20]>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return CompletableFutureT that applies the flatMap function to the wrapped CompletableFuture
	 */

   public <B> CompletableFutureT<B> flatMap(Function<? super A,CompletableFutureT<B>> f){
	   return of(run.map(future-> future.thenCompose(a-> f.apply(a).run.asSequence().toList().get(0))));
   }
   /**
	 * Lift a function into one that accepts and returns an CompletableFutureT
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add list handling  / iteration (via CompletableFuture) and iteration (via Stream) to an existing function
	 * <pre>
	 * {@code 
	    Function<Integer,Integer> add2 = i -> i+2;
		Function<CompletableFutureT<Integer>, CompletableFutureT<Integer>> optTAdd2 = CompletableFutureT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyM<Integer> stream = AnyM.fromStream(withNulls);
		AnyM<CompletableFuture<Integer>> streamOpt = stream.map(CompletableFuture::completedFuture);
		List<Integer> results = optTAdd2.apply(CompletableFutureT.of(streamOpt))
										.unwrap()
										.<Stream<CompletableFuture<Integer>>>unwrap()
										.map(CompletableFuture::join)
										.collect(Collectors.toList());
		
		
		//CompletableFuture.completedFuture(List[3,4]);
	 * 
	 * 
	 * }</pre>
	 * 
	 * 
	 * @param fn Function to enhance with functionality from CompletableFuture and another monad type
	 * @return Function that accepts and returns an CompletableFutureT
	 */   
   public static <U, R> Function<CompletableFutureT<U>, CompletableFutureT<R>> lift(Function<? super U,? extends R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}
   /**
  	 * Lift a BiFunction into one that accepts and returns  CompletableFutureTs
  	 * This allows multiple monad types to add functionality to existing functions and methods
  	 * 
  	 * e.g. to add list handling / iteration (via CompletableFuture), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
  	 * to an existing function
  	 * 
  	 * <pre>
  	 * {@code 
  		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<CompletableFutureT<Integer>,CompletableFutureT<Integer>,CompletableFutureT<Integer>> optTAdd2 = CompletableFutureT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyM<Integer> stream = AnyM.ofMonad(withNulls);
		AnyM<CompletableFuture<Integer>> streamOpt = stream.map(CompletableFuture::completedFuture);
		
		CompletableFuture<CompletableFuture<Integer>> two = CompletableFuture.completedFuture(CompletableFuture.completedFuture(2));
		AnyM<CompletableFuture<Integer>> future=  AnyM.fromCompletableFuture(two);
		List<Integer> results = optTAdd2.apply(CompletableFutureT.of(streamOpt),CompletableFutureT.of(future))
										.unwrap()
										.<Stream<CompletableFuture<Integer>>>unwrap()
										.map(CompletableFuture::join)
										.collect(Collectors.toList());
										
  			//CompletableFuture.completedFuture(List[3,4,5]);						
  	  }
  	  </pre>
  	 * @param fn BiFunction to enhance with functionality from CompletableFuture and another monad type
  	 * @return Function that accepts and returns an CompletableFutureT
  	 */
	public static <U1, U2, R> BiFunction<CompletableFutureT<U1>, CompletableFutureT<U2>, CompletableFutureT<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMap(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an CompletableFutureT from an AnyM that contains a monad type that contains type other than CompletableFuture
	 * The values in the underlying monad will be mapped to CompletableFuture<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an CompletableFuture
	 * @return CompletableFutureT
	 */
   public static <A> CompletableFutureT<A> fromAnyM(AnyM<A> anyM){
	   return of(anyM.map(CompletableFuture::completedFuture));
   }
   /**
	 * Construct an CompletableFutureT from an AnyM that wraps a monad containing  CompletableFutures
	 * 
	 * @param monads AnyM that contains a monad wrapping an CompletableFuture
	 * @return CompletableFutureT
	 */   
   public static <A> CompletableFutureT<A> of(AnyM<CompletableFuture<A>> monads){
	   return new CompletableFutureT<>(monads);
   }
   
   
   /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return run.toString();
	}
 
}
package com.aol.cyclops.control.monads.transformers;


import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.monads.transformers.seq.CompletableFutureTSeq;
import com.aol.cyclops.control.monads.transformers.values.CompletableFutureTValue;
import com.aol.cyclops.control.monads.transformers.values.OptionalTValue;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Unit;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;

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
public interface CompletableFutureT<A> extends Unit<A>, Publisher<A>{
   
    public <R> CompletableFutureT<R> empty();
    MaybeT<A> filter(Predicate<? super A> test);
   default <B> CompletableFutureT<B> bind(Function<? super A, CompletableFutureT<? extends B>> f) {
        return of(unwrap().bind(opt -> {
                return f.apply(opt.join()).unwrap().unwrap();
        }));
    }
   
   /**
    * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
    * 
    * @param monads
    *            AnyM that contains a monad wrapping an Maybe
    * @return MaybeT
    */
   public static <A> CompletableFutureT<A> of(AnyM<CompletableFuture<A>> monads) {
     
       return Matchables.anyM(monads).visit(v-> CompletableFutureTValue.of(v), s->CompletableFutureTSeq.of(s));
       
   }
   
   /**
	 * @return The wrapped AnyM
	 */
   public AnyM<CompletableFuture<A>> unwrap();
  
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
   public CompletableFutureT<A> peek(Consumer<? super A> peek);
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
   public <B> CompletableFutureT<B> map(Function<? super A,? extends B> f);
   

   public <B> CompletableFutureT<B> flatMap(Function<? super A,? extends MonadicValue<? extends B>> f);
   
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
		return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

    public static <A> CompletableFutureT<A> fromAnyM(AnyM<A> anyM) {
        return of(anyM.map(CompletableFuture::completedFuture));
    }

    public static <A> CompletableFutureTValue<A> fromAnyMValue(AnyMValue<A> anyM) {
        return CompletableFutureTValue.fromAnyM(anyM);
    }

    public static <A> CompletableFutureTSeq<A> fromAnyMSeq(AnyMSeq<A> anyM) {
        return CompletableFutureTSeq.fromAnyM(anyM);
    }

    public static <A> CompletableFutureTSeq<A> fromIterable(
            Iterable<CompletableFuture<A>> iterableOfCompletableFutures) {
        return CompletableFutureTSeq.of(AnyM.fromIterable(iterableOfCompletableFutures));
    }

    public static <A> CompletableFutureTSeq<A> fromStream(Stream<CompletableFuture<A>> streamOfCompletableFutures) {
        return CompletableFutureTSeq.of(AnyM.fromStream(streamOfCompletableFutures));
    }

    public static <A> CompletableFutureTSeq<A> fromPublisher(
            Publisher<CompletableFuture<A>> publisherOfCompletableFutures) {
        return CompletableFutureTSeq.of(AnyM.fromPublisher(publisherOfCompletableFutures));
    }

    public static <A, V extends MonadicValue<CompletableFuture<A>>> CompletableFutureTValue<A> fromValue(
            V monadicValue) {
        return CompletableFutureTValue.fromValue(monadicValue);
    }

    public static <A> CompletableFutureTValue<A> fromOptional(Optional<CompletableFuture<A>> optional) {
        return CompletableFutureTValue.of(AnyM.fromOptional(optional));
    }

    public static <A> CompletableFutureTValue<A> fromFuture(CompletableFuture<CompletableFuture<A>> future) {
        return CompletableFutureTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A> CompletableFutureTValue<A> fromIterableValue(
            Iterable<CompletableFuture<A>> iterableOfCompletableFutures) {
        return CompletableFutureTValue.of(AnyM.fromIterableValue(iterableOfCompletableFutures));
    }
    public static <T> CompletableFutureTValue<T> emptyMaybe(){
        return CompletableFutureTValue.emptyOptional();
    }

}
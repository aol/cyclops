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
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.monads.transformers.seq.FutureWTSeq;
import com.aol.cyclops.control.monads.transformers.values.CompletableFutureTValue;
import com.aol.cyclops.control.monads.transformers.values.FutureWTValue;
import com.aol.cyclops.control.monads.transformers.values.MaybeTValue;
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
public interface FutureWT<A> extends Unit<A>, Publisher<A>{
    
    MaybeT<A> filter(Predicate<? super A> test);
    
    public <R> FutureWT<R> empty();
    
   default <B> FutureWT<B> bind(Function<? super A, FutureWT<? extends B>> f) {
        return of(unwrap().bind(opt -> {
                return f.apply(opt.get()).unwrap().unwrap();
        }));
    }
   
   /**
    * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
    * 
    * @param monads
    *            AnyM that contains a monad wrapping an Maybe
    * @return MaybeT
    */
   public static <A> FutureWT<A> of(AnyM<FutureW<A>> monads) {
     
       return Matchables.anyM(monads).visit(v-> FutureWTValue.of(v), s->FutureWTSeq.of(s));
       
   }
   
   /**
	 * @return The wrapped AnyM
	 */
   public AnyM<FutureW<A>> unwrap();
  
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
   public FutureWT<A> peek(Consumer<? super A> peek);
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
   public <B> FutureWT<B> map(Function<? super A,? extends B> f);
   

   public <B> FutureWT<B> flatMap(Function<? super A,? extends MonadicValue<? extends B>> f);
   
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
   public static <U, R> Function<FutureWT<U>, FutureWT<R>> lift(Function<? super U,? extends R> fn) {
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
	public static <U1, U2, R> BiFunction<FutureWT<U1>, FutureWT<U2>, FutureWT<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

    public static <A> FutureWT<A> fromAnyM(AnyM<A> anyM) {
        return of(anyM.map(FutureW::ofResult));
    }

    public static <A> FutureWTValue<A> fromAnyMValue(AnyMValue<A> anyM) {
        return FutureWTValue.fromAnyM(anyM);
    }

    public static <A> FutureWTSeq<A> fromAnyMSeq(AnyMSeq<A> anyM) {
        return FutureWTSeq.fromAnyM(anyM);
    }

    public static <A> FutureWTSeq<A> fromIterable(
            Iterable<FutureW<A>> iterableOfCompletableFutures) {
        return FutureWTSeq.of(AnyM.fromIterable(iterableOfCompletableFutures));
    }

    public static <A> FutureWTSeq<A> fromStream(Stream<FutureW<A>> streamOfCompletableFutures) {
        return FutureWTSeq.of(AnyM.fromStream(streamOfCompletableFutures));
    }

    public static <A> FutureWTSeq<A> fromPublisher(
            Publisher<FutureW<A>> publisherOfCompletableFutures) {
        return FutureWTSeq.of(AnyM.fromPublisher(publisherOfCompletableFutures));
    }

    public static <A, V extends MonadicValue<FutureW<A>>> FutureWTValue<A> fromValue(
            V monadicValue) {
        return FutureWTValue.fromValue(monadicValue);
    }

    public static <A> FutureWTValue<A> fromOptional(Optional<FutureW<A>> optional) {
        return FutureWTValue.of(AnyM.fromOptional(optional));
    }

    public static <A> FutureWTValue<A> fromFuture(CompletableFuture<FutureW<A>> future) {
        return FutureWTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A> FutureWTValue<A> fromIterableValue(
            Iterable<FutureW<A>> iterableOfCompletableFutures) {
        return FutureWTValue.of(AnyM.fromIterableValue(iterableOfCompletableFutures));
    }

}
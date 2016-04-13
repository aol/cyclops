package com.aol.cyclops.control.monads.transformers.seq;


import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.lambda.Collectable;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.control.monads.transformers.values.TransformerSeq;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad Transformer for Java  FutureWs
 * 
 * FutureWT consists of an AnyM instance that in turns wraps anoter Monad type that contains an FutureW
 * 
 * FutureWT<AnyMSeq<*SOME_MONAD_TYPE*<FutureW<T>>>>
 * 
 * FutureWT allows the deeply wrapped FutureW to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public class FutureWTSeq<A> implements FutureWT<A>, 
                                        Traversable<A>,
                                        Foldable<A>,
                                        ConvertableSequence<A>,
                                        CyclopsCollectable<A>,
                                        Sequential<A>{
                                                
   
   private final AnyMSeq<FutureW<A>> run;
   /**
	 * @return The wrapped AnyM
	 */
   public AnyMSeq<FutureW<A>> unwrap(){
	   return run;
   }
   private FutureWTSeq(final AnyMSeq<FutureW<A>> run){
       this.run = run;
   }
   
   /**
    * Filter the wrapped Maybe
    * 
    * <pre>
    * {@code 
    *    MaybeT.of(AnyM.fromStream(Maybe.of(10))
    *             .filter(t->t!=10);
    *             
    *     //MaybeT<AnyMSeq<Stream<Maybe.empty>>>
    * }
    * </pre>
    * 
    * @param test
    *            Predicate to filter the wrapped Maybe
    * @return MaybeT that applies the provided filter
    */
   public MaybeTSeq<A> filter(Predicate<? super A> test) {
       return MaybeTSeq.of(run.map(opt -> opt.filter(test)));
   }
   /**
	 * Peek at the current value of the FutureW
	 * <pre>
	 * {@code 
	 *    FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
	 *             .peek(System.out::println);
	 *             
	 *     //prints 10        
	 * }
	 * </pre>
	 * 
	 * @param peek  Consumer to accept current value of FutureW
	 * @return FutureWT with peek call
	 */
   public FutureWTSeq<A> peek(Consumer<? super A> peek){
	  return of(run.peek(future-> future.map(a->{peek.accept(a); return a;})));
   }
   /**
	 * Map the wrapped FutureW
	 * 
	 * <pre>
	 * {@code 
	 *  FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
	 *             .map(t->t=t+1);
	 *  
	 *  
	 *  //FutureWT<AnyMSeq<Stream<FutureW[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped FutureW
	 * @return FutureWT that applies the map function to the wrapped FutureW
	 */   
   public <B> FutureWTSeq<B> map(Function<? super A,? extends B> f){
       return new FutureWTSeq<B>(run.map(o-> o.map(f)));
   }
   /**
	 * Flat Map the wrapped FutureW
	  * <pre>
	 * {@code 
	 *  FutureWT.of(AnyM.fromStream(Arrays.asFutureW(10))
	 *             .flatMap(t->FutureW.completedFuture(20));
	 *  
	 *  
	 *  //FutureWT<AnyMSeq<Stream<FutureW[20]>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return FutureWT that applies the flatMap function to the wrapped FutureW
	 */

   public <B> FutureWTSeq<B> flatMapT(Function<? super A,FutureWTSeq<B>> f){
	   return of(run.map(future-> future.flatMap(a-> f.apply(a).run.stream().toList().get(0))));
   }
   private static  <B> AnyMSeq<FutureW<B>> narrow(AnyMSeq<FutureW<? extends B>> run){
       return (AnyMSeq)run;
   }
   public <B> FutureWTSeq<B> flatMap(Function<? super A,? extends MonadicValue<? extends B>> f){
      
       AnyMSeq<FutureW<? extends B>> mapped=  run.map(o -> o.flatMap(f));
       return of(narrow(mapped));
     
   }
   /**
	 * Lift a function into one that accepts and returns an FutureWT
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add list handling  / iteration (via FutureW) and iteration (via Stream) to an existing function
	 * <pre>
	 * {@code 
	    Function<Integer,Integer> add2 = i -> i+2;
		Function<FutureWT<Integer>, FutureWT<Integer>> optTAdd2 = FutureWT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyMSeq<Integer> stream = AnyM.fromStream(withNulls);
		AnyMSeq<FutureW<Integer>> streamOpt = stream.map(FutureW::completedFuture);
		List<Integer> results = optTAdd2.apply(FutureWT.of(streamOpt))
										.unwrap()
										.<Stream<FutureW<Integer>>>unwrap()
										.map(FutureW::join)
										.collect(Collectors.toList());
		
		
		//FutureW.completedFuture(List[3,4]);
	 * 
	 * 
	 * }</pre>
	 * 
	 * 
	 * @param fn Function to enhance with functionality from FutureW and another monad type
	 * @return Function that accepts and returns an FutureWT
	 */   
   public static <U, R> Function<FutureWTSeq<U>, FutureWTSeq<R>> lift(Function<? super U,? extends R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}
   /**
  	 * Lift a BiFunction into one that accepts and returns  FutureWTs
  	 * This allows multiple monad types to add functionality to existing functions and methods
  	 * 
  	 * e.g. to add list handling / iteration (via FutureW), iteration (via Stream)  and asynchronous execution (FutureW) 
  	 * to an existing function
  	 * 
  	 * <pre>
  	 * {@code 
  		BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<FutureWT<Integer>,FutureWT<Integer>,FutureWT<Integer>> optTAdd2 = FutureWT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,3);
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<FutureW<Integer>> streamOpt = stream.map(FutureW::completedFuture);
		
		FutureW<FutureW<Integer>> two = FutureW.completedFuture(FutureW.completedFuture(2));
		AnyMSeq<FutureW<Integer>> future=  AnyM.fromFutureW(two);
		List<Integer> results = optTAdd2.apply(FutureWT.of(streamOpt),FutureWT.of(future))
										.unwrap()
										.<Stream<FutureW<Integer>>>unwrap()
										.map(FutureW::join)
										.collect(Collectors.toList());
										
  			//FutureW.completedFuture(List[3,4,5]);						
  	  }
  	  </pre>
  	 * @param fn BiFunction to enhance with functionality from FutureW and another monad type
  	 * @return Function that accepts and returns an FutureWT
  	 */
	public static <U1, U2, R> BiFunction<FutureWTSeq<U1>, FutureWTSeq<U2>, FutureWTSeq<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an FutureWT from an AnyM that contains a monad type that contains type other than FutureW
	 * The values in the underlying monad will be mapped to FutureW<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an FutureW
	 * @return FutureWT
	 */
   public static <A> FutureWTSeq<A> fromAnyM(AnyMSeq<A> anyM){
	   return of(anyM.map(FutureW::ofResult));
   }
   /**
	 * Construct an FutureWT from an AnyM that wraps a monad containing  FutureWs
	 * 
	 * @param monads AnyM that contains a monad wrapping an FutureW
	 * @return FutureWT
	 */   
   public static <A> FutureWTSeq<A> of(AnyMSeq<FutureW<A>> monads){
	   return new FutureWTSeq<>(monads);
   }
   
   
   
   
   /*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return run.toString();
	}
	
    
    
    @Override
    public ReactiveSeq<A> stream() {
        return run.stream().map(cf->cf.get());
    }

    @Override
    public Iterator<A> iterator() {
       return stream().iterator();
    }

    @Override
    public void subscribe(Subscriber<? super A> s) {
        run.forEachEvent(e->e.subscribe(s),e->s.onError(e),()->s.onComplete());
       
        
    }

    
    public <R> FutureWTSeq<R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->FutureW.ofResult(i)));
    }
  
    public <R> FutureWTSeq<R> unit(R value){
       return of(run.unit(FutureW.ofResult(value)));
    }
    public <R> FutureWTSeq<R> empty(){
        return of(run.unit(FutureW.empty()));
     }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<A> collectable() {
        return stream();
    }
 
 
}
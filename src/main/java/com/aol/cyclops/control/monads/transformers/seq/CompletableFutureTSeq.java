package com.aol.cyclops.control.monads.transformers.seq;


import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jooq.lambda.Collectable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.control.monads.transformers.values.TransformerSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.ExtendedTraversable;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.FilterableFunctor;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.IterableCollectable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.applicative.Applicativable;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;

import lombok.val;

/**
 * Monad Transformer for Java  CompletableFutures
 * 
 * CompletableFutureT consists of an AnyM instance that in turns wraps anoter Monad type that contains an CompletableFuture
 * 
 * CompletableFutureT<AnyMSeq<*SOME_MONAD_TYPE*<CompletableFuture<T>>>>
 * 
 * CompletableFutureT allows the deeply wrapped CompletableFuture to be manipulating within it's nested /contained context
 * @author johnmcclean
 *
 * @param <T>
 */
public class CompletableFutureTSeq<A> implements CompletableFutureT<A>, 
                                                 TransformerSeq<A>{
                                                
   
   private final AnyMSeq<CompletableFuture<A>> run;
   /**
	 * @return The wrapped AnyM
	 */
   public AnyMSeq<CompletableFuture<A>> unwrap(){
	   return run;
   }
   private CompletableFutureTSeq(final AnyMSeq<CompletableFuture<A>> run){
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
       return MaybeTSeq.of(run.map(opt -> FutureW.of(opt).filter(test)));
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
   public CompletableFutureTSeq<A> peek(Consumer<? super A> peek){
	  
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
	 *  //CompletableFutureT<AnyMSeq<Stream<CompletableFuture[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped CompletableFuture
	 * @return CompletableFutureT that applies the map function to the wrapped CompletableFuture
	 */   
   public <B> CompletableFutureTSeq<B> map(Function<? super A,? extends B> f){
       return new CompletableFutureTSeq<B>(run.map(o-> o.thenApply(f)));
   }
   /**
	 * Flat Map the wrapped CompletableFuture
	  * <pre>
	 * {@code 
	 *  CompletableFutureT.of(AnyM.fromStream(Arrays.asCompletableFuture(10))
	 *             .flatMap(t->CompletableFuture.completedFuture(20));
	 *  
	 *  
	 *  //CompletableFutureT<AnyMSeq<Stream<CompletableFuture[20]>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return CompletableFutureT that applies the flatMap function to the wrapped CompletableFuture
	 */

   public <B> CompletableFutureTSeq<B> flatMapT(Function<? super A,CompletableFutureTSeq<B>> f){
	   return of(run.map(future-> future.thenCompose(a-> f.apply(a).run.stream().toList().get(0))));
   }
   private static  <B> AnyMSeq<CompletableFuture<B>> narrow(AnyMSeq<CompletableFuture<? extends B>> run){
       return (AnyMSeq)run;
   }
   public <B> CompletableFutureTSeq<B> flatMap(Function<? super A,? extends MonadicValue<? extends B>> f){
      
       AnyMSeq<CompletableFuture<? extends B>> mapped=  run.map(o -> FutureW.of(o).flatMap(f).getFuture());
       return of(narrow(mapped));
     
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
		AnyMSeq<Integer> stream = AnyM.fromStream(withNulls);
		AnyMSeq<CompletableFuture<Integer>> streamOpt = stream.map(CompletableFuture::completedFuture);
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
   public static <U, R> Function<CompletableFutureTSeq<U>, CompletableFutureTSeq<R>> lift(Function<? super U,? extends R> fn) {
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
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<CompletableFuture<Integer>> streamOpt = stream.map(CompletableFuture::completedFuture);
		
		CompletableFuture<CompletableFuture<Integer>> two = CompletableFuture.completedFuture(CompletableFuture.completedFuture(2));
		AnyMSeq<CompletableFuture<Integer>> future=  AnyM.fromCompletableFuture(two);
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
	public static <U1, U2, R> BiFunction<CompletableFutureTSeq<U1>, CompletableFutureTSeq<U2>, CompletableFutureTSeq<R>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}
	/**
	 * Construct an CompletableFutureT from an AnyM that contains a monad type that contains type other than CompletableFuture
	 * The values in the underlying monad will be mapped to CompletableFuture<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an CompletableFuture
	 * @return CompletableFutureT
	 */
   public static <A> CompletableFutureTSeq<A> fromAnyM(AnyMSeq<A> anyM){
	   return of(anyM.map(CompletableFuture::completedFuture));
   }
   /**
	 * Construct an CompletableFutureT from an AnyM that wraps a monad containing  CompletableFutures
	 * 
	 * @param monads AnyM that contains a monad wrapping an CompletableFuture
	 * @return CompletableFutureT
	 */   
   public static <A> CompletableFutureTSeq<A> of(AnyMSeq<CompletableFuture<A>> monads){
	   return new CompletableFutureTSeq<>(monads);
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
        return run.stream().map(cf->cf.join());
    }

    @Override
    public Iterator<A> iterator() {
       return stream().iterator();
    }

    @Override
    public void subscribe(Subscriber<? super A> s) {
        run.forEachEvent(e->FutureW.of(e).subscribe(s),e->s.onError(e),()->s.onComplete()); 
    }

    
    public <R> CompletableFutureTSeq<R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->CompletableFuture.completedFuture(i)));
    }
  
    public <R> CompletableFutureTSeq<R> unit(R value){
       return of(run.unit(CompletableFuture.completedFuture(value)));
    }
    public <R> CompletableFutureTSeq<R> empty(){
        return of(run.unit(new CompletableFuture<R>()));
     }
 
    @Override
    public AnyM<? extends Foldable<A>> nestedFoldables() {
        return run.map(f->FutureW.of(f));
       
    }
    @Override
    public AnyM<? extends CyclopsCollectable<A>> nestedCollectables() {
        return run.map(f->FutureW.of(f).toListX());
       
    }
    @Override
    public <T> CompletableFutureTSeq<T> unitAnyM(AnyM<Traversable<T>> traversable) {
        
        return of((AnyMSeq)traversable.map(t->FutureW.fromIterable(t).toCompletableFuture()));
    }
    @Override
    public AnyMSeq<? extends Traversable<A>> transformerStream() {
        
        return run.map(f->FutureW.of(f).toListX());
    }
 
}
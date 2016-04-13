package com.aol.cyclops.control.monads.transformers.seq;


import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.lambda.Collectable;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.Try.Success;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.control.monads.transformers.values.TransformerSeq;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;


/**
 * Monad transformer for JDK Try
 * 
 * TryT consists of an AnyM instance that in turns wraps another Monad type that contains an Try
 * 
 * TryT<AnyMSeq<*SOME_MONAD_TYPE*<Try<T>>>>
 * 
 * TryT allows the deeply wrapped Try to be manipulating within it's nested /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the Try within
 */
public class TryTSeq<T,X extends Throwable> implements TryT<T,X>,
                                                        Traversable<T>,
                                                        Foldable<T>,
                                                        ConvertableSequence<T>,
                                                        CyclopsCollectable<T>,
                                                        Sequential<T>{
                                                       
   private final AnyMSeq<Try<T,X>> run;
   
   
   private TryTSeq(final AnyMSeq<Try<T,X>> run){
       this.run = run;
   }
   
	/**
	 * @return The wrapped AnyM
	 */
   @Override
	public AnyMSeq<Try<T,X>> unwrap() {
		return run;
	}

   
	/**
	 * Peek at the current value of the Try
	 * <pre>
	 * {@code 
	 *    TryT.of(AnyM.fromStream(Success.of(10))
	 *             .peek(System.out::println);
	 *             
	 *     //prints 10        
	 * }
	 * </pre>
	 * 
	 * @param peek  Consumer to accept current value of Try
	 * @return TryT with peek call
	 */
   @Override
	public TryTSeq<T,X> peek(Consumer<? super T> peek) {
		return of(run.peek(opt -> opt.map(a -> {
			peek.accept(a);
			return a;
		})));
	}
   
	/**
	 * Filter the wrapped Try
	 * <pre>
	 * {@code 
	 *    TryT.of(AnyM.fromStream(Success.of(10))
	 *             .filter(t->t!=10);
	 *             
	 *     //TryT<AnyMSeq<Stream<Optional.empty>>>
	 * }
	 * </pre>
	 * @param test Predicate to filter the wrapped Try
	 * @return OptionalT that applies the provided filter
	 */
   @Override
	public MaybeTSeq<T> filter(Predicate<? super T> test) {
		return MaybeTSeq.of(run.map(opt -> opt.filter(test)));
	}

	/**
	 * Map the wrapped Try
	 * 
	 * <pre>
	 * {@code 
	 *  TryT.of(AnyM.fromStream(Success.of(10))
	 *             .map(t->t=t+1);
	 *  
	 *  
	 *  //TryT<AnyMSeq<Stream<Success[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped Try
	 * @return TryT that applies the map function to the wrapped Try
	 */
   @Override
	public <B> TryTSeq<B,X> map(Function<? super T,? extends B> f) {
		return new TryTSeq<B,X>(run.map(o -> o.map(f)));
	}

	/**
	 * Flat Map the wrapped Try
	  * <pre>
	 * {@code 
	 *  TryT.of(AnyM.fromStream(Success.of(10))
	 *             .flatMap(t->Failure.of(new Exception());
	 *  
	 *  
	 *  //TryT<AnyMSeq<Stream<Failure[Excption]>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return TryT that applies the flatMap function to the wrapped Try
	 */
	public <B> TryTSeq<B,X> flatMapT(Function<? super T, TryTSeq<B,X>> f) {

		return of(run.bind(opt -> {
			if (opt.isSuccess())
				return f.apply(opt.get()).run.unwrap();
			Try<B,X> ret = (Try)opt;
			return run.unit(ret).unwrap();
		}));

	}
	@Override
    public <B> TryTSeq<B,X> flatMap(Function<? super T, ? extends Try<B,X>> f) {

        return new TryTSeq<B,X>(run.map(o -> o.flatMap(f)));

    }
	/**
	 * Lift a function into one that accepts and returns an TryT
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add Exception Handling (via Try) and iteration (via Stream) to an existing function
	 * <pre>
	 * {@code 
	 *  Function<Integer,Integer> add2 = i -> i+2;
		Function<TryT<Integer,RuntimeException>, TryT<Integer,RuntimeException>> optTAdd2 = TryT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<Try<Integer,RuntimeException>> streamOpt = stream.map(this::toTry);
		List<Integer> results = optTAdd2.apply(TryT.of(streamOpt))
										.unwrap()
										.<Stream<Try<Integer,RuntimeException>>>unwrap()
										.filter(Try::isSuccess)
										.map(Try::get)
										.collect(Collectors.toList());
		
		//Arrays.asList(3,4);
	 * 
	 * 
	 * }</pre>
	 * 
	 * 
	 * @param fn Function to enhance with functionality from Try and another monad type
	 * @return Function that accepts and returns an TryT
	 */
	public static <U, R, X extends Throwable> Function<TryTSeq<U,X>, TryTSeq<R,X>> lift(Function<? super U,? extends R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}

	/**
	 * Lift a BiFunction into one that accepts and returns  TryTs
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add Exception handling (via Try), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
	 * to an existing function
	 * 
	 * <pre>
	 * {@code 
	BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<TryT<Integer,RuntimeException>,TryT<Integer,RuntimeException>, TryT<Integer,RuntimeException>> optTAdd2 = TryT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<Try<Integer,RuntimeException>> streamOpt = stream.map(this::toTry);
		
		CompletableFuture<Try<Integer,RuntimeException>> two = CompletableFuture.completedFuture(Try.of(2));
		AnyMSeq<Try<Integer,RuntimeException>> future=  AnyM.ofMonad(two);
		List<Integer> results = optTAdd2.apply(TryT.of(streamOpt),TryT.of(future))
										.unwrap()
										.<Stream<Try<Integer,RuntimeException>>>unwrap()
										.filter(Try::isSuccess)
										.map(Try::get)
										.collect(Collectors.toList());
			//Arrays.asList(3,4);							
	  }
	  </pre>
	 * @param fn BiFunction to enhance with functionality from Try and another monad type
	 * @return Function that accepts and returns an TryT
	 */
	public static <U1, U2, R, X extends Throwable> BiFunction<TryTSeq<U1,X>, TryTSeq<U2,X>, TryTSeq<R,X>> lift2(BiFunction<? super U1,? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

	/**
	 * Construct an TryT from an AnyM that contains a monad type that contains type other than Try
	 * The values in the underlying monad will be mapped to Try<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Try
	 * @return TryT
	 */
	@SuppressWarnings("unchecked")
	public static <A, X extends Throwable> TryTSeq<A,X> fromAnyM(AnyMSeq<A> anyM) {
		return (TryTSeq<A, X>) of(anyM.map(Success::of));
	}
   
	/**
	 * Construct an TryT from an AnyM that wraps a monad containing  Trys
	 * 
	 * @param monads AnyM that contains a monad wrapping an Try
	 * @return TryT
	 */
	public static <A,X extends Throwable> TryTSeq<A,X> of(AnyMSeq<Try<A,X>> monads) {
		return new TryTSeq<>(monads);
	}

   
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return run.toString();
	}
   
    
    
    
    public boolean isSuccess(){
        return run.allMatch(t->t.isSuccess());
    }
    public boolean isFailure(){
        return run.allMatch(t->t.isFailure());
    }

 

  
    public <R> R visit(Function<? super T,? extends R> success, 
            Function<? super X,? extends R> failure,Monoid<R> combiner){
        return run.map(t->t.visit(success, failure)).reduce(combiner);   
    }
    
   
    

   
    
    public <R> TryTSeq<R,X> unit(R value){
       return of(run.unit(Try.success(value)));
    }
    public <R> TryTSeq<R,X> empty(){
        return of(run.unit(Try.failure(null)));
     }
    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMapIterable(e->e);
    }

    @Override
    public Iterator<T> iterator() {
       return stream().iterator();
    }

   
    public <R> TryTSeq<R,X> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->Try.success(i)));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<T> collectable() {
        return stream();
    }
    
}

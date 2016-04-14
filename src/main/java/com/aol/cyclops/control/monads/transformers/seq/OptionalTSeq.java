package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.lambda.Collectable;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;


/**
 * Monad transformer for JDK Optional
 * 
 * OptionalT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Optional
 * 
 * OptionalT<AnyMSeq<*SOME_MONAD_TYPE*<Optional<T>>>>
 * 
 * OptionalT allows the deeply wrapped Optional to be manipulating within it's nested /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the Optional within
 */
public class OptionalTSeq<T> implements OptionalT<T>,
                                        Traversable<T>,
                                        Foldable<T>,
                                        ConvertableSequence<T>,
                                        CyclopsCollectable<T>,
                                        Sequential<T>{
   
   private final AnyMSeq<Optional<T>> run;
   
   
   private OptionalTSeq(final AnyMSeq<Optional<T>> run){
       this.run = run;
   }
   
	/**
	 * @return The wrapped AnyM
	 */
	public AnyMSeq<Optional<T>> unwrap() {
		return run;
	}

   
	/**
	 * Peek at the current value of the Optional
	 * <pre>
	 * {@code 
	 *    OptionalT.of(AnyM.fromStream(Optional.of(10))
	 *             .peek(System.out::println);
	 *             
	 *     //prints 10        
	 * }
	 * </pre>
	 * 
	 * @param peek  Consumer to accept current value of Optional
	 * @return OptionalT with peek call
	 */
	public OptionalTSeq<T> peek(Consumer<? super T> peek) {
		return of(run.peek(opt -> opt.map(a -> {
			peek.accept(a);
			return a;
		})));
	}
   
	/**
	 * Filter the wrapped Optional
	 * <pre>
	 * {@code 
	 *    OptionalT.of(AnyM.fromStream(Optional.of(10))
	 *             .filter(t->t!=10);
	 *             
	 *     //OptionalT<AnyMSeq<Stream<Optional.empty>>>
	 * }
	 * </pre>
	 * @param test Predicate to filter the wrapped Optional
	 * @return OptionalT that applies the provided filter
	 */
	public OptionalTSeq<T> filter(Predicate<? super T> test) {
		return of(run.map(opt -> opt.filter(test)));
	}

	/**
	 * Map the wrapped Optional
	 * 
	 * <pre>
	 * {@code 
	 *  OptionalT.of(AnyM.fromStream(Optional.of(10))
	 *             .map(t->t=t+1);
	 *  
	 *  
	 *  //OptionalT<AnyMSeq<Stream<Optional[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped Optional
	 * @return OptionalT that applies the map function to the wrapped Optional
	 */
	@Override
	public <B> OptionalTSeq<B> map(Function<? super T,? extends B> f) {
		return new OptionalTSeq<B>(run.map(o -> o.map(f)));
	}

	/**
	 * Flat Map the wrapped Optional
	  * <pre>
	 * {@code 
	 *  OptionalT.of(AnyM.fromStream(Optional.of(10))
	 *             .flatMap(t->Optional.empty();
	 *  
	 *  
	 *  //OptionalT<AnyMSeq<Stream<Optional.empty>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return OptionalT that applies the flatMap function to the wrapped Optional
	 */
	public <B> OptionalTSeq<B> flatMapT(Function<? super T, OptionalTSeq<? extends B>> f) {

		return of(run.bind(opt -> {
			if (opt.isPresent())
				return f.apply(opt.get()).run.unwrap();
			return run.unit(Optional.<B> empty()).unwrap();
		}));

	}
	private static  <B> AnyMSeq<Optional<B>> narrow(AnyMSeq<Optional<? extends B>> run){
	       return (AnyMSeq)run;
	}
	@Override
	public <B> OptionalTSeq<B> flatMap(Function<? super T, ? extends MonadicValue<? extends B>> f) {
	     AnyMSeq<Optional<? extends B>> mapped=  run.map(o -> Maybe.fromOptional(o).flatMap(f).toOptional());
	     return of(narrow(mapped));
	}
	/**
	 * Lift a function into one that accepts and returns an OptionalT
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add null handling (via Optional) and iteration (via Stream) to an existing function
	 * <pre>
	 * {@code 
	 *  Function<Integer,Integer> add2 = i -> i+2;
		Function<OptionalT<Integer>, OptionalT<Integer>> optTAdd2 = OptionalT.lift(add2);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
		List<Integer> results = optTAdd2.apply(OptionalT.of(streamOpt))
										.unwrap()
										.<Stream<Optional<Integer>>>unwrap()
										.filter(Optional::isPresent)
										.map(Optional::get)
										.collect(Collectors.toList());
		
		//Arrays.asList(3,4);
	 * 
	 * 
	 * }</pre>
	 * 
	 * 
	 * @param fn Function to enhance with functionality from Optional and another monad type
	 * @return Function that accepts and returns an OptionalT
	 */
	public static <U, R> Function<OptionalTSeq<U>, OptionalTSeq<R>> lift(Function<U, R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}

	/**
	 * Lift a BiFunction into one that accepts and returns  OptionalTs
	 * This allows multiple monad types to add functionality to existing functions and methods
	 * 
	 * e.g. to add null handling (via Optional), iteration (via Stream)  and asynchronous execution (CompletableFuture) 
	 * to an existing function
	 * 
	 * <pre>
	 * {@code 
	 * BiFunction<Integer,Integer,Integer> add = (a,b) -> a+b;
		BiFunction<OptionalT<Integer>,OptionalT<Integer>, OptionalT<Integer>> optTAdd2 = OptionalT.lift2(add);
		
		Stream<Integer> withNulls = Stream.of(1,2,null);
		AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMSeq<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
		
		CompletableFuture<Optional<Integer>> two = CompletableFuture.supplyAsync(()->Optional.of(2));
		AnyMSeq<Optional<Integer>> future=  AnyM.ofMonad(two);
		List<Integer> results = optTAdd2.apply(OptionalT.of(streamOpt),OptionalT.of(future))
										.unwrap()
										.<Stream<Optional<Integer>>>unwrap()
										.filter(Optional::isPresent)
										.map(Optional::get)
										.collect(Collectors.toList());
			//Arrays.asList(3,4);							
	  }
	  </pre>
	 * @param fn BiFunction to enhance with functionality from Optional and another monad type
	 * @return Function that accepts and returns an OptionalT
	 */
	public static <U1, U2, R> BiFunction<OptionalTSeq<U1>, OptionalTSeq<U2>, OptionalTSeq<R>> lift2(BiFunction<? super U1, ? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

	/**
	 * Construct an OptionalT from an AnyM that contains a monad type that contains type other than Optional
	 * The values in the underlying monad will be mapped to Optional<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Optional
	 * @return OptionalT
	 */
	public static <A> OptionalTSeq<A> fromAnyM(AnyMSeq<A> anyM) {
		return of(anyM.map(Optional::ofNullable));
	}
   
	/**
	 * Construct an OptionalT from an AnyM that wraps a monad containing  Optionals
	 * 
	 * @param monads AnyM that contains a monad wrapping an Optional
	 * @return OptionalT
	 */
	public static <A> OptionalTSeq<A> of(AnyMSeq<Optional<A>> monads) {
		return new OptionalTSeq<>(monads);
	}
	public static <A> OptionalTSeq<A> of(Optional<A> monads){
        return OptionalT.fromIterable(ListX.of(monads));
    }
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return run.toString();
	}

    public boolean isPresent() {
        return run.anyMatch(m -> m.isPresent());
    }
    
    public <R> OptionalTSeq<R> unit(R value) {
        return of(run.unit(Optional.of(value)));
    }

    public <R> OptionalTSeq<R> empty() {
        return of(run.unit(Optional.empty()));
    }
    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().flatMapIterable(e->Maybe.fromOptional(e));
    }

    @Override
    public Iterator<T> iterator() {
       return stream().iterator();
    }

    
   
    public <R> OptionalTSeq<R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->Optional.of(i)));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<T> collectable() {
        return stream();
    }
    public boolean isSeqPresent() {
        return !run.isEmpty();
     }
     public static <T> OptionalTSeq<T> emptyList() {
         return OptionalT.fromIterable(ListX.of());
     }
}

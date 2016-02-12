package com.aol.cyclops.control.monads.transformers;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.lambda.function.Function1;

import com.aol.cyclops.monad.AnyM;


/**
 * Monad transformer for JDK Optional
 * 
 * OptionalT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Optional
 * 
 * OptionalT<AnyM<*SOME_MONAD_TYPE*<Optional<T>>>>
 * 
 * OptionalT allows the deeply wrapped Optional to be manipulating within it's nested /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the Optional within
 */
public class OptionalT<T> {
   
   private final AnyM<Optional<T>> run;
   
   
   private OptionalT(final AnyM<Optional<T>> run){
       this.run = run;
   }
   
	/**
	 * @return The wrapped AnyM
	 */
	public AnyM<Optional<T>> unwrap() {
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
	public OptionalT<T> peek(Consumer<T> peek) {
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
	 *     //OptionalT<AnyM<Stream<Optional.empty>>>
	 * }
	 * </pre>
	 * @param test Predicate to filter the wrapped Optional
	 * @return OptionalT that applies the provided filter
	 */
	public OptionalT<T> filter(Predicate<T> test) {
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
	 *  //OptionalT<AnyM<Stream<Optional[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped Optional
	 * @return OptionalT that applies the map function to the wrapped Optional
	 */
	public <B> OptionalT<B> map(Function<T, B> f) {
		return new OptionalT<B>(run.map(o -> o.map(f)));
	}

	/**
	 * Flat Map the wrapped Optional
	  * <pre>
	 * {@code 
	 *  OptionalT.of(AnyM.fromStream(Optional.of(10))
	 *             .flatMap(t->Optional.empty();
	 *  
	 *  
	 *  //OptionalT<AnyM<Stream<Optional.empty>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return OptionalT that applies the flatMap function to the wrapped Optional
	 */
	public <B> OptionalT<B> flatMap(Function1<T, OptionalT<B>> f) {

		return of(run.bind(opt -> {
			if (opt.isPresent())
				return f.apply(opt.get()).run;
			return run.unit(Optional.<B> empty());
		}));

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
		AnyM<Integer> stream = AnyM.ofMonad(withNulls);
		AnyM<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
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
	public static <U, R> Function<OptionalT<U>, OptionalT<R>> lift(Function<U, R> fn) {
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
		AnyM<Integer> stream = AnyM.ofMonad(withNulls);
		AnyM<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
		
		CompletableFuture<Optional<Integer>> two = CompletableFuture.supplyAsync(()->Optional.of(2));
		AnyM<Optional<Integer>> future=  AnyM.ofMonad(two);
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
	public static <U1, U2, R> BiFunction<OptionalT<U1>, OptionalT<U2>, OptionalT<R>> lift2(BiFunction<U1, U2, R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMap(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

	/**
	 * Construct an OptionalT from an AnyM that contains a monad type that contains type other than Optional
	 * The values in the underlying monad will be mapped to Optional<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Optional
	 * @return OptionalT
	 */
	public static <A> OptionalT<A> fromAnyM(AnyM<A> anyM) {
		return of(anyM.map(Optional::ofNullable));
	}
   
	/**
	 * Construct an OptionalT from an AnyM that wraps a monad containing  Optionals
	 * 
	 * @param monads AnyM that contains a monad wrapping an Optional
	 * @return OptionalT
	 */
	public static <A> OptionalT<A> of(AnyM<Optional<A>> monads) {
		return new OptionalT<>(monads);
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

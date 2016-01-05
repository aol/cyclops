package com.aol.cyclops.lambda.monads.transformers;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.Getter;

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

	public <B> OptionalT<B> flatMap(Function1<T, OptionalT<B>> f) {

		return of(run.flatMap(opt -> {
			if (opt.isPresent())
				return f.apply(opt.get()).run;
			return run.unit(Optional.<B> empty());
		}));

	}

	public static <U, R> Function<OptionalT<U>, OptionalT<R>> lift(Function<U, R> fn) {
		return optTu -> optTu.map(input -> fn.apply(input));
	}

	public static <U1, U2, R> BiFunction<OptionalT<U1>, OptionalT<U2>, OptionalT<R>> lift2(BiFunction<U1, U2, R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMap(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

	public static <A> OptionalT<A> fromAnyM(AnyM<A> anyM) {
		return of(anyM.map(Optional::ofNullable));
	}
   
   public static <A> OptionalT<A> of(AnyM<Optional<A>> monads){
	   return new OptionalT<>(monads);
   }
   
   public String toString(){
	   return run.toString();
   }
   
 
}

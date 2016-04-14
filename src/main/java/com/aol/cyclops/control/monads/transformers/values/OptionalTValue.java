package com.aol.cyclops.control.monads.transformers.values;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.applicative.Applicativable;
import com.aol.cyclops.util.stream.StreamUtils;


/**
 * Monad transformer for JDK Optional
 * 
 * OptionalT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Optional
 * 
 * OptionalT<AnyMValue<*SOME_MONAD_TYPE*<Optional<T>>>>
 * 
 * OptionalT allows the deeply wrapped Optional to be manipulating within it's nested /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the Optional within
 */
public class OptionalTValue<T> implements OptionalT<T>,
                                    TransformerValue<T>,
                                    MonadicValue<T>,
                                    Supplier<T>, 
                                    ConvertableFunctor<T>, 
                                    Filterable<T>,
                                    Applicativable<T>,
                                    Matchable.ValueAndOptionalMatcher<T>
                                    {
   
   private final AnyMValue<Optional<T>> run;
   
   
   private OptionalTValue(final AnyMValue<Optional<T>> run){
       this.run = run;
   }
   public MonadicValue<T> value(){
       return Maybe.fromOptional(run.get());
   }
   public boolean isValuePresent(){
       return !run.isEmpty();
   }
	/**
	 * @return The wrapped AnyM
	 */
	public AnyMValue<Optional<T>> unwrap() {
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
	public OptionalTValue<T> peek(Consumer<? super T> peek) {
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
	 *     //OptionalT<AnyMValue<Stream<Optional.empty>>>
	 * }
	 * </pre>
	 * @param test Predicate to filter the wrapped Optional
	 * @return OptionalT that applies the provided filter
	 */
	public OptionalTValue<T> filter(Predicate<? super T> test) {
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
	 *  //OptionalT<AnyMValue<Stream<Optional[11]>>>
	 * }
	 * </pre>
	 * 
	 * @param f Mapping function for the wrapped Optional
	 * @return OptionalT that applies the map function to the wrapped Optional
	 */
	public <B> OptionalTValue<B> map(Function<? super T,? extends B> f) {
		return new OptionalTValue<B>(run.map(o -> o.map(f)));
	}

	/**
	 * Flat Map the wrapped Optional
	  * <pre>
	 * {@code 
	 *  OptionalT.of(AnyM.fromStream(Optional.of(10))
	 *             .flatMap(t->Optional.empty();
	 *  
	 *  
	 *  //OptionalT<AnyMValue<Stream<Optional.empty>>>
	 * }
	 * </pre>
	 * @param f FlatMap function
	 * @return OptionalT that applies the flatMap function to the wrapped Optional
	 */
	public <B> OptionalTValue<B> flatMapT(Function<? super T, OptionalTValue<? extends B>> f) {

		return of(run.bind(opt -> {
			if (opt.isPresent())
				return f.apply(opt.get()).run.unwrap();
			return run.unit(Optional.<B> empty()).unwrap();
		}));

	}
	private static  <B> AnyMValue<Optional<B>> narrow(AnyMValue<Optional<? extends B>> run){
	       return (AnyMValue)run;
	}
	public <B> OptionalTValue<B> flatMap(Function<? super T, ? extends MonadicValue<? extends B>> f) {
	     AnyMValue<Optional<? extends B>> mapped=  run.map(o -> Maybe.fromOptional(o).flatMap(f).toOptional());
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
		AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMValue<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
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
	public static <U, R> Function<OptionalTValue<U>, OptionalTValue<R>> lift(Function<U, R> fn) {
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
		AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
		AnyMValue<Optional<Integer>> streamOpt = stream.map(Optional::ofNullable);
		
		CompletableFuture<Optional<Integer>> two = CompletableFuture.supplyAsync(()->Optional.of(2));
		AnyMValue<Optional<Integer>> future=  AnyM.ofMonad(two);
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
	public static <U1, U2, R> BiFunction<OptionalTValue<U1>, OptionalTValue<U2>, OptionalTValue<R>> lift2(BiFunction<? super U1, ? super U2,? extends R> fn) {
		return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
	}

	/**
	 * Construct an OptionalT from an AnyM that contains a monad type that contains type other than Optional
	 * The values in the underlying monad will be mapped to Optional<A>
	 * 
	 * @param anyM AnyM that doesn't contain a monad wrapping an Optional
	 * @return OptionalT
	 */
	public static <A> OptionalTValue<A> fromAnyM(AnyMValue<A> anyM) {
		return of(anyM.map(Optional::ofNullable));
	}
   
	/**
	 * Construct an OptionalT from an AnyM that wraps a monad containing  Optionals
	 * 
	 * @param monads AnyM that contains a monad wrapping an Optional
	 * @return OptionalT
	 */
	public static <A> OptionalTValue<A> of(AnyMValue<Optional<A>> monads) {
		return new OptionalTValue<>(monads);
	}
	 public static <A> OptionalTValue<A> of(Optional<A> maybe) {
	        
	        return fromValue(Maybe.just(maybe));
	    }
	public static <A,V extends MonadicValue<Optional<A>>> OptionalTValue<A> fromValue(V monadicValue){
	        return of(AnyM.ofValue(monadicValue));
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
    public T get() {
        return run.get().get();
    }

    public boolean isPresent() {
        return run.orElse(Optional.empty()).isPresent();
    }

    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromStream(StreamUtils.optionalToStream(run.orElse(Optional.empty())));
    }

    @Override
    public Iterator<T> iterator() {
        return Maybe.fromOptional(run.orElse(Optional.empty())).iterator();
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        Maybe.fromOptional(run.orElse(Optional.empty())).subscribe(s);

    }

    @Override
    public boolean test(T t) {
        return Maybe.fromOptional(run.orElse(Optional.empty())).test(t);
    }

    public <R> OptionalTValue<R> unit(R value) {
        return of(run.unit(Optional.of(value)));
    }

    public <R> OptionalTValue<R> empty() {
        return of(run.unit(Optional.empty()));
    }
    public static<T>  OptionalTValue<T> emptyMaybe() {
        return fromValue(Maybe.none());
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    public <U> OptionalTValue<U> cast(Class<U> type) {
       
        return (OptionalTValue<U>)TransformerValue.super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    public <R> OptionalTValue<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       
        return (OptionalTValue<R>)TransformerValue.super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> OptionalTValue<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (OptionalTValue<R>)TransformerValue.super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    public <U> OptionalTValue<U> ofType(Class<U> type) {
       
        return (OptionalTValue<U>)OptionalT.super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    public OptionalTValue<T> filterNot(Predicate<? super T> fn) {
       
        return (OptionalTValue<T>)OptionalT.super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    public OptionalTValue<T> notNull() {
       
        return (OptionalTValue<T>)OptionalT.super.notNull();
    }
}

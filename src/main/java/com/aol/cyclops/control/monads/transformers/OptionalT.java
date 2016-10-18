package com.aol.cyclops.control.monads.transformers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.seq.OptionalTSeq;
import com.aol.cyclops.control.monads.transformers.values.OptionalTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;

/**
 * Monad transformer for JDK Optional
 * 
 * OptionalT consists of an AnyM instance that in turns wraps anoter Monad type that contains an Optional
 * <pre>
 * {@code 
 * OptionalT<AnyM<*SOME_MONAD_TYPE*<Optional<T>>>>
 * }
 * </pre>
 * OptionalT allows the deeply wrapped Optional to be manipulating within it's nested /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the Optional within
 */
public interface OptionalT<T> extends Publisher<T>, Functor<T>, Filterable<T> {

    public ReactiveSeq<T> stream();

    public <R> OptionalT<R> unit(R value);

    public <R> OptionalT<R> empty();

    /**
     * @return The wrapped AnyM
     */
    public AnyM<Optional<T>> unwrap();

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
    @Override
    public OptionalT<T> peek(Consumer<? super T> peek);

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
    @Override
    public OptionalT<T> filter(Predicate<? super T> test);

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
    @Override
    public <B> OptionalT<B> map(Function<? super T, ? extends B> f);

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
    default <B> OptionalT<B> bind(final Function<? super T, OptionalT<? extends B>> f) {

        return of(unwrap().bind(opt -> {
            if (opt.isPresent())
                return f.apply(opt.get())
                        .unwrap()
                        .unwrap();
            return unwrap().unit(Optional.<B> empty())
                           .unwrap();
        }));

    }

    public <B> OptionalT<B> flatMap(Function<? super T, ? extends MonadicValue<? extends B>> f);

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
    public static <U, R> Function<OptionalT<U>, OptionalT<R>> lift(final Function<U, R> fn) {
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
    public static <U1, U2, R> BiFunction<OptionalT<U1>, OptionalT<U2>, OptionalT<R>> lift2(final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an OptionalT from an AnyM that contains a monad type that contains type other than Optional
     * The values in the underlying monad will be mapped to Optional<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Optional
     * @return OptionalT
     */
    public static <A> OptionalT<A> fromAnyM(final AnyM<A> anyM) {
        return of(anyM.map(Optional::ofNullable));
    }

    /**
     * Construct an OptionalT from an AnyM that wraps a monad containing  Optionals
     * 
     * @param monads AnyM that contains a monad wrapping an Optional
     * @return OptionalT
     */
    public static <A> OptionalT<A> of(final AnyM<Optional<A>> monads) {
        return Matchables.anyM(monads)
                         .visit(v -> OptionalTValue.of(v), s -> OptionalTSeq.of(s));
    }

    public static <A> OptionalTValue<A> fromAnyMValue(final AnyMValue<A> anyM) {
        return OptionalTValue.fromAnyM(anyM);
    }

    public static <A> OptionalTSeq<A> fromAnyMSeq(final AnyMSeq<A> anyM) {
        return OptionalTSeq.fromAnyM(anyM);
    }

    public static <A> OptionalTSeq<A> fromIterable(final Iterable<Optional<A>> iterableOfOptionals) {
        return OptionalTSeq.of(AnyM.fromIterable(iterableOfOptionals));
    }

    public static <A> OptionalTSeq<A> fromStream(final Stream<Optional<A>> streamOfOptionals) {
        return OptionalTSeq.of(AnyM.fromStream(streamOfOptionals));
    }

    public static <A> OptionalTSeq<A> fromPublisher(final Publisher<Optional<A>> publisherOfOptionals) {
        return OptionalTSeq.of(AnyM.fromPublisher(publisherOfOptionals));
    }

    public static <A> OptionalTValue<A> fromValue(final MonadicValue<Optional<A>> monadicValue) {
        return OptionalTValue.fromValue(monadicValue);
    }

    public static <A> OptionalTValue<A> fromOptional(final Optional<Optional<A>> optional) {
        return OptionalTValue.of(AnyM.fromOptional(optional));
    }

    public static <A> OptionalTValue<A> fromFuture(final CompletableFuture<Optional<A>> future) {
        return OptionalTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A> OptionalTValue<A> fromIterablOptionalue(final Iterable<Optional<A>> iterableOfOptionals) {
        return OptionalTValue.of(AnyM.fromIterableValue(iterableOfOptionals));
    }

    public static <T> OptionalTValue<T> emptyOptional() {
        return OptionalTValue.emptyOptional();
    }

    public static <T> OptionalTSeq<T> emptyList() {
        return OptionalT.fromIterable(ListX.of());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> OptionalT<U> cast(final Class<? extends U> type) {
        return (OptionalT<U>) Functor.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> OptionalT<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (OptionalT<R>) Functor.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> OptionalT<R> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {
        return (OptionalT<R>) Functor.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> OptionalT<U> ofType(final Class<? extends U> type) {

        return (OptionalT<U>) Filterable.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default OptionalT<T> filterNot(final Predicate<? super T> fn) {

        return (OptionalT<T>) Filterable.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    default OptionalT<T> notNull() {

        return (OptionalT<T>) Filterable.super.notNull();
    }
}

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
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.monads.transformers.seq.TryTSeq;
import com.aol.cyclops.control.monads.transformers.values.TryTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.To;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;

/**
 * Monad transformer for cyclops-react Try
 * 
 * TryT allows the deeply wrapped Try to be manipulating within it's nested /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the nested Try within
 */
public interface TryT<T, X extends Throwable> extends To<TryT<T,X>>,Publisher<T>, Foldable<T>, Functor<T>, Filterable<T> {

    public <R> TryT<R, X> unit(R value);

    public <R> TryT<R, X> empty();

    public <B> TryT<B, X> flatMap(Function<? super T, ? extends MonadicValue<? extends B>> f);

    /**
     * @return The wrapped AnyM
     */
    AnyM<Try<T, X>> unwrap();

    /**
     * Peek at the current value of the Try
     * <pre>
     * {@code 
     *    TryT.of(AnyM.fromStream(Try.success(10))
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
    public TryT<T, X> peek(Consumer<? super T> peek);

    /**
     * Filter the wrapped Try
     * <pre>
     * {@code 
     *    TryT.of(AnyM.fromStream(Try.success(10))
     *             .filter(t->t!=10);
     *             
     *     //TryT<AnyM<Stream<Optional.empty>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped Try
     * @return OptionalT that applies the provided filter
     */
    @Override
    public MaybeT<T> filter(Predicate<? super T> test);

    /**
     * Map the wrapped Try
     * 
     * <pre>
     * {@code 
     *  TryT.of(AnyM.fromStream(Try.success(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //TryT<AnyM<Stream<Success[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Try
     * @return TryT that applies the map function to the wrapped Try
     */
    @Override
    public <B> TryT<B, X> map(Function<? super T, ? extends B> f);

    /**
     * Flat Map the wrapped Try
      * <pre>
     * {@code 
     *  TryT.of(AnyM.fromStream(Try.success(10))
     *             .flatMap(t->Try.failure(new Exception());
     *  
     *  
     *  //TryT<AnyM<Stream<Failure[Excption]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return TryT that applies the flatMap function to the wrapped Try
     */
    default <B> TryT<B, X> bind(final Function<? super T, TryT<B, X>> f) {

        return of(unwrap().bind(opt -> {
            if (opt.isSuccess())
                return f.apply(opt.get())
                        .unwrap()
                        .unwrap();
            final Try<B, X> ret = (Try) opt;
            return unwrap().unit(ret)
                           .unwrap();
        }));

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
    	AnyM<Integer> stream = AnyM.ofMonad(withNulls);
    	AnyM<Try<Integer,RuntimeException>> streamOpt = stream.map(this::toTry);
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
    public static <U, R, X extends Throwable> Function<TryT<U, X>, TryT<R, X>> lift(final Function<? super U, ? extends R> fn) {
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
    	AnyM<Integer> stream = AnyM.ofMonad(withNulls);
    	AnyM<Try<Integer,RuntimeException>> streamOpt = stream.map(this::toTry);
    	
    	CompletableFuture<Try<Integer,RuntimeException>> two = CompletableFuture.completedFuture(Try.of(2));
    	AnyM<Try<Integer,RuntimeException>> future=  AnyM.ofMonad(two);
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
    public static <U1, U2, R, X extends Throwable> BiFunction<TryT<U1, X>, TryT<U2, X>, TryT<R, X>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an TryT from an AnyM that contains a monad type that contains type other than Try
     * The values in the underlying monad will be mapped to Try<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an Try
     * @return TryT
     */
    @SuppressWarnings("unchecked")
    public static <A, X extends Throwable> TryT<A, X> fromAnyM(final AnyM<A> anyM) {
        return (TryT<A, X>) of(anyM.map(Try::success));
    }

    /**
     * Construct an TryT from an AnyM that wraps a monad containing  Trys
     * 
     * @param monads AnyM that contains a monad wrapping an Try
     * @return TryT
     */
    public static <A, X extends Throwable> TryT<A, X> of(final AnyM<Try<A, X>> monads) {
        return Matchables.anyM(monads)
                         .visit(v -> TryTValue.of(v), s -> TryTSeq.of(s));
    }

    public static <A, X extends Throwable> TryTValue<A, X> fromAnyMValue(final AnyMValue<A> anyM) {
        return TryTValue.fromAnyM(anyM);
    }

    public static <A, X extends Throwable> TryTSeq<A, X> fromAnyMSeq(final AnyMSeq<A> anyM) {
        return TryTSeq.fromAnyM(anyM);
    }

    public static <A, X extends Throwable> TryTSeq<A, X> fromIterable(final Iterable<Try<A, X>> iterableOfTrys) {
        return TryTSeq.of(AnyM.fromIterable(iterableOfTrys));
    }

    public static <A, X extends Throwable> TryTSeq<A, X> fromStream(final Stream<Try<A, X>> streamOfTrys) {
        return TryTSeq.of(AnyM.fromStream(streamOfTrys));
    }

    public static <A, X extends Throwable> TryTSeq<A, X> fromPublisher(final Publisher<Try<A, X>> publisherOfTrys) {
        return TryTSeq.of(AnyM.fromPublisher(publisherOfTrys));
    }

    public static <A, X extends Throwable, V extends MonadicValue<Try<A, X>>> TryTValue<A, X> fromValue(final V monadicValue) {
        return TryTValue.fromValue(monadicValue);
    }

    public static <A, X extends Throwable> TryTValue<A, X> fromOptional(final Optional<Try<A, X>> optional) {
        return TryTValue.of(AnyM.fromOptional(optional));
    }

    public static <A, X extends Throwable> TryTValue<A, X> fromFuture(final CompletableFuture<Try<A, X>> future) {
        return TryTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A, X extends Throwable> TryTValue<A, X> fromIterablTryue(final Iterable<Try<A, X>> iterableOfTrys) {
        return TryTValue.of(AnyM.fromIterableValue(iterableOfTrys));
    }

    public static <T, X extends Throwable> TryTSeq<T, X> emptyList() {
        return TryT.fromIterable(ListX.of());
    }

    public static <A, X extends Throwable> TryTValue<A, X> emptyOptional() {
        return TryTValue.emptyOptional();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> TryT<U, X> cast(final Class<? extends U> type) {
        return (TryT<U, X>) Functor.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> TryT<R, X> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (TryT<R, X>) Functor.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> TryT<R, X> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {
        return (TryT<R, X>) Functor.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    default <U> MaybeT<U> ofType(final Class<? extends U> type) {

        return (MaybeT<U>) Filterable.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    default MaybeT<T> filterNot(final Predicate<? super T> fn) {

        return (MaybeT<T>) Filterable.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    default MaybeT<T> notNull() {

        return (MaybeT<T>) Filterable.super.notNull();
    }

}

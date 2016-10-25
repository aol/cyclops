package com.aol.cyclops.control.monads.transformers.values;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;

/**
 * Monad transformer for Try nested within Scalar data types (e.g. Optional, CompletableFuture, Eval, Maybe)
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the Try within
 */
public class TryTValue<T, X extends Throwable> implements TryT<T, X>, TransformerValue<T>, MonadicValue<T>, Supplier<T>, ConvertableFunctor<T>,
        Filterable<T>, ApplicativeFunctor<T>, Matchable.ValueAndOptionalMatcher<T> {

    private final AnyMValue<Try<T, X>> run;

    private TryTValue(final AnyMValue<Try<T, X>> run) {
        this.run = run;
    }

    @Override
    public Try<T, X> value() {
        return run.get();
    }

    @Override
    public boolean isValuePresent() {
        return !run.isEmpty();
    }

    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyMValue<Try<T, X>> unwrap() {
        return run;
    }

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
    public TryTValue<T, X> peek(final Consumer<? super T> peek) {
        return of(run.peek(opt -> opt.map(a -> {
            peek.accept(a);
            return a;
        })));
    }

    /**
     * Filter the wrapped Try
     * <pre>
     * {@code 
     *    TryT.of(AnyM.fromStream(Try.success(10))
     *             .filter(t->t!=10);
     *             
     *     //TryT<AnyMValue<Stream<Optional.empty>>>
     * }
     * </pre>
     * @param test Predicate to filter the wrapped Try
     * @return OptionalT that applies the provided filter
     */
    @Override
    public MaybeTValue<T> filter(final Predicate<? super T> test) {
        return MaybeTValue.of(run.map(opt -> opt.filter(test)));
    }

    /**
     * Map the wrapped Try
     * 
     * <pre>
     * {@code 
     *  TryT.of(AnyM.fromStream(Try.success(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //TryT<AnyMValue<Stream<Success[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped Try
     * @return TryT that applies the map function to the wrapped Try
     */
    @Override
    public <B> TryTValue<B, X> map(final Function<? super T, ? extends B> f) {
        return new TryTValue<B, X>(
                                   run.map(o -> o.map(f)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> TryTValue<R, X> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new TryTValue<>(
                               run.map(o -> o.combine(app, fn)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> TryTValue<R, X> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new TryTValue<>(
                               run.map(o -> o.zip(app, fn)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> TryTValue<R, X> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {
        return new TryTValue<>(
                               run.map(o -> o.zip(fn, app)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    public <U, R> TryTValue<R, X> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (TryTValue<R, X>) TransformerValue.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    public <U, R> TryTValue<R, X> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (TryTValue<R, X>) TransformerValue.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> TryTValue<Tuple2<T, U>, X> zip(final Stream<? extends U> other) {

        return (TryTValue) TransformerValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> TryTValue<Tuple2<T, U>, X> zip(final Seq<? extends U> other) {

        return (TryTValue) TransformerValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    public <U> TryTValue<Tuple2<T, U>, X> zip(final Iterable<? extends U> other) {

        return (TryTValue) TransformerValue.super.zip(other);
    }

    /**
     * Flat Map the wrapped Try
      * <pre>
     * {@code 
     *  TryT.of(AnyM.fromStream(Try.success(10))
     *             .flatMap(t->Try.failure(new Exception());
     *  
     *  
     *  //TryT<AnyMValue<Stream<Failure[Excption]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return TryT that applies the flatMap function to the wrapped Try
     */
    public <B> TryTValue<B, X> flatMapT(final Function<? super T, TryTValue<B, X>> f) {

        return of(run.bind(opt -> {
            if (opt.isSuccess())
                return f.apply(opt.get()).run.unwrap();
            final Try<B, X> ret = (Try) opt;
            return run.unit(ret)
                      .unwrap();
        }));

    }

    @Override
    public <B> TryTValue<B, X> flatMap(final Function<? super T, ? extends Try<B, X>> f) {

        return new TryTValue<B, X>(
                                   run.map(o -> o.flatMap(f)));

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
    	AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
    	AnyMValue<Try<Integer,RuntimeException>> streamOpt = stream.map(this::toTry);
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
    public static <U, R, X extends Throwable> Function<TryTValue<U, X>, TryTValue<R, X>> lift(final Function<? super U, ? extends R> fn) {
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
    	AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
    	AnyMValue<Try<Integer,RuntimeException>> streamOpt = stream.map(this::toTry);
    	
    	CompletableFuture<Try<Integer,RuntimeException>> two = CompletableFuture.completedFuture(Try.of(2));
    	AnyMValue<Try<Integer,RuntimeException>> future=  AnyM.ofMonad(two);
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
    public static <U1, U2, R, X extends Throwable> BiFunction<TryTValue<U1, X>, TryTValue<U2, X>, TryTValue<R, X>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
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
    public static <A, X extends Throwable> TryTValue<A, X> fromAnyM(final AnyMValue<A> anyM) {
        return (TryTValue<A, X>) of(anyM.map(Try::success));
    }

    /**
     * Construct an TryT from an AnyM that wraps a monad containing  Trys
     * 
     * @param monads AnyM that contains a monad wrapping an Try
     * @return TryT
     */
    public static <A, X extends Throwable> TryTValue<A, X> of(final AnyMValue<Try<A, X>> monads) {
        return new TryTValue<>(
                               monads);
    }

    public static <A, X extends Throwable> TryTValue<A, X> of(final Try<A, X> monads) {
        return TryT.fromOptional(Optional.of(monads));
    }

    public static <A, X extends Throwable, V extends MonadicValue<Try<A, X>>> TryTValue<A, X> fromValue(final V monadicValue) {
        return of(AnyM.ofValue(monadicValue));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("TryTValue[%s]", run);
    }

    @Override
    public T get() {
        return run.get()
                  .get();
    }

    public boolean isSuccess() {
        return run.orElse(Try.failure(null))
                  .isSuccess();
    }

    public boolean isFailure() {
        return run.orElse(Try.success(null))
                  .isFailure();
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.orElseGet(() -> Try.failure(null))
                  .stream();
    }

    @Override
    public Iterator<T> iterator() {
        return run.orElse(Try.failure(null))
                  .iterator();
    }

    @Override
    public void subscribe(final Subscriber<? super T> s) {
        run.orElse(Try.failure(null))
           .subscribe(s);

    }

    @Override
    public boolean test(final T t) {
        return run.get()
                  .test(t);
    }

    public <R> R visit(final Function<? super T, ? extends R> success, final Function<? super X, ? extends R> failure, final Supplier<R> none) {

        if (!isFailure() && !isSuccess())
            return none.get();

        return run.get()
                  .visit(success, failure);

    }

    @Override
    public <R> TryTValue<R, X> unit(final R value) {
        return of(run.unit(Try.success(value)));
    }

    @Override
    public <R> TryTValue<R, X> empty() {
        return of(run.unit(Try.failure(null)));
    }

    public static <T, X extends Throwable> TryTValue<T, X> emptyOptional() {
        return TryT.fromOptional(Optional.empty());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    public <U> TryTValue<U, X> cast(final Class<? extends U> type) {
        return (TryTValue<U, X>) TryT.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    public <R> TryTValue<R, X> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (TryTValue<R, X>) TryT.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> TryTValue<R, X> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {
        return (TryTValue<R, X>) TryT.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    public <U> MaybeTValue<U> ofType(final Class<? extends U> type) {

        return (MaybeTValue<U>) TryT.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    public MaybeTValue<T> filterNot(final Predicate<? super T> fn) {

        return (MaybeTValue<T>) TryT.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    public MaybeTValue<T> notNull() {

        return (MaybeTValue<T>) TryT.super.notNull();
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof TryTValue) {
            return run.equals(((TryTValue) o).run);
        }
        return false;
    }
}

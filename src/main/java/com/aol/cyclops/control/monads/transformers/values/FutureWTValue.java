package com.aol.cyclops.control.monads.transformers.values;

import java.util.Arrays;
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
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;

import lombok.val;

/**
 * Monad Transformer for Futures nested within Scalar data types (e.g. Optional, CompletableFuture, Eval, Maybe)
 * 
 * @author johnmcclean
 *
 * @param <A> The type contained on the Future within
 */
public class FutureWTValue<A> implements FutureWT<A>, TransformerValue<A>, MonadicValue<A>, Supplier<A>, ConvertableFunctor<A>, Filterable<A>,
        ApplicativeFunctor<A>, Matchable.ValueAndOptionalMatcher<A> {

    private final AnyMValue<FutureW<A>> run;

    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyMValue<FutureW<A>> unwrap() {
        return run;
    }

    private FutureWTValue(final AnyMValue<FutureW<A>> run) {
        this.run = run;
    }

    @Override
    public FutureW<A> value() {
        return run.get();
    }

    @Override
    public boolean isValuePresent() {
        return !run.isEmpty();
    }

    /**
    * Filter the wrapped Maybe
    * 
    * <pre>
    * {@code 
    *    MaybeT.of(AnyM.fromStream(Maybe.of(10))
    *             .filter(t->t!=10);
    *             
    *     //MaybeT<AnyMValue<Stream<Maybe.empty>>>
    * }
    * </pre>
    * 
    * @param test
    *            Predicate to filter the wrapped Maybe
    * @return MaybeT that applies the provided filter
    */
    @Override
    public MaybeTValue<A> filter(final Predicate<? super A> test) {
        return MaybeTValue.of(run.map(opt -> opt.filter(test)));
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
    @Override
    public FutureWTValue<A> peek(final Consumer<? super A> peek) {

        return of(run.peek(future -> future.map(a -> {
            peek.accept(a);
            return a;
        })));
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
     *  //CompletableFutureT<AnyMValue<Stream<CompletableFuture[11]>>>
     * }
     * </pre>
     * 
     * @param f Mapping function for the wrapped CompletableFuture
     * @return CompletableFutureT that applies the map function to the wrapped CompletableFuture
     */
    @Override
    public <B> FutureWTValue<B> map(final Function<? super A, ? extends B> f) {
        return new FutureWTValue<B>(
                                    run.map(o -> o.map(f)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops.
     * types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> FutureWTValue<R> combine(final Value<? extends T2> app, final BiFunction<? super A, ? super T2, ? extends R> fn) {
        return new FutureWTValue<>(
                                   run.map(o -> o.combine(app, fn)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.lang.
     * Iterable, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> FutureWTValue<R> zip(final Iterable<? extends T2> app, final BiFunction<? super A, ? super T2, ? extends R> fn) {

        return new FutureWTValue<>(
                                   run.map(o -> o.zip(app, fn)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.util.
     * function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> FutureWTValue<R> zip(final BiFunction<? super A, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {
        return new FutureWTValue<>(
                                   run.map(o -> o.zip(fn, app)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    public <U, R> FutureWTValue<R> zip(final Seq<? extends U> other, final BiFunction<? super A, ? super U, ? extends R> zipper) {

        return (FutureWTValue<R>) TransformerValue.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    public <U, R> FutureWTValue<R> zip(final Stream<? extends U> other, final BiFunction<? super A, ? super U, ? extends R> zipper) {

        return (FutureWTValue<R>) TransformerValue.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> FutureWTValue<Tuple2<A, U>> zip(final Stream<? extends U> other) {

        return (FutureWTValue) TransformerValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> FutureWTValue<Tuple2<A, U>> zip(final Seq<? extends U> other) {

        return (FutureWTValue) TransformerValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    public <U> FutureWTValue<Tuple2<A, U>> zip(final Iterable<? extends U> other) {

        return (FutureWTValue) TransformerValue.super.zip(other);
    }

    /**
     * Flat Map the wrapped CompletableFuture
      * <pre>
     * {@code 
     *  CompletableFutureT.of(AnyM.fromStream(Arrays.asCompletableFuture(10))
     *             .flatMap(t->CompletableFuture.completedFuture(20));
     *  
     *  
     *  //CompletableFutureT<AnyMValue<Stream<CompletableFuture[20]>>>
     * }
     * </pre>
     * @param f FlatMap function
     * @return CompletableFutureT that applies the flatMap function to the wrapped CompletableFuture
     */

    public <B> FutureWTValue<B> flatMapT(final Function<? super A, FutureWTValue<B>> f) {
        return of(run.map(future -> future.flatMap(a -> f.apply(a).run.stream()
                                                                      .toList()
                                                                      .get(0))));
    }

    private static <B> AnyMValue<FutureW<B>> narrow(final AnyMValue<FutureW<? extends B>> run) {
        return (AnyMValue) run;
    }

    @Override
    public <B> FutureWTValue<B> flatMap(final Function<? super A, ? extends MonadicValue<? extends B>> f) {

        final AnyMValue<FutureW<? extends B>> mapped = run.map(o -> o.flatMap(f));
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
    	AnyMValue<Integer> stream = AnyM.fromStream(withNulls);
    	AnyMValue<CompletableFuture<Integer>> streamOpt = stream.map(CompletableFuture::completedFuture);
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
    public static <U, R> Function<FutureWTValue<U>, FutureWTValue<R>> lift(final Function<? super U, ? extends R> fn) {
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
    	AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
    	AnyMValue<CompletableFuture<Integer>> streamOpt = stream.map(CompletableFuture::completedFuture);
    	
    	CompletableFuture<CompletableFuture<Integer>> two = CompletableFuture.completedFuture(CompletableFuture.completedFuture(2));
    	AnyMValue<CompletableFuture<Integer>> future=  AnyM.fromCompletableFuture(two);
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
    public static <U1, U2, R> BiFunction<FutureWTValue<U1>, FutureWTValue<U2>, FutureWTValue<R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an CompletableFutureT from an AnyM that contains a monad type that contains type other than CompletableFuture
     * The values in the underlying monad will be mapped to CompletableFuture<A>
     * 
     * @param anyM AnyM that doesn't contain a monad wrapping an CompletableFuture
     * @return CompletableFutureT
     */
    public static <A> FutureWTValue<A> fromAnyM(final AnyMValue<A> anyM) {
        return of(anyM.map(FutureW::ofResult));
    }

    /**
     * Construct an CompletableFutureT from an AnyM that wraps a monad containing  CompletableFutures
     * 
     * @param monads AnyM that contains a monad wrapping an CompletableFuture
     * @return CompletableFutureT
     */
    public static <A> FutureWTValue<A> of(final AnyMValue<FutureW<A>> monads) {
        return new FutureWTValue<>(
                                   monads);
    }

    public static <A> FutureWTValue<A> of(final FutureW<A> monads) {
        return FutureWT.fromOptional(Optional.of(monads));
    }

    public static <A, V extends MonadicValue<FutureW<A>>> FutureWTValue<A> fromValue(final V monadicValue) {
        return of(AnyM.ofValue(monadicValue));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("FutureWTValue[%s]", run);
    }

    @Override
    public A get() {
        return run.get()
                  .get();
    }

    @Override
    public ReactiveSeq<A> stream() {
        val maybeEval = run.toMaybe();
        return maybeEval.isPresent() ? maybeEval.get()
                                                .stream()
                : ReactiveSeq.of();
    }

    @Override
    public Iterator<A> iterator() {
        val maybeEval = run.toMaybe();
        return maybeEval.isPresent() ? maybeEval.get()
                                                .iterator()
                : Arrays.<A> asList()
                        .iterator();
    }

    @Override
    public void subscribe(final Subscriber<? super A> s) {
        run.toMaybe()
           .forEach(e -> e.subscribe(s));

    }

    public boolean isFuturePresent() {
        return !run.isEmpty();

    }

    @Override
    public boolean test(final A t) {
        val maybeEval = run.toMaybe();
        return maybeEval.isPresent() ? maybeEval.get()
                                                .test(t)
                : false;

    }

    @Override
    public <R> FutureWTValue<R> unit(final R value) {
        return of(run.unit(FutureW.ofResult(value)));
    }

    @Override
    public <R> FutureWTValue<R> empty() {
        return of(run.unit(FutureW.empty()));
    }

    public static <T> FutureWTValue<T> emptyOptional() {
        return FutureWT.fromOptional(Optional.empty());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    public <U> FutureWTValue<U> cast(final Class<? extends U> type) {
        return (FutureWTValue<U>) TransformerValue.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    public <R> FutureWTValue<R> trampoline(final Function<? super A, ? extends Trampoline<? extends R>> mapper) {
        return (FutureWTValue<R>) TransformerValue.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> FutureWTValue<R> patternMatch(final Function<CheckValue1<A, R>, CheckValue1<A, R>> case1, final Supplier<? extends R> otherwise) {
        return (FutureWTValue<R>) TransformerValue.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    public <U> MaybeTValue<U> ofType(final Class<? extends U> type) {

        return (MaybeTValue<U>) FutureWT.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    public MaybeTValue<A> filterNot(final Predicate<? super A> fn) {

        return (MaybeTValue<A>) FutureWT.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    public MaybeTValue<A> notNull() {

        return (MaybeTValue<A>) FutureWT.super.notNull();
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof FutureWTValue) {
            return run.equals(((FutureWTValue) o).run);
        }
        return false;
    }
}
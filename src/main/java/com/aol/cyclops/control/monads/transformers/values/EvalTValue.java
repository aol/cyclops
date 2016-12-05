package com.aol.cyclops.control.monads.transformers.values;

import java.util.Arrays;
import java.util.Iterator;
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
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;

import lombok.val;

/**
 * Monad Transformer for Evals nested within Scalar data types (e.g. Optional, CompletableFuture, Eval, Maybe)
 * 
 * @author johnmcclean
 *
 * @param <T> The type contained on the Eval within
 */
public class EvalTValue<T> implements EvalT<T>, TransformerValue<T>, MonadicValue<T>, Supplier<T>, ConvertableFunctor<T>, Filterable<T>,
        ApplicativeFunctor<T>, Matchable.ValueAndOptionalMatcher<T> {

    private final AnyMValue<Eval<T>> run;

    private EvalTValue(final AnyMValue<Eval<T>> run) {
        this.run = run;
    }

    @Override
    public Eval<T> value() {
        return run.get();
    }

    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyMValue<Eval<T>> unwrap() {
        return run;
    }

    /**
     * Peek at the current value of the Maybe
     * 
     * <pre>
     * {@code 
     *    MaybeT.of(AnyM.fromStream(Maybe.of(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek
     *            Consumer to accept current value of Maybe
     * @return MaybeT with peek call
     */
    @Override
    public EvalTValue<T> peek(final Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });
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
    public MaybeTValue<T> filter(final Predicate<? super T> test) {
        return MaybeTValue.of(run.map(opt -> opt.filter(test)));
    }

    /**
     * Map the wrapped Maybe
     * 
     * <pre>
     * {@code 
     *  MaybeT.of(AnyM.fromStream(Maybe.of(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //MaybeT<AnyMValue<Stream<Maybe[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Maybe
     * @return MaybeT that applies the map function to the wrapped Maybe
     */
    @Override
    public <B> EvalTValue<B> map(final Function<? super T, ? extends B> f) {
        return new EvalTValue<B>(
                                 run.map(o -> o.map(f)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> EvalTValue<R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new EvalTValue<R>(
                                 run.map(o -> o.combine(app, fn)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> EvalTValue<R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new EvalTValue<R>(
                                 run.map(o -> o.zip(app, fn)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> EvalTValue<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {
        return new EvalTValue<R>(
                                 run.map(o -> o.zip(fn, app)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    public <U, R> EvalTValue<R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (EvalTValue<R>) TransformerValue.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    public <U, R> EvalTValue<R> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (EvalTValue<R>) TransformerValue.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> EvalTValue<Tuple2<T, U>> zip(final Stream<? extends U> other) {

        return (EvalTValue) TransformerValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> EvalTValue<Tuple2<T, U>> zip(final Seq<? extends U> other) {

        return (EvalTValue) TransformerValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    public <U> EvalTValue<Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (EvalTValue) TransformerValue.super.zip(other);
    }

    /**
     * Flat Map the wrapped Maybe
     * 
     * <pre>
    * {@code 
    *  MaybeT.of(AnyM.fromStream(Maybe.of(10))
    *             .flatMap(t->Maybe.empty();
    *  
    *  
    *  //MaybeT<AnyMValue<Stream<Maybe.empty>>>
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return MaybeT that applies the flatMap function to the wrapped Maybe
     */
    public <B> EvalTValue<B> flatMapT(final Function<? super T, EvalTValue<? extends B>> f) {

        return of(run.bind(opt -> {

            return f.apply(opt.get()).run.unwrap();

        }));

    }

    @Override
    public <B> EvalTValue<B> flatMap(final Function<? super T, ? extends MonadicValue<? extends B>> f) {

        return new EvalTValue<B>(
                                 run.map(o -> o.flatMap(f)));

    }

    /**
     * Lift a function into one that accepts and returns an MaybeT This allows
     * multiple monad types to add functionality to existing functions and
     * methods
     * 
     * e.g. to add null handling (via Maybe) and iteration (via Stream) to an
     * existing function
     * 
     * <pre>
     * {
     *     &#64;code
     *     Function<Integer, Integer> add2 = i -> i + 2;
     *     Function<MaybeT<Integer>, MaybeT<Integer>> optTAdd2 = MaybeT.lift(add2);
     * 
     *     Stream<Integer> withNulls = Stream.of(1, 2, null);
     *     AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMValue<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
     *     List<Integer> results = optTAdd2.apply(MaybeT.of(streamOpt)).unwrap().<Stream<Maybe<Integer>>> unwrap()
     *             .filter(Maybe::isPresent).map(Maybe::get).collect(Collectors.toList());
     * 
     *     // Arrays.asList(3,4);
     * 
     * }
     * </pre>
     * 
     * 
     * @param fn
     *            Function to enhance with functionality from Maybe and another
     *            monad type
     * @return Function that accepts and returns an MaybeT
     */
    public static <U, R> Function<EvalTValue<U>, EvalTValue<R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns MaybeTs This allows
     * multiple monad types to add functionality to existing functions and
     * methods
     * 
     * e.g. to add null handling (via Maybe), iteration (via Stream) and
     * asynchronous execution (CompletableFuture) to an existing function
     * 
     * <pre>
     * {
     *     &#64;code
     *     BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
     *     BiFunction<MaybeT<Integer>, MaybeT<Integer>, MaybeT<Integer>> optTAdd2 = MaybeT.lift2(add);
     * 
     *     Stream<Integer> withNulls = Stream.of(1, 2, null);
     *     AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMValue<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
     * 
     *     CompletableFuture<Maybe<Integer>> two = CompletableFuture.supplyAsync(() -> Maybe.of(2));
     *     AnyMValue<Maybe<Integer>> future = AnyM.ofMonad(two);
     *     List<Integer> results = optTAdd2.apply(MaybeT.of(streamOpt), MaybeT.of(future)).unwrap()
     *             .<Stream<Maybe<Integer>>> unwrap().filter(Maybe::isPresent).map(Maybe::get)
     *             .collect(Collectors.toList());
     *     // Arrays.asList(3,4);
     * }
     * </pre>
     * 
     * @param fn
     *            BiFunction to enhance with functionality from Maybe and
     *            another monad type
     * @return Function that accepts and returns an MaybeT
     */
    public static <U1, U2, R> BiFunction<EvalTValue<U1>, EvalTValue<U2>, EvalTValue<R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an MaybeT from an AnyM that contains a monad type that contains
     * type other than Maybe The values in the underlying monad will be mapped
     * to Maybe<A>
     * 
     * @param anyM
     *            AnyM that doesn't contain a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <A> EvalTValue<A> fromAnyM(final AnyMValue<A> anyM) {
        return of(anyM.map(a -> Eval.later(() -> a)));
    }

    /**
     * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <A> EvalTValue<A> of(final AnyMValue<Eval<A>> monads) {
        return new EvalTValue<>(
                                monads);
    }

    public static <A, V extends MonadicValue<Eval<A>>> EvalTValue<A> fromValue(final V monadicValue) {
        return of(AnyM.ofValue(monadicValue));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("EvalTValue[%s]", run);
    }

    @Override
    public T get() {
        return run.get()
                  .get();
    }

    @Override
    public boolean isValuePresent() {
        return !run.isEmpty();
    }

    @Override
    public ReactiveSeq<T> stream() {
        val maybeEval = run.toMaybe();
        return maybeEval.isPresent() ? maybeEval.get()
                                                .stream()
                : ReactiveSeq.of();
    }

    @Override
    public Iterator<T> iterator() {
        val maybeEval = run.toMaybe();
        return maybeEval.isPresent() ? maybeEval.get()
                                                .iterator()
                : Arrays.<T> asList()
                        .iterator();
    }

    @Override
    public void subscribe(final Subscriber<? super T> s) {
        run.toMaybe()
           .forEach(e -> e.subscribe(s));

    }

    @Override
    public boolean test(final T t) {
        val maybeEval = run.toMaybe();
        return maybeEval.isPresent() ? maybeEval.get()
                                                .test(t)
                : false;

    }

    @Override
    public <R> EvalTValue<R> unit(final R value) {
        return of(run.unit(Eval.now(value)));
    }

    @Override
    public <R> EvalTValue<R> empty() {
        return of(run.unit(Eval.later(() -> null)));
    }

    public static <T> EvalTValue<T> of(final Eval<T> eval) {
        return fromValue(Maybe.just(eval));
    }

    public static <T> EvalTValue<T> emptyMaybe() {
        return fromValue(Maybe.none());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    public <U> EvalTValue<U> cast(final Class<? extends U> type) {

        return (EvalTValue<U>) TransformerValue.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    public <R> EvalTValue<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {

        return (EvalTValue<R>) TransformerValue.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> EvalTValue<R> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {

        return (EvalTValue<R>) TransformerValue.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    public <U> MaybeTValue<U> ofType(final Class<? extends U> type) {

        return (MaybeTValue<U>) EvalT.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    public MaybeTValue<T> filterNot(final Predicate<? super T> fn) {

        return (MaybeTValue<T>) EvalT.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    public MaybeTValue<T> notNull() {

        return (MaybeTValue<T>) EvalT.super.notNull();
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof EvalTValue) {
            return run.equals(((EvalTValue) o).run);
        }
        return false;
    }

}

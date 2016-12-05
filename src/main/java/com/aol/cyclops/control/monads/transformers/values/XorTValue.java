package com.aol.cyclops.control.monads.transformers.values;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
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
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.XorT;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.MonadicValue2;
import com.aol.cyclops.types.Value;
import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.applicative.ApplicativeFunctor;

/**
 * Monad transformer for Xor nested within Scalar data types (e.g. Optional, CompletableFuture, Eval, Maybe)
 * 
 * XorT allows the deeply wrapped Xor to be manipulating within it's nested
 * /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T>
 *            The type contained on the Xor within
 */
public class XorTValue<ST, T> implements XorT<ST, T>, MonadicValue<T>, TransformerValue<T>, Supplier<T>, ConvertableFunctor<T>, Filterable<T>,
        ApplicativeFunctor<T>, Matchable.ValueAndOptionalMatcher<T> {

    private final AnyMValue<Xor<ST, T>> run;

    private XorTValue(final AnyMValue<Xor<ST, T>> run) {
        this.run = run;
    }

    @Override
    public Xor<ST, T> value() {
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
    public AnyMValue<Xor<ST, T>> unwrap() {
        return run;
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.XorT#swap()
     */
    @Override
    public XorTValue<T, ST> swap() {
        return of(run.map(xor -> xor.swap()));
    }

    /**
     * Peek at the current value of the Xor
     * 
     * <pre>
     * {@code 
     *    XorT.of(AnyM.fromStream(Xor.of(10))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek
     *            Consumer to accept current value of Xor
     * @return XorT with peek call
     */
    @Override
    public XorTValue<ST, T> peek(final Consumer<? super T> peek) {
        return of(run.peek(opt -> opt.map(a -> {
            peek.accept(a);
            return a;
        })));
    }

    /**
     * Filter the wrapped Xor
     * 
     * <pre>
     * {@code 
     *    XorT.of(AnyM.fromStream(Xor.of(10))
     *             .filter(t->t!=10);
     *             
     *     //XorT<AnyMValue<Stream<Xor.empty>>>
     * }
     * </pre>
     * 
     * @param test
     *            Predicate to filter the wrapped Xor
     * @return XorT that applies the provided filter
     */
    @Override
    public XorTValue<ST, T> filter(final Predicate<? super T> test) {
        return XorTValue.of(run.map(opt -> opt.filter(test)));
    }

    /**
     * Map the wrapped Xor
     * 
     * <pre>
     * {@code 
     *  XorT.of(AnyM.fromStream(Xor.of(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //XorT<AnyMValue<Stream<Xor[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Xor
     * @return XorT that applies the map function to the wrapped Xor
     */
    @Override
    public <B> XorTValue<ST, B> map(final Function<? super T, ? extends B> f) {
        return new XorTValue<ST, B>(
                                    run.map(o -> o.map(f)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> XorTValue<ST, R> combine(final Value<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new XorTValue<>(
                               run.map(o -> o.combine(app, fn)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> XorTValue<ST, R> zip(final Iterable<? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new XorTValue<>(
                               run.map(o -> o.zip(app, fn)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> XorTValue<ST, R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> app) {
        return new XorTValue<>(
                               run.map(o -> o.zip(fn, app)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    public <U, R> XorTValue<ST, R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (XorTValue<ST, R>) TransformerValue.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    public <U, R> XorTValue<ST, R> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (XorTValue<ST, R>) TransformerValue.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> XorTValue<ST, Tuple2<T, U>> zip(final Stream<? extends U> other) {

        return (XorTValue) TransformerValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> XorTValue<ST, Tuple2<T, U>> zip(final Seq<? extends U> other) {

        return (XorTValue) TransformerValue.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    public <U> XorTValue<ST, Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (XorTValue) TransformerValue.super.zip(other);
    }

    public XorTValue<PStackX<ST>, T> list() {
        return new XorTValue<>(
                               run.map(o -> o.list()));
    }

    public <T2, R> XorTValue<PStackX<ST>, R> combineToList(final Xor<ST, ? extends T2> app, final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new XorTValue<>(
                               run.map(o -> o.combineToList(app, fn)));
    }

    public <T2, R> XorTValue<ST, R> combine(final Xor<? extends ST, ? extends T2> app, final BinaryOperator<ST> semigroup,
            final BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new XorTValue<>(
                               run.map(o -> o.combine(app, semigroup, fn)));
    }

    /**
     * Flat Map the wrapped Xor
     * 
     * <pre>
    * {@code 
    *  XorT.of(AnyM.fromStream(Xor.of(10))
    *             .flatMap(t->Xor.empty();
    *  
    *  
    *  //XorT<AnyMValue<Stream<Xor.empty>>>
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return XorT that applies the flatMap function to the wrapped Xor
     */
    public <ST2, B> XorTValue<ST, B> flatMapT(final Function<? super T, XorTValue<ST2, ? extends B>> f) {

        return of(run.bind(opt -> {
            if (opt.isPrimary())
                return f.apply(opt.get()).run.unwrap();
            return this;
        }));

    }

   
    @Override
    public <B> XorTValue<ST, B> flatMap(final Function<? super T, ? extends MonadicValue<? extends B>> f) {

        return new XorTValue<ST, B>(
                                     run.map(o -> o.flatMap(f)));

    }



    /**
     * Lift a function into one that accepts and returns an XorT This allows
     * multiple monad types to add functionality to existing functions and
     * methods
     * 
     * e.g. to add null handling (via Xor) and iteration (via Stream) to an
     * existing function
     * 
     * <pre>
     * {
     *     &#64;code
     *     Function<Integer, Integer> add2 = i -> i + 2;
     *     Function<XorT<Integer>, XorT<Integer>> optTAdd2 = XorT.lift(add2);
     * 
     *     Stream<Integer> withNulls = Stream.of(1, 2, null);
     *     AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMValue<Xor<Integer>> streamOpt = stream.map(Xor::ofNullable);
     *     List<Integer> results = optTAdd2.apply(XorT.of(streamOpt)).unwrap().<Stream<Xor<Integer>>> unwrap()
     *             .filter(Xor::isPresent).map(Xor::get).collect(Collectors.toList());
     * 
     *     // Arrays.asList(3,4);
     * 
     * }
     * </pre>
     * 
     * 
     * @param fn
     *            Function to enhance with functionality from Xor and another
     *            monad type
     * @return Function that accepts and returns an XorT
     */
    public static <ST, U, R> Function<XorTValue<ST, U>, XorTValue<ST, R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns XorTs This allows
     * multiple monad types to add functionality to existing functions and
     * methods
     * 
     * e.g. to add null handling (via Xor), iteration (via Stream) and
     * asynchronous execution (CompletableFuture) to an existing function
     * 
     * <pre>
     * {
     *     &#64;code
     *     BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
     *     BiFunction<XorT<Integer>, XorT<Integer>, XorT<Integer>> optTAdd2 = XorT.lift2(add);
     * 
     *     Stream<Integer> withNulls = Stream.of(1, 2, null);
     *     AnyMValue<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMValue<Xor<Integer>> streamOpt = stream.map(Xor::ofNullable);
     * 
     *     CompletableFuture<Xor<Integer>> two = CompletableFuture.supplyAsync(() -> Xor.of(2));
     *     AnyMValue<Xor<Integer>> future = AnyM.ofMonad(two);
     *     List<Integer> results = optTAdd2.apply(XorT.of(streamOpt), XorT.of(future)).unwrap()
     *             .<Stream<Xor<Integer>>> unwrap().filter(Xor::isPresent).map(Xor::get)
     *             .collect(Collectors.toList());
     *     // Arrays.asList(3,4);
     * }
     * </pre>
     * 
     * @param fn
     *            BiFunction to enhance with functionality from Xor and
     *            another monad type
     * @return Function that accepts and returns an XorT
     */
    public static <ST, U1, U2, R> BiFunction<XorTValue<ST, U1>, XorTValue<ST, U2>, XorTValue<ST, R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an XorT from an AnyM that contains a monad type that contains
     * type other than Xor The values in the underlying monad will be mapped
     * to Xor<A>
     * 
     * @param anyM
     *            AnyM that doesn't contain a monad wrapping an Xor
     * @return XorT
     */
    public static <ST, A> XorTValue<ST, A> fromAnyM(final AnyMValue<A> anyM) {
        return of(anyM.map(Xor::primary));
    }

    /**
     * Construct an XorT from an AnyM that wraps a monad containing Xors
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Xor
     * @return XorT
     */
    public static <ST, A> XorTValue<ST, A> of(final AnyMValue<Xor<ST, A>> monads) {
        return new XorTValue<>(
                               monads);
    }

    public static <ST, A> XorTValue<ST, A> of(final Xor<ST, A> monads) {
        return XorT.fromOptional(Optional.of(monads));
    }

    public static <A, ST, V extends MonadicValue<Xor<ST, A>>> XorTValue<ST, A> fromValue(final V monadicValue) {
        return of(AnyM.ofValue(monadicValue));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("XorTValue[%s]", run);
    }

    @Override
    public T get() {
        return run.get()
                  .get();
    }

    public boolean isPrimary() {
        return run.orElse(Xor.secondary(null))
                  .isPrimary();
    }

    public boolean isSecondary() {
        return run.orElseGet(Xor.primary(null))
                  .isSecondary();
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.orElseGet(() -> Xor.secondary(null))
                  .stream();
    }

    @Override
    public Iterator<T> iterator() {
        return run.orElse(Xor.secondary(null))
                  .iterator();
    }

    @Override
    public void subscribe(final Subscriber<? super T> s) {
        run.orElse(Xor.secondary(null))
           .subscribe(s);

    }

    @Override
    public boolean test(final T t) {
        return run.get()
                  .test(t);
    }

    public <R> R visit(final Function<?, ? extends R> secondary, final Function<? super T, ? extends R> primary, final Supplier<R> none) {

        if (!isSecondary() && !isPrimary())
            return none.get();
        if (isSecondary())
            return (R) run.get()
                          .swap()
                          .visit((Function) secondary, (Supplier) () -> null);
        return visit(primary, () -> null);
    }

    @Override
    public <R> XorTValue<ST, R> unit(final R value) {
        return of(run.unit(Xor.primary(value)));
    }

    @Override
    public <R> XorTValue<ST, R> empty() {
        return of(run.unit(Xor.secondary(null)));
    }

    public static <ST, PT> XorTValue<ST, PT> emptyOptional() {
        return XorT.fromOptional(Optional.empty());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    public <U> XorTValue<ST, U> cast(final Class<? extends U> type) {
        return (XorTValue<ST, U>) XorT.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    public <R> XorTValue<ST, R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (XorTValue<ST, R>) XorT.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> XorTValue<ST, R> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {
        return (XorTValue<ST, R>) XorT.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    public <U> XorTValue<ST, U> ofType(final Class<? extends U> type) {

        return (XorTValue<ST, U>) XorT.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    public XorTValue<ST, T> filterNot(final Predicate<? super T> fn) {

        return (XorTValue<ST, T>) XorT.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#notNull()
     */
    @Override
    public XorTValue<ST, T> notNull() {

        return (XorTValue<ST, T>) XorT.super.notNull();
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof XorTValue) {
            return run.equals(((XorTValue) o).run);
        }
        return false;
    }

}

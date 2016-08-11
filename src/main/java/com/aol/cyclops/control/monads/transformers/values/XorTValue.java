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
 * Monad transformer for JDK Xor
 * 
 * XorT consists of an AnyM instance that in turns wraps anoter Monad type
 * that contains an Xor
 * 
 * XorT<AnyMValue<*SOME_MONAD_TYPE*<Xor<T>>>>
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
public class XorTValue<ST, T> implements XorT<ST, T>, MonadicValue2<ST, T>, TransformerValue<T>, Supplier<T>, ConvertableFunctor<T>, Filterable<T>,
        ApplicativeFunctor<T>, Matchable.ValueAndOptionalMatcher<T> {

    private final AnyMValue<Xor<ST, T>> run;

    private XorTValue(final AnyMValue<Xor<ST, T>> run) {
        this.run = run;
    }

    public Xor<ST, T> value() {
        return run.get();
    }

    public boolean isValuePresent() {
        return !run.isEmpty();
    }

    /**
     * @return The wrapped AnyM
     */
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
    public XorTValue<ST, T> peek(Consumer<? super T> peek) {
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
    public XorTValue<ST, T> filter(Predicate<? super T> test) {
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
    public <B> XorTValue<ST, B> map(Function<? super T, ? extends B> f) {
        return new XorTValue<ST, B>(
                                    run.map(o -> o.map(f)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#ap(com.aol.cyclops.types.Value, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> XorTValue<ST, R> combine(Value<? extends T2> app, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new XorTValue<>(
                               run.map(o -> o.combine(app, fn)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <T2, R> XorTValue<ST, R> zip(Iterable<? extends T2> app, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new XorTValue<>(
                               run.map(o -> o.zip(app, fn)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.applicative.ApplicativeFunctor#zip(java.util.function.BiFunction, org.reactivestreams.Publisher)
     */
    @Override
    public <T2, R> XorTValue<ST, R> zip(BiFunction<? super T, ? super T2, ? extends R> fn, Publisher<? extends T2> app) {
        return new XorTValue<>(
                               run.map(o -> o.zip(fn, app)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq, java.util.function.BiFunction)
     */
    @Override
    public <U, R> XorTValue<ST, R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (XorTValue<ST, R>) MonadicValue2.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream, java.util.function.BiFunction)
     */
    @Override
    public <U, R> XorTValue<ST, R> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (XorTValue<ST, R>) MonadicValue2.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> XorTValue<ST, Tuple2<T, U>> zip(Stream<? extends U> other) {

        return (XorTValue) MonadicValue2.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> XorTValue<ST, Tuple2<T, U>> zip(Seq<? extends U> other) {

        return (XorTValue) MonadicValue2.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Zippable#zip(java.lang.Iterable)
     */
    @Override
    public <U> XorTValue<ST, Tuple2<T, U>> zip(Iterable<? extends U> other) {

        return (XorTValue) MonadicValue2.super.zip(other);
    }

    public XorTValue<PStackX<ST>, T> list() {
        return new XorTValue<>(
                               run.map(o -> o.list()));
    }

    public <T2, R> XorTValue<PStackX<ST>, R> combineToList(Xor<ST, ? extends T2> app, BiFunction<? super T, ? super T2, ? extends R> fn) {
        return new XorTValue<>(
                               run.map(o -> o.combineToList(app, fn)));
    }

    public <T2, R> XorTValue<ST, R> combine(Xor<? extends ST, ? extends T2> app, BinaryOperator<ST> semigroup,
            BiFunction<? super T, ? super T2, ? extends R> fn) {
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
    public <ST2, B> XorTValue<ST, B> flatMapT(Function<? super T, XorTValue<ST2, ? extends B>> f) {

        return of(run.bind(opt -> {
            if (opt.isPrimary())
                return f.apply(opt.get()).run.unwrap();
            return this;
        }));

    }

    public <ST2, B> XorTValue<ST2, B> flatMap(Function<? super T, ? extends MonadicValue2<? extends ST2, ? extends B>> f) {

        return new XorTValue<ST2, B>(
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
    public static <ST, U, R> Function<XorTValue<ST, U>, XorTValue<ST, R>> lift(Function<? super U, ? extends R> fn) {
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
            BiFunction<? super U1, ? super U2, ? extends R> fn) {
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
    public static <ST, A> XorTValue<ST, A> fromAnyM(AnyMValue<A> anyM) {
        return of(anyM.map(Xor::primary));
    }

    /**
     * Construct an XorT from an AnyM that wraps a monad containing Xors
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Xor
     * @return XorT
     */
    public static <ST, A> XorTValue<ST, A> of(AnyMValue<Xor<ST, A>> monads) {
        return new XorTValue<>(
                               monads);
    }

    public static <ST, A> XorTValue<ST, A> of(Xor<ST, A> monads) {
        return XorT.fromOptional(Optional.of(monads));
    }

    public static <A, ST, V extends MonadicValue<Xor<ST, A>>> XorTValue<ST, A> fromValue(V monadicValue) {
        return of(AnyM.ofValue(monadicValue));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
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
    public void subscribe(Subscriber<? super T> s) {
        run.orElse(Xor.secondary(null))
           .subscribe(s);

    }

    @Override
    public boolean test(T t) {
        return run.get()
                  .test(t);
    }

    public <R> R visit(Function<?, ? extends R> secondary, Function<? super T, ? extends R> primary, Supplier<R> none) {

        if (!isSecondary() && !isPrimary())
            return none.get();
        if (isSecondary())
            return (R) run.get()
                          .swap()
                          .visit((Function) secondary, (Supplier) () -> null);
        return visit(primary, () -> null);
    }

    public <R> XorTValue<ST, R> unit(R value) {
        return of(run.unit(Xor.primary(value)));
    }

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
    public <U> XorTValue<ST, U> cast(Class<? extends U> type) {
        return (XorTValue<ST, U>) XorT.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    public <R> XorTValue<ST, R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (XorTValue<ST, R>) XorT.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> XorTValue<ST, R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, Supplier<? extends R> otherwise) {
        return (XorTValue<ST, R>) XorT.super.patternMatch(case1, otherwise);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
     */
    @Override
    public <U> XorTValue<ST, U> ofType(Class<? extends U> type) {

        return (XorTValue<ST, U>) XorT.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filterNot(java.util.function.Predicate)
     */
    @Override
    public XorTValue<ST, T> filterNot(Predicate<? super T> fn) {

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
    public boolean equals(Object o) {
        if (o instanceof XorTValue) {
            return run.equals(((XorTValue) o).run);
        }
        return false;
    }

}

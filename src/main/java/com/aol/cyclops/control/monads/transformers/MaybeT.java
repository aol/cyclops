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
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.control.monads.transformers.seq.MaybeTSeq;
import com.aol.cyclops.control.monads.transformers.values.MaybeTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Filterable;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.To;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.anyM.AnyMValue;

/**
 * Monad transformer for Maybe
 * 
 * MaybeT consists of an AnyM instance that in turns wraps anoter Monad type
 * that contains an Maybe
 * 

 * MaybeT allows the deeply wrapped Maybe to be manipulating within it's nested
 * /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T>
 *            The type contained on the Maybe within
 */
public interface MaybeT<T> extends To<MaybeT<T>>,Publisher<T>, Functor<T>,Foldable<T>, Filterable<T> {

    public <R> MaybeT<R> unit(R value);

    public <R> MaybeT<R> empty();

    /**
     * @return The wrapped AnyM
     */
    public AnyM<Maybe<T>> unwrap();

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
    public MaybeT<T> peek(Consumer<? super T> peek);

    /**
     * Filter the wrapped Maybe
     * 
     * <pre>
     * {@code 
     *    MaybeT.of(AnyM.fromStream(Maybe.of(10))
     *             .filter(t->t!=10);
     *             
     *     //MaybeT<AnyM<Stream<Maybe.empty>>>
     * }
     * </pre>
     * 
     * @param test
     *            Predicate to filter the wrapped Maybe
     * @return MaybeT that applies the provided filter
     */
    @Override
    public MaybeT<T> filter(Predicate<? super T> test);

    /**
     * Map the wrapped Maybe
     * 
     * <pre>
     * {@code 
     *  MaybeT.of(AnyM.fromStream(Maybe.of(10))
     *             .map(t->t=t+1);
     *  
     *  
     *  //MaybeT<AnyM<Stream<Maybe[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Maybe
     * @return MaybeT that applies the map function to the wrapped Maybe
     */
    @Override
    public <B> MaybeT<B> map(Function<? super T, ? extends B> f);

    public <B> MaybeT<B> flatMap(Function<? super T, ? extends MonadicValue<? extends B>> f);

    /**
     * Flat Map the wrapped Maybe
     * 
     * <pre>
    * {@code 
    *  MaybeT.of(AnyM.fromStream(Maybe.of(10))
    *             .bind(t->MaybeT.empty();
    *  
    *  
    *  //MaybeT<AnyM<Stream<Maybe.empty>>>
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return MaybeT that applies the flatMap function to the wrapped Maybe
     */
    default <B> MaybeT<B> bind(final Function<? super T, MaybeT<? extends B>> f) {

        return of(unwrap().bind(opt -> {
            if (opt.isPresent())
                return f.apply(opt.get())
                        .unwrap()
                        .unwrap();
            return unwrap().unit(Maybe.<B> none())
                           .unwrap();
        }));

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
     * {@code
     *     
     *     Function<Integer, Integer> add2 = i -> i + 2;
     *     Function<MaybeT<Integer>, MaybeT<Integer>> optTAdd2 = MaybeT.lift(add2);
     * 
     *     Stream<Integer> withNulls = Stream.of(1, 2, null);
     *     AnyM<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyM<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
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
    public static <U, R> Function<MaybeT<U>, MaybeT<R>> lift(final Function<? super U, ? extends R> fn) {
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
     * {@code 
     *     
     *     BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
     *     BiFunction<MaybeT<Integer>, MaybeT<Integer>, MaybeT<Integer>> optTAdd2 = MaybeT.lift2(add);
     * 
     *     Stream<Integer> withNulls = Stream.of(1, 2, null);
     *     AnyM<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyM<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
     * 
     *     CompletableFuture<Maybe<Integer>> two = CompletableFuture.supplyAsync(() -> Maybe.of(2));
     *     AnyM<Maybe<Integer>> future = AnyM.ofMonad(two);
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
    public static <U1, U2, R> BiFunction<MaybeT<U1>, MaybeT<U2>, MaybeT<R>> lift2(final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
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
    public static <A> MaybeT<A> fromAnyM(final AnyM<A> anyM) {
        return of(anyM.map(Maybe::ofNullable));
    }

    public static <A> MaybeTValue<A> fromAnyMValue(final AnyMValue<A> anyM) {
        return MaybeTValue.fromAnyM(anyM);
    }

    public static <A> MaybeTSeq<A> fromAnyMSeq(final AnyMSeq<A> anyM) {
        return MaybeTSeq.fromAnyM(anyM);
    }

    public static <A> MaybeTSeq<A> fromIterable(final Iterable<Maybe<A>> iterableOfMaybes) {
        return MaybeTSeq.of(AnyM.fromIterable(iterableOfMaybes));
    }

    public static <A> MaybeTSeq<A> fromStream(final Stream<Maybe<A>> streamOfMaybes) {
        return MaybeTSeq.of(AnyM.fromStream(streamOfMaybes));
    }

    public static <A> MaybeTSeq<A> fromPublisher(final Publisher<Maybe<A>> publisherOfMaybes) {
        return MaybeTSeq.of(AnyM.fromPublisher(publisherOfMaybes));
    }

    public static <A, V extends MonadicValue<Maybe<A>>> MaybeTValue<A> fromValue(final V monadicValue) {
        return MaybeTValue.fromValue(monadicValue);
    }

    public static <A> MaybeTValue<A> fromOptional(final Optional<Maybe<A>> optional) {
        return MaybeTValue.of(AnyM.fromOptional(optional));
    }

    public static <A> MaybeTValue<A> fromFuture(final CompletableFuture<Maybe<A>> future) {
        return MaybeTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A> MaybeTValue<A> fromIterableValue(final Iterable<Maybe<A>> iterableOfMaybes) {
        return MaybeTValue.of(AnyM.fromIterableValue(iterableOfMaybes));
    }

    /**
     * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <A> MaybeT<A> of(final AnyM<Maybe<A>> monads) {
        return Matchables.anyM(monads)
                         .visit(v -> MaybeTValue.of(v), s -> MaybeTSeq.of(s));
    }

    public static <T> MaybeTValue<T> emptyOptional() {
        return fromOptional(Optional.empty());
    }

    public static <T> MaybeTSeq<T> emptyList() {
        return MaybeT.fromIterable(ListX.of());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#cast(java.lang.Class)
     */
    @Override
    default <U> MaybeT<U> cast(final Class<? extends U> type) {
        return (MaybeT<U>) Functor.super.cast(type);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#trampoline(java.util.function.Function)
     */
    @Override
    default <R> MaybeT<R> trampoline(final Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (MaybeT<R>) Functor.super.trampoline(mapper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    default <R> MaybeT<R> patternMatch(final Function<CheckValue1<T, R>, CheckValue1<T, R>> case1, final Supplier<? extends R> otherwise) {
        return (MaybeT<R>) Functor.super.patternMatch(case1, otherwise);
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

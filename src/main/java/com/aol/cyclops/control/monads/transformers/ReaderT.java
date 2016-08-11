package com.aol.cyclops.control.monads.transformers;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.control.monads.transformers.seq.ReaderTSeq;
import com.aol.cyclops.control.monads.transformers.values.ReaderTValue;
import com.aol.cyclops.types.MonadicValue;

/**
 * Monad transformer for JDK Maybe
 * 
 * MaybeT consists of an AnyM instance that in turns wraps anoter Monad type
 * that contains an Maybe
 * 
 * MaybeT<AnyM<*SOME_MONAD_TYPE*<Maybe<T>>>>
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
public interface ReaderT<T, R> extends Publisher<T> {

    /**
     * @return The wrapped AnyM
     */
    public AnyM<Reader<T, R>> unwrap();

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
    public ReaderTValue<T, R> peek(Consumer<? super R> peek);

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
    public ReaderT<T, R> filter(Predicate<? super R> test);

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
    public <B> ReaderT<T, B> map(Function<? super R, ? extends B> f);

    /**
     * Flat Map the wrapped Maybe
     * 
     * <pre>
    * {@code 
    *  MaybeT.of(AnyM.fromStream(Maybe.of(10))
    *             .bind(t->Maybe.empty();
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
    default <B> ReaderT<T, B> bind(Function<? super R, ReaderT<? extends T, B>> f) {

        return of(unwrap().bind(reader -> reader.flatMap(r -> f.apply(r)
                                                               .unwrap()
                                                               .unwrap())));

    }

    public <B> ReaderT<B, R> flatMap(Function<? super T, ? extends Reader<? extends B, R>> f);

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
    public static <T, U, R> Function<ReaderT<T, U>, ReaderT<T, R>> lift(Function<? super U, ? extends R> fn) {
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
    public static <T, U1, U2, R> BiFunction<ReaderT<T, U1>, ReaderT<T, U2>, ReaderT<T, R>> lift2(BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.bind(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <A, R> ReaderT<A, R> of(AnyM<Reader<A, R>> monads) {
        return (ReaderT<A, R>) Matchables.anyM(monads)
                                         .visit(v -> ReaderTValue.<A, R> of(v), s -> ReaderTSeq.<A, R> of(s));

    }

    public static <A, R> ReaderTSeq<A, R> fromIterable(Iterable<Reader<A, R>> iterableOfEvals) {
        return ReaderTSeq.of(AnyM.fromIterable(iterableOfEvals));
    }

    public static <A, R> ReaderTSeq<A, R> fromStream(Stream<Reader<A, R>> streamOfEvals) {
        return ReaderTSeq.of(AnyM.fromStream(streamOfEvals));
    }

    public static <A, R> ReaderTSeq<A, R> fromPublisher(Publisher<Reader<A, R>> publisherOfEvals) {
        return ReaderTSeq.of(AnyM.fromPublisher(publisherOfEvals));
    }

    public static <A, R, V extends MonadicValue<Reader<A, R>>> ReaderTValue<A, R> fromValue(V monadicValue) {
        return ReaderTValue.fromValue(monadicValue);
    }

    public static <A, R> ReaderTValue<A, R> fromOptional(Optional<Reader<A, R>> optional) {
        return ReaderTValue.of(AnyM.fromOptional(optional));
    }

    public static <A, R> ReaderTValue<A, R> fromFuture(CompletableFuture<Reader<A, R>> future) {
        return ReaderTValue.of(AnyM.fromCompletableFuture(future));
    }

    public static <A, R> ReaderTValue<A, R> fromIterableValue(Iterable<Reader<A, R>> iterableOfEvals) {
        return ReaderTValue.of(AnyM.fromIterableValue(iterableOfEvals));
    }

}

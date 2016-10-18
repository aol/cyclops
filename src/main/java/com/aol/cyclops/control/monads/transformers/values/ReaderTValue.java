package com.aol.cyclops.control.monads.transformers.values;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FluentFunctions;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.anyM.AnyMValue;

/**
 * Monad transformer for Reader nested within Scalar data types (e.g. Optional, CompletableFuture, Eval, Maybe)
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> Type of input into the nested Reader Monad(s)
 * @param <R> Return type in the nested Reader Monad(s)
 * 
 */
public class ReaderTValue<T, R> implements Function<T, R> {

    private final AnyMValue<Reader<T, R>> run;

    private ReaderTValue(final AnyMValue<Reader<T, R>> run) {
        this.run = run;
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyMValue<Reader<T, R>> unwrap() {
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
    public ReaderTValue<T, R> peek(final Consumer<? super R> peek) {
        return of(run.peek(opt -> opt.map(a -> {
            peek.accept(a);
            return a;
        })));
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
    
    public ReaderT<T,R> filter(Predicate<? super T> test) {
        return of(run.map(opt -> opt.filter(test)));
    } */

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
    public <B> ReaderTValue<T, B> map(final Function<? super R, ? extends B> f) {
        return new ReaderTValue<T, B>(
                                      run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Reader
     * 
     
     * 
     * @param f
     *            FlatMap function
     * @return ReaderT that applies the flatMap function to the wrapped Maybe
     */
    public <B> ReaderTValue<T, B> flatMapT(final Function<? super R, ReaderTValue<T, B>> mapper) {

        return of(run.bind(reader -> reader.flatMap(r -> mapper.apply(r).run.unwrap())));

    }

    public <B> ReaderTValue<T, B> flatMap(final Function<? super R, ? extends Reader<T, B>> f) {

        return new ReaderTValue<T, B>(
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
    public static <T, U, R> Function<ReaderTValue<T, U>, ReaderTValue<T, R>> lift(final Function<? super U, ? extends R> fn) {
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
    public static <T, U1, U2, R> BiFunction<ReaderTValue<T, U1>, ReaderTValue<T, U2>, ReaderTValue<T, R>> lift2(
            final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    public static <T, A, V extends MonadicValue<Reader<T, A>>> ReaderTValue<T, A> fromValue(final V monadicValue) {
        return of(AnyM.ofValue(monadicValue));
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
    public static <T, A> ReaderTValue<T, A> fromAnyM(final AnyMValue<Function<T, A>> anyM) {
        return of(anyM.map(FluentFunctions::of));
    }

    /**
     * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <T, A> ReaderTValue<T, A> of(final AnyMValue<Reader<T, A>> monads) {

        return new ReaderTValue<>(
                                  monads);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return run.toString();
    }

    @Override
    public R apply(final T t) {
        return run.get()
                  .apply(t);
    }

    public Maybe<R> maybeApply(final T t) {
        return run.toMaybe()
                  .map(fn -> fn.apply(t));
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ReaderTValue) {
            return run.equals(((ReaderTValue) o).run);
        }
        return false;
    }

}

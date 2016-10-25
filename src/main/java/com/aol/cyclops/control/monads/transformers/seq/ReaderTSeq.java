package com.aol.cyclops.control.monads.transformers.seq;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.FluentFunctions;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Reader;
import com.aol.cyclops.types.anyM.AnyMSeq;

/**
 * Monad transformer for Reader nested within Sequential or non-scalar data types (e.g. Lists, Streams etc)
 * 
 * 
 * @author johnmcclean
 *
 * @param <T> Type of input into the nested Reader Monad(s)
 * @param <R> Return type in the nested Reader Monad(s)
 */
public class ReaderTSeq<T, R> {

    private final AnyMSeq<Reader<T, R>> run;

    private ReaderTSeq(final AnyMSeq<Reader<T, R>> run) {
        this.run = run;
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyMSeq<Reader<T, R>> unwrap() {
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
    public ReaderTSeq<T, R> peek(final Consumer<? super R> peek) {
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
     *     //MaybeT<AnyMSeq<Stream<Maybe.empty>>>
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
     *  //MaybeT<AnyMSeq<Stream<Maybe[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Maybe
     * @return MaybeT that applies the map function to the wrapped Maybe
     */
    public <B> ReaderTSeq<T, B> map(final Function<? super R, ? extends B> f) {
        return new ReaderTSeq<T, B>(
                                    run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Reader
     * 
     
     * 
     * @param mapper
     *            FlatMap function
     * @return ReaderT that applies the flatMap function to the wrapped Maybe
     */
    public <B> ReaderTSeq<T, B> flatMapT(final Function<? super R, ReaderTSeq<T, B>> mapper) {

        return of(run.bind(reader -> reader.flatMap(r -> mapper.apply(r).run.unwrap())));

    }

    public <B> ReaderTSeq<T, B> flatMap(final Function<? super R, ? extends Reader<T, B>> f) {

        return new ReaderTSeq<T, B>(
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
     *     AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMSeq<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
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
    public static <T, U, R> Function<ReaderTSeq<T, U>, ReaderTSeq<T, R>> lift(final Function<? super U, ? extends R> fn) {
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
     *     AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMSeq<Maybe<Integer>> streamOpt = stream.map(Maybe::ofNullable);
     * 
     *     CompletableFuture<Maybe<Integer>> two = CompletableFuture.supplyAsync(() -> Maybe.of(2));
     *     AnyMSeq<Maybe<Integer>> future = AnyM.ofMonad(two);
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
    public static <T, U1, U2, R> BiFunction<ReaderTSeq<T, U1>, ReaderTSeq<T, U2>, ReaderTSeq<T, R>> lift2(
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
    public static <T, A> ReaderTSeq<T, A> fromAnyM(final AnyMSeq<Function<T, A>> anyM) {
        return of(anyM.map(FluentFunctions::of));
    }

    /**
     * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <T, A> ReaderTSeq<T, A> of(final AnyMSeq<Reader<T, A>> monads) {

        return new ReaderTSeq<>(
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

    public R apply(final T t, final Monoid<R> combiner) {
        return run.map(r -> r.apply(t))
                  .reduce(combiner);

    }

    public ReactiveSeq<R> streamApply(final T t) {
        return run.map(fn -> fn.apply(t))
                  .stream();
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ReaderTSeq) {
            return run.equals(((ReaderTSeq) o).run);
        }
        return false;
    }

}

package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Collectable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.control.monads.transformers.values.EvalTValue;
import com.aol.cyclops.control.monads.transformers.values.ValueTransformerSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.Foldable;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad transformer for JDK Maybe
 * 
 * MaybeT consists of an AnyM instance that in turns wraps anoter Monad type
 * that contains an Maybe
 * 
 * MaybeT<AnyMSeq<*SOME_MONAD_TYPE*<Maybe<T>>>>
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
public class EvalTSeq<T> implements EvalT<T>,
                                    ValueTransformerSeq<T>,
                                    Foldable<T>,
                                    ConvertableSequence<T>,
                                    CyclopsCollectable<T>,
                                    Sequential<T>{

    private final AnyMSeq<Eval<T>> run;

    private EvalTSeq(final AnyMSeq<Eval<T>> run) {
        this.run = run;
    }

    /**
     * @return The wrapped AnyM
     */
    public AnyMSeq<Eval<T>> unwrap() {
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
    public EvalTSeq<T> peek(Consumer<? super T> peek) {
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
     *     //MaybeT<AnyMSeq<Stream<Maybe.empty>>>
     * }
     * </pre>
     * 
     * @param test
     *            Predicate to filter the wrapped Maybe
     * @return MaybeT that applies the provided filter
     */
    public MaybeTSeq<T> filter(Predicate<? super T> test) {
        return MaybeTSeq.of(run.map(opt -> opt.filter(test)));
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
     *  //MaybeT<AnyMSeq<Stream<Maybe[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Maybe
     * @return MaybeT that applies the map function to the wrapped Maybe
     */
    public <B> EvalTSeq<B> map(Function<? super T, ? extends B> f) {
        return new EvalTSeq<B>(run.map(o -> o.map(f)));
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
    *  //MaybeT<AnyMSeq<Stream<Maybe.empty>>>
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return MaybeT that applies the flatMap function to the wrapped Maybe
     */
    public <B> EvalTSeq<B> flatMapT(Function<? super T, EvalTSeq<? extends B>> f) {

        return of(run.bind(opt -> {
                return f.apply(opt.get()).run.unwrap();
           
        }));

    }
    public <B> EvalTSeq<B> flatMap(Function<? super T, ? extends Eval<? extends B>> f) {

        return new EvalTSeq<B>(run.map(o -> o.flatMap(f)));

    }
    @Override
    public <T>  EvalTSeq<T> unitStream(ReactiveSeq<T> traversable) {
        return EvalT.fromStream(traversable.map(Eval::now));
       
    }

    @Override
    public <T>  EvalTSeq<T> unitAnyM(AnyM<Traversable<T>> traversable) {
        return of((AnyMSeq)traversable.map(t->Eval.fromIterable(t)));
    }
    @Override
    public AnyMSeq<? extends Traversable<T>> transformerStream() {
       
        return AnyM.fromList(ListX.of(stream().peek(System.out::println)));
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
    public static <U, R> Function<EvalTSeq<U>, EvalTSeq<R>> lift(Function<? super U, ? extends R> fn) {
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
    public static <U1, U2, R> BiFunction<EvalTSeq<U1>, EvalTSeq<U2>, EvalTSeq<R>> lift2(BiFunction<? super U1,? super U2, ? extends R> fn) {
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
    public static <A> EvalTSeq<A> fromAnyM(AnyMSeq<A> anyM) {
        return of(anyM.map(a->Eval.later(()->a)));
    }

    /**
     * Construct an MaybeT from an AnyM that wraps a monad containing Maybes
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Maybe
     * @return MaybeT
     */
    public static <A> EvalTSeq<A> of(AnyMSeq<Eval<A>> monads) {
        return new EvalTSeq<>(monads);
    }
    public static <A> EvalTSeq<A> of(Eval<A> monads){
        return EvalT.fromIterable(ListX.of(monads));
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return String.format("EvalTSeq[%s]", run );
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public EvalTSeq<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.combine(predicate, op);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(int)
     */
    @Override
    public EvalTSeq<T> cycle(int times) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.cycle(times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public EvalTSeq<T> cycle(Monoid<T> m, int times) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.cycle(m, times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> cycleWhile(Predicate<? super T> predicate) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.cycleWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> cycleUntil(Predicate<? super T> predicate) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.cycleUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> EvalTSeq<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (EvalTSeq<R>)ValueTransformerSeq.super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> EvalTSeq<Tuple2<T, U>> zipStream(Stream<? extends U> other) {
       
        return (EvalTSeq)ValueTransformerSeq.super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> EvalTSeq<Tuple2<T, U>> zip(Seq<? extends U> other) {
       
        return (EvalTSeq)ValueTransformerSeq.super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> EvalTSeq<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (EvalTSeq)ValueTransformerSeq.super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> EvalTSeq<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (EvalTSeq<Tuple4<T, T2, T3, T4>>)ValueTransformerSeq.super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipWithIndex()
     */
    @Override
    public EvalTSeq<Tuple2<T, Long>> zipWithIndex() {
       
        return (EvalTSeq<Tuple2<T, Long>>)ValueTransformerSeq.super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int)
     */
    @Override
    public EvalTSeq<ListX<T>> sliding(int windowSize) {
       
        return (EvalTSeq<ListX<T>>)ValueTransformerSeq.super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int, int)
     */
    @Override
    public EvalTSeq<ListX<T>> sliding(int windowSize, int increment) {
       
        return (EvalTSeq<ListX<T>>)ValueTransformerSeq.super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> EvalTSeq<C> grouped(int size, Supplier<C> supplier) {
       
        return (EvalTSeq<C> )ValueTransformerSeq.super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (EvalTSeq<ListX<T>>)ValueTransformerSeq.super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public EvalTSeq<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
       
        return (EvalTSeq<ListX<T>>)ValueTransformerSeq.super.groupedStatefullyWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (EvalTSeq<ListX<T>>)ValueTransformerSeq.super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> EvalTSeq<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
       
        return (EvalTSeq<C>)ValueTransformerSeq.super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> EvalTSeq<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
       
        return (EvalTSeq<C>)ValueTransformerSeq.super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int)
     */
    @Override
    public EvalTSeq<ListX<T>> grouped(int groupSize) {
       
        return ( EvalTSeq<ListX<T>>)ValueTransformerSeq.super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> EvalTSeq<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (EvalTSeq)ValueTransformerSeq.super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function)
     */
    @Override
    public <K> EvalTSeq<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (EvalTSeq)ValueTransformerSeq.super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#distinct()
     */
    @Override
    public EvalTSeq<T> distinct() {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public EvalTSeq<T> scanLeft(Monoid<T> monoid) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> EvalTSeq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
       
        return (EvalTSeq<U>)ValueTransformerSeq.super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public EvalTSeq<T> scanRight(Monoid<T> monoid) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> EvalTSeq<U> scanRight(U identity, BiFunction<? super T, ? super U,? extends U> combiner) {
       
        return (EvalTSeq<U>)ValueTransformerSeq.super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted()
     */
    @Override
    public EvalTSeq<T> sorted() {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.Comparator)
     */
    @Override
    public EvalTSeq<T> sorted(Comparator<? super T> c) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> takeWhile(Predicate<? super T> p) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> dropWhile(Predicate<? super T> p) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> takeUntil(Predicate<? super T> p) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> dropUntil(Predicate<? super T> p) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropRight(int)
     */
    @Override
    public EvalTSeq<T> dropRight(int num) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeRight(int)
     */
    @Override
    public EvalTSeq<T> takeRight(int num) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skip(long)
     */
    @Override
    public EvalTSeq<T> skip(long num) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> skipWhile(Predicate<? super T> p) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> skipUntil(Predicate<? super T> p) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limit(long)
     */
    @Override
    public EvalTSeq<T> limit(long num) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> limitWhile(Predicate<? super T> p) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> limitUntil(Predicate<? super T> p) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#intersperse(java.lang.Object)
     */
    @Override
    public EvalTSeq<T> intersperse(T value) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#reverse()
     */
    @Override
    public EvalTSeq<T> reverse() {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle()
     */
    @Override
    public EvalTSeq<T> shuffle() {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipLast(int)
     */
    @Override
    public EvalTSeq<T> skipLast(int num) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitLast(int)
     */
    @Override
    public EvalTSeq<T> limitLast(int num) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    public EvalTSeq<T> onEmpty(T value) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public EvalTSeq<T> onEmptyGet(Supplier<? extends T> supplier) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> EvalTSeq<T> onEmptyThrow(Supplier<? extends X> supplier) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle(java.util.Random)
     */
    @Override
    public EvalTSeq<T> shuffle(Random random) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#slice(long, long)
     */
    @Override
    public EvalTSeq<T> slice(long from, long to) {
       
        return (EvalTSeq<T>)ValueTransformerSeq.super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> EvalTSeq<T> sorted(Function<? super T, ? extends U> function) {
        return (EvalTSeq)ValueTransformerSeq.super.sorted(function);
    }
    
   

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream().map(e->e.get());
    }

    @Override
    public Iterator<T> iterator() {
       return stream().iterator();
    }

    public <R> EvalTSeq<R> unitIterator(Iterator<R> it){
        return of(run.unitIterator(it).map(i->Eval.now(i)));
    }
    public <R> EvalTSeq<R> unit(R value){
       return of(run.unit(Eval.now(value)));
    }
    public <R> EvalTSeq<R> empty(){
        return of(run.unit(Eval.later(()->null)));
     }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<T> collectable() {
        return stream();
    }
    public boolean isSeqPresent() {
        return !run.isEmpty();
     }
     public static <T> EvalTSeq<T> emptyList() {
         return EvalT.fromIterable(ListX.of());
     }
     @Override
     public int hashCode(){
         return run.hashCode();
     }
     
     @Override
     public boolean equals(Object o){
         if(o instanceof EvalTSeq){
             return run.equals( ((EvalTSeq)o).run);
         }
         return false;
     }
    
    
 
}

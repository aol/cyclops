package com.aol.cyclops.control.monads.transformers.seq;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
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
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.control.monads.transformers.values.ValueTransformerSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.MonadicValue;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * 
 * Monad Transformer for Evals nested within Sequential or non-scalar data types (e.g. Lists, Streams etc)


 * @author johnmcclean
 *
 * @param <T> Type of data stored inside the nested Evals
 */
public class EvalTSeq<T>
        implements EvalT<T>, ValueTransformerSeq<T>, IterableFoldable<T>, ConvertableSequence<T>, CyclopsCollectable<T>, Sequential<T> {

    private final AnyMSeq<Eval<T>> run;

    private EvalTSeq(final AnyMSeq<Eval<T>> run) {
        this.run = run;
    }

    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyMSeq<Eval<T>> unwrap() {
        return run;
    }

    /**
     * Peek at the current value of the Eval
     * 
     * <pre>
     * {@code 
     *    EvalT.fromStream(Stream.of(Eval.now(10)))
     *             .peek(System.out::println);
     *             
     *     //prints 10        
     * }
     * </pre>
     * 
     * @param peek
     *            Consumer to accept current value of Eval
     * @return EvalT with peek call
     */
    @Override
    public EvalTSeq<T> peek(final Consumer<? super T> peek) {
        return map(a -> {
            peek.accept(a);
            return a;
        });
    }

    /**
     * Filter the wrapped Eval
     * 
     * <pre>
     * {@code 
     *    EvalT.fromStream(Stream.of(Eval.now(10)))
     *             .filter(t->t!=10);
     *             
     *    //EvalT[Stream[Eval[10]]] 
     * }
     * </pre>
     * 
     * @param test
     *            Predicate to filter the wrapped Eval
     * @return MaybeT that applies the provided filter
     */
    @Override
    public MaybeTSeq<T> filter(final Predicate<? super T> test) {
        return MaybeTSeq.of(run.map(opt -> opt.filter(test)));
    }

    /**
     * Map the wrapped Eval
     * 
     * <pre>
     * {@code 
     *  EvalT.fromStream(Stream.of(Eval.now(10)))
     *             .map(t->t=t+1);
     *  
     *  
     *  //EvalT[Stream[Eval[11]]] 
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Eval
     * @return EvalT that applies the map function to the wrapped Eval
     */
    @Override
    public <B> EvalTSeq<B> map(final Function<? super T, ? extends B> f) {
        return new EvalTSeq<B>(
                               run.map(o -> o.map(f)));
    }

    /**
     * Flat Map the wrapped Eval
     * 
     * <pre>
    * {@code 
    *  EvalT.fromStream(Stream.of(Eval.now(10)))
    *       .flatMap(t->Maybe.just(i->i+1));
    *  
    *  
    *  EvalT[Stream[Eval[11]]]
    *  
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return EvalT that applies the flatMap function to the wrapped Eval
     */
    public <B> EvalTSeq<B> flatMapT(final Function<? super T, EvalTSeq<? extends B>> f) {

        return of(run.bind(opt -> {
            return f.apply(opt.get()).run.unwrap();

        }));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.EvalT#flatMap(java.util.function.Function)
     */
    @Override
    public <B> EvalTSeq<B> flatMap(final Function<? super T, ? extends MonadicValue<? extends B>> f) {

        return new EvalTSeq<B>(
                               run.map(o -> o.flatMap(f)));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.ValueTransformerSeq#unitStream(com.aol.cyclops.control.ReactiveSeq)
     */
    @Override
    public <T> EvalTSeq<T> unitStream(final ReactiveSeq<T> traversable) {
        return EvalT.fromStream(traversable.map(Eval::now));

    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#unitAnyM(com.aol.cyclops.control.AnyM)
     */
    @Override
    public <T> EvalTSeq<T> unitAnyM(final AnyM<Traversable<T>> traversable) {
        return of((AnyMSeq) traversable.map(t -> Eval.fromIterable(t)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#transformerStream()
     */
    @Override
    public AnyMSeq<? extends Traversable<T>> transformerStream() {

        return AnyM.fromList(ListX.of(stream()));
    }

    /**
     * Lift a function into one that accepts and returns an EvalT This allows
     * multiple monad types to add functionality to existing functions and
     * methods
     * 
     * e.g. to add null handling (via Maybe) and iteration (via Stream) to an
     * existing function
     * 
     * 
     * @param fn
     *            Function to enhance with functionality from Eval and another
     *            monad type
     * @return Function that accepts and returns an EvalT
     */
    public static <U, R> Function<EvalTSeq<U>, EvalTSeq<R>> lift(final Function<? super U, ? extends R> fn) {
        return optTu -> optTu.map(input -> fn.apply(input));
    }

    /**
     * Lift a BiFunction into one that accepts and returns EvalTs This allows
     * multiple monad types to add functionality to existing functions and
     * methods
     * 
     * e.g. to add null handling (via Maybe), iteration (via Stream) and
     * asynchronous execution (CompletableFuture) to an existing function
     * 
    
     * 
     * @param fn
     *            BiFunction to enhance with functionality from Eval and
     *            another monad type
     * @return Function that accepts and returns an EvalT
     */
    public static <U1, U2, R> BiFunction<EvalTSeq<U1>, EvalTSeq<U2>, EvalTSeq<R>> lift2(final BiFunction<? super U1, ? super U2, ? extends R> fn) {
        return (optTu1, optTu2) -> optTu1.flatMapT(input1 -> optTu2.map(input2 -> fn.apply(input1, input2)));
    }

    /**
     * Construct an EvalT from an AnyM that contains a monad. The values in the underlying monad will be mapped
     * to Eval<A>
     * 
     * @param anyM
     *            AnyM that doesn't contain a monad wrapping an Eval
     * @return EvalT
     */
    public static <A> EvalTSeq<A> fromAnyM(final AnyMSeq<A> anyM) {
        return of(anyM.map(a -> Eval.later(() -> a)));
    }

    /**
     * Construct an EvalT from an AnyM that wraps a monad containing Evals
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Eval
     * @return EvalT
     */
    public static <A> EvalTSeq<A> of(final AnyMSeq<Eval<A>> monads) {
        return new EvalTSeq<>(
                              monads);
    }

    public static <A> EvalTSeq<A> of(final Eval<A> monads) {
        return EvalT.fromIterable(ListX.of(monads));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("EvalTSeq[%s]", run);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public EvalTSeq<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(int)
     */
    @Override
    public EvalTSeq<T> cycle(final int times) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public EvalTSeq<T> cycle(final Monoid<T> m, final int times) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> cycleWhile(final Predicate<? super T> predicate) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> cycleUntil(final Predicate<? super T> predicate) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> EvalTSeq<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (EvalTSeq<R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    @Override
    public <U, R> EvalTSeq<R> zip(final Seq<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (EvalTSeq<R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    @Override
    public <U, R> EvalTSeq<R> zip(final Stream<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (EvalTSeq<R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> EvalTSeq<Tuple2<T, U>> zip(final Stream<? extends U> other) {

        return (EvalTSeq) ValueTransformerSeq.super.zip(other);
    }

    @Override
    public <U> EvalTSeq<Tuple2<T, U>> zip(final Iterable<? extends U> other) {

        return (EvalTSeq) ValueTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> EvalTSeq<Tuple2<T, U>> zip(final Seq<? extends U> other) {

        return (EvalTSeq) ValueTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> EvalTSeq<Tuple3<T, S, U>> zip3(final Stream<? extends S> second, final Stream<? extends U> third) {

        return (EvalTSeq) ValueTransformerSeq.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> EvalTSeq<Tuple4<T, T2, T3, T4>> zip4(final Stream<? extends T2> second, final Stream<? extends T3> third,
            final Stream<? extends T4> fourth) {

        return (EvalTSeq) ValueTransformerSeq.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipWithIndex()
     */
    @Override
    public EvalTSeq<Tuple2<T, Long>> zipWithIndex() {

        return (EvalTSeq<Tuple2<T, Long>>) ValueTransformerSeq.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int)
     */
    @Override
    public EvalTSeq<ListX<T>> sliding(final int windowSize) {

        return (EvalTSeq<ListX<T>>) ValueTransformerSeq.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int, int)
     */
    @Override
    public EvalTSeq<ListX<T>> sliding(final int windowSize, final int increment) {

        return (EvalTSeq<ListX<T>>) ValueTransformerSeq.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> EvalTSeq<C> grouped(final int size, final Supplier<C> supplier) {

        return (EvalTSeq<C>) ValueTransformerSeq.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<ListX<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (EvalTSeq<ListX<T>>) ValueTransformerSeq.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    public EvalTSeq<ListX<T>> groupedStatefullyUntil(final BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (EvalTSeq<ListX<T>>) ValueTransformerSeq.super.groupedStatefullyUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<ListX<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (EvalTSeq<ListX<T>>) ValueTransformerSeq.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> EvalTSeq<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (EvalTSeq<C>) ValueTransformerSeq.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> EvalTSeq<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (EvalTSeq<C>) ValueTransformerSeq.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int)
     */
    @Override
    public EvalTSeq<ListX<T>> grouped(final int groupSize) {

        return (EvalTSeq<ListX<T>>) ValueTransformerSeq.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> EvalTSeq<Tuple2<K, D>> grouped(final Function<? super T, ? extends K> classifier, final Collector<? super T, A, D> downstream) {

        return (EvalTSeq) ValueTransformerSeq.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function)
     */
    @Override
    public <K> EvalTSeq<Tuple2<K, Seq<T>>> grouped(final Function<? super T, ? extends K> classifier) {

        return (EvalTSeq) ValueTransformerSeq.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#distinct()
     */
    @Override
    public EvalTSeq<T> distinct() {

        return (EvalTSeq<T>) ValueTransformerSeq.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public EvalTSeq<T> scanLeft(final Monoid<T> monoid) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> EvalTSeq<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {

        return (EvalTSeq<U>) ValueTransformerSeq.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public EvalTSeq<T> scanRight(final Monoid<T> monoid) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> EvalTSeq<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (EvalTSeq<U>) ValueTransformerSeq.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted()
     */
    @Override
    public EvalTSeq<T> sorted() {

        return (EvalTSeq<T>) ValueTransformerSeq.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.Comparator)
     */
    @Override
    public EvalTSeq<T> sorted(final Comparator<? super T> c) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> takeWhile(final Predicate<? super T> p) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> dropWhile(final Predicate<? super T> p) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> takeUntil(final Predicate<? super T> p) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> dropUntil(final Predicate<? super T> p) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropRight(int)
     */
    @Override
    public EvalTSeq<T> dropRight(final int num) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeRight(int)
     */
    @Override
    public EvalTSeq<T> takeRight(final int num) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skip(long)
     */
    @Override
    public EvalTSeq<T> skip(final long num) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> skipWhile(final Predicate<? super T> p) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> skipUntil(final Predicate<? super T> p) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limit(long)
     */
    @Override
    public EvalTSeq<T> limit(final long num) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> limitWhile(final Predicate<? super T> p) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    public EvalTSeq<T> limitUntil(final Predicate<? super T> p) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#intersperse(java.lang.Object)
     */
    @Override
    public EvalTSeq<T> intersperse(final T value) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#reverse()
     */
    @Override
    public EvalTSeq<T> reverse() {

        return (EvalTSeq<T>) ValueTransformerSeq.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle()
     */
    @Override
    public EvalTSeq<T> shuffle() {

        return (EvalTSeq<T>) ValueTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipLast(int)
     */
    @Override
    public EvalTSeq<T> skipLast(final int num) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitLast(int)
     */
    @Override
    public EvalTSeq<T> limitLast(final int num) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    public EvalTSeq<T> onEmpty(final T value) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public EvalTSeq<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> EvalTSeq<T> onEmptyThrow(final Supplier<? extends X> supplier) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle(java.util.Random)
     */
    @Override
    public EvalTSeq<T> shuffle(final Random random) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#slice(long, long)
     */
    @Override
    public EvalTSeq<T> slice(final long from, final long to) {

        return (EvalTSeq<T>) ValueTransformerSeq.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> EvalTSeq<T> sorted(final Function<? super T, ? extends U> function) {
        return (EvalTSeq) ValueTransformerSeq.super.sorted(function);
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream()
                  .map(e -> e.get());
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    public <R> EvalTSeq<R> unitIterator(final Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Eval.now(i)));
    }

    @Override
    public <R> EvalTSeq<R> unit(final R value) {
        return of(run.unit(Eval.now(value)));
    }

    @Override
    public <R> EvalTSeq<R> empty() {
        return of(run.unit(Eval.later(() -> null)));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.CyclopsCollectable#collectable()
     */
    @Override
    public Collectable<T> collectable() {
        return stream();
    }

    @Override
    public boolean isSeqPresent() {
        return !run.isEmpty();
    }

    public static <T> EvalTSeq<T> emptyList() {
        return EvalT.fromIterable(ListX.of());
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof EvalTSeq) {
            return run.equals(((EvalTSeq) o).run);
        }
        return false;
    }

}

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
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.XorT;
import com.aol.cyclops.control.monads.transformers.values.ValueTransformerSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.IterableFoldable;
import com.aol.cyclops.types.MonadicValue2;
import com.aol.cyclops.types.Sequential;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.AnyMSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * Monad transformer for JDK Xor
 * 
 * XorT consists of an AnyM instance that in turns wraps anoter Monad type
 * that contains an Xor
 * <pre>
 * {@code 
 * XorT<AnyMSeq<*SOME_MONAD_TYPE*<Xor<T>>>>
 * }</pre>
 * XorT allows the deeply wrapped Xor to be manipulating within it's nested
 * /contained context
 * 
 * 
 * @author johnmcclean
 *
 * @param <T>
 *            The type contained on the Xor within
 */
public class XorTSeq<ST, T>
        implements XorT<ST, T>, ValueTransformerSeq<T>, IterableFoldable<T>, ConvertableSequence<T>, CyclopsCollectable<T>, Sequential<T> {

    private final AnyMSeq<Xor<ST, T>> run;

    private XorTSeq(final AnyMSeq<Xor<ST, T>> run) {

        this.run = run;
    }

    /**
     * @return The wrapped AnyM
     */
    @Override
    public AnyMSeq<Xor<ST, T>> unwrap() {
        return run;
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
    public XorTSeq<ST, T> peek(Consumer<? super T> peek) {
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
     *     //XorT<AnyMSeq<Stream<Xor.empty>>>
     * }
     * </pre>
     * 
     * @param test
     *            Predicate to filter the wrapped Xor
     * @return XorT that applies the provided filter
     */
    @Override
    public XorTSeq<ST, T> filter(Predicate<? super T> test) {
        return XorTSeq.of(run.map(opt -> opt.filter(test)));
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
     *  //XorT<AnyMSeq<Stream<Xor[11]>>>
     * }
     * </pre>
     * 
     * @param f
     *            Mapping function for the wrapped Xor
     * @return XorT that applies the map function to the wrapped Xor
     */
    @Override
    public <B> XorTSeq<ST, B> map(Function<? super T, ? extends B> f) {
        return new XorTSeq<ST, B>(
                                  run.map(o -> o.map(f)));
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
    *  //XorT<AnyMSeq<Stream<Xor.empty>>>
    * }
     * </pre>
     * 
     * @param f
     *            FlatMap function
     * @return XorT that applies the flatMap function to the wrapped Xor
     */
    public <ST2, B> XorTSeq<ST, B> flatMapT(Function<? super T, XorTSeq<ST2, ? extends B>> f) {

        return of(run.bind(opt -> {
            if (opt.isPrimary())
                return f.apply(opt.get()).run.unwrap();
            return this;
        }));

    }

    @Override
    public <T> XorTSeq<ST, T> unitStream(ReactiveSeq<T> traversable) {
        return XorT.fromStream(traversable.map(Xor::primary));

    }

    @Override
    public <T> XorTSeq<ST, T> unitAnyM(AnyM<Traversable<T>> traversable) {

        return of((AnyMSeq) traversable.map(t -> Xor.fromIterable(t)));
    }

    @Override
    public AnyMSeq<? extends Traversable<T>> transformerStream() {

        return run.map(e -> e.toListX());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.XorT#swap()
     */
    @Override
    public XorTSeq<T, ST> swap() {
        return of(run.map(xor -> xor.swap()));
    }

    @Override
    public <ST2, B> XorTSeq<ST2, B> flatMap(Function<? super T, ? extends MonadicValue2<? extends ST2, ? extends B>> f) {

        return new XorTSeq<ST2, B>(
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
     *     AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMSeq<Xor<Integer>> streamOpt = stream.map(Xor::ofNullable);
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
    public static <ST, U, R> Function<XorTSeq<ST, U>, XorTSeq<ST, R>> lift(Function<? super U, ? extends R> fn) {
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
     *     AnyMSeq<Integer> stream = AnyM.ofMonad(withNulls);
     *     AnyMSeq<Xor<Integer>> streamOpt = stream.map(Xor::ofNullable);
     * 
     *     CompletableFuture<Xor<Integer>> two = CompletableFuture.supplyAsync(() -> Xor.of(2));
     *     AnyMSeq<Xor<Integer>> future = AnyM.ofMonad(two);
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
    public static <ST, U1, U2, R> BiFunction<XorTSeq<ST, U1>, XorTSeq<ST, U2>, XorTSeq<ST, R>> lift2(
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
    public static <ST, A> XorTSeq<ST, A> fromAnyM(AnyMSeq<A> anyM) {
        return of(anyM.map(Xor::primary));
    }

    /**
     * Construct an XorT from an AnyM that wraps a monad containing Xors
     * 
     * @param monads
     *            AnyM that contains a monad wrapping an Xor
     * @return XorT
     */
    public static <ST, A> XorTSeq<ST, A> of(AnyMSeq<Xor<ST, A>> monads) {
        return new XorTSeq<>(
                             monads);
    }

    public static <ST, A> XorTSeq<ST, A> of(Xor<ST, A> monads) {
        return XorT.fromIterable(ListX.of(monads));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return String.format("XorTSeq[%s]", run);
    }

    public boolean isPrimary() {
        return run.allMatch(x -> x.isPrimary());
    }

    public boolean isSecondary() {
        return run.allMatch(x -> x.isSecondary());
    }

    public <R> R visit(Function<ST, ? extends R> secondary, Function<? super T, ? extends R> primary, Monoid<R> combiner) {
        return run.map(t -> t.visit(secondary, primary))
                  .reduce(combiner);
    }

    public <R> XorTSeq<ST, R> unit(R value) {
        return of(run.unit(Xor.primary(value)));
    }

    public <R> XorTSeq<ST, R> empty() {
        return of(run.unit(Xor.secondary(null)));
    }

    @Override
    public ReactiveSeq<T> stream() {
        return run.stream()
                  .flatMapIterable(e -> e);
    }

    @Override
    public Iterator<T> iterator() {
        return stream().iterator();
    }

    public <R> XorTSeq<ST, R> unitIterator(Iterator<R> it) {
        return of(run.unitIterator(it)
                     .map(i -> Xor.primary(i)));
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

    public static <ST, T> XorTSeq<ST, T> emptyList() {
        return XorT.fromIterable(ListX.of());
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public XorTSeq<ST, T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.combine(predicate, op);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(int)
     */
    @Override
    public XorTSeq<ST, T> cycle(int times) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public XorTSeq<ST, T> cycle(Monoid<T> m, int times) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, T> cycleWhile(Predicate<? super T> predicate) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, T> cycleUntil(Predicate<? super T> predicate) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> XorTSeq<ST, R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (XorTSeq<ST, R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    @Override
    public <U, R> XorTSeq<ST, R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (XorTSeq<ST, R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    @Override
    public <U, R> XorTSeq<ST, R> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (XorTSeq<ST, R>) ValueTransformerSeq.super.zip(other, zipper);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    public <U> XorTSeq<ST, Tuple2<T, U>> zip(Stream<? extends U> other) {

        return (XorTSeq) ValueTransformerSeq.super.zip(other);
    }

    @Override
    public <U> XorTSeq<ST, Tuple2<T, U>> zip(Seq<? extends U> other) {

        return (XorTSeq) ValueTransformerSeq.super.zip(other);
    }

    @Override
    public <U> XorTSeq<ST, Tuple2<T, U>> zip(Iterable<? extends U> other) {

        return (XorTSeq) ValueTransformerSeq.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> XorTSeq<ST, Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {

        return (XorTSeq) ValueTransformerSeq.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> XorTSeq<ST, Tuple4<T, T2, T3, T4>> zip4(Stream<? extends T2> second, Stream<? extends T3> third,
            Stream<? extends T4> fourth) {

        return (XorTSeq) ValueTransformerSeq.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#zipWithIndex()
     */
    @Override
    public XorTSeq<ST, Tuple2<T, Long>> zipWithIndex() {

        return (XorTSeq<ST, Tuple2<T, Long>>) ValueTransformerSeq.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int)
     */
    @Override
    public XorTSeq<ST, ListX<T>> sliding(int windowSize) {

        return (XorTSeq<ST, ListX<T>>) ValueTransformerSeq.super.sliding(windowSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sliding(int, int)
     */
    @Override
    public XorTSeq<ST, ListX<T>> sliding(int windowSize, int increment) {

        return (XorTSeq<ST, ListX<T>>) ValueTransformerSeq.super.sliding(windowSize, increment);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> XorTSeq<ST, C> grouped(int size, Supplier<C> supplier) {

        return (XorTSeq<ST, C>) ValueTransformerSeq.super.grouped(size, supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, ListX<T>> groupedUntil(Predicate<? super T> predicate) {

        return (XorTSeq<ST, ListX<T>>) ValueTransformerSeq.super.groupedUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedStatefullyUntil(java.util.function.BiPredicate)
     */
    @Override
    public XorTSeq<ST, ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {

        return (XorTSeq<ST, ListX<T>>) ValueTransformerSeq.super.groupedStatefullyUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, ListX<T>> groupedWhile(Predicate<? super T> predicate) {

        return (XorTSeq<ST, ListX<T>>) ValueTransformerSeq.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> XorTSeq<ST, C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {

        return (XorTSeq<ST, C>) ValueTransformerSeq.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> XorTSeq<ST, C> groupedUntil(Predicate<? super T> predicate, Supplier<C> factory) {

        return (XorTSeq<ST, C>) ValueTransformerSeq.super.groupedUntil(predicate, factory);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(int)
     */
    @Override
    public XorTSeq<ST, ListX<T>> grouped(int groupSize) {

        return (XorTSeq<ST, ListX<T>>) ValueTransformerSeq.super.grouped(groupSize);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> XorTSeq<ST, Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {

        return (XorTSeq) ValueTransformerSeq.super.grouped(classifier, downstream);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#grouped(java.util.function.Function)
     */
    @Override
    public <K> XorTSeq<ST, Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {

        return (XorTSeq) ValueTransformerSeq.super.grouped(classifier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#distinct()
     */
    @Override
    public XorTSeq<ST, T> distinct() {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public XorTSeq<ST, T> scanLeft(Monoid<T> monoid) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.scanLeft(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> XorTSeq<ST, U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {

        return (XorTSeq<ST, U>) ValueTransformerSeq.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public XorTSeq<ST, T> scanRight(Monoid<T> monoid) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> XorTSeq<ST, U> scanRight(U identity, BiFunction<? super T, ? super U, ? extends U> combiner) {

        return (XorTSeq<ST, U>) ValueTransformerSeq.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted()
     */
    @Override
    public XorTSeq<ST, T> sorted() {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.Comparator)
     */
    @Override
    public XorTSeq<ST, T> sorted(Comparator<? super T> c) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeWhile(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, T> takeWhile(Predicate<? super T> p) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropWhile(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, T> dropWhile(Predicate<? super T> p) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeUntil(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, T> takeUntil(Predicate<? super T> p) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropUntil(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, T> dropUntil(Predicate<? super T> p) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#dropRight(int)
     */
    @Override
    public XorTSeq<ST, T> dropRight(int num) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#takeRight(int)
     */
    @Override
    public XorTSeq<ST, T> takeRight(int num) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skip(long)
     */
    @Override
    public XorTSeq<ST, T> skip(long num) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipWhile(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, T> skipWhile(Predicate<? super T> p) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipUntil(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, T> skipUntil(Predicate<? super T> p) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limit(long)
     */
    @Override
    public XorTSeq<ST, T> limit(long num) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitWhile(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, T> limitWhile(Predicate<? super T> p) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitUntil(java.util.function.Predicate)
     */
    @Override
    public XorTSeq<ST, T> limitUntil(Predicate<? super T> p) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#intersperse(java.lang.Object)
     */
    @Override
    public XorTSeq<ST, T> intersperse(T value) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#reverse()
     */
    @Override
    public XorTSeq<ST, T> reverse() {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle()
     */
    @Override
    public XorTSeq<ST, T> shuffle() {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#skipLast(int)
     */
    @Override
    public XorTSeq<ST, T> skipLast(int num) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#limitLast(int)
     */
    @Override
    public XorTSeq<ST, T> limitLast(int num) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    public XorTSeq<ST, T> onEmpty(T value) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public XorTSeq<ST, T> onEmptyGet(Supplier<? extends T> supplier) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> XorTSeq<ST, T> onEmptyThrow(Supplier<? extends X> supplier) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.onEmptyThrow(supplier);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#shuffle(java.util.Random)
     */
    @Override
    public XorTSeq<ST, T> shuffle(Random random) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#slice(long, long)
     */
    @Override
    public XorTSeq<ST, T> slice(long from, long to) {

        return (XorTSeq<ST, T>) ValueTransformerSeq.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.Traversable#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> XorTSeq<ST, T> sorted(Function<? super T, ? extends U> function) {
        return (XorTSeq) ValueTransformerSeq.super.sorted(function);
    }

    @Override
    public int hashCode() {
        return run.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof XorTSeq) {
            return run.equals(((XorTSeq) o).run);
        }
        return false;
    }
}

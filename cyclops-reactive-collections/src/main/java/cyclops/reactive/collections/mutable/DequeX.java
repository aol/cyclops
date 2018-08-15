package cyclops.reactive.collections.mutable;

import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.data.ReactiveWitness.deque;
import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.lazy.LazyDequeX;
import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.companion.Streams;
import cyclops.control.Either;
import cyclops.control.Future;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.tuple.Tuple3;
import cyclops.data.tuple.Tuple4;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.reactivestreams.Publisher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.oath.cyclops.types.foldable.Evaluation.LAZY;
import static cyclops.data.tuple.Tuple.tuple;


/**
 * An eXtended Deque type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this DequeX
 */
public interface DequeX<T> extends To<DequeX<T>>,
                                   Deque<T>,
                                   LazyCollectionX<T>,
                                   OnEmptySwitch<T, Deque<T>>,
                                   Higher<deque,T>{

    DequeX<T> lazy();
    DequeX<T> eager();
    default Tuple2<DequeX<T>, DequeX<T>> splitAt(int n) {
        materialize();
        return Tuple.tuple(take(n), drop(n));
    }

    default Tuple2<DequeX<T>, DequeX<T>> span(Predicate<? super T> pred) {
        return tuple(takeWhile(pred), dropWhile(pred));
    }

    default Tuple2<DequeX<T>,DequeX<T>> splitBy(Predicate<? super T> test) {
        return span(test.negate());
    }
    default Tuple2<DequeX<T>, DequeX<T>> partition(final Predicate<? super T> splitter) {

        return tuple(filter(splitter), filter(splitter.negate()));

    }
    public static <T> DequeX<T> defer(Supplier<DequeX<T>> s){
        return of(s)
                  .map(Supplier::get)
                  .concatMap(l->l);
    }


    static <T> CompletableDequeX<T> completable(){
        return new CompletableDequeX<>();
    }

    static class CompletableDequeX<T> implements InvocationHandler {
        Future<DequeX<T>> future = Future.future();
        public boolean complete(Deque<T> result){
            return future.complete(DequeX.fromIterable(result));
        }

        public DequeX<T> asDequeX(){
            DequeX f = (DequeX) Proxy.newProxyInstance(DequeX.class.getClassLoader(),
                    new Class[] { DequeX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            DequeX<T> target = future.fold(l->l, t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }
    public static <T> Higher<deque, T> widen(DequeX<T> narrow) {
    return narrow;
  }

    /**
     * Widen a DequeType nest inside another HKT encoded type
     *
     * @param list HTK encoded type containing  a Deque to widen
     * @return HKT encoded type with a widened Deque
     */
    public static <C2,T> Higher<C2, Higher<deque,T>> widen2(Higher<C2, DequeX<T>> list){
        //a functor could be used (if C2 is a functor / one exists for C2 type) instead of casting
        //cast seems safer as Higher<DequeType.deque,T> must be a ListType
        return (Higher)list;
    }

    /**
     * Convert the raw Higher Kinded Type for Deque types into the DequeType type definition class
     *
     * @param list HKT encoded list into a DequeType
     * @return DequeType
     */
    public static <T> DequeX<T> narrowK(final Higher<deque, T> list) {
        return (DequeX<T>)list;
    }

    /**
     * Create a DequeX that contains the Integers between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range DequeX
     */
    public static DequeX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end)
                          .to(ReactiveConvertableSequence::converter)
                          .dequeX(LAZY);
    }

    /**
     * Create a DequeX that contains the Longs between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range DequeX
     */
    public static DequeX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end)
                          .to(ReactiveConvertableSequence::converter)
                          .dequeX(LAZY);
    }

    /**
     * Unfold a function into a DequeX
     *
     * <pre>
     * {@code
     *  DequeX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     *
     * //(1,2,3,4,5)
     *
     * }</pre>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return DequeX generated by unfolder function
     */
    static <U, T> DequeX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .to(ReactiveConvertableSequence::converter)
                          .dequeX(LAZY);
    }
    /**
     * Generate a DequeX from the provided value up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Value for DequeX elements
     * @return DequeX generated from the provided Supplier
     */
    public static <T> DequeX<T> fill(final long limit, final T s) {

        return ReactiveSeq.fill(s)
                          .limit(limit)
                          .to(ReactiveConvertableSequence::converter)
                          .dequeX(LAZY);
    }

    /**
     * Generate a DequeX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate DequeX elements
     * @return DequeX generated from the provided Supplier
     */
    public static <T> DequeX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .to(ReactiveConvertableSequence::converter)
                          .dequeX(LAZY);
    }

    /**
     * Create a DequeX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return DequeX generated by iterative application
     */
    public static <T> DequeX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .to(ReactiveConvertableSequence::converter)
                          .dequeX(LAZY);

    }

    /**
     * @return A Collector that generates a mutable Deque from a Collection
     */
    static <T> Collector<T, ?, Deque<T>> defaultCollector() {
        return java.util.stream.Collectors.toCollection(() -> new ArrayDeque<>());
    }

    /**
     * @return An zero DequeX
     */
    public static <T> DequeX<T> empty() {
        return fromIterable((Deque<T>) defaultCollector().supplier()
                                                         .get());
    }

    /**
     * Construct a Deque from the provided values
     *
     * <pre>
     * {@code
     *     DequeX<Integer> deque = DequeX.of(1,2,3,4);
     *
     * }</pre>
     *
     *
     *
     * @param values to construct a Deque from
     * @return DequeX
     */
    public static <T> DequeX<T> of(final T... values) {
        return new LazyDequeX<T>(null,
                ReactiveSeq.of(values),
                defaultCollector(), LAZY);
    }
    /**
     *
     * Construct a Deque from the provided Iterator
     *
     * @param it Iterator to populate Deque
     * @return Newly populated DequeX
     */
    public static <T> DequeX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }

    /**
     * Construct a DequeX with a single value
     *
     * <pre>
     * {@code
     *     DequeX<Integer> deque = DequeX.of(1);
     *
     * }</pre>
     *
     *
     * @param value Active value
     * @return DequeX
     */
    public static <T> DequeX<T> singleton(final T value) {
        return of(value);
    }

    /**
     * Construct a DequeX from an Publisher
     *
     * @param publisher
     *            to construct DequeX from
     * @return DequeX
     */
    public static <T> DequeX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher)
                          .to(ReactiveConvertableSequence::converter)
                          .dequeX(LAZY);
    }

    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  DequeX<Integer> deque = dequeX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create DequeX from
     * @param <T> DequeX generated from Stream
     * @return
     */
    public static <T> DequeX<T> dequeX(ReactiveSeq<T> stream){
        return new LazyDequeX<T>(null,
                stream,
                defaultCollector(), LAZY);
    }

    /**
     * Construct a DequeX from an Iterable
     *
     * @param it
     *            to construct DequeX from
     * @return DequeX
     */
    public static <T> DequeX<T> fromIterable(final Iterable<T> it) {

        if (it instanceof DequeX)
            return (DequeX) it;
        if (it instanceof Deque)
            return new LazyDequeX<T>(
                                     (Deque) it, defaultCollector(), LAZY);
        return new LazyDequeX<T>(null,
                                    ReactiveSeq.fromIterable(it),
                                    defaultCollector(), LAZY);
    }

    /**
     * Construct a Deque from the provided Collector and Iterable.
     *
     * @param collector To construct DequeX from
     * @param it Iterable to construct DequeX
     * @return DequeX
     */
    public static <T> DequeX<T> fromIterable(final Collector<T, ?, Deque<T>> collector, final Iterable<T> it) {
        if (it instanceof DequeX)
            return ((DequeX) it).type(collector);
        if (it instanceof Deque)
            return new LazyDequeX<T>(
                                     (Deque) it, collector, LAZY);
        return new LazyDequeX<T>(
                                 Streams.stream(it)
                                            .collect(collector),
                                 collector, LAZY);
    }

    @Override
    default DequeX<T> materialize() {
        return (DequeX<T>)LazyCollectionX.super.materialize();
    }

    DequeX<T> type(Collector<T, ?, Deque<T>> collector);

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> DequeX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (DequeX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> DequeX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (DequeX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> DequeX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (DequeX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> DequeX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (DequeX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> DequeX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (DequeX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> DequeX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (DequeX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);

    }



    /**
     * @return The collector for this DequeX
     */
    public <T> Collector<T, ?, Deque<T>> getCollector();

    /* (non-Javadoc)
     * @see CollectionX#from(java.util.Collection)
     */
    @Override
    default <T1> DequeX<T1> from(final Iterable<T1> c) {
        return DequeX.<T1> fromIterable(getCollector(), c);
    }

    /* (non-Javadoc)
     * @see LazyCollectionX#fromStream(java.util.stream.Stream)
     */
    @Override
    default <X> DequeX<X> fromStream(final ReactiveSeq<X> stream) {
        return new LazyDequeX<>(
                                ReactiveSeq.fromStream(stream), getCollector(), LAZY);
    }

    /**
     * Combine two adjacent elements in a DequeX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code
     *  DequeX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),SemigroupK.intSum)
                   .listX()

     *  //ListX(3,4)
     * }</pre>
     *
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced DequeX
     */
    @Override
    default DequeX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (DequeX<T>) LazyCollectionX.super.combine(predicate, op);
    }

    @Override
    default DequeX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (DequeX<T>)LazyCollectionX.super.combine(op,predicate);
    }

    /**
     * coflatMap pattern, can be used to perform maybe reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *     DequeX.of(1,2,3)
     *           .map(i->i*2)
     *           .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *      //DequeX[12]
     * }
     * </pre>
     *
     *
     * @param fn mapping function
     * @return Transformed Deque
     */
    default <R> DequeX<R> coflatMap(Function<? super DequeX<T>, ? extends R> fn){
        return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }

    /* (non-Javadoc)
     * @see FluentCollectionX#unit(java.util.Collection)
     */
    @Override
    default <R> DequeX<R> unit(final Iterable<R> col) {
        return fromIterable(col);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.Pure#unit(java.lang.Object)
     */
    @Override
    default <R> DequeX<R> unit(final R value) {
        return singleton(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.IterableFunctor#unitIterable(java.util.Iterator)
     */
    @Override
    default <R> DequeX<R> unitIterable(final Iterable<R> it) {
        return fromIterable(it);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#stream()
     */
    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#reverse()
     */
    @Override
    default DequeX<T> reverse() {

        return (DequeX<T>) LazyCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> filter(final Predicate<? super T> pred) {

        return (DequeX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> DequeX<R> map(final Function<? super T, ? extends R> mapper) {

        return (DequeX<R>) LazyCollectionX.super.map(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> DequeX<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (DequeX<R>) LazyCollectionX.super.concatMap(mapper);
    }


    @Override
    default DequeX<T> take(final long num) {

        return (DequeX<T>) LazyCollectionX.super.take(num);
    }
    @Override
    default DequeX<T> drop(final long num) {

        return (DequeX<T>) this.drop(num);
    }


    @Override
    default DequeX<T> slice(final long from, final long to) {
        return (DequeX<T>) LazyCollectionX.super.slice(from, to);
    }

    @Override
    default DequeX<Vector<T>> grouped(final int groupSize) {
        return (DequeX<Vector<T>>) LazyCollectionX.super.grouped(groupSize);
    }


    /* (non-Javadoc)
     * @see LazyCollectionX#zip(java.lang.Iterable)
     */
    @Override
    default <U> DequeX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (DequeX) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> DequeX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {

        return (DequeX<R>) LazyCollectionX.super.zip(other, zipper);
    }



    @Override
    default DequeX<Seq<T>> sliding(final int windowSize) {
        return (DequeX<Seq<T>>) LazyCollectionX.super.sliding(windowSize);
    }


    @Override
    default DequeX<Seq<T>> sliding(final int windowSize, final int increment) {
        return (DequeX<Seq<T>>) LazyCollectionX.super.sliding(windowSize, increment);
    }


    @Override
    default DequeX<T> scanLeft(final Monoid<T> monoid) {
        return (DequeX<T>) LazyCollectionX.super.scanLeft(monoid);
    }


    @Override
    default <U> DequeX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (DequeX<U>) LazyCollectionX.super.scanLeft(seed, function);
    }

    /* (non-Javadoc)
     * @see LazyCollectionX#scanRight(cyclops2.function.Monoid)
     */
    @Override
    default DequeX<T> scanRight(final Monoid<T> monoid) {
        return (DequeX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    /* (non-Javadoc)
     * @see LazyCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    default <U> DequeX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (DequeX<U>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> DequeX<T> sorted(final Function<? super T, ? extends U> function) {

        return (DequeX<T>) LazyCollectionX.super.sorted(function);
    }

    /* (non-Javadoc)
     * @see LazyCollectionX#plus(java.lang.Object)
     */
    @Override
    default DequeX<T> plus(final T e) {
        add(e);
        return this;
    }

    /* (non-Javadoc)
     * @see LazyCollectionX#insertAt(java.util.Collection)
     */
    @Override
    default DequeX<T> plusAll(final Iterable<? extends T> list) {
        addAll(ListX.fromIterable(list));
        return this;
    }

    /* (non-Javadoc)
     * @see LazyCollectionX#removeValue(java.lang.Object)
     */
    @Override
    default DequeX<T> removeValue(final T e) {
        remove(e);
        return this;
    }

    /* (non-Javadoc)
     * @see LazyCollectionX#removeAll(java.util.Collection)
     */
    @Override
    default DequeX<T> removeAll(final Iterable<? extends T> list) {
        Collection c = this;
        c.removeAll(ListX.fromIterable(list));
        return this;
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default DequeX<T> peek(final Consumer<? super T> c) {
        return (DequeX<T>) LazyCollectionX.super.peek(c);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#cycle(int)
     */
    @Override
    default DequeX<T> cycle(final long times) {

        return (DequeX<T>) LazyCollectionX.super.cycle(times);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#cycle(com.oath.cyclops.sequence.Monoid, int)
     */
    @Override
    default DequeX<T> cycle(final Monoid<T> m, final long times) {

        return (DequeX<T>) LazyCollectionX.super.cycle(m, times);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> cycleWhile(final Predicate<? super T> predicate) {

        return (DequeX<T>) LazyCollectionX.super.cycleWhile(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> cycleUntil(final Predicate<? super T> predicate) {

        return (DequeX<T>) LazyCollectionX.super.cycleUntil(predicate);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#zip(java.util.stream.Stream)
     */
    @Override
    default <U> DequeX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (DequeX) LazyCollectionX.super.zipWithStream(other);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> DequeX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (DequeX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> DequeX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (DequeX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#zipWithIndex()
     */
    @Override
    default DequeX<Tuple2<T, Long>> zipWithIndex() {
        //
        return (DequeX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#distinct()
     */
    @Override
    default DequeX<T> distinct() {

        return (DequeX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#sorted()
     */
    @Override
    default DequeX<T> sorted() {

        return (DequeX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#sorted(java.util.Comparator)
     */
    @Override
    default DequeX<T> sorted(final Comparator<? super T> c) {

        return (DequeX<T>) LazyCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#skipWhile(java.util.function.Predicate)
     */
    default DequeX<T> dropWhile(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.dropWhile(p);
    }


    default DequeX<T> dropUntil(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.dropUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#limitWhile(java.util.function.Predicate)
     */
    default DequeX<T> takeWhile(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#limitUntil(java.util.function.Predicate)
     */
    default DequeX<T> takeUntil(final Predicate<? super T> p) {

        return (DequeX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#intersperse(java.lang.Object)
     */
    @Override
    default DequeX<T> intersperse(final T value) {

        return (DequeX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#shuffle()
     */
    @Override
    default DequeX<T> shuffle() {

        return (DequeX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#skipLast(int)
     */
    default DequeX<T> dropRight(final int num) {

        return (DequeX<T>) LazyCollectionX.super.dropRight(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#limitLast(int)
     */
    default DequeX<T> takeRight(final int num) {

        return (DequeX<T>) LazyCollectionX.super.takeRight(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#onEmpty(java.lang.Object)
     */
    @Override
    default DequeX<T> onEmpty(final T value) {

        return (DequeX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default DequeX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (DequeX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#onEmptyError(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> DequeX<T> onEmptyError(final Supplier<? extends X> supplier) {

        return (DequeX<T>) LazyCollectionX.super.onEmptyError(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#shuffle(java.util.Random)
     */
    @Override
    default DequeX<T> shuffle(final Random random) {

        return (DequeX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#permutations()
     */
    @Override
    default DequeX<ReactiveSeq<T>> permutations() {

        return (DequeX<ReactiveSeq<T>>) LazyCollectionX.super.permutations();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#combinations(int)
     */
    @Override
    default DequeX<ReactiveSeq<T>> combinations(final int size) {

        return (DequeX<ReactiveSeq<T>>) LazyCollectionX.super.combinations(size);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.Traversable#combinations()
     */
    @Override
    default DequeX<ReactiveSeq<T>> combinations() {

        return (DequeX<ReactiveSeq<T>>) LazyCollectionX.super.combinations();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> DequeX<U> ofType(final Class<? extends U> type) {

        return (DequeX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default DequeX<T> filterNot(final Predicate<? super T> fn) {

        return (DequeX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#notNull()
     */
    @Override
    default DequeX<T> notNull() {

        return (DequeX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default DequeX<T> removeStream(final Stream<? extends T> stream) {

        return (DequeX<T>) LazyCollectionX.super.removeStream(stream);
    }

    @Override
    default DequeX<T> removeAll(CollectionX<? extends T> it) {
      return removeAll(narrowIterable());
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default DequeX<T> removeAll(final T... values) {

        return (DequeX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default DequeX<T> retainAll(final Iterable<? extends T> it) {

        return (DequeX<T>) LazyCollectionX.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default DequeX<T> retainStream(final Stream<? extends T> seq) {

        return (DequeX<T>) LazyCollectionX.super.retainStream(seq);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default DequeX<T> retainAll(final T... values) {

        return (DequeX<T>) LazyCollectionX.super.retainAll(values);
    }

    /* (non-Javadoc)
     * @see LazyCollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends PersistentCollection<? super T>> DequeX<C> grouped(final int size, final Supplier<C> supplier) {

        return (DequeX<C>) LazyCollectionX.super.grouped(size, supplier);
    }


    @Override
    default DequeX<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (DequeX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }


    @Override
    default DequeX<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (DequeX<Vector<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    /* (non-Javadoc)
     * @see LazyCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends PersistentCollection<? super T>> DequeX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (DequeX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    /* (non-Javadoc)
     * @see LazyCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends PersistentCollection<? super T>> DequeX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (DequeX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default DequeX<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return (DequeX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }


    @Override
    default DequeX<T> onEmptySwitch(final Supplier<? extends Deque<T>> supplier) {
        if (isEmpty())
            return DequeX.fromIterable(supplier.get());
        return this;
    }


    @Override
    default <R> DequeX<R> retry(final Function<? super T, ? extends R> fn) {
        return (DequeX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> DequeX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (DequeX<R>)LazyCollectionX.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <R> DequeX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (DequeX<R>)LazyCollectionX.super.flatMap(fn);
    }

    @Override
    default <R> DequeX<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (DequeX<R>)LazyCollectionX.super.mergeMap(fn);
    }

    @Override
    default DequeX<T> prependStream(Stream<? extends T> stream) {
        return (DequeX<T>)LazyCollectionX.super.prependStream(stream);
    }

    @Override
    default DequeX<T> appendAll(T... values) {
        return (DequeX<T>)LazyCollectionX.super.appendAll(values);
    }

    @Override
    default DequeX<T> append(T value) {
        return (DequeX<T>)LazyCollectionX.super.append(value);
    }

    @Override
    default DequeX<T> prepend(T value) {
        return (DequeX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default DequeX<T> prependAll(T... values) {
        return (DequeX<T>)LazyCollectionX.super.prependAll(values);
    }

    @Override
    default DequeX<T> insertAt(int pos, T... values) {
        return (DequeX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default DequeX<T> deleteBetween(int start, int end) {
        return (DequeX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default DequeX<T> insertStreamAt(int pos, Stream<T> stream) {
        return (DequeX<T>)LazyCollectionX.super.insertStreamAt(pos,stream);
    }

    @Override
    default DequeX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (DequeX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> DequeX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (DequeX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }
    @Override
    default DequeX<T> plusLoop(int max, IntFunction<T> value) {
        return (DequeX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default DequeX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (DequeX<T>)LazyCollectionX.super.plusLoop(supplier);
    }

    /**
     * Narrow a covariant Deque
     *
     * <pre>
     * {@code
     * DequeX<? extends Fruit> deque = DequeX.of(apple,bannana);
     * DequeX<Fruit> fruitDeque = DequeX.narrow(deque);
     * }
     * </pre>
     *
     * @param dequeX to narrow generic type
     * @return dequeX with narrowed type
     */
    public static <T> DequeX<T> narrow(final DequeX<? extends T> dequeX) {
        return (DequeX<T>) dequeX;
    }

  @Override
    default <T2, R> DequeX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return (DequeX<R>)LazyCollectionX.super.zip(fn, publisher);
    }



    @Override
    default <U> DequeX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (DequeX)LazyCollectionX.super.zipWithPublisher(other);
    }


    @Override
    default <S, U, R> DequeX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (DequeX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> DequeX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (DequeX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }



    public static  <T,R> DequeX<R> tailRec(T initial, Function<? super T, ? extends DequeX<? extends Either<T, R>>> fn) {
        ListX<Either<T, R>> lazy = ListX.of(Either.left(initial));
        ListX<Either<T, R>> next = lazy.eager();
        boolean newValue[] = {true};
        for(;;){

            next = next.concatMap(e -> e.fold(s -> {
                        newValue[0]=true;
                        return fn.apply(s); },
                    p -> {
                        newValue[0]=false;
                        return ListX.of(e);
                    }));
            if(!newValue[0])
                break;

        }
        return Either.sequenceRight(next)
                     .orElse(ReactiveSeq.empty())
                     .to(ReactiveConvertableSequence::converter)
                     .dequeX(Evaluation.LAZY);
    }
}

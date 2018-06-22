package cyclops.reactive.collections.mutable;

import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.data.collections.extensions.lazy.LazySortedSetX;
import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;
import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.persistent.PersistentCollection;
import com.oath.cyclops.types.recoverable.OnEmptySwitch;
import com.oath.cyclops.util.ExceptionSoftener;
import cyclops.control.Either;
import cyclops.control.Future;
import cyclops.control.Option;
import cyclops.data.Seq;
import cyclops.data.Vector;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.oath.cyclops.types.foldable.Evaluation.LAZY;

/**
 * An eXtended SortedSet type, that offers additional functional style operators such as bimap, filter and more
 * Can operate eagerly, lazily or reactively (async push)
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this SortedSetX
 */
public interface SortedSetX<T> extends To<SortedSetX<T>>,SortedSet<T>, LazyCollectionX<T>, OnEmptySwitch<T, SortedSet<T>> {


    public static <T> SortedSetX<T> defer(Supplier<SortedSetX<T>> s){
      return of(s)
              .map(Supplier::get)
              .concatMap(l->l);
    }

    static <T> CompletableSortedSetX<T> completable(){
        return new CompletableSortedSetX<>();
    }

    static class CompletableSortedSetX<T> implements InvocationHandler {
        Future<SortedSetX<T>> future = Future.future();
        public boolean complete(SortedSet<T> result){
            return future.complete(SortedSetX.fromIterable(result));
        }

        public SortedSetX<T> asSortedSetX(){
            SortedSetX f = (SortedSetX) Proxy.newProxyInstance(SortedSetX.class.getClassLoader(),
                    new Class[] { SortedSetX.class },
                    this);
            return f;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            SortedSetX<T> target =  future.fold(l->l, t->{throw ExceptionSoftener.throwSoftenedException(t);});
            return method.invoke(target,args);
        }
    }
    @Override
    default Object[] toArray(){
        return LazyCollectionX.super.toArray();
    }

    @Override
    default <T1> T1[] toArray(T1[] a){
        return LazyCollectionX.super.toArray(a);
    }
    SortedSetX<T> lazy();
    SortedSetX<T> eager();
    static <T> Collector<T, ?, SortedSet<T>> defaultCollector() {
        return Collectors.toCollection(() -> new TreeSet<T>(
                                                            (Comparator) Comparator.<Comparable> naturalOrder()));
    }

    static <T> Collector<T, ?, SortedSet<T>> immutableCollector() {
        return Collectors.collectingAndThen(defaultCollector(), (final SortedSet<T> d) -> Collections.unmodifiableSortedSet(d));

    }

    /**
    * Create a SortedSetX that contains the Integers between skip and take
    *
    * @param start
    *            Number of range to skip from
    * @param end
    *            Number for range to take at
    * @return Range SortedSetX
    */
    public static SortedSetX<Integer> range(final int start, final int end) {
        return ReactiveSeq.range(start, end)
                          .to(ReactiveConvertableSequence::converter)
                          .sortedSetX(Evaluation.LAZY);
    }

    /**
     * Create a SortedSetX that contains the Longs between skip and take
     *
     * @param start
     *            Number of range to skip from
     * @param end
     *            Number for range to take at
     * @return Range SortedSetX
     */
    public static SortedSetX<Long> rangeLong(final long start, final long end) {
        return ReactiveSeq.rangeLong(start, end)
                          .to(ReactiveConvertableSequence::converter)
                          .sortedSetX(Evaluation.LAZY);
    }

    /**
     * Unfold a function into a SortedSetX
     *
     * <pre>
     * {@code
     *  SortedSetX.unfold(1,i->i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.zero());
     *
     * //(1,2,3,4,5)
     *
     * }</pre>
     *
     * @param seed Initial value
     * @param unfolder Iteratively applied function, terminated by an zero Optional
     * @return SortedSetX generated by unfolder function
     */
    static <U, T> SortedSetX<T> unfold(final U seed, final Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return ReactiveSeq.unfold(seed, unfolder)
                          .to(ReactiveConvertableSequence::converter)
                          .sortedSetX(Evaluation.LAZY);
    }

    /**
     * Generate a SortedSetX from the provided Supplier up to the provided limit number of times
     *
     * @param limit Max number of elements to generate
     * @param s Supplier to generate SortedSetX elements
     * @return SortedSetX generated from the provided Supplier
     */
    public static <T> SortedSetX<T> generate(final long limit, final Supplier<T> s) {

        return ReactiveSeq.generate(s)
                          .limit(limit)
                          .to(ReactiveConvertableSequence::converter)
                          .sortedSetX(Evaluation.LAZY);
    }

    /**
     * Create a SortedSetX by iterative application of a function to an initial element up to the supplied limit number of times
     *
     * @param limit Max number of elements to generate
     * @param seed Initial element
     * @param f Iteratively applied to each element to generate the next element
     * @return SortedSetX generated by iterative application
     */
    public static <T> SortedSetX<T> iterate(final long limit, final T seed, final UnaryOperator<T> f) {
        return ReactiveSeq.iterate(seed, f)
                          .limit(limit)
                          .to(ReactiveConvertableSequence::converter)
                          .sortedSetX(Evaluation.LAZY);

    }

    public static <T> SortedSetX<T> empty() {
        return fromIterable((SortedSet<T>) defaultCollector().supplier()
                                                             .get());
    }

    public static <T> SortedSetX<T> of(final T... values) {
        return new LazySortedSetX<T>(null,
                ReactiveSeq.of(values),
                defaultCollector(),Evaluation.LAZY);
    }

    public static <T> SortedSetX<T> singleton(final T value) {
        return of(value);
    }

    @Override
    default SortedSetX<T> materialize() {
        return (SortedSetX<T>)LazyCollectionX.super.materialize();
    }


    @Override
    default SortedSetX<T> take(final long num) {

        return (SortedSetX<T>) LazyCollectionX.super.limit(num);
    }
    @Override
    default SortedSetX<T> drop(final long num) {

        return (SortedSetX<T>) LazyCollectionX.super.skip(num);
    }
    /**
     * Construct a SortedSetX from an Publisher
     *
     * @param publisher
     *            to construct SortedSetX from
     * @return SortedSetX
     */
    public static <T> SortedSetX<T> fromPublisher(final Publisher<? extends T> publisher) {
        return Spouts.from((Publisher<T>) publisher)
                          .to(ReactiveConvertableSequence::converter)
                          .sortedSetX(Evaluation.LAZY);
    }
    /**
     *
     * <pre>
     * {@code
     *  import static cyclops.stream.ReactiveSeq.range;
     *
     *  SortedSetX<Integer> sortedSet = sortedSetX(range(10,20));
     *
     * }
     * </pre>
     * @param stream To create SortedSetX from
     * @param <T> SortedSetX generated from Stream
     * @return
     */
    public static <T> SortedSetX<T> sortedSetX(ReactiveSeq<T> stream){
        return new LazySortedSetX<T>(null,
                stream,
                defaultCollector(),Evaluation.LAZY);
    }

    public static <T> SortedSetX<T> fromIterable(final Iterable<T> it) {
        if (it instanceof SortedSetX)
            return (SortedSetX<T>) it;
        if (it instanceof SortedSet)
            return new LazySortedSetX<T>(
                                         (SortedSet) it, defaultCollector(),Evaluation.LAZY);
        return new LazySortedSetX<T>(null,
                                       ReactiveSeq.fromIterable(it),
                                     defaultCollector(),Evaluation.LAZY);
    }
    public static <T> SortedSetX<T> fromIterator(final Iterator<T> it) {
        return fromIterable(()->it);
    }

    public static <T> SortedSetX<T> fromIterable(final Collector<T, ?, SortedSet<T>> collector, final Iterable<T> it) {
        if (it instanceof SortedSetX)
            return ((SortedSetX<T>) it).type(collector);
        if (it instanceof SortedSet)
            return new LazySortedSetX<T>(
                                         (SortedSet) it, collector,Evaluation.LAZY);
        return new LazySortedSetX<T>(null,
                                     ReactiveSeq.fromIterable(it),
                                     collector,Evaluation.LAZY);
    }

    SortedSetX<T> type(Collector<T, ?, SortedSet<T>> collector);

    /**
     * coflatMap pattern, can be used to perform lazy reductions / collections / folds and other terminal operations
     *
     * <pre>
     * {@code
     *
     *     SortedSetX.of(1,2,3)
     *               .map(i->i*2)
     *               .coflatMap(s -> s.reduce(0,(a,b)->a+b))
     *
     *      //SortedSetX[12]
     * }
     * </pre>
     *
     *
     * @param fn mapping function
     * @return Transformed SortedSet
     */
    default <R> SortedSetX<R> coflatMap(Function<? super SortedSetX<T>, ? extends R> fn){
        return fn.andThen(r ->  this.<R>unit(r))
                .apply(this);
    }


    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> SortedSetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (SortedSetX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach4(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.QuadFunction, com.oath.cyclops.util.function.QuadFunction)
     */
    @Override
    default <R1, R2, R3, R> SortedSetX<R> forEach4(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends Iterable<R3>> stream3,
            Function4<? super T, ? super R1, ? super R2, ? super R3, Boolean> filterFunction,
            Function4<? super T, ? super R1, ? super R2, ? super R3, ? extends R> yieldingFunction) {

        return (SortedSetX)LazyCollectionX.super.forEach4(stream1, stream2, stream3, filterFunction, yieldingFunction);
    }
    /**
    default ConvertableSequence<T> to(){


        return new ConvertableSequence<>(this);
    }
    default Collectable<T> collectors(){



        return Seq.seq(this);
    }**/
    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> SortedSetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (SortedSetX)LazyCollectionX.super.forEach3(stream1, stream2, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach3(java.util.function.Function, java.util.function.BiFunction, com.oath.cyclops.util.function.TriFunction, com.oath.cyclops.util.function.TriFunction)
     */
    @Override
    default <R1, R2, R> SortedSetX<R> forEach3(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends Iterable<R2>> stream2,
            Function3<? super T, ? super R1, ? super R2, Boolean> filterFunction,
            Function3<? super T, ? super R1, ? super R2, ? extends R> yieldingFunction) {

        return (SortedSetX)LazyCollectionX.super.forEach3(stream1, stream2, filterFunction, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> SortedSetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (SortedSetX)LazyCollectionX.super.forEach2(stream1, yieldingFunction);
    }

    /* (non-Javadoc)
     * @see CollectionX#forEach2(java.util.function.Function, java.util.function.BiFunction, java.util.function.BiFunction)
     */
    @Override
    default <R1, R> SortedSetX<R> forEach2(Function<? super T, ? extends Iterable<R1>> stream1,
            BiFunction<? super T, ? super R1, Boolean> filterFunction,
            BiFunction<? super T, ? super R1, ? extends R> yieldingFunction) {

        return (SortedSetX)LazyCollectionX.super.forEach2(stream1, filterFunction, yieldingFunction);
    }


    /**
     * Combine two adjacent elements in a SortedSetX using the supplied BinaryOperator
     * This is a stateful grouping and reduction operation. The emitted of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code
     *  SortedSetX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),SemigroupK.intSum)
                   .listX(Evaluation.LAZY)

     *  //ListX(3,4)
     * }</pre>
     *
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced SortedSetX
     */
    @Override
    default SortedSetX<T> combine(final BiPredicate<? super T, ? super T> predicate, final BinaryOperator<T> op) {
        return (SortedSetX<T>) LazyCollectionX.super.combine(predicate, op);
    }
    @Override
    default SortedSetX<T> combine(final Monoid<T> op, final BiPredicate<? super T, ? super T> predicate) {
        return (SortedSetX<T>)LazyCollectionX.super.combine(op,predicate);
    }
    @Override
    default <R> SortedSetX<R> unit(final R value) {
        return singleton(value);
    }

    @Override
    default <R> SortedSetX<R> unit(final Iterable<R> col) {
        return fromIterable(col);
    }

    @Override
    default <R> SortedSetX<R> unitIterable(final Iterable<R> it) {
        return fromIterable(it);
    }

    @Override
    default ReactiveSeq<T> stream() {

        return ReactiveSeq.fromIterable(this);
    }

    @Override
    default <T1> SortedSetX<T1> from(final Iterable<T1> c) {
        return SortedSetX.<T1> fromIterable(getCollector(), c);
    }

    public <T> Collector<T, ?, SortedSet<T>> getCollector();

    @Override
    default <X> SortedSetX<X> fromStream(final ReactiveSeq<X> stream) {
        return new LazySortedSetX<>(
                                    stream.collect(getCollector()), getCollector(),Evaluation.LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#reverse()
     */
    @Override
    default SortedSetX<T> reverse() {
        return (SortedSetX<T>) LazyCollectionX.super.reverse();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> filter(final Predicate<? super T> pred) {

        return (SortedSetX<T>) LazyCollectionX.super.filter(pred);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#transform(java.util.function.Function)
     */
    @Override
    default <R> SortedSetX<R> map(final Function<? super T, ? extends R> mapper) {

        return (SortedSetX<R>) LazyCollectionX.super.<R> map(mapper);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    default <R> SortedSetX<R> concatMap(final Function<? super T, ? extends Iterable<? extends R>> mapper) {

        return (SortedSetX<R>) LazyCollectionX.super.concatMap(mapper);
}

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limit(long)
     */
    @Override
    default SortedSetX<T> limit(final long num) {
        return (SortedSetX<T>) LazyCollectionX.super.limit(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#skip(long)
     */
    @Override
    default SortedSetX<T> skip(final long num) {

        return (SortedSetX<T>) LazyCollectionX.super.skip(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> takeWhile(final Predicate<? super T> p) {

        return (SortedSetX<T>) LazyCollectionX.super.takeWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> dropWhile(final Predicate<? super T> p) {

        return (SortedSetX<T>) LazyCollectionX.super.dropWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> takeUntil(final Predicate<? super T> p) {

        return (SortedSetX<T>) LazyCollectionX.super.takeUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> dropUntil(final Predicate<? super T> p) {

        return (SortedSetX<T>) LazyCollectionX.super.dropUntil(p);
    }

    @Override
    default SortedSetX<T> takeRight(final int num) {
        return (SortedSetX<T>) LazyCollectionX.super.takeRight(num);
    }

    @Override
    default SortedSetX<T> dropRight(final int num) {
        return (SortedSetX<T>) LazyCollectionX.super.dropRight(num);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#slice(long, long)
     */
    @Override
    default SortedSetX<T> slice(final long from, final long to) {

        return (SortedSetX<T>) LazyCollectionX.super.slice(from, to);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#sorted(java.util.function.Function)
     */
    @Override
    default <U extends Comparable<? super U>> SortedSetX<T> sorted(final Function<? super T, ? extends U> function) {

        return (SortedSetX<T>) LazyCollectionX.super.sorted(function);
    }

    @Override
    default SortedSetX<Vector<T>> grouped(final int groupSize) {
        return (SortedSetX<Vector<T>>) (SortedSetX<T>) LazyCollectionX.super.grouped(groupSize);
    }



    @Override
    default <U> SortedSetX<Tuple2<T, U>> zip(final Iterable<? extends U> other) {
        return (SortedSetX<Tuple2<T, U>>) (SortedSetX<T>) LazyCollectionX.super.zip(other);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    default <U, R> SortedSetX<R> zip(final Iterable<? extends U> other, final BiFunction<? super T, ? super U, ? extends R> zipper) {
        return (SortedSetX<R>) LazyCollectionX.super.zip(other, zipper);
    }


  @Override
    default SortedSetX<Seq<T>> sliding(final int windowSize) {
        return (SortedSetX<Seq<T>>) (SortedSetX<T>) LazyCollectionX.super.sliding(windowSize);
    }

    @Override
    default SortedSetX<Seq<T>> sliding(final int windowSize, final int increment) {
        return (SortedSetX<Seq<T>>) (SortedSetX<T>) LazyCollectionX.super.sliding(windowSize, increment);
    }

    @Override
    default SortedSetX<T> scanLeft(final Monoid<T> monoid) {
        return (SortedSetX<T>) LazyCollectionX.super.scanLeft(monoid);
    }

    @Override
    default <U> SortedSetX<U> scanLeft(final U seed, final BiFunction<? super U, ? super T, ? extends U> function) {
        return (SortedSetX<U>) (SortedSetX<T>) LazyCollectionX.super.scanLeft(seed, function);
    }

    @Override
    default SortedSetX<T> scanRight(final Monoid<T> monoid) {
        return (SortedSetX<T>) LazyCollectionX.super.scanRight(monoid);
    }

    @Override
    default <U> SortedSetX<U> scanRight(final U identity, final BiFunction<? super T, ? super U, ? extends U> combiner) {
        return (SortedSetX<U>) (SortedSetX<T>) LazyCollectionX.super.scanRight(identity, combiner);
    }

    @Override
    default SortedSetX<T> plus(final T e) {
        add(e);
        return this;
    }

    @Override
    default SortedSetX<T> plusAll(final Iterable<? extends T> list) {
        for(T next : list) {
            add(next);
        }
        return this;
    }

    @Override
    default SortedSetX<T> removeValue(final T e) {
        remove(e);
        return this;
    }

    @Override
    default SortedSetX<T> removeAll(final Iterable<? extends T> list) {
        for(T next : list) {
            remove(next);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycle(int)
     */
    @Override
    default ListX<T> cycle(final long times) {

        return this.stream()
                   .cycle(times)
                   .to(ReactiveConvertableSequence::converter)
                    .listX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycle(com.oath.cyclops.sequence.Monoid, int)
     */
    @Override
    default ListX<T> cycle(final Monoid<T> m, final long times) {

        return this.stream()
                   .cycle(m, times)
                   .to(ReactiveConvertableSequence::converter).listX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    default ListX<T> cycleWhile(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleWhile(predicate)
                   .to(ReactiveConvertableSequence::converter).listX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    default ListX<T> cycleUntil(final Predicate<? super T> predicate) {

        return this.stream()
                   .cycleUntil(predicate)
                   .to(ReactiveConvertableSequence::converter).listX(LAZY);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zip(java.util.stream.Stream)
     */
    @Override
    default <U> SortedSetX<Tuple2<T, U>> zipWithStream(final Stream<? extends U> other) {

        return (SortedSetX) LazyCollectionX.super.zipWithStream(other);
    }



    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <S, U> SortedSetX<Tuple3<T, S, U>> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third) {

        return (SortedSetX) LazyCollectionX.super.zip3(second, third);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    default <T2, T3, T4> SortedSetX<Tuple4<T, T2, T3, T4>> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third,
            final Iterable<? extends T4> fourth) {

        return (SortedSetX) LazyCollectionX.super.zip4(second, third, fourth);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#zipWithIndex()
     */
    @Override
    default SortedSetX<Tuple2<T, Long>> zipWithIndex() {

        return (SortedSetX<Tuple2<T, Long>>) LazyCollectionX.super.zipWithIndex();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#distinct()
     */
    @Override
    default SortedSetX<T> distinct() {

        return (SortedSetX<T>) LazyCollectionX.super.distinct();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#sorted()
     */
    @Override
    default SortedSetX<T> sorted() {

        return (SortedSetX<T>) LazyCollectionX.super.sorted();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#sorted(java.util.Comparator)
     */
    @Override
    default SortedSetX<T> sorted(final Comparator<? super T> c) {

        return (SortedSetX<T>) LazyCollectionX.super.sorted(c);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> skipWhile(final Predicate<? super T> p) {

        return (SortedSetX<T>) LazyCollectionX.super.skipWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> skipUntil(final Predicate<? super T> p) {

        return (SortedSetX<T>) LazyCollectionX.super.skipUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> limitWhile(final Predicate<? super T> p) {

        return (SortedSetX<T>) LazyCollectionX.super.limitWhile(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> limitUntil(final Predicate<? super T> p) {

        return (SortedSetX<T>) LazyCollectionX.super.limitUntil(p);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#intersperse(java.lang.Object)
     */
    @Override
    default SortedSetX<T> intersperse(final T value) {

        return (SortedSetX<T>) LazyCollectionX.super.intersperse(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#shuffle()
     */
    @Override
    default SortedSetX<T> shuffle() {

        return (SortedSetX<T>) LazyCollectionX.super.shuffle();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#skipLast(int)
     */
    @Override
    default SortedSetX<T> skipLast(final int num) {

        return (SortedSetX<T>) LazyCollectionX.super.skipLast(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#limitLast(int)
     */
    @Override
    default SortedSetX<T> limitLast(final int num) {

        return (SortedSetX<T>) LazyCollectionX.super.limitLast(num);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.types.recoverable.OnEmptySwitch#onEmptySwitch(java.util.function.Supplier)
     */
    @Override
    default SortedSetX<T> onEmptySwitch(final Supplier<? extends SortedSet<T>> supplier) {
        if (isEmpty())
            return SortedSetX.fromIterable(supplier.get());
        return this;
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    default SortedSetX<T> onEmpty(final T value) {

        return (SortedSetX<T>) LazyCollectionX.super.onEmpty(value);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    default SortedSetX<T> onEmptyGet(final Supplier<? extends T> supplier) {

        return (SortedSetX<T>) LazyCollectionX.super.onEmptyGet(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#onEmptyError(java.util.function.Supplier)
     */
    @Override
    default <X extends Throwable> SortedSetX<T> onEmptyError(final Supplier<? extends X> supplier) {

        return (SortedSetX<T>) LazyCollectionX.super.onEmptyError(supplier);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#shuffle(java.util.Random)
     */
    @Override
    default SortedSetX<T> shuffle(final Random random) {

        return (SortedSetX<T>) LazyCollectionX.super.shuffle(random);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#ofType(java.lang.Class)
     */
    @Override
    default <U> SortedSetX<U> ofType(final Class<? extends U> type) {

        return (SortedSetX<U>) LazyCollectionX.super.ofType(type);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    default SortedSetX<T> filterNot(final Predicate<? super T> fn) {

        return (SortedSetX<T>) LazyCollectionX.super.filterNot(fn);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#notNull()
     */
    @Override
    default SortedSetX<T> notNull() {

        return (SortedSetX<T>) LazyCollectionX.super.notNull();
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    default SortedSetX<T> removeStream(final Stream<? extends T> stream) {

        return (SortedSetX<T>) LazyCollectionX.super.removeStream(stream);
    }

    @Override
    default SortedSetX<T> removeAll(CollectionX<? extends T> it) {
      return removeAll(narrowIterable());
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    default SortedSetX<T> removeAll(final T... values) {

        return (SortedSetX<T>) LazyCollectionX.super.removeAll(values);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Iterable)
     */
    @Override
    default SortedSetX<T> retainAll(final Iterable<? extends T> it) {

        return (SortedSetX<T>) LazyCollectionX.super.retainAll(it);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#retainAllI(java.util.stream.Stream)
     */
    @Override
    default SortedSetX<T> retainStream(final Stream<? extends T> seq) {

        return (SortedSetX<T>) LazyCollectionX.super.retainStream(seq);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.collections.extensions.standard.LazyCollectionX#retainAllI(java.lang.Object[])
     */
    @Override
    default SortedSetX<T> retainAll(final T... values) {

        return (SortedSetX<T>) LazyCollectionX.super.retainAll(values);
    }



    @Override
    default <C extends PersistentCollection<? super T>> SortedSetX<C> grouped(final int size, final Supplier<C> supplier) {

        return (SortedSetX<C>) LazyCollectionX.super.grouped(size, supplier);
    }

    @Override
    default SortedSetX<Vector<T>> groupedUntil(final Predicate<? super T> predicate) {

        return (SortedSetX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }

    @Override
    default SortedSetX<Vector<T>> groupedWhile(final Predicate<? super T> predicate) {

        return (SortedSetX<Vector<T>>) LazyCollectionX.super.groupedWhile(predicate);
    }

    @Override
    default <C extends PersistentCollection<? super T>> SortedSetX<C> groupedWhile(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (SortedSetX<C>) LazyCollectionX.super.groupedWhile(predicate, factory);
    }

    @Override
    default <C extends PersistentCollection<? super T>> SortedSetX<C> groupedUntil(final Predicate<? super T> predicate, final Supplier<C> factory) {

        return (SortedSetX<C>) LazyCollectionX.super.groupedUntil(predicate, factory);
    }

    @Override
    default SortedSetX<Vector<T>> groupedUntil(final BiPredicate<Vector<? super T>, ? super T> predicate) {

        return (SortedSetX<Vector<T>>) LazyCollectionX.super.groupedUntil(predicate);
    }


    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.ExtendedTraversable#permutations()
     */
    @Override
    default SetX<ReactiveSeq<T>> permutations() {
        ReactiveSeq<ReactiveSeq<T>> x = stream().permutations()
                .map(c->{
                    return Comparables.comparable(c);
                });
        return SetX.setX(x);

    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.ExtendedTraversable#combinations(int)
     */
    @Override
    default SetX<ReactiveSeq<T>> combinations(final int size) {
        ReactiveSeq<ReactiveSeq<T>> x = stream().combinations(size)
                .map(c->{
                    return Comparables.comparable(c);
                });
        return SetX.setX(x);
    }

    /* (non-Javadoc)
     * @see com.oath.cyclops.lambda.monads.ExtendedTraversable#combinations()
     */
    @Override
    default SetX<ReactiveSeq<T>> combinations() {

        ReactiveSeq<ReactiveSeq<T>> x = stream().combinations()
                                            .map(c->{
                                                return Comparables.comparable(c);
                                            });
        return SetX.setX(x);
    }


    @Override
    default <R> SortedSetX<R> retry(final Function<? super T, ? extends R> fn) {
        return (SortedSetX<R>)LazyCollectionX.super.retry(fn);
    }

    @Override
    default <R> SortedSetX<R> retry(final Function<? super T, ? extends R> fn, final int retries, final long delay, final TimeUnit timeUnit) {
        return (SortedSetX<R>)LazyCollectionX.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    default <R> SortedSetX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return (SortedSetX<R>)LazyCollectionX.super.flatMap(fn);
    }

    @Override
    default <R> SortedSetX<R> mergeMap(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return (SortedSetX<R>)LazyCollectionX.super.mergeMap(fn);
    }

    @Override
    default SortedSetX<T> prependStream(Stream<? extends T> stream) {
        return (SortedSetX<T>)LazyCollectionX.super.prependStream(stream);
    }

    @Override
    default SortedSetX<T> appendAll(T... values) {
        return (SortedSetX<T>)LazyCollectionX.super.appendAll(values);
    }

    @Override
    default SortedSetX<T> append(T value) {
        return (SortedSetX<T>)LazyCollectionX.super.append(value);
    }

    @Override
    default SortedSetX<T> prepend(T value) {
        return (SortedSetX<T>)LazyCollectionX.super.prepend(value);
    }

    @Override
    default SortedSetX<T> prependAll(T... values) {
        return (SortedSetX<T>)LazyCollectionX.super.prependAll(values);
    }

    @Override
    default SortedSetX<T> insertAt(int pos, T... values) {
        return (SortedSetX<T>)LazyCollectionX.super.insertAt(pos,values);
    }

    @Override
    default SortedSetX<T> deleteBetween(int start, int end) {
        return (SortedSetX<T>)LazyCollectionX.super.deleteBetween(start,end);
    }

    @Override
    default SortedSetX<T> insertStreamAt(int pos, Stream<T> stream) {
        return (SortedSetX<T>)LazyCollectionX.super.insertStreamAt(pos,stream);
    }

    @Override
    default SortedSetX<T> recover(final Function<? super Throwable, ? extends T> fn) {
        return (SortedSetX<T>)LazyCollectionX.super.recover(fn);
    }

    @Override
    default <EX extends Throwable> SortedSetX<T> recover(Class<EX> exceptionClass, final Function<? super EX, ? extends T> fn) {
        return (SortedSetX<T>)LazyCollectionX.super.recover(exceptionClass,fn);
    }
    @Override
    default SortedSetX<T> plusLoop(int max, IntFunction<T> value) {
        return (SortedSetX<T>)LazyCollectionX.super.plusLoop(max,value);
    }

    @Override
    default SortedSetX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (SortedSetX<T>)LazyCollectionX.super.plusLoop(supplier);
    }

  @Override
    default <T2, R> SortedSetX<R> zip(final BiFunction<? super T, ? super T2, ? extends R> fn, final Publisher<? extends T2> publisher) {
        return (SortedSetX<R>)LazyCollectionX.super.zip(fn, publisher);
    }



    @Override
    default <U> SortedSetX<Tuple2<T, U>> zipWithPublisher(final Publisher<? extends U> other) {
        return (SortedSetX)LazyCollectionX.super.zipWithPublisher(other);
    }


    @Override
    default <S, U, R> SortedSetX<R> zip3(final Iterable<? extends S> second, final Iterable<? extends U> third, final Function3<? super T, ? super S, ? super U, ? extends R> fn3) {
        return (SortedSetX<R>)LazyCollectionX.super.zip3(second,third,fn3);
    }

    @Override
    default <T2, T3, T4, R> SortedSetX<R> zip4(final Iterable<? extends T2> second, final Iterable<? extends T3> third, final Iterable<? extends T4> fourth, final Function4<? super T, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        return (SortedSetX<R>)LazyCollectionX.super.zip4(second,third,fourth,fn);
    }

    /**
     * Narrow a covariant SortedSet
     *
     * <pre>
     * {@code
     * SortedSetX<? extends Fruit> set = SortedSetX.of(apple,bannana);
     * SortedSetX<Fruit> fruitSet = SortedSetX.narrowK3(set);
     * }
     * </pre>
     *
     * @param setX to narrowK3 generic type
     * @return SortedSetX with narrowed type
     */
    public static <T> SortedSetX<T> narrow(final SortedSetX<? extends T> setX) {
        return (SortedSetX<T>) setX;
    }

    static class Comparables {



        @SuppressWarnings("unchecked")

        static <T, R extends ReactiveSeq<T> & Comparable<T>> R comparable(final ReactiveSeq<T> seq) {
            final Method compareTo = Stream.of(Comparable.class.getMethods())
                                           .filter(m -> m.getName()
                                                         .equals("compareTo"))
                                           .findFirst()
                                           .get();

            return (R) Proxy.newProxyInstance(SortedSetX.class.getClassLoader(), new Class[] { ReactiveSeq.class, Comparable.class },
                                              (proxy, method, args) -> {
                                                  if (compareTo.equals(method))
                                                      return Objects.compare(System.identityHashCode(seq), System.identityHashCode(args[0]),
                                                                             Comparator.naturalOrder());
                                                  else
                                                      return method.invoke(seq, args);
                                              });

        }
    }

    public static  <T,R> SortedSetX<R> tailRec(T initial, Function<? super T, ? extends SortedSetX<? extends Either<T, R>>> fn) {
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
                     .sortedSetX(Evaluation.LAZY);
    }

}
